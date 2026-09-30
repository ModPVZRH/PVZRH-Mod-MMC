<template>
  <div class="drawer-form">
    <el-drawer
      class="client-version-form-drawer"
      :title="addFlag ? $t('clientVersion.add') : $t('clientVersion.edit')"
      :size="drawerSize"
      v-model="visibleFlag"
      :before-close="onClose"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="client-version-form">
        <div class="form-section-title">{{ $t('common.basicInfo') }}</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('clientVersion.versionNumber')" prop="versionNumber">
              <el-input v-model="form.versionNumber" :placeholder="$t('clientVersion.versionPlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('clientVersion.isReleased')" prop="isReleased">
              <el-switch v-model="form.isReleased" :active-value="true" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('clientVersion.versionDescription')" prop="versionDescription">
              <el-input v-model="form.versionDescription" :placeholder="$t('clientVersion.descriptionPlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('clientVersion.downloadUrl')" prop="downloadUrl">
              <el-input v-model="form.downloadUrl" :placeholder="$t('clientVersion.downloadPlaceholder')" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-section-title">{{ $t('clientVersion.updateContentSection') }}</div>
        <el-form-item prop="updateContent" class="markdown-editor-item" label-width="0">
          <MdEditor
            v-model="form.updateContent"
            :language="mdLang"
            previewTheme="github"
            :placeholder="$t('clientVersion.contentPlaceholder')"
            :footers="[]"
            :toolbarsExclude="['github']"
            style="width: 100%; height: 400px"
            @onUploadImg="onUploadImg"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="onClose">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="onSubmit">{{ $t('common.save') }}</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>
<script setup>
import { reactive, ref, nextTick, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import _ from 'lodash';
import { ElMessage } from 'element-plus';
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import { clientVersionApi } from '@/api/client-version-api';
import { fileApi } from '@/api/file-api.js';

const { t, locale } = useI18n();
const mdLang = computed(() => locale.value === 'en' ? 'en-US' : 'zh-CN');

const emits = defineEmits(['reloadList']);

const visibleFlag = ref(false);
const addFlag = ref(false);
const drawerSize = computed(() => (window.innerWidth < 960 ? '96%' : '880px'));

function show(rowData) {
  Object.assign(form, formDefault);
  if (rowData && !_.isEmpty(rowData)) {
    Object.assign(form, rowData);
  }
  form.updateContent = form.updateContent || '';
  visibleFlag.value = true;
  addFlag.value = rowData.id == null;
  nextTick(() => {
    formRef.value.clearValidate();
  });
}

function onClose() {
  Object.keys(form).forEach(key => form[key] = null);
  form.updateContent = '';
  visibleFlag.value = false;
}

async function onUploadImg(files, callback) {
  try {
    const urls = [];
    for (const file of files) {
      const fileForm = new FormData();
      fileForm.append('file', file);
      const res = await fileApi.upload(fileForm, 2);
      if (res.data?.fileUrl) {
        urls.push(res.data.fileUrl);
      }
    }
    callback(urls);
  } catch (err) {
    ElMessage.error(t('mods.imageUploadFailed'));
    callback([]);
  }
}

const formRef = ref();

const formDefault = {
  id: undefined,
  versionNumber: undefined,
  versionDescription: undefined,
  updateContent: '',
  downloadUrl: undefined,
  isReleased: undefined,
  createdTime: undefined,
  updatedTime: undefined,
};

let form = reactive({ ...formDefault });

const rules = computed(() => ({
  versionNumber: [{
    required: true,
    message: t('common.required', { field: t('clientVersion.versionNumber') }),
    trigger: 'blur'
  }],
}));

async function onSubmit() {
  try {
    await formRef.value.validate();
    save();
  } catch (err) {
    ElMessage.error(t('common.validateError'));
  }
}

async function save() {
  try {
    if (addFlag.value) {
      await clientVersionApi.add(form);
    } else {
      await clientVersionApi.update(form);
    }
    ElMessage.success(t('common.success'));
    emits('reloadList');
    onClose();
  } catch (err) {
    console.log(err)
  }
}

defineExpose({
  show,
});
</script>
<style scoped lang="scss">
.client-version-form {
  padding-right: 8px;
}

.form-section-title {
  margin: 4px 0 16px;
  padding-left: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1;
  border-left: 3px solid var(--el-color-primary);
}

.markdown-editor-item {
  margin-bottom: 22px;

  :deep(.el-form-item__content) {
    margin-left: 0 !important;
    width: 100%;
    line-height: normal;
  }
}
</style>

<style lang="scss">
.client-version-form-drawer {
  .el-drawer__header {
    margin-bottom: 8px;
    padding: 16px 20px 12px;
  }

  .el-drawer__body {
    padding: 8px 20px 16px;
  }

  .md-editor {
    border-radius: 6px;
  }
}
</style>
