<template>
  <div style="width: 100%" class="page-tab-row">
    <div class="page-tab">
      <el-tabs v-model="selectedKey"
               type="card"
               :stretch="true"
               @tab-remove="removeTab"
               @tab-click="selectTab"
      >
        <el-tab-pane :label="t('common.home')" :name="'-2'" :closable="false"
        ></el-tab-pane>
        <el-tab-pane v-for="item in tabNav" :name="item.menuId" :closable="true">
          <template #label>
                <span v-if="item.name !== HOME_PAGE">
                {{ translateMenuName(item.title) }}
                </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>
    <div class="tab-action">
      <el-dropdown>
        <el-icon :size="15">
          <Grid/>
        </el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="closeByMenu(false)">{{ t('layout.closeOthers') }}</el-dropdown-item>
            <el-dropdown-item @click="closeByMenu(true)">{{ t('layout.closeAll') }}</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {useRoute, useRouter} from 'vue-router'
import {HOME_PAGE} from '@/constants/index.js'
import {useMenuStore} from "@/stores/menu.js";
import {translateMenuName} from '@/i18n/menu.js'

const {t} = useI18n()

const router = useRouter()
const route = useRoute()
const tabNav = computed(() => useMenuStore().tabNav || [])
const selectedKey = ref(route.meta.menuId)

watch(
    () => route.meta.menuId,
    (newValue, oldValue) => {
      selectedKey.value = newValue
    },
    {immediate: true}
)

//选择某个标签页
function selectTab(pane) {
  // 寻找tab
  let tab = tabNav.value.find((e) => e.menuId === pane.props.name)
  if (!tab) {
    router.push({name: HOME_PAGE})
    return
  }
  router.push({path: tab.path, query: tab.query})
}

function removeTab(name) {
  let find = tabNav.value.find((e) => e.menuId === name)
  closeTab(find, false)
}

//通过菜单关闭
function closeByMenu(closeAll) {
  let find = tabNav.value.find((e) => e.menuId === selectedKey.value)
  if (!find || closeAll) {
    closeTab(null, true)
  } else {
    closeTab(find, true)
  }
}

//直接关闭
function closeTab(item, closeAll) {
  // 关闭单个tab
  if (item && !closeAll) {
    let goPath = undefined
    let goQuery = undefined
    if (item.fromPath && item.fromPath !== item.path && tabNav.value.some((e) => e.path === item.fromPath)) {
      // 往回显示
      goPath = item.fromPath
      goQuery = item.fromQuery
    } else {
      // 往左显示
      let index = tabNav.value.findIndex((e) => e.path === item.path)
      if (index > 0) {
        // 查询左侧tab
        let leftTabNav = tabNav.value[index - 1]
        goPath = leftTabNav.name
        goQuery = leftTabNav.query
      }
    }
    if (goPath) router.push({path: goPath, query: goQuery})
    else router.push({name: HOME_PAGE})
  } else if (!item && closeAll) {
    // 关闭所有tab
    router.push({name: HOME_PAGE})
  }
  // 关闭其他tab不做处理 直接调用closeTabNav
  useMenuStore().closeTabNav(item ? item.menuId : null, closeAll)
}

</script>

<style lang="scss" scoped>

.page-tab-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  min-height: 40px;
  max-width: 100%;
  overflow: hidden;
  padding: 0 8px;
  background: transparent;
  border-bottom: none;

  .page-tab {
    .el-tabs {
      margin-left: 4px;
    }

    max-width: 96%;
  }

  .tab-action {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    margin: 0 8px 4px 0;
    border-radius: 8px;
    color: #64748b;
    cursor: pointer;

    &:hover {
      background: #f1f5f9;
      color: var(--el-color-primary);
    }
  }
}
</style>