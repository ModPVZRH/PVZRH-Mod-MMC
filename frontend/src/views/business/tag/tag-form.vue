<template>
  <div class="drawer-form">
    <el-drawer
      class="tag-form-drawer"
      :title="addFlag ? $t('tag.add') : $t('tag.edit')"
      :size="drawerSize"
      v-model="visibleFlag"
      :before-close="onClose"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px" class="tag-form">
        <div class="form-section-title">{{ $t('common.basicInfo') }}</div>
        <el-form-item :label="$t('tag.name')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('tag.namePlaceholder')" maxlength="50" />
        </el-form-item>
        <el-form-item :label="$t('tag.colorLabel')" prop="color">
          <el-color-picker v-model="form.color" />
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
import { tagApi } from '@/api/tag-api';

const { t } = useI18n();

const emits = defineEmits(['reloadList']);

const visibleFlag = ref(false);
const addFlag = ref(false);
const drawerSize = computed(() => (window.innerWidth < 960 ? '96%' : '480px'));

function show(rowData) {
  Object.assign(form, formDefault);
  if (rowData && !_.isEmpty(rowData)) {
    Object.assign(form, rowData);
  }
  form.color = form.color || '#409EFF';
  visibleFlag.value = true;
  addFlag.value = rowData.id == null;
  nextTick(() => {
    formRef.value.clearValidate();
  });
}

function onClose() {
  Object.keys(form).forEach(key => form[key] = null);
  form.color = '#409EFF';
  visibleFlag.value = false;
}

const formRef = ref();

const formDefault = {
  id: undefined,
  name: undefined,
  color: '#409EFF',
  createdAt: undefined,
  updatedAt: undefined,
};

let form = reactive({ ...formDefault });

const rules = computed(() => ({
  name: [{
    required: true,
    message: t('common.required', { field: t('tag.name') }),
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
      await tagApi.add(form);
    } else {
      await tagApi.update(form);
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
.tag-form {
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
