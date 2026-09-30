<template>
<div class="table-list" v-if="hasPerm('business:category:get')">
  <div class="query">
    <el-card>
      <div class="query-operation">
        <div class="search-query">
          <div class="query-item">
            <div class="query-placeholder">{{ $t('category.name') }}:</div>
            <div class="query-input">
              <el-input v-model="queryForm.name" :placeholder="$t('category.name')" />
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
        <el-button @click="showForm" type="primary" v-if="hasPerm('business:category:add')">
          <el-icon>
            <plus />
          </el-icon>
          {{ $t('common.add') }}
        </el-button>
        <el-button @click="confirmBatchDelete" type="danger" plain
                   :disabled="selectedRowKeyList.length === 0" v-if="hasPerm('business:category:del')">
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
          <el-table-column prop="name" :label="$t('category.name')" min-width="140" align="center"/>
          <el-table-column prop="description" :label="$t('category.description')" min-width="220" align="center" show-overflow-tooltip/>
          <el-table-column prop="sortOrder" :label="$t('category.sortOrder')" min-width="80" align="center"/>
          <el-table-column prop="createdAt" :label="$t('common.createTime')" min-width="180" align="center"/>
          <el-table-column fixed="right" :label="$t('common.operation')" width="120" align="center">
            <template #default="scope">
              <el-button link type="primary" @click="showForm(scope.row)" v-if="hasPerm('business:category:upd')">
                {{ $t('common.edit') }}
              </el-button>
              <el-button link type="danger" @click="onDelete(scope.row)" v-if="hasPerm('business:category:del')">
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
<CategoryForm ref="formRef" @reloadList="queryData" />

</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { categoryApi } from '@/api/category-api'
import CategoryForm from './category-form.vue'
import { Delete, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {hasPerm} from "@/utils/permission.js";

const { t } = useI18n()

const queryFormState = {
  pageNum: 1,
  pageSize: 10,
  name: undefined,
  sortItemList: []
}
const queryForm = reactive({ ...queryFormState })
const tableData = ref([])
const total = ref(0)

function resetQuery() {
  let pageSize = queryForm.pageSize
  Object.assign(queryForm, queryFormState)
  queryForm.pageSize = pageSize
  queryData()
}

function onSearch() {
  queryForm.pageNum = 1
  queryData()
}

async function queryData() {
  try {
    let queryResult = await categoryApi.page(queryForm)
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
    await categoryApi.delete(id)
    ElMessage.success(t('common.deleteSuccess'))
    queryData()
  } catch (e) {
  }
}

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

async function requestBatchDelete() {
  try {
    await categoryApi.batchDelete(selectedRowKeyList.value)
    ElMessage.success(t('common.deleteSuccess'))
    queryData()
  } catch (e) {
    ElMessage.error(t('common.deleteFailed'))
  }
}

const formRef = ref(null)

function showForm(row) {
  formRef.value.show(row)
}
</script>
