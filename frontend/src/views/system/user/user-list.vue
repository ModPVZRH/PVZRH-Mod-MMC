<template>
  <div class="table-list" v-if="hasPerm('system:user:get')">
    <div class="register-setting" v-if="hasPerm('system:user:upd')">
      <el-card>
        <div class="register-setting-row">
          <div>
            <div class="register-setting-title">{{ $t('user.registerEnabled') }}</div>
            <div class="register-setting-tip">{{ $t('user.registerEnabledTip') }}</div>
          </div>
          <el-switch v-model="registerEnabled" :loading="registerSettingLoading" @change="onRegisterEnabledChange" />
        </div>
      </el-card>
    </div>
    <div class="query">
      <el-card>
        <div class="query-operation">
          <div class="sort-query">
            <div class="query-item">
              <div class="query-placeholder">{{ $t('common.sortField') }}:</div>
              <div class="query-input">
                <el-select
                    v-model="selectedSortItems"
                    multiple
                    collapse-tags
                    collapse-tags-tooltip
                    :max-collapse-tags="4"
                    :placeholder="$t('common.sortField')"
                    value-key="column"
                    @change="sortItemChange"
                    style="min-width: 260px; max-width: 600px;"
                >
                  <template #tag>
                    <el-tag
                        v-for="item in selectedSortItems"
                        :key="item.column"
                        :type="item.isAsc ? 'success' : 'warning'"
                        class="sort-tag"
                        closable
                        @close="handleTagClose(item)"
                    >
                      {{ item.label }}
                      <div class="sort-direction-wrapper" @click.stop="toggleSortDirection(item)">
                        <el-icon class="sort-direction" :class="{ 'is-asc': item.isAsc }">
                          <ArrowUp v-if="item.isAsc"/>
                          <ArrowDown v-else/>
                        </el-icon>
                      </div>
                    </el-tag>
                  </template>
                  <el-option
                      v-for="item in sortItemOptions"
                      :key="item.column"
                      :label="item.label"
                      :value="item"
                      :disabled="isColumnSelected(item.column)"
                  >
                    {{ item.label }}
                  </el-option>
                </el-select>
              </div>
            </div>
          </div>
          <div class="search-query">
            <div class="query-item">
              <div class="query-placeholder">{{ $t('user.username') }}:</div>
              <div class="query-input">
                <el-input v-model="queryForm.username" :placeholder="$t('user.username')"/>
              </div>
            </div>
            <div class="query-item">
              <div class="query-placeholder">{{ $t('user.nickname') }}:</div>
              <div class="query-input">
                <el-input v-model="queryForm.nickname" :placeholder="$t('user.nickname')"/>
              </div>
            </div>
          </div>
          <div class="query-btn-group">
            <el-button type="primary" @click="onSearch">
              <el-icon>
                <search/>
              </el-icon>
              {{ $t('common.query') }}
            </el-button>
            <el-button @click="resetQuery">
              <el-icon>
                <refresh/>
              </el-icon>
              {{ $t('common.reset') }}
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <div class="list">
      <el-card>
        <div class="table-operation">
          <el-button @click="showForm" type="primary" v-if="hasPerm('system:user:add')">
            <el-icon>
              <plus />
            </el-icon>
            {{ $t('common.add') }}
          </el-button>
          <el-button @click="confirmBatchDelete" type="danger" plain
                     :disabled="selectedRowKeyList.length === 0" v-if="hasPerm('system:user:del')">
            <el-icon>
              <Delete />
            </el-icon>
            {{ $t('common.batchDelete') }}
          </el-button>
        </div>

        <div class="pagination-table">
          <el-table
              :data="tableData"
              :row-key="(row) => row.userId"
              @selection-change="handleSelectionChange"
              border
              style="width: 100%"
          >
            <el-table-column type="selection" width="42" />
            <el-table-column prop="userId" :label="$t('user.userId')" min-width="120" />
            <el-table-column prop="username" :label="$t('user.username')" min-width="120" />
            <el-table-column prop="nickname" :label="$t('user.nickname')" min-width="120" />
            <el-table-column prop="avatar" :label="$t('user.avatar')" min-width="120">
              <template #default="scope">
                <el-image
                    style="width: 95px; height: 95px"
                    :src="scope.row.avatar"
                    :preview-src-list="scope.row.avatar ? [scope.row.avatar] : []"
                    :zoom-rate="1.2"
                    :max-scale="7"
                    :min-scale="0.2"
                    :initial-index="0"
                    :z-index="1000"
                    :preview-teleported="true"
                    fit="scale-down"
                >
                  <template #error>
                    <div class="avatar-fallback">
                      {{ getAvatarFallbackChar(scope.row.username, scope.row.nickname) }}
                    </div>
                  </template>
                </el-image>
              </template>
            </el-table-column>
            <el-table-column :label="$t('user.roleList')" min-width="120">
              <template #default="scope">
                <el-button type="primary" text @click="scope.row.roleVOFlag = true">{{ $t('common.view') }}</el-button>
                <el-dialog
                    v-model="scope.row.roleVOFlag"
                    :title="$t('user.roleList')"
                    width="60%"
                    align-center
                    :z-index="999"
                    append-to-body
                >
                  <RoleVoList v-if="scope.row.roleVOFlag" :old-data="scope.row.roleVOList"></RoleVoList>
                </el-dialog>
              </template>
            </el-table-column>
            <el-table-column prop="gender" :label="$t('user.gender')" min-width="120">
              <template #default="scope">
                {{ getGenderOptionsLabel(scope.row.gender) }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" :label="$t('common.createTime')" min-width="120" />
            <el-table-column prop="updateTime" :label="$t('common.updateTime')" min-width="120" />
            <el-table-column prop="createUser" :label="$t('common.createUser')" min-width="120" />
            <el-table-column prop="updateUser" :label="$t('common.updateUser')" min-width="120" />
            <el-table-column fixed="right" :label="$t('common.operation')" width="120">
              <template #default="scope">
                <el-button link type="primary" @click="showForm(scope.row)" v-if="hasPerm('system:user:upd')">
                  {{ $t('common.edit') }}
                </el-button>
                <el-button link type="danger" @click="onDelete(scope.row)" v-if="hasPerm('system:user:del')">
                  {{ $t('common.delete') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination">
            <el-pagination
                background
                :hide-on-single-page="false"
                layout="total, prev, pager, next, sizes"
                :total="total"
                v-model:page-size="queryForm.pageSize"
                v-model:current-page="queryForm.pageNum"
                @current-change="queryData"
                :page-sizes="[5, 10, 20, 50]"
                @size-change="handleSizeChange"
            />
          </div>
        </div>
      </el-card>
    </div>
  </div>
  <UserForm ref="formRef" @reloadList="queryData" />

</template>

<script setup>
  import { ref, reactive, onMounted, nextTick, watch, computed } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { userApi } from '@/api/user-api'
  import { systemSettingApi } from '@/api/system-setting-api'
  import { ElMessage } from 'element-plus'
  import UserForm from './user-form.vue'
  import {Delete, Plus, Refresh, Search, ArrowUp, ArrowDown} from '@element-plus/icons-vue'
  import {hasPerm} from "@/utils/permission.js";
  import { getAvatarFallbackChar } from '@/utils/avatar.js'
  // ------------------------ 导入列表 ------------------------
  import RoleVoList from "./role-vo-list.vue";
  // --------------------------------------------------------
  const { t } = useI18n()
  // ------------------------ 枚举量 ------------------------
  const genderOptions = computed(() => [
    { label: t('gender.male'), value: 1 },
    { label: t('gender.female'), value: 2 },
  ])
  function getGenderOptionsLabel(value) {
    const option = genderOptions.value.find(option => option.value === value)
    return option ? option.label : ''
  }


  // --------------------------------------------------------

  // 查询数据表单和方法
  const queryFormState = {
    pageNum: 1,
    pageSize: 10,
    username: null,
    nickname: null,
    sortItemList: []
  }
  const queryForm = reactive({...queryFormState})
  const tableData = ref([])
  const total = ref(0)
  const registerEnabled = ref(true)
  const registerSettingReady = ref(false)
  const registerSettingLoading = ref(false)

  async function loadRegisterSetting() {
    registerSettingReady.value = false
    try {
      const res = await systemSettingApi.getPublic()
      registerEnabled.value = res?.data?.registerEnabled !== false
    } catch (e) {
      console.log(e)
    }
    await nextTick()
    registerSettingReady.value = true
  }

  async function onRegisterEnabledChange(value) {
    if (!registerSettingReady.value) {
      return
    }
    registerSettingLoading.value = true
    try {
      const res = await systemSettingApi.update({ registerEnabled: value })
      if (res.code !== 0) {
        await revertRegisterEnabled(value)
        ElMessage.error(res.msg || t('common.validateError'))
      } else {
        ElMessage.success(t('common.success'))
      }
    } catch (e) {
      await revertRegisterEnabled(value)
      console.log(e)
    } finally {
      registerSettingLoading.value = false
    }
  }

  async function revertRegisterEnabled(value) {
    registerSettingReady.value = false
    registerEnabled.value = !value
    await nextTick()
    registerSettingReady.value = true
  }

  // 重置查询条件
  function resetQuery() {
    selectedSortItems.value = []
    sortItemChange(selectedSortItems.value)
    let pageSize = queryForm.pageSize
    Object.assign(queryForm, queryFormState)
    queryForm.pageSize = pageSize
    queryData()
  }

  // 搜索
  function onSearch() {
    queryForm.pageNum = 1
    queryData()
  }

  // 查询数据
  async function queryData() {
    try {
      let queryResult = await userApi.page(queryForm)
      tableData.value = queryResult.data.list
      total.value = queryResult.data.total
    } catch (e) {
      console.log(e)
    }
  }

  function handleSizeChange(newSize) {
    queryForm.pageSize = newSize
    onSearch()
  }


  onMounted(() => {
    queryData()
    if (hasPerm('system:user:upd')) {
      loadRegisterSetting()
    }
  })

  // 删除
  const selectedRowKeyList = ref([])

  function handleSelectionChange(val) {
    selectedRowKeyList.value = val.map(it => it.userId)
  }

  async function onDelete(row) {
    if (row) {
      await handleDelete([row.userId])
    } else {
      ElMessage.warning(t('common.selectAtLeastOne'))
    }
  }

  async function handleDelete(id) {
    try {
      await ElMessageBox.confirm(t('common.confirmDelete'), t('common.tip'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
      await userApi.delete(id)
      ElMessage.success(t('common.deleteSuccess'))
      queryData()
    } catch (e) {
    }
  }

  // 批量删除
  function confirmBatchDelete() {
    ElMessageBox.confirm(
        t('common.confirmBatchDelete'),
        t('common.tip'),
        {
          confirmButtonText: t('common.delete'),
          cancelButtonText: t('common.cancel'),
          type: 'warning'
        }
    )
        .then(() => {
          requestBatchDelete()
        })
        .catch(() => {
          // 当用户点击取消按钮时，这里可以处理一些逻辑
        })
  }

  // 请求批量删除
  async function requestBatchDelete() {
    try {
      await userApi.batchDelete(selectedRowKeyList.value)
      ElMessage.success(t('common.deleteSuccess'))
      queryData()
    } catch (e) {
      ElMessage.error(t('common.deleteFailed'))
    }
  }

  // 表单相关
  const formRef = ref(null)

  function showForm(row) {
    formRef.value.show(row)
  }

  // -------------------------- 排序字段 -------------------------
  const sortItemOptions = computed(() => [
    {label: t('user.userId'), column: 'user_id', isAsc: false},
    {label: t('user.username'), column: 'username', isAsc: false},
    {label: t('user.nickname'), column: 'nickname', isAsc: false},
    {label: t('user.password'), column: 'password', isAsc: false},
    {label: t('user.gender'), column: 'gender', isAsc: false},
    {label: t('user.avatar'), column: 'avatar', isAsc: false},
    {label: t('common.createTime'), column: 'create_time', isAsc: false},
    {label: t('common.updateTime'), column: 'update_time', isAsc: false},
    {label: t('common.createUser'), column: 'create_user', isAsc: false},
    {label: t('common.updateUser'), column: 'update_user', isAsc: false},
  ])

  const selectedSortItems = ref([])

  function sortItemChange(selected) {
    queryForm.sortItemList = selected.map(item => ({
      isAsc: item.isAsc,
      column: item.column
    }))
  }

  function isColumnSelected(column) {
    return selectedSortItems.value.some(item => item.column === column)
  }

  function handleTagClose(tag) {
    selectedSortItems.value = selectedSortItems.value.filter(item => item.column !== tag.column)
    sortItemChange(selectedSortItems.value)
  }

  function toggleSortDirection(item) {
    item.isAsc = !item.isAsc
    sortItemChange(selectedSortItems.value)
  }

  watch(selectedSortItems, (newSelectedSortItems, oldSelectedSortItems) => {
    queryForm.sortItemList = newSelectedSortItems.map(item => {
      return {isAsc: item.isAsc, column: item.column}
    })
  }, {deep: true})
  // --------------------------------------------------------
</script>
<style scoped lang="scss">
.register-setting {
  margin-bottom: 16px;
}

.register-setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.register-setting-title {
  font-weight: 600;
}

.register-setting-tip {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  font-size: 36px;
  font-weight: 600;
}
</style>
