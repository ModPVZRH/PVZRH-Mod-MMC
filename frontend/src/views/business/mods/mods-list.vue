<template>
<div class="table-list" v-if="hasPerm('business:mods:get')">
  <div class="query">
    <el-card>
      <div class="query-operation">
        <div class="sort-query">
          <div class="query-item">
            <div class="query-placeholder">排序字段:</div>
            <div class="query-input">
              <el-select
                  v-model="selectedSortItems"
                  multiple
                  collapse-tags
                  collapse-tags-tooltip
                  :max-collapse-tags="4"
                  placeholder="排序字段"
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
            <div class="query-placeholder">Mod名称:</div>
            <div class="query-input">
              <el-input v-model="queryForm.modName" placeholder="Mod名称"/>
            </div>
          </div>
          <div class="query-item">
            <div class="query-placeholder">分类:</div>
            <div class="query-input">
              <el-select v-model="queryForm.categoryId" clearable placeholder="全部分类" style="width: 160px">
                <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </div>
          </div>
          <div class="query-item">
            <div class="query-placeholder">标签:</div>
            <div class="query-input">
              <el-select v-model="queryForm.tagId" clearable placeholder="全部标签" style="width: 160px">
                <el-option v-for="item in tagList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
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
        <el-button @click="showForm" type="primary" v-if="hasPerm('business:mods:add')">
          <el-icon>
            <plus />
          </el-icon>
          新增
        </el-button>
        <el-button @click="confirmBatchDelete" type="danger" plain
                   :disabled="selectedRowKeyList.length === 0" v-if="hasPerm('business:mods:del')">
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
          <el-table-column prop="id" label="ID" min-width="50" align="center" />
          <el-table-column label="图标" width="80" align="center">
            <template #default="scope">
              <el-image
                v-if="scope.row.iconUrl"
                :src="scope.row.iconUrl"
                :preview-src-list="[scope.row.iconUrl]"
                :preview-teleported="true"
                fit="cover"
                style="width: 48px; height: 48px; border-radius: 8px"
              />
              <span v-else style="color: var(--el-text-color-placeholder)">无</span>
            </template>
          </el-table-column>
          <el-table-column prop="modName" label="Mod名称" min-width="120" align="center" show-overflow-tooltip/>
          <el-table-column prop="categoryName" label="分类" min-width="100" align="center">
            <template #default="scope">
              {{ scope.row.categoryName || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="标签" min-width="180" align="center">
            <template #default="scope">
              <div v-if="scope.row.tags && scope.row.tags.length" class="mod-tag-cell">
                <el-tag
                  v-for="tag in scope.row.tags"
                  :key="tag.id"
                  :color="tag.color"
                  effect="dark"
                  size="small"
                  style="border: none"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
              <span v-else style="color: var(--el-text-color-placeholder)">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="modDescription" label="Mod介绍" min-width="200" align="left">
            <template #default="scope">
              <el-tooltip
                :content="getDescriptionTooltip(scope.row.modDescription)"
                placement="top"
                :disabled="!getDescriptionPreview(scope.row.modDescription)"
                :show-after="300"
              >
                <div class="mod-desc-cell">
                  {{ getDescriptionPreview(scope.row.modDescription) || '暂无介绍' }}
                </div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column prop="isVisible" label="是否发布" min-width="120" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.isVisible ? 'success' : 'danger'">{{ scope.row.isVisible ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="isModpack" label="是否整合包" min-width="120" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.isModpack ? 'success' : 'info'">{{ scope.row.isModpack ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="supportedVersions" label="支持版本" min-width="120" align="center"/>
          <el-table-column prop="frameworkName" label="Mod框架" min-width="120" align="center">
            <template #default="scope">
              {{ getFrameworkNameOptionsLabel(scope.row.frameworkName) }}
            </template>
          </el-table-column>
          <el-table-column prop="downloadCloudUrl" label="网盘下载地址" min-width="300" align="center">
            <template #default="scope">
              <el-link :href="scope.row.downloadCloudUrl" target="_blank" type="primary">
                {{ scope.row.downloadCloudUrl }}
              </el-link>
            </template>
          </el-table-column>
                    <el-table-column prop="downloadDirectUrl" label="直链下载地址" min-width="300" align="center">
            <template #default="scope">
              <el-link :href="scope.row.downloadDirectUrl" target="_blank" type="primary">
                {{ scope.row.downloadDirectUrl }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="version" label="Mod版本" min-width="100" align="center"/>
          <el-table-column prop="downloadCount" label="下载次数" min-width="100" align="center"/>
          <el-table-column prop="viewCount" label="浏览次数" min-width="100" align="center"/>
          <el-table-column prop="createdAt" label="创建时间" min-width="200" align="center"/>
          <el-table-column fixed="right" label="操作" width="120" align="center">
            <template #default="scope">
              <el-button link type="primary" @click="showForm(scope.row)" v-if="hasPerm('business:mods:upd')">
                编辑
              </el-button>
              <el-button link type="danger" @click="onDelete(scope.row)" v-if="hasPerm('business:mods:del')">
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
<ModsForm ref="formRef" @reloadList="queryData" />

</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { modsApi } from '@/api/mods-api'
import { categoryApi } from '@/api/category-api'
import { tagApi } from '@/api/tag-api'
import ModsForm from './mods-form.vue'
import {Delete, Plus, Refresh, Search, ArrowUp, ArrowDown} from '@element-plus/icons-vue'
import {hasPerm} from "@/utils/permission.js";
// ------------------------ 导入列表 ------------------------
// --------------------------------------------------------
// ------------------------ 枚举量 ------------------------
const frameworkNameOptions = [
    { label: 'Bepinex', value: '1' },
    { label: 'MelonLoader', value: '2' },
]
function getFrameworkNameOptionsLabel(value) {
  const option = frameworkNameOptions.find(option => option.value === value)
  return option ? option.label : ''
}

function getDescriptionPreview(text) {
  if (!text) return ''
  return String(text)
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/[#>*_`~-]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

function getDescriptionTooltip(text) {
  const preview = getDescriptionPreview(text)
  if (preview.length <= 180) return preview
  return `${preview.slice(0, 180)}...`
}


// --------------------------------------------------------

// 查询数据表单和方法
const queryFormState = {
  pageNum: 1,
  pageSize: 10,
  modName: undefined,
  categoryId: undefined,
  tagId: undefined,
  sortItemList: []
}
const queryForm = reactive({ ...queryFormState })
const tableData = ref([])
const total = ref(0)
const categoryList = ref([])
const tagList = ref([])

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
    let queryResult = await modsApi.page(queryForm)
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
  categoryApi.getList().then(res => {
    categoryList.value = res.data || []
  }).catch(() => {})
  tagApi.getList().then(res => {
    tagList.value = res.data || []
  }).catch(() => {})
  queryData()
})

// 删除
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
    await modsApi.delete(id)
    ElMessage.success('删除成功')
    queryData()
  } catch (e) {
  }
}

// 批量删除
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
        // 当用户点击取消按钮时，这里可以处理一些逻辑
      })
}

// 请求批量删除
async function requestBatchDelete() {
  try {
    await modsApi.batchDelete(selectedRowKeyList.value)
    ElMessage.success('删除成功')
    queryData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// 表单相关
const formRef = ref(null)

function showForm(row) {
  formRef.value.show(row)
}
// -------------------------- 排序字段 -------------------------
const sortItemOptions = ref([
  {label: 'ID', column: 'id', isAsc: false},
  {label: 'Mod名称', column: 'mod_name', isAsc: false},
  {label: 'Mod英文名', column: 'english_name', isAsc: false},
  {label: '作者ID', column: 'author_id', isAsc: false},
  {label: 'Mod介绍', column: 'mod_description', isAsc: false},
  {label: '模组图标', column: 'icon_url', isAsc: false},
  {label: '支持游戏', column: 'game_name', isAsc: false},
  {label: '支持版本', column: 'supported_versions', isAsc: false},
  {label: 'Mod框架', column: 'framework_name', isAsc: false},
  {label: '直链下载地址', column: 'download_direct_url', isAsc: false},
  {label: '网盘下载地址', column: 'download_cloud_url', isAsc: false},
  {label: 'Mod版本', column: 'version', isAsc: false},
  {label: '文件大小', column: 'file_size', isAsc: false},
  {label: '下载次数', column: 'download_count', isAsc: false},
  {label: '查看次数', column: 'view_count', isAsc: false},
  {label: '是否通过审核', column: 'is_approved', isAsc: false},
  {label: '是否推荐', column: 'is_featured', isAsc: false},
  {label: '是否可见', column: 'is_visible', isAsc: false},
  {label: '是否整合包', column: 'is_modpack', isAsc: false},
  {label: '创建时间', column: 'created_at', isAsc: false},
  {label: '修改时间', column: 'updated_at', isAsc: false},
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
.mod-tag-cell {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px;
}

.mod-desc-cell {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
  max-height: 2.8em;
  word-break: break-all;
  white-space: normal;
  text-align: left;
  cursor: default;
}
</style>