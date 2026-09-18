<template>
  <div class="drawer-form">
    <el-drawer
      class="mods-form-drawer"
      :title="addFlag ? '添加模组' : '编辑模组'"
      :size="drawerSize"
      v-model="visibleFlag"
      :before-close="onClose"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="mods-form">
        <div class="form-section-title">基本信息</div>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Mod名称" prop="modName">
              <el-input v-model="form.modName" placeholder="请输入 Mod 名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Mod英文名" prop="englishName">
              <el-input v-model="form.englishName" placeholder="请输入英文名" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="hasPerm('business:mod:sup')">
            <el-form-item label="作者" prop="authorId">
              <el-select v-model="form.authorId" placeholder="请选择作者" filterable style="width: 100%">
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
            <el-form-item label="添加共创" prop="otherAuthor">
              <el-select v-model="form.otherAuthors" multiple placeholder="请选择共创作者" filterable style="width: 100%">
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
            <el-form-item label="支持游戏" prop="gameName">
              <el-input v-model="form.gameName" placeholder="支持游戏" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支持版本" prop="supportedVersions">
              <el-input v-model="form.supportedVersions" placeholder="支持版本" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Mod框架" prop="frameworkName">
              <el-select v-model="form.frameworkName" clearable placeholder="请选择 Mod 框架" style="width: 100%">
                <el-option v-for="(option, index) in frameworkNameOptions" :key="index" :label="option.label"
                  :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" clearable filterable placeholder="请选择分类" style="width: 100%">
                <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签" prop="tagIds">
              <el-select v-model="form.tagIds" multiple filterable collapse-tags collapse-tags-tooltip placeholder="请从已有标签中选择" style="width: 100%">
                <el-option v-for="item in tagList" :key="item.id" :label="item.name" :value="item.id">
                  <div class="option-item">
                    <span class="tag-color-dot" :style="{ background: item.color || '#409EFF' }"></span>
                    <span>{{ item.name }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Mod版本" prop="version">
              <el-input v-model="form.version" placeholder="例如 1.0.0" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="模组图标" prop="iconUrl">
              <ModIconUpload v-model="form.iconUrl" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="视频Url" prop="videoUrl">
              <el-input v-model="form.videoUrl" placeholder="视频地址，选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="form-section-title">模组介绍</div>
        <el-form-item prop="modDescription" class="mod-description-item" label-width="0">
          <MdEditor
            v-model="form.modDescription"
            language="zh-CN"
            previewTheme="github"
            placeholder="请输入 Mod 介绍，支持 Markdown 语法"
            :footers="[]"
            :toolbarsExclude="['github']"
            style="width: 100%; height: 400px"
            @onUploadImg="onUploadImg"
          />
        </el-form-item>

        <div class="form-section-title">下载信息</div>
        <el-form-item label="直链下载" prop="downloadDirectUrl">
          <el-input v-model="form.downloadDirectUrl" placeholder="直链下载地址" />
        </el-form-item>
        <el-form-item label="网盘下载" prop="downloadCloudUrl">
          <el-input v-model="form.downloadCloudUrl" placeholder="网盘下载地址" />
        </el-form-item>

        <div class="form-section-title">发布设置</div>
        <el-row :gutter="16">
          <el-col :span="8" v-if="hasPerm('business:mod:sup')">
            <el-form-item label="是否前置" prop="isPreposition">
              <el-switch v-model="form.isPreposition" :active-value="true" />
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="hasPerm('business:mod:sup')">
            <el-form-item label="是否推荐" prop="isFeatured">
              <el-switch v-model="form.isFeatured" :active-value="true" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="显示直链" prop="showDirectUrl">
              <el-switch v-model="form.showDirectUrl" :active-value="true" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否发布" prop="isVisible">
              <el-switch v-model="form.isVisible" :active-value="true" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否整合包" prop="isModpack">
              <el-switch v-model="form.isModpack" :active-value="true" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="onClose">取消</el-button>
          <el-button type="primary" @click="onSubmit">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>
<script setup>
import { reactive, ref, nextTick, computed } from 'vue';
import _ from 'lodash';
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
  if (rowData && !_.isEmpty(rowData)) {
    Object.assign(form, rowData);
  }
  form.modDescription = form.modDescription || '';
  form.otherAuthors = form.otherAuthors || [];
  form.tagIds = form.tagIds || [];
  form.isModpack = !!form.isModpack;
  visibleFlag.value = true;
  addFlag.value = rowData.id == null;
  nextTick(() => {
    formRef.value.clearValidate();
  });
}

function onClose() {
  Object.keys(form).forEach(key => form[key] = null);
  form.modDescription = '';
  form.otherAuthors = [];
  form.tagIds = [];
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
    ElMessage.error('图片上传失败');
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
  createdAt: undefined,
  updatedAt: undefined,
};

let form = reactive({ ...formDefault });

const rules = {
  modName: [{
    required: true,
    message: 'Mod名称 必填',
    trigger: 'blur'
  }],
  downloadDirectUrl: [{
    required: true,
    message: '直链下载地址 必填',
    trigger: 'blur'
  }, {
    pattern: /^https?:\/\/[^\s]+$/,
    message: '直链下载地址格式不正确',
    trigger: 'blur'
  }],
  downloadCloudUrl: [{
    required: true,
    message: '网盘下载地址 必填',
    trigger: 'blur'
  }, {
    pattern: /^https?:\/\/[^\s]+$/,
    message: '网盘下载地址格式不正确',
    trigger: 'blur'
  }],
  version: [{
    message: 'Mod版本 必填',
    trigger: 'blur'
  }, {
    pattern: /^\d+\.\d+\.\d+(-[a-zA-Z]+)?$/,
    message: '版本格式不正确，应为 X.Y.Z 或 X.Y.Z-类型',
    trigger: 'blur'
  }]
};

async function onSubmit() {
  try {
    await formRef.value.validate();
    save();
  } catch (err) {
    ElMessage.error('参数验证错误，请仔细填写表单数据!');
  }
}

async function save() {
  try {
    if (addFlag.value) {
      await modsApi.add(form);
    } else {
      await modsApi.update(form);
    }
    ElMessage.success('操作成功');
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
</style>
