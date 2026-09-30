<template>
  <div class="drawer-form">
    <el-drawer
        :title="addFlag ? $t('common.addTitle') : $t('common.editTitle')"
        :size="500"
        v-model="visibleFlag"
        :before-close="onClose"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('menuManage.menuName')" prop="menuName" >
          <el-input v-model="form.menuName" :placeholder="$t('menuManage.menuName')"/>
        </el-form-item>
        <el-form-item :label="$t('menuManage.parentId')" prop="parentId" >
          <el-input v-model="form.parentId" :placeholder="$t('menuManage.parentId')"/>
        </el-form-item>
        <el-form-item :label="$t('menuManage.type')" prop="type" >
          <el-select v-model="form.type" clearable :placeholder="$t('menuManage.type')">
            <el-option v-for="(option, index) in typeOptions" :key="index" :label="option.label" :value="option.value"/>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('menuManage.code')" prop="code" >
          <el-input v-model="form.code" :placeholder="$t('menuManage.code')"/>
        </el-form-item>
        <el-form-item :label="$t('menuManage.uri')" prop="uri" >
          <el-input v-model="form.uri" :placeholder="$t('menuManage.uri')"/>
        </el-form-item>
        <el-form-item :label="$t('menuManage.componentPath')" prop="componentPath" >
          <el-input v-model="form.componentPath" :placeholder="$t('menuManage.componentPath')"/>
        </el-form-item>
        <el-form-item :label="$t('menuManage.icon')" prop="icon" >
          <IconPicker v-model="form.icon"></IconPicker>
        </el-form-item>
        <el-form-item :label="$t('menuManage.priority')" prop="priority" >
          <el-input-number v-model="form.priority" :placeholder="$t('menuManage.priority')"/>
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
  import {menuApi} from '@/api/menu-api';
  import IconPicker from "@/components/icon-picker.vue";
  const { t } = useI18n()
  // ------------------------ 枚举量 ------------------------
  const typeOptions = computed(() => [
    { label: t('menuManage.directory'), value: 1 },
    { label: t('menuManage.menu'), value: 2 },
    { label: t('menuManage.point'), value: 3 },
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
    addFlag.value = rowData.menuId == null;
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
    menuId: undefined, // 菜单ID
    menuName: undefined, // 菜单名称
    parentId: undefined, // 父菜单ID
    type: undefined, // 权限类型
    code: undefined, // 权限字符串
    uri: undefined, // 路由地址
    componentPath: undefined, // 组件路径
    icon: undefined, // 图标
    priority: undefined, // 排序优先级
    createTime: undefined, // 创建时间
    updateTime: undefined, // 更新时间
    createUser: undefined, // 创建用户
    updateUser: undefined, // 更新用户
  };

  let form = reactive({...formDefault});

  const rules = computed(() => ({
    menuName: [{
      required: true,
      message: t('common.required', { field: t('menuManage.menuName') }),
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
        await menuApi.add(form);
      } else {
        await menuApi.update(form);
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
