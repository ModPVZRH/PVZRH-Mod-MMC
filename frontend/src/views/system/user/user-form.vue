<template>
  <div class="drawer-form">
    <el-drawer
        :title="addFlag ? $t('common.addTitle') : $t('common.editTitle')"
        :size="500"
        v-model="visibleFlag"
        :before-close="onClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('user.username')" prop="username">
          <el-input v-model="form.username" :placeholder="$t('user.username')"/>
        </el-form-item>
        <el-form-item :label="$t('user.nickname')" prop="nickname" >
          <el-input v-model="form.nickname" :placeholder="$t('user.nickname')"/>
        </el-form-item>
        <el-form-item :label="$t('user.password')" prop="password" >
          <el-input v-model="form.password" :placeholder="$t('user.password')"/>
        </el-form-item>
        <el-form-item :label="$t('user.gender')" prop="gender" >
          <el-select v-model="form.gender" clearable :placeholder="$t('user.gender')">
            <el-option v-for="(option, index) in genderOptions" :key="index" :label="option.label" :value="option.value"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('user.avatar')" prop="avatar" >
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
              <Plus />
            </el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item :label="$t('common.createTime')" prop="createTime"  v-if="false">
          <el-date-picker v-model="form.createTime" type="datetime"
                          :placeholder="$t('common.createTime')"
                          format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item :label="$t('common.updateTime')" prop="updateTime"  v-if="false">
          <el-date-picker v-model="form.updateTime" type="datetime"
                          :placeholder="$t('common.updateTime')"
                          format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item :label="$t('common.createUser')" prop="createUser"  v-if="false">
          <el-input v-model="form.createUser" :placeholder="$t('common.createUser')"/>
        </el-form-item>
        <el-form-item :label="$t('common.updateUser')" prop="updateUser"  v-if="false">
          <el-input v-model="form.updateUser" :placeholder="$t('common.updateUser')"/>
        </el-form-item>
        <el-form-item :label="$t('user.roleList')">
          <span>
            <el-button type="primary" plain @click="form.roleVOFlag = true"><el-icon><Plus /></el-icon>{{ $t('common.select') }}</el-button>
            <el-tag type="info" round style="margin-left: 5px" v-if="form.roleVOList">{{ $t('common.selectedCount', { count: form.roleVOList ? form.roleVOList.length : 0 }) }}</el-tag>
          </span>
          <el-dialog
              v-model="form.roleVOFlag"
              width="60%"
              align-center
              :z-index="999"
              :close-on-click-modal="false"
              :show-close="false"
          >
            <template #header>
              <span>{{ $t('user.roleList') }}</span>
              <span style="float: right;">
              <el-button type="primary" @click="saveRoleVO">{{ $t('common.save') }}</el-button>
              <el-button @click="form.roleVOFlag = false">{{ $t('common.close') }}</el-button>
              </span>
            </template>
            <RoleVoList v-if="form.roleVOFlag"
                        :old-data="form.roleVOList" mode="edit"
                        ref="voRoleListRef"
            ></RoleVoList>
          </el-dialog>
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
  import {reactive, ref, nextTick, computed} from 'vue';
  import {useI18n} from 'vue-i18n';
  import _ from 'lodash';
  import {ElMessage} from 'element-plus';
  import {userApi} from '@/api/user-api';
  import UserAvatar from "@/components/user-avatar.vue";
  const { t } = useI18n()
  // ------------------------ 联表查询VO ------------------------
  const queryFormState = {
    pageNum: 1,
    pageSize: 10,
    sortItemList: []
  }

  // ------------------------ 中间表更新vo ------------------------
  import RoleVoList from "./role-vo-list.vue";
  const voRoleListRef = ref(null)
  function saveRoleVO() {
    if (voRoleListRef.value) {
      form.roleVOList = voRoleListRef.value.getSelectedRowList()
      ElMessage.success(t('common.saveSuccess'))
      form.roleVOFlag = false
    }
  }
  // ------------------------ 枚举量 ------------------------
  const genderOptions = computed(() => [
    { label: t('gender.male'), value: 1 },
    { label: t('gender.female'), value: 2 },
  ])



  // ------------------------ 图片上传 ------------------------
  import {fileApi} from "@/api/file-api.js";
  async function doAvatarUpload(options) {
    let file = options.file
    let fileForm = new FormData()
    fileForm.append('file', file)
    let res = await fileApi.upload(fileForm, 2)
    if (res.code === 0) { // on-success
      form.avatar = res.data.fileUrl
    } else {
      ElMessage.error(res.msg)
    }
  }

  function beforeAvatarUpload(rawFile) {
    if (rawFile.type !== 'image/jpeg' &&
        rawFile.type !== 'image/png') {
      ElMessage.error(t('validate.unsupportedFileType', { type: rawFile.type }))
      return false
    } else if (rawFile.size / 1024 / 1024 > 10) {
      ElMessage.error(t('validate.fileTooLarge'))
      return false
    }
    return true
  }

  // ------------------------ 事件 ------------------------

  const emits = defineEmits(['reloadList']);

  // ------------------------ 显示与隐藏 ------------------------
  // 是否显示
  const visibleFlag = ref(false);
  // 是否新增
  const addFlag = ref(false);

  function show(rowData) {
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
    }
    visibleFlag.value = true;
    addFlag.value = rowData.userId == null;
    nextTick(() => {
      formRef.value.clearValidate();
    });
  }

  function onClose() {
    Object.keys(form).forEach(key => form[key] = null);
    visibleFlag.value = false;
  }

  // ------------------------ 表单 ------------------------

  // 组件ref
  const formRef = ref();

  const formDefault = {
    userId: undefined, // 用户ID
    username: undefined, // 用户名
    nickname: undefined, // 昵称
    password: undefined, // 密码
    gender: undefined, // 性别
    avatar: undefined, // 头像
    createTime: undefined, // 创建时间
    updateTime: undefined, // 更新时间
    createUser: undefined, // 创建用户
    updateUser: undefined, // 更新用户
  };

  let form = reactive({...formDefault});

  const rules = computed(() => ({
    username: [{
      required: true,
      message: t('common.required', { field: t('user.username') }),
      trigger: 'blur'
    }],
    nickname: [{
      required: true,
      message: t('common.required', { field: t('user.nickname') }),
      trigger: 'blur'
    }],
  }));

  // 点击确定，验证表单
  async function onSubmit() {
    try {
      await formRef.value.validate();
      save();
    } catch (err) {
      ElMessage.error(t('common.validateError'));
    }
  }

  // 新建、编辑API
  async function save() {
    try {
      if (addFlag.value) {
        await userApi.add(form);
      } else {
        await userApi.update(form);
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
</style>
