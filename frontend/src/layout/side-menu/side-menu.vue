<template>
  <div class="side-menu" :style="{width: !collapse ? sideMenuWidth : 'auto'}">
    <div class="logo" @click="onGoHome">
      <img class="logo-img" src="/logo.png"/>
      <div class="title" v-show="!collapse">{{ websiteName }}</div>
    </div>
    <el-menu mode="vertical"
             :collapse="collapse"
             :default-openeds="openedMenuIds"
             :default-active="activeMenuId"
    >
      <recursiveMenu
          v-for="item in menuData"
          :key="item.menuId"
          :menu-item="item"
      />
    </el-menu>
  </div>
</template>
<script setup>
import {ref, computed, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import RecursiveMenu from "@/layout/side-menu/recursive-menu.vue";
import {useMenuStore} from "@/stores/menu.js";
import {HOME_PAGE} from "@/constants/index.js";
import {useRoute, useRouter} from "vue-router";
import {useAdminConfigStore} from "@/stores/admin-config.js";
import _ from 'lodash'

const {t} = useI18n()

const props = defineProps({
  collapse: {
    type: Boolean,
    default: false
  }
})

const menuData = computed(() => useMenuStore().menuTree)
const websiteName = computed(() => t('app.name'))
//菜单宽度
const sideMenuWidth = computed(() => useAdminConfigStore().sideMenuWidth)
const router = useRouter()

function onGoHome() {
  router.push({name: HOME_PAGE})
}

// ------------------------------- 打开的菜单栏 ---------------------------------
const openedMenuIds = ref([])
const activeMenuId = ref('')
let currentRoute = useRoute()

function updateMenuOpened() {
  activeMenuId.value = currentRoute.meta.menuId

  let menuParentListMap = useMenuStore().menuParentListMap
  let parentList = menuParentListMap[currentRoute.meta.menuId] || []
  if(!props.collapse){
    let needOpenIds = _.map(parentList, 'menuId')
    openedMenuIds.value = _.union(openedMenuIds.value, needOpenIds)
  }
}

watch(
    currentRoute,
    () => {
      updateMenuOpened()
    },
    {
      immediate: true
    }
)


</script>
<style scoped lang="scss">
.side-menu {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--admin-sidebar-bg);

  .logo {
    display: flex;
    align-items: center;
    height: var(--admin-header-height);
    padding: 0 14px;
    cursor: pointer;
    flex-shrink: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);

    .logo-img {
      height: 32px;
      width: 32px;
      border-radius: 8px;
      object-fit: cover;
      flex-shrink: 0;
    }

    .title {
      margin-left: 10px;
      color: #fff;
      font-size: 15px;
      font-weight: 700;
      letter-spacing: 0.02em;
      white-space: nowrap;
    }
  }

  .el-menu {
    flex: 1;
    min-height: 0;
    height: auto;
    background: var(--admin-sidebar-bg);
    --el-menu-bg-color: var(--admin-sidebar-bg);
    --el-menu-hover-bg-color: rgba(255, 255, 255, 0.08);
    --el-menu-text-color: var(--admin-sidebar-text);
    --el-menu-active-color: var(--admin-sidebar-active);
    --el-menu-item-height: 46px;
    padding: 8px 8px 16px;
    box-sizing: border-box;
    overflow-x: hidden;
    overflow-y: auto;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      border-radius: 8px;
      margin-bottom: 4px;
    }

    :deep(.el-menu-item.is-active) {
      background: var(--el-color-primary);
      color: #fff;
    }

    :deep(.el-sub-menu .el-menu) {
      background: transparent;
    }

    :deep(.el-sub-menu .el-menu-item) {
      min-width: auto;
    }
  }
}
</style>