<template>
<div class="table-list" v-if="hasPerm('business:tag:get')">
  <div class="query">
    <el-card>
      <div class="query-operation">
        <div class="search-query">
          <div class="query-item">
            <div class="query-placeholder">标签名称:</div>
            <div class="query-input">
              <el-input v-model="queryForm.name" placeholder="标签名称" />
            </div>
          </div>
        </div>
        <div class="query-btn-group">
          <el-button type="primary" @click="onSearch">
            <el-icon>
              <search/>
            </el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon>
              <refresh/>
            </el-icon>
            重置
          </el-button>
        </div>
      </div>
    </el-card>
  </div>

  <div class="list">
    <el-card>
      <div class="table-operation">
        <el-button @click="showForm" type="primary" v-if="hasPerm('business:tag:add')">
          <el-icon>
            <plus />
          </el-icon>
          新增
        </el-button>
        <el-button @click="confirmBatchDelete" type="danger" plain
                   :disabled="selectedRowKeyList.length === 0" v-if="hasPerm('business:tag:del')">
          <el-icon>
            <Delete />
          </el-icon>
          批量删除
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
          <el-table-column prop="id" label="ID" min-width="80" align="center"/>
          <el-table-column prop="name" label="标签名称" min-width="140" align="center">
            <template #default="scope">
              <el-tag :color="scope.row.color" effect="dark" style="border: none">
                {{ scope.row.name }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="color" label="颜色" min-width="120" align="center"/>
          <el-table-column prop="createdAt" label="创建时间" min-width="180" align="center"/>
          <el-table-column fixed="right" label="操作" width="120" align="center">
            <template #default="scope">
              <el-button link type="primary" @click="showForm(scope.row)" v-if="hasPerm('business:tag:upd')">
                编辑
              </el-button>
              <el-button link type="danger" @click="onDelete(scope.row)" v-if="hasPerm('business:tag:del')">
                删除
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
<TagForm ref="formRef" @reloadList="queryData" />

</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { tagApi } from '@/api/tag-api'
import TagForm from './tag-form.vue'
import { Delete, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {hasPerm} from "@/utils/permission.js";

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
    let queryResult = await tagApi.page(queryForm)
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
    ElMessage.warning('请至少选择一条数据')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await tagApi.delete(id)
    ElMessage.success('删除成功')
    queryData()
  } catch (e) {
  }
}

function confirmBatchDelete() {
  ElMessageBox.confirm(
      '确定要批量删除这些数据吗?',
      '提示',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
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
    await tagApi.batchDelete(selectedRowKeyList.value)
    ElMessage.success('删除成功')
    queryData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const formRef = ref(null)

function showForm(row) {
  formRef.value.show(row)
}
</script>
