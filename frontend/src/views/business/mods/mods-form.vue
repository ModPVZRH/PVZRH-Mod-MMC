<template>
  <div class="drawer-form">
    <el-drawer
      class="mods-form-drawer"
      :title="addFlag ? $t('mods.add') : $t('mods.edit')"
      :size="drawerSize"
      v-model="visibleFlag"
      :before-close="onClose"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="mods-form">
        <div class="form-section-title">{{ $t('common.basicInfo') }}</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="$t('mods.name')" prop="modName">
              <el-input v-model="form.modName" :placeholder="$t('mods.namePlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('mods.englishName')" prop="englishName">
              <el-input v-model="form.englishName" :placeholder="$t('mods.englishPlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="hasPerm('business:mod:sup')">
            <el-form-item :label="$t('mods.author')" prop="authorId">
              <el-select v-model="form.authorId" :placeholder="$t('mods.authorPlaceholder')" filterable style="width: 100%">
                <el-option v-for="item in userList" :key="item.userId" :label="item.nickname" :value="item.userId">
                  <div class="option-item">
                    <UserAvatar :src="item.avatar" :username="item.username" :nickname="item.nickname" :size="24" style="margin-right: 8px;" />
                    <span>{{ item.nickname }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('mods.otherAuthor')" prop="otherAuthor">
              <el-select v-model="form.otherAuthors" multiple :placeholder="$t('mods.otherAuthorPlaceholder')" filterable style="width: 100%">
                <el-option v-for="item in userList" :key="item.userId" :label="item.nickname" :value="item.userId">
                  <div class="option-item">
                    <UserAvatar :src="item.avatar" :username="item.username" :nickname="item.nickname" :size="24" style="margin-right: 8px;" />
                    <span>{{ item.nickname }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('mods.gameName')" prop="gameName">
              <el-input v-model="form.gameName" :placeholder="$t('mods.gameName')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('mods.supportedVersions')" prop="supportedVersions">
              <el-input v-model="form.supportedVersions" :placeholder="$t('mods.supportedVersions')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('mods.framework')" prop="frameworkName">
              <el-select v-model="form.frameworkName" clearable :placeholder="$t('mods.frameworkPlaceholder')" style="width: 100%">
                <el-option v-for="(option, index) in frameworkNameOptions" :key="index" :label="option.label"
                  :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('mods.category')" prop="categoryId">
              <el-select v-model="form.categoryId" clearable filterable :placeholder="$t('mods.categoryPlaceholder')" style="width: 100%">
                <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('mods.tags')" prop="tagIds">
              <el-select v-model="form.tagIds" multiple filterable collapse-tags collapse-tags-tooltip :placeholder="$t('mods.tagPlaceholder')" style="width: 100%">
                <el-option v-for="item in tagList" :key="item.id" :label="item.name" :value="item.id">
                  <div class="option-item">
                    <span class="tag-color-dot" :style="{ background: item.color || '#409EFF' }"></span>
                    <span>{{ item.name }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('mods.icon')" prop="iconUrl">
              <ModIconUpload v-model="form.iconUrl" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item :label="$t('mods.videoUrl')" prop="videoUrl">
              <el-input v-model="form.videoUrl" :placeholder="$t('mods.videoPlaceholder')" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-section-title">{{ $t('mods.descriptionSection') }}</div>
        <el-form-item prop="modDescription" class="mod-description-item" label-width="0">
          <MdEditor
            v-model="form.modDescription"
            :language="mdLang"
            previewTheme="github"
            :placeholder="$t('mods.descriptionPlaceholder')"
            :footers="[]"
            :toolbarsExclude="['github']"
            style="width: 100%; height: 400px"
            @onUploadImg="onUploadImg"
          />
        </el-form-item>

        <div class="form-section-title">{{ $t('mods.downloadSection') }}</div>
        <p class="version-tip">{{ $t('mods.versionTip') }}</p>
        <div class="version-toolbar">
          <el-button type="primary" plain @click="openVersionDialog">{{ $t('mods.addVersion') }}</el-button>
        </div>
        <div class="version-list">
          <div v-for="(item, index) in form.versions" :key="item.uid" class="version-card">
            <div class="version-card-head">
              <div>
                <el-tag v-if="item.current" type="success" size="small">{{ $t('mods.currentVersion') }}</el-tag>
                <el-button v-else link type="primary" @click="setCurrentVersion(index)">{{ $t('mods.setCurrent') }}</el-button>
              </div>
              <el-button link type="danger" :disabled="form.versions.length <= 1" @click="removeVersion(index)">
                {{ $t('common.delete') }}
              </el-button>
            </div>
            <el-form-item :label="$t('mods.version')" :prop="'versions.' + index + '.version'" :rules="versionFieldRules">
              <el-input v-model="item.version" :placeholder="$t('mods.versionPlaceholder')" />
            </el-form-item>
            <el-form-item :label="$t('mods.versionDescription')" class="version-description-item">
              <MdEditor
                :id="'version-editor-' + item.uid"
                v-model="item.description"
                :language="mdLang"
                previewTheme="github"
                :placeholder="$t('mods.versionDescriptionPlaceholder')"
                :footers="[]"
                :toolbarsExclude="['github']"
                style="width: 100%; height: 280px"
                @onUploadImg="onUploadImg"
              />
            </el-form-item>
            <el-form-item :label="$t('mods.directUrl')" :prop="'versions.' + index + '.downloadDirectUrl'" :rules="directUrlRules">
              <el-input v-model="item.downloadDirectUrl" :placeholder="$t('mods.directUrlFull')" />
            </el-form-item>
            <el-form-item :label="$t('mods.cloudUrl')" :prop="'versions.' + index + '.downloadCloudUrl'" :rules="cloudUrlRules">
              <el-input v-model="item.downloadCloudUrl" :placeholder="$t('mods.cloudUrlFull')" />
            </el-form-item>
          </div>
        </div>

        <div class="form-section-title">{{ $t('mods.publishSection') }}</div>
        <el-row :gutter="16">
          <el-col :span="8" v-if="hasPerm('business:mod:sup')">
            <el-form-item :label="$t('mods.isPreposition')" prop="isPreposition">
              <el-switch v-model="form.isPreposition" :active-value="true" />
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="hasPerm('business:mod:sup')">
            <el-form-item :label="$t('mods.isFeatured')" prop="isFeatured">
              <el-switch v-model="form.isFeatured" :active-value="true" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="$t('mods.showDirectUrl')" prop="showDirectUrl">
              <el-switch v-model="form.showDirectUrl" :active-value="true" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="$t('mods.isVisible')" prop="isVisible">
              <el-switch v-model="form.isVisible" :active-value="true" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="$t('mods.isModpack')" prop="isModpack">
              <el-switch v-model="form.isModpack" :active-value="true" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="onClose">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="onSubmit">{{ $t('common.save') }}</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog
      v-model="versionDialogVisible"
      :title="$t('mods.addVersion')"
      width="800px"
      append-to-body
      destroy-on-close
      class="version-add-dialog"
    >
      <el-form ref="versionDialogRef" :model="versionDialog" label-width="108px">
        <el-form-item :label="$t('mods.version')" prop="version" :rules="versionFieldRules">
          <el-input v-model="versionDialog.version" :placeholder="$t('mods.versionPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('mods.directUrl')" prop="downloadDirectUrl" :rules="directUrlRules">
          <el-input v-model="versionDialog.downloadDirectUrl" :placeholder="$t('mods.directUrlFull')" />
        </el-form-item>
        <el-form-item :label="$t('mods.cloudUrl')" prop="downloadCloudUrl" :rules="cloudUrlRules">
          <el-input v-model="versionDialog.downloadCloudUrl" :placeholder="$t('mods.cloudUrlFull')" />
        </el-form-item>
        <el-form-item :label="$t('mods.currentVersion')">
          <el-switch v-model="versionDialog.current" />
        </el-form-item>
        <el-form-item :label="$t('mods.versionDescription')" class="version-description-item">
          <MdEditor
            id="version-editor-dialog"
            v-model="versionDialog.description"
            :language="mdLang"
            previewTheme="github"
            :placeholder="$t('mods.versionDescriptionPlaceholder')"
            :footers="[]"
            :toolbarsExclude="['github']"
            style="width: 100%; height: 360px"
            @onUploadImg="onUploadImg"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="versionDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="confirmVersionDialog">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { reactive, ref, nextTick, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ElMessage } from 'element-plus';
import { MdEditor } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import { modsApi } from '@/api/mods-api';
import { userApi } from '@/api/user-api';
import { categoryApi } from '@/api/category-api';
import { tagApi } from '@/api/tag-api';
import { fileApi } from '@/api/file-api.js';
import { hasPerm } from "@/utils/permission.js";
import UserAvatar from "@/components/user-avatar.vue";
import ModIconUpload from "@/components/mod-icon-upload.vue";

const { t, locale } = useI18n();
const mdLang = computed(() => locale.value === 'en' ? 'en-US' : 'zh-CN');

const frameworkNameOptions = [
  { label: 'Bepinex', value: '1' },
  { label: 'MelonLoader', value: '2' },
]

const emits = defineEmits(['reloadList']);

const visibleFlag = ref(false);
const addFlag = ref(false);
const userList = ref([]);
const categoryList = ref([]);
const tagList = ref([]);
const drawerSize = computed(() => (window.innerWidth < 960 ? '96%' : '880px'));
let versionSeed = 1;

function blankVersion(current = false) {
  return {
    uid: versionSeed++,
    id: undefined,
    version: '',
    description: '',
    downloadDirectUrl: '',
    downloadCloudUrl: '',
    current,
  };
}

function normalizeVersions(rowData) {
  const source = Array.isArray(rowData?.versions) ? rowData.versions : [];
  const versions = source.map((item) => ({
    uid: versionSeed++,
    id: item.id,
    version: item.version || '',
    description: item.description || '',
    downloadDirectUrl: item.downloadDirectUrl || '',
    downloadCloudUrl: item.downloadCloudUrl || '',
    current: !!item.current,
  }));
  if (!versions.length) {
    versions.push({
      ...blankVersion(true),
      version: rowData?.version || '',
      downloadDirectUrl: rowData?.downloadDirectUrl || '',
      downloadCloudUrl: rowData?.downloadCloudUrl || '',
    });
  }
  if (!versions.some((item) => item.current)) {
    versions[0].current = true;
  }
  return versions;
}

function show(rowData) {
  userApi.getList().then(res => {
    userList.value = res.data;
  });
  categoryApi.getList().then(res => {
    categoryList.value = res.data || [];
  });
  tagApi.getList().then(res => {
    tagList.value = res.data || [];
  });
  Object.assign(form, formDefault);
  if (rowData && rowData.id != null) {
    Object.assign(form, rowData);
  }
  form.modDescription = form.modDescription || '';
  form.otherAuthors = form.otherAuthors || [];
  form.tagIds = form.tagIds || [];
  form.versions = normalizeVersions(rowData && rowData.id != null ? rowData : null);
  form.isModpack = !!form.isModpack;
  visibleFlag.value = true;
  addFlag.value = rowData.id == null;
  nextTick(() => {
    formRef.value.clearValidate();
  });
}

function onClose() {
  versionDialogVisible.value = false;
  Object.keys(form).forEach(key => form[key] = null);
  form.modDescription = '';
  form.otherAuthors = [];
  form.tagIds = [];
  form.versions = [];
  visibleFlag.value = false;
}

const versionDialogVisible = ref(false);
const versionDialogRef = ref();
const versionDialog = reactive({
  version: '',
  description: '',
  downloadDirectUrl: '',
  downloadCloudUrl: '',
  current: true,
});

function openVersionDialog() {
  versionDialog.version = '';
  versionDialog.description = '';
  versionDialog.downloadDirectUrl = '';
  versionDialog.downloadCloudUrl = '';
  versionDialog.current = true;
  versionDialogVisible.value = true;
  nextTick(() => {
    versionDialogRef.value?.clearValidate();
  });
}

async function confirmVersionDialog() {
  try {
    await versionDialogRef.value.validate();
  } catch (err) {
    ElMessage.error(t('common.validateError'));
    return;
  }
  const version = versionDialog.version.trim();
  if (form.versions.some((item) => (item.version || '').trim() === version)) {
    ElMessage.error(t('mods.duplicateVersion'));
    return;
  }
  if (versionDialog.current) {
    form.versions.forEach((item) => {
      item.current = false;
    });
  }
  const created = blankVersion(versionDialog.current);
  created.version = version;
  created.description = versionDialog.description || '';
  created.downloadDirectUrl = versionDialog.downloadDirectUrl.trim();
  created.downloadCloudUrl = versionDialog.downloadCloudUrl.trim();
  form.versions.unshift(created);
  if (!form.versions.some((item) => item.current)) {
    created.current = true;
  }
  versionDialogVisible.value = false;
}

function setCurrentVersion(index) {
  form.versions.forEach((item, itemIndex) => {
    item.current = itemIndex === index;
  });
}

function removeVersion(index) {
  const removed = form.versions[index];
  form.versions.splice(index, 1);
  if (removed?.current && form.versions.length) {
    form.versions[0].current = true;
  }
}

function hasDuplicateVersion() {
  const seen = new Set();
  for (const item of form.versions) {
    const version = (item.version || '').trim();
    if (!version || seen.has(version)) {
      if (version) {
        return true;
      }
      continue;
    }
    seen.add(version);
  }
  return false;
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
  modName: undefined,
  englishName: undefined,
  authorId: undefined,
  modDescription: '',
  iconUrl: undefined,
  videoUrl: undefined,
  gameName: undefined,
  supportedVersions: undefined,
  isPreposition: undefined,
  frameworkName: undefined,
  downloadDirectUrl: undefined,
  downloadCloudUrl: undefined,
  version: undefined,
  fileSize: undefined,
  otherAuthors: [],
  categoryId: undefined,
  tagIds: [],
  showDirectUrl: undefined,
  downloadCount: undefined,
  viewCount: undefined,
  isApproved: undefined,
  isFeatured: undefined,
  isVisible: undefined,
  isModpack: false,
  versions: [],
  createdAt: undefined,
  updatedAt: undefined,
};

let form = reactive({ ...formDefault });

const rules = computed(() => ({
  modName: [{
    required: true,
    message: t('common.required', { field: t('mods.name') }),
    trigger: 'blur'
  }]
}));

const versionFieldRules = computed(() => ([
  {
    required: true,
    message: t('common.required', { field: t('mods.version') }),
    trigger: 'blur'
  },
  {
    pattern: /^\d+\.\d+\.\d+(-[a-zA-Z]+)?$/,
    message: t('mods.invalidVersion'),
    trigger: 'blur'
  }
]));

const directUrlRules = computed(() => ([
  {
    required: true,
    message: t('common.required', { field: t('mods.directUrlFull') }),
    trigger: 'blur'
  },
  {
    pattern: /^https?:\/\/[^\s]+$/,
    message: t('mods.invalidDirectUrl'),
    trigger: 'blur'
  }
]));

const cloudUrlRules = computed(() => ([
  {
    required: true,
    message: t('common.required', { field: t('mods.cloudUrlFull') }),
    trigger: 'blur'
  },
  {
    pattern: /^https?:\/\/[^\s]+$/,
    message: t('mods.invalidCloudUrl'),
    trigger: 'blur'
  }
]));

async function onSubmit() {
  if (!form.versions.length) {
    ElMessage.error(t('mods.atLeastOneVersion'));
    return;
  }
  if (hasDuplicateVersion()) {
    ElMessage.error(t('mods.duplicateVersion'));
    return;
  }
  const current = form.versions.find((item) => item.current) || form.versions[0];
  form.version = current.version;
  form.downloadDirectUrl = current.downloadDirectUrl;
  form.downloadCloudUrl = current.downloadCloudUrl;
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
      await modsApi.add(form);
    } else {
      await modsApi.update(form);
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
.option-item {
  display: flex;
  align-items: center;
}

.tag-color-dot {
  width: 10px;
  height: 10px;
  margin-right: 8px;
  border-radius: 50%;
}

.mods-form {
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

.form-section-title + .el-row,
.form-section-title + .el-form-item {
  margin-top: 0;
}

.version-tip {
  margin: 0 0 12px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.5;
}

.version-toolbar {
  margin-bottom: 12px;
}

.version-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.version-card {
  padding: 12px 12px 0;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
}

.version-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.version-description-item {
  :deep(.el-form-item__content) {
    line-height: normal;
  }
}

.mod-description-item {
  margin-bottom: 22px;

  :deep(.el-form-item__content) {
    margin-left: 0 !important;
    width: 100%;
    line-height: normal;
  }
}
</style>

<style lang="scss">
.mods-form-drawer {
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

.version-add-dialog {
  .md-editor {
    border-radius: 6px;
  }

  .el-dialog__body {
    padding-top: 8px;
  }
}
</style>
