<template>
  <div class="drawer-form">
    <el-drawer
      class="category-form-drawer"
      :title="addFlag ? $t('category.add') : $t('category.edit')"
      :size="drawerSize"
      v-model="visibleFlag"
      :before-close="onClose"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="category-form">
        <div class="form-section-title">{{ $t('common.basicInfo') }}</div>
        <el-form-item :label="$t('category.name')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('category.namePlaceholder')" maxlength="50" />
        </el-form-item>
        <el-form-item :label="$t('category.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" :placeholder="$t('category.descriptionPlaceholder')" maxlength="255" />
        </el-form-item>
        <el-form-item :label="$t('category.sortOrder')" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" style="width: 100%" />
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
import { categoryApi } from '@/api/category-api';

const { t } = useI18n();

const emits = defineEmits(['reloadList']);

const visibleFlag = ref(false);
const addFlag = ref(false);
const drawerSize = computed(() => (window.innerWidth < 960 ? '96%' : '520px'));

function show(rowData) {
  Object.assign(form, formDefault);
  if (rowData && !_.isEmpty(rowData)) {
    Object.assign(form, rowData);
  }
  if (form.sortOrder == null) {
    form.sortOrder = 0;
  }
  visibleFlag.value = true;
  addFlag.value = rowData.id == null;
  nextTick(() => {
    formRef.value.clearValidate();
  });
}

function onClose() {
  Object.keys(form).forEach(key => form[key] = null);
  form.sortOrder = 0;
  visibleFlag.value = false;
}

const formRef = ref();

const formDefault = {
  id: undefined,
  name: undefined,
  description: undefined,
  sortOrder: 0,
  createdAt: undefined,
  updatedAt: undefined,
};

let form = reactive({ ...formDefault });

const rules = computed(() => ({
  name: [{
    required: true,
    message: t('common.required', { field: t('category.name') }),
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
      await categoryApi.add(form);
    } else {
      await categoryApi.update(form);
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
.category-form {
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
</style>
