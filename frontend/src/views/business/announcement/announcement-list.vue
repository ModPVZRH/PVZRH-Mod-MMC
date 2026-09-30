<template>
<div class="announcement-list">
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
                  value-key="label"
                  @change="sortItemChange"
                  style="min-width: 260px; max-width: 600px;"
              >
                <template #tag>
                  <el-tag v-for="item in selectedSortItems"
                          :key="item.id"
                          :type="item.isAsc ? 'primary' : 'success'"
                          round closable
                          @close="handleTagClose(item)">
                    {{ item.label }}
                  </el-tag>
                </template>
                <el-option
                    v-for="item in sortItemOptions"
                    :key="item.label"
                    :label="item.label"
                    :value="item"
                    :disabled="item.disabled"
                >
                  <span style="float: left">{{ item.desc }}</span>
                  <span style="float: right;color: var(--el-text-color-secondary);font-size: 13px;">
                  <span v-if="item.isAsc">{{ $t('common.asc') }}</span>
                  <span v-else>{{ $t('common.desc') }}</span>
                </span>
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
        <el-button @click="showForm" type="primary">
          <el-icon>
            <plus />
          </el-icon>
          {{ $t('common.add') }}
        </el-button>
        <el-button @click="confirmBatchDelete" type="danger" plain
                   :disabled="selectedRowKeyList.length === 0">
          <el-icon>
            <Delete />
          </el-icon>
          {{ $t('common.batchDelete') }}
        </el-button>
      </div>

      <div class="pagination-table">
        <el-table
            :data="tableData"
            :row-key="(row) => row.id"
            @selection-change="handleSelectionChange"
            border
            style="width: 100%"
        >
          <el-table-column type="selection" width="42" />
          <el-table-column prop="id" :label="$t('common.id')" min-width="80" align="center"/>
          <el-table-column prop="title" :label="$t('announcement.title')" min-width="120" align="center"/>
          <el-table-column prop="isPublished" :label="$t('announcement.isPublished')" min-width="120" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.isPublished ? 'success' : 'danger'">{{ scope.row.isPublished ? $t('common.yes') : $t('common.no') }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" :label="$t('common.createTime')" min-width="120" align="center"/>
          <el-table-column fixed="right" :label="$t('common.operation')" width="120" align="center">
            <template #default="scope">
              <el-button link type="primary" @click="showForm(scope.row)">
                {{ $t('common.edit') }}
              </el-button>
              <el-button link type="danger" @click="onDelete(scope.row)">
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
<AnnouncementForm ref="formRef" @reloadList="queryData" />

</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { announcementApi } from '@/api/announcement-api'
import AnnouncementForm from './announcement-form.vue'
import { Delete, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {hasPerm} from "@/utils/permission.js";

const { t } = useI18n()

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
    let queryResult = await announcementApi.page(queryForm)
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
  selectedRowKeyList.value = val.map(it => it.id)
}

async function onDelete(row) {
  if (row) {
    await handleDelete([row.id])
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
    await announcementApi.delete(id)
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
      })
}

// 请求批量删除
async function requestBatchDelete() {
  try {
    await announcementApi.batchDelete(selectedRowKeyList.value)
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

// 排序字段
const selectedSortItems = ref([])
const sortItemOptions = computed(() => {
  const selectedColumns = new Map()
  selectedSortItems.value.forEach(item => {
    selectedColumns.set(item.column, item.isAsc)
  })
  return [
    { label: t('sort.descLabel', { field: t('common.id') }), desc: t('common.id'), isAsc: false, column: 'id' },
    { label: t('sort.ascLabel', { field: t('common.id') }), desc: t('common.id'), isAsc: true, column: 'id' },
    { label: t('sort.descLabel', { field: t('announcement.title') }), desc: t('announcement.title'), isAsc: false, column: 'title' },
    { label: t('sort.ascLabel', { field: t('announcement.title') }), desc: t('announcement.title'), isAsc: true, column: 'title' },
    { label: t('sort.descLabel', { field: t('announcement.isPublished') }), desc: t('announcement.isPublished'), isAsc: false, column: 'isPublished' },
    { label: t('sort.ascLabel', { field: t('announcement.isPublished') }), desc: t('announcement.isPublished'), isAsc: true, column: 'isPublished' },
    { label: t('sort.descLabel', { field: t('common.createTime') }), desc: t('common.createTime'), isAsc: false, column: 'createdTime' },
    { label: t('sort.ascLabel', { field: t('common.createTime') }), desc: t('common.createTime'), isAsc: true, column: 'createdTime' },
    { label: t('sort.descLabel', { field: t('common.updateTime') }), desc: t('common.updateTime'), isAsc: false, column: 'updatedTime' },
    { label: t('sort.ascLabel', { field: t('common.updateTime') }), desc: t('common.updateTime'), isAsc: true, column: 'updatedTime' },
  ].map(option => ({
    ...option,
    disabled: selectedColumns.has(option.column) && selectedColumns.get(option.column) !== option.isAsc
  }))
})

function sortItemChange() {}

function handleTagClose(tag) {
  selectedSortItems.value.splice(selectedSortItems.value.indexOf(tag), 1)
  sortItemChange(selectedSortItems.value)
}

watch(selectedSortItems, (newSelectedSortItems, oldSelectedSortItems) => {
  queryForm.sortItemList = newSelectedSortItems.map(item => {
    return { isAsc: item.isAsc, column: item.column }
  })
}, { deep: true })
</script>
<style scoped lang="scss">
.announcement-list{
  .query {
    .el-card__body {
      padding: 10px 10px 10px 10px;
    }

    .query-operation {
      display: flex;
      align-items: center;
      flex-wrap: wrap;


      .query-item {
        display: flex;
        align-items: center;

        .query-placeholder {
          margin-right: 10px;
          padding: 5px 0;
        }

        .query-input {
          margin-right: 12px;
          padding: 5px 0;
        }
      }

      .sort-query {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
      }

      .search-query {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
      }

      .query-btn-group {
        padding: 5px 0;
      }
    }
  }

  .list {
    margin-top: 10px;

    .table-operation {
      margin-bottom: 10px;
    }

    .pagination {
      margin-top: 10px;
      float: right;
    }
  }
}
</style>
