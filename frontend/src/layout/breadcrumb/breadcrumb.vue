<template>
  <el-breadcrumb separator=">" class="breadcrumb">
    <el-breadcrumb-item v-for="(item, index) in parentMenuList" :key="index">{{ translateMenuName(item.menuName) }}</el-breadcrumb-item>
    <el-breadcrumb-item>{{ translateRouteTitle(currentRoute.meta.title) }}</el-breadcrumb-item>
  </el-breadcrumb>
</template>
<script setup>
import {useRoute} from 'vue-router'
import {computed} from 'vue'
import {useMenuStore} from "@/stores/menu.js";
import {translateMenuName, translateRouteTitle} from '@/i18n/menu.js'

let currentRoute = useRoute()

const parentMenuList = computed(() => {
  let menuId = currentRoute.meta.menuId
  if (!menuId || typeof menuId !== 'string') {
    return []
  }
  let menuParentListMap = useMenuStore().menuParentListMap
  return menuParentListMap[menuId] || []
})
</script>
<style scoped lang="scss">
.breadcrumb {
  :deep(.el-breadcrumb__inner) {
    color: #64748b;
    font-weight: 500;
  }

  :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
    color: #0f172a;
  }
}
</style>