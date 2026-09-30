<template>
<el-drawer
    :title="addFlag ? $t('common.addTitle') : $t('common.editTitle')"
    :size="500"
    v-model="visibleFlag"
    :before-close="onClose"
>
  <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
    <el-form-item :label="$t('role.name')" prop="roleName" >
      <el-input v-model="form.roleName" :placeholder="$t('role.name')"/>
    </el-form-item>
    <el-form-item :label="$t('common.createTime')" prop="createTime" >
      <el-date-picker v-model="form.createTime" type="datetime"
                      :placeholder="$t('common.createTime')"
                      format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss"/>
    </el-form-item>
    <el-form-item :label="$t('common.updateTime')" prop="updateTime" >
      <el-date-picker v-model="form.updateTime" type="datetime"
                      :placeholder="$t('common.updateTime')"
                      format="YYYY-MM-DD HH:mm:ss" value-format="YYYY-MM-DD HH:mm:ss"/>
    </el-form-item>
    <el-form-item :label="$t('common.createUser')" prop="createUser" >
      <el-input v-model="form.createUser" :placeholder="$t('common.createUser')"/>
    </el-form-item>
    <el-form-item :label="$t('common.updateUser')" prop="updateUser" >
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
</template>
<script setup>
import {reactive, ref, nextTick, computed} from 'vue';
import {useI18n} from 'vue-i18n';
import _ from 'lodash';
import {ElMessage} from 'element-plus';
import {roleApi} from '@/api/role-api';

const { t } = useI18n()

// ------------------------ 联表查询VO ------------------------
const queryFormState = {
  pageNum: 1,
  pageSize: 10,
  sortItemList: []
}

// ------------------------ 枚举量 ------------------------

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
  addFlag.value = rowData.roleId == null;
  nextTick(() => {
    formRef.value.clearValidate();
  });
}

function onClose() {
  Object.assign(form, formDefault);
  visibleFlag.value = false;
}

// ------------------------ 表单 ------------------------

// 组件ref
const formRef = ref();

const formDefault = {
  roleId: undefined, // 角色ID
  roleName: undefined, // 角色名称
  createTime: undefined, // 创建时间
  updateTime: undefined, // 更新时间
  createUser: undefined, // 创建用户
  updateUser: undefined, // 更新用户
};

let form = reactive({...formDefault});

const rules = computed(() => ({
  roleName: [{
    required: true,
    message: t('common.required', { field: t('role.name') }),
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
      await roleApi.add(form);
    } else {
      await roleApi.update(form);
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
.pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
}
</style>
