<template>
  <el-card class="account-card" shadow="never">
    <template #header>
      <div class="account-header">{{ $t('account.title') }}</div>
    </template>
    <div class="account-content">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('account.userId')" prop="userId">
          <el-input v-model="form.userId" :placeholder="$t('account.userId')" disabled/>
        </el-form-item>
        <el-form-item :label="$t('account.username')" prop="username">
          <el-input v-model="form.username" :placeholder="$t('account.username')" disabled/>
        </el-form-item>
        <el-form-item :label="$t('account.nickname')" prop="nickname">
          <el-input v-model="form.nickname" :placeholder="$t('account.nickname')"/>
        </el-form-item>
        <el-form-item :label="$t('account.newPassword')" prop="password">
          <el-input v-model="form.newPassword" :placeholder="$t('account.newPassword')"/>
        </el-form-item>
        <el-form-item :label="$t('account.gender')" prop="gender">
          <el-select v-model="form.gender" clearable :placeholder="$t('account.gender')">
            <el-option v-for="item in genderOptions" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('account.avatar')" prop="avatar">
          <el-upload
              action="#"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="doAvatarUpload"
              class="mod-image-uploader"
          >
            <UserAvatar
              v-if="form.avatar"
              :src="form.avatar"
              :username="form.username"
              :nickname="form.nickname"
              :size="178"
              shape="square"
            />
            <el-icon v-else class="mod-image-uploader-icon">
              <Plus/>
            </el-icon>
          </el-upload>
        </el-form-item>
      </el-form>

      <div>
        <el-button type="primary" @click="onSubmit">{{ $t('account.update') }}</el-button>
      </div>
    </div>

  </el-card>
</template>

<script setup>

import {reactive, ref, nextTick, computed} from 'vue'
import {useI18n} from 'vue-i18n'
import _ from 'lodash'
import {ElMessage} from 'element-plus'
import {userApi} from '@/api/user-api'
import {useUserStore} from '@/stores/user.js'
import {fileApi} from "@/api/file-api.js";
import UserAvatar from "@/components/user-avatar.vue";

const {t} = useI18n()

const genderOptions = computed(() => [
  {label: t('gender.unknown'), value: 0},
  {label: t('gender.male'), value: 1},
  {label: t('gender.female'), value: 2}
])

async function doAvatarUpload(options) {
  let file = options.file
  let fileForm = new FormData()
  fileForm.append('file', file)
  let res = await fileApi.upload(fileForm, 1)
  if (res.code === 0) { // on-success
    console.log(res.data)
  form.avatar = res.data.fileUrl
  } else {
    ElMessage.error(res.msg)
  }
}

function beforeAvatarUpload(rawFile) {
  if (rawFile.type !== 'image/jpeg' &&
      rawFile.type !== 'image/png') {
    ElMessage.error(t('validate.unsupportedFileType', {type: rawFile.type}))
    return false
  } else if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error(t('validate.fileTooLarge'))
    return false
  }
  return true
}


// ------------------------ 表单 ------------------------

// 组件ref
const formRef = ref()

const formDefault = {
  userId: useUserStore().userId, //用户ID
  username: useUserStore().username, //用户名
  nickname: useUserStore().nickname, //昵称
  password: undefined, //密码
  newPassword: undefined, //新密码
  gender: useUserStore().gender, //性别，0未知，1男，2女
  avatar: useUserStore().avatar, //头像URL
}

let form = reactive({...formDefault})

const rules = computed(() => ({
  username: [{
    required: true,
    message: t('common.required', {field: t('account.username')}),
    trigger: 'blur'
  }],
  phone: [{
    required: true,
    message: t('account.phoneRequired'),
    trigger: 'blur'
  }],
  gender: [{
    required: true,
    message: t('account.genderRequired'),
    trigger: 'blur'
  }],
  avatar: [{
    required: true,
    message: t('account.avatarRequired'),
    trigger: 'blur'
  }],
}))

// 点击确定，验证表单
async function onSubmit() {
  try {
    await formRef.value.validate()
    save()
  } catch (err) {
    ElMessage.error(t('common.validateError'))
  }
}

// 新建、编辑API
async function save() {
  try {
    await userApi.update(form)
    ElMessage.success(t('common.success'))
  } catch (err) {
    console.log(err)
  }
}


</script>
<style lang="scss" scoped>
.account-card {
  max-width: 760px;
}

.account-header {
  font-weight: 600;
}

.account-content {
  max-width: 520px;
}


.mod-image-uploader .el-upload {
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.mod-image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
  border: 1px dashed var(--el-border-color);
}

.mod-image-uploader-icon:hover {
  border-color: var(--el-color-primary);
}

.mod-img {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
