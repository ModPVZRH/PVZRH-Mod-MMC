<template>
  <div class="mod-icon-upload">
    <div class="icon-preview" @click="pickFile">
      <img v-if="modelValue" :src="modelValue" class="icon-img" :alt="$t('iconUpload.alt')" />
      <div v-else class="icon-placeholder">
        <el-icon :size="28"><Plus /></el-icon>
        <span>{{ $t('iconUpload.upload') }}</span>
      </div>
    </div>
    <div class="icon-actions">
      <el-button size="small" type="primary" @click="pickFile">
        {{ modelValue ? $t('iconUpload.change') : $t('common.upload') }}
      </el-button>
      <el-button size="small" :disabled="!modelValue" @click="openCropForCurrent">{{ $t('iconUpload.crop') }}</el-button>
      <el-button size="small" type="danger" plain :disabled="!modelValue" @click="clearIcon">{{ $t('common.delete') }}</el-button>
      <span class="icon-tip">{{ $t('iconUpload.tip') }}</span>
    </div>
    <input
      ref="fileInputRef"
      class="hidden-input"
      type="file"
      accept="image/jpeg,image/png,image/webp,image/gif"
      @change="onFileChange"
    />

    <el-dialog
      v-model="cropVisible"
      :title="$t('iconUpload.cropTitle')"
      width="560px"
      append-to-body
      :z-index="5000"
      :close-on-click-modal="false"
      destroy-on-close
      @closed="onCropClosed"
    >
      <div class="cropper-wrap">
        <VueCropper
          v-if="cropImg"
          ref="cropperRef"
          :img="cropImg"
          :autoCrop="true"
          :autoCropWidth="280"
          :autoCropHeight="280"
          :fixed="true"
          :fixedNumber="[1, 1]"
          :centerBox="true"
          :canMoveBox="true"
          :info="true"
          :full="false"
          outputType="jpeg"
          :outputSize="1"
        />
      </div>
      <template #footer>
        <el-button @click="cropVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="uploading" @click="confirmCrop">{{ $t('iconUpload.confirmUpload') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { VueCropper } from 'vue-cropper'
import 'vue-cropper/dist/index.css'
import { fileApi } from '@/api/file-api.js'
import { compressImageToMaxSize, urlToObjectUrl, MAX_ICON_BYTES } from '@/utils/image.js'

const { t } = useI18n()

const props = defineProps({
  modelValue: { type: String, default: '' },
  folderType: { type: Number, default: 3 },
})

const emit = defineEmits(['update:modelValue'])

const fileInputRef = ref()
const cropperRef = ref()
const cropVisible = ref(false)
const cropImg = ref('')
const uploading = ref(false)
const objectUrls = []

function pickFile() {
  fileInputRef.value?.click()
}

function onFileChange(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.error(t('iconUpload.selectImage'))
    return
  }
  if (file.size / 1024 / 1024 > 10) {
    ElMessage.error(t('iconUpload.tooLarge'))
    return
  }
  openCrop(URL.createObjectURL(file))
}

function openCrop(imgUrl) {
  if (cropImg.value && objectUrls.includes(cropImg.value)) {
    URL.revokeObjectURL(cropImg.value)
  }
  cropImg.value = imgUrl
  if (imgUrl.startsWith('blob:')) {
    objectUrls.push(imgUrl)
  }
  cropVisible.value = true
}

async function openCropForCurrent() {
  if (!props.modelValue) return
  try {
    const objectUrl = await urlToObjectUrl(props.modelValue)
    openCrop(objectUrl)
  } catch (err) {
    openCrop(props.modelValue)
  }
}

function clearIcon() {
  emit('update:modelValue', '')
}

function onCropClosed() {
  cropImg.value = ''
}

function getCropBlob() {
  return new Promise((resolve, reject) => {
    if (!cropperRef.value) {
      reject(new Error(t('iconUpload.cropperNotReady')))
      return
    }
    cropperRef.value.getCropBlob((data) => {
      if (!data) {
        reject(new Error(t('iconUpload.cropFailed')))
        return
      }
      resolve(data)
    })
  })
}

async function confirmCrop() {
  uploading.value = true
  try {
    const cropped = await getCropBlob()
    const compressed = await compressImageToMaxSize(cropped, MAX_ICON_BYTES, {
      maxWidth: 512,
      mimeType: 'image/jpeg',
    })
    const file = new File([compressed], 'mod-icon.jpg', { type: 'image/jpeg' })
    const fileForm = new FormData()
    fileForm.append('file', file)
    const res = await fileApi.upload(fileForm, props.folderType)
    if (res.data?.fileUrl) {
      emit('update:modelValue', res.data.fileUrl)
      cropVisible.value = false
      ElMessage.success(t('iconUpload.uploaded', { size: Math.ceil(compressed.size / 1024) }))
    } else {
      ElMessage.error(res.msg || t('iconUpload.uploadFailed'))
    }
  } catch (err) {
    ElMessage.error(err.message || t('iconUpload.uploadFailed'))
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped lang="scss">
.mod-icon-upload {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.icon-preview {
  width: 120px;
  height: 120px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  background: var(--el-fill-color-lighter);

  &:hover {
    border-color: var(--el-color-primary);
  }
}

.icon-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.icon-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.icon-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding-top: 4px;
}

.icon-tip {
  width: 100%;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.4;
}

.hidden-input {
  display: none;
}

.cropper-wrap {
  height: 400px;
}
</style>
