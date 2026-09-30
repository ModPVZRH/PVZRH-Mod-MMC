<template>
  <div class="drawer-form">
    <el-drawer
        :title="addFlag ? $t('common.addTitle') : $t('common.editTitle')"
        :size="500"
        v-model="visibleFlag"
        :before-close="onClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('file.folderType')" prop="folderType" >
          <el-select v-model="form.folderType" clearable :placeholder="$t('file.folderType')">
            <el-option v-for="(option, index) in folderTypeOptions" :key="index" :label="option.label" :value="option.value"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('file.fileName')" prop="fileName" >
          <el-input v-model="form.fileName" :placeholder="$t('file.fileName')"/>
        </el-form-item>
        <el-form-item :label="$t('file.fileSize')" prop="fileSize" >
          <el-input-number v-model="form.fileSize" :placeholder="$t('file.fileSize')"/>
        </el-form-item>
        <el-form-item :label="$t('file.fileType')" prop="fileType" >
          <el-input v-model="form.fileType" :placeholder="$t('file.fileType')"/>
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
  import {fileApi} from '@/api/file-api';
  const { t } = useI18n()
  // ------------------------ 枚举量 ------------------------
  const folderTypeOptions = computed(() => [
    { label: t('file.avatar'), value: 1 },
    { label: t('file.other'), value: 2 },
    { label: t('file.modIcon'), value: 3 },
  ])



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
    addFlag.value = rowData.fileId == null;
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
    fileId: undefined, // 文件ID
    folderType: undefined, // 文件夹类型
    fileName: undefined, // 文件名称
    fileSize: undefined, // 文件大小
    fileType: undefined, // 文件类型
    createTime: undefined, // 创建时间
    updateTime: undefined, // 更新时间
    createUser: undefined, // 创建用户
    updateUser: undefined, // 更新用户
  };

  let form = reactive({...formDefault});

  const rules = computed(() => ({
    folderType: [{
      required: true,
      message: t('common.required', { field: t('file.folderType') }),
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
        await fileApi.add(form);
      } else {
        await fileApi.update(form);
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
