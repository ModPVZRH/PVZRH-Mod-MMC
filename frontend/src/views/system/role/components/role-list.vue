<template>
  <el-card class="role-container" v-if="hasPerm('system:role:get')">
    <template #header>
      {{ $t('role.list') }}
      <el-button style="float: right" type="primary" size="default" @click="showRoleForm"
                 v-if="hasPerm(['system:role:add'])">{{ $t('role.add') }}
      </el-button>
    </template>
    <el-menu :default-active="activeKey" @select="(key) => activeKey = key">
      <el-menu-item v-for="item in tableData" :key="item.roleId" :index="String(item.roleId)">
        <el-popover placement="right" :width="200" trigger="hover">
          <template #reference>
            <span>{{ item.roleName }}</span>
          </template>
          <el-button text bg type="danger" @click="deleteRole(item.roleId)" v-if="hasPerm('system:role:del')">{{ $t('common.delete') }}
          </el-button>
          <el-button text bg type="primary" @click="showRoleForm(item)" v-if="hasPerm('system:role:upd')">{{ $t('common.edit') }}</el-button>
        </el-popover>
      </el-menu-item>
    </el-menu>
    <div class="pagination" style="display:flex;justify-content: center;">
      <el-pagination
          background
          :hide-on-single-page="true"
          layout="prev, pager, next"
          :total="total"
          v-model:page-size="queryForm.pageSize"
          v-model:current-page="queryForm.pageNum"
          @current-change="queryData"
          :page-sizes="[5, 10, 20, 50]"
          :pager-count="5"
          size="small"
      />
    </div>
  </el-card>
  <RoleForm ref="formRef" @reloadList="queryData"/>
</template>

<script setup>
import _ from 'lodash';
import {computed, onMounted, reactive, ref} from 'vue';
import {useI18n} from 'vue-i18n';
import {roleApi} from "@/api/role-api.js";
import {usemodLoadingStore} from "@/stores/mod-loading.js";
import RoleForm from "./role-form.vue";
import {hasPerm} from "@/utils/permission.js";

const { t } = useI18n()

const queryFormState = {
  pageNum: 1,
  pageSize: 10,
  sortItemList: []
}
const queryForm = reactive({...queryFormState})
const tableData = ref([])
const total = ref(0)

onMounted(queryData);

// 查询列表
async function queryData() {
  let res = await roleApi.page(queryForm)
  if (res.code === 0) {
    tableData.value = res.data.list;
    total.value = res.data.total
    if (!_.isEmpty(tableData.value) && tableData.value[0].roleId) {
      activeKey.value = String(tableData.value[0].roleId);
    }
  }

}

let activeKey = ref();
const activeRoleId = computed(() => activeKey.value ?? null);
defineExpose({activeRoleId});


// ----------------------- 添加、修改、删除 ---------------------------------
const formRef = ref();

// 显示表单框
function showRoleForm(role) {
  formRef.value.show(role);
}

// 删除角色
function deleteRole(roleId) {
  if (!roleId) return;
  ElMessageBox.confirm(
      t('role.confirmDelete'),
      t('common.tip'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
  ).then(async () => {
    usemodLoadingStore().show()
    try {
      await roleApi.delete(roleId);
      ElMessage({
        type: 'success',
        message: t('common.deleteSuccess')
      });
      queryData();
    } catch (e) {
      console.log(e)
    } finally {
      usemodLoadingStore().hide()
    }
  }).catch(() => {
  });
}

</script>

<style scoped lang="scss">
  .role-container {
    height: 100%;

    .el-menu {
      border-right: none;
      padding: 8px;

      .el-menu-item {
        height: 44px;
        line-height: 44px;
        border-radius: 4px;
        margin: 4px 0;

        &:hover, &.is-active {
          background-color: #ecf5ff;
        }
      }
    }

    .pagination {
      padding: 16px 0;
      display: flex;
      justify-content: center;
      border-top: 1px solid #ebeef5;
    }

  }
</style>
