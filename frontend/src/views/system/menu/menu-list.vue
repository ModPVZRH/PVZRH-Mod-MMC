<template>
  <div class="table-list" v-if="hasPerm('system:menu:get')">
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
          <el-button @click="showForm" type="primary" v-if="hasPerm('system:menu:add')">
            <el-icon>
              <plus />
            </el-icon>
            {{ $t('common.add') }}
          </el-button>
          <el-button @click="confirmBatchDelete" type="danger" plain
                     :disabled="selectedRowKeyList.length === 0" v-if="hasPerm('system:menu:del')">
            <el-icon>
              <Delete />
            </el-icon>
            {{ $t('common.batchDelete') }}
          </el-button>
        </div>

        <div class="pagination-table">
          <el-table
              :data="tableData"
              :row-key="(row) => row.menuId"
              @selection-change="handleSelectionChange"
              border
              style="width: 100%"
              :tree-props="{ children: 'children', checkStrictly : true }"
          >
            <el-table-column type="selection" width="42" />
            <el-table-column prop="menuId" :label="$t('menuManage.menuId')" min-width="150" />
            <el-table-column prop="menuName" :label="$t('menuManage.menuName')" min-width="120" />
            <el-table-column prop="type" :label="$t('menuManage.type')" width="90">
              <template #default="scope">
                <el-tag effect="dark" :type="getTypeOptionsType(scope.row.type)">
                  {{ getTypeOptionsLabel(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="code" :label="$t('menuManage.code')" min-width="120" />
            <el-table-column prop="uri" :label="$t('menuManage.uri')" min-width="220" />
            <el-table-column prop="componentPath" :label="$t('menuManage.componentPath')" min-width="300" />
            <el-table-column prop="icon" :label="$t('menuManage.icon')" min-width="120">
              <template #default="scope">
                <component :is="scope.row.icon" style="width: 24px; height: 24px;">
                </component>
              </template>
            </el-table-column>
            <el-table-column prop="priority" :label="$t('menuManage.priority')" min-width="120" />
            <el-table-column prop="createTime" :label="$t('common.createTime')" min-width="120" />
            <el-table-column prop="updateTime" :label="$t('common.updateTime')" min-width="120" />
            <el-table-column prop="createUser" :label="$t('common.createUser')" min-width="120" />
            <el-table-column prop="updateUser" :label="$t('common.updateUser')" min-width="120" />
            <el-table-column fixed="right" :label="$t('common.operation')" width="120">
              <template #default="scope">
                <el-button link type="primary" @click="showForm(scope.row)" v-if="hasPerm('system:menu:upd')">
                  {{ $t('common.edit') }}
                </el-button>
                <el-button link type="danger" @click="onDelete(scope.row)" v-if="hasPerm('system:menu:del')">
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
  <MenuForm ref="formRef" @reloadList="queryData" />

</template>

<script setup>
  import { ref, reactive, onMounted, watch, computed } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { menuApi } from '@/api/menu-api'
  import MenuForm from './menu-form.vue'
  import {Delete, Plus, Refresh, Search, ArrowUp, ArrowDown} from '@element-plus/icons-vue'
  import {hasPerm} from "@/utils/permission.js";
  const { t } = useI18n()
  // ------------------------ 导入列表 ------------------------
  // --------------------------------------------------------
  // ------------------------ 枚举量 ------------------------
  const typeOptions = computed(() => [
    {label: t('menuManage.directory'), value: 1, type: 'success'},
    {label: t('menuManage.menu'), value: 2, type: 'warning'},
    {label: t('menuManage.point'), value: 3, type: 'danger'},
  ])

  function getTypeOptionsLabel(value) {
    const option = typeOptions.value.find(option => option.value === value)
    return option ? option.label : ''
  }
  function getTypeOptionsType(value) {
    const option = typeOptions.value.find(option => option.value === value)
    return option ? option.type : ''
  }
  // --------------------------------------------------------

  // 查询数据表单和方法
  const queryFormState = {
    pageNum: 1,
    pageSize: 10,
    sortItemList: []
  }
  const queryForm = reactive({ ...queryFormState })
  const tableData = ref([])
  const total = ref(0)

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
      let queryResult = await menuApi.page(queryForm)
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


  onMounted(queryData)

  // 删除
  const selectedRowKeyList = ref([])

  function handleSelectionChange(val) {
    selectedRowKeyList.value = val.map(it => it.menuId)
  }

  async function onDelete(row) {
    if (row) {
      await handleDelete([row.menuId])
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
      await menuApi.delete(id)
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
      await menuApi.batchDelete(selectedRowKeyList.value)
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
    {label: t('menuManage.menuId'), column: 'menu_id', isAsc: false},
    {label: t('menuManage.menuName'), column: 'menu_name', isAsc: false},
    {label: t('menuManage.parentId'), column: 'parent_id', isAsc: false},
    {label: t('menuManage.type'), column: 'type', isAsc: false},
    {label: t('menuManage.code'), column: 'code', isAsc: false},
    {label: t('menuManage.uri'), column: 'uri', isAsc: false},
    {label: t('menuManage.componentPath'), column: 'component_path', isAsc: false},
    {label: t('menuManage.icon'), column: 'icon', isAsc: false},
    {label: t('menuManage.priority'), column: 'priority', isAsc: false},
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
</style>
