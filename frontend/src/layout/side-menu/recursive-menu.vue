<template>
  <el-sub-menu v-if="menuItem.children && menuItem.children.length > 0"
               :index="menuItem.menuId"
  >
    <template #title>
      <component v-if="menuItem.icon" :is="menuItem.icon" class="icon"/>
      <span class="title">{{ translateMenuName(menuItem.menuName, menuItem.uri) }}</span>
    </template>
    <recursive-menu
        v-for="child in menuItem.children"
        :key="child.menuId"
        :menu-item="child"
    />
  </el-sub-menu>
  <el-menu-item v-else :index="menuItem.menuId" @click="turnToPage(menuItem)">
    <template #default>
      <component v-if="menuItem.icon" :is="menuItem.icon" class="icon"/>
      <span>{{ translateMenuName(menuItem.menuName, menuItem.uri) }}</span>
    </template>
  </el-menu-item>
</template>
<script setup>
import {router} from "@/router/index.js";
import {translateMenuName} from '@/i18n/menu.js'

const props = defineProps({
  menuItem: {
    type: Object,
    required: true,
  }
})

// 页面跳转
function turnToPage(menu) {
  router.push({path: menu.uri})
}
</script>

<style lang="scss" scoped>
.icon {
  width: 18px;
  height: 18px;
  margin-right: 10px;
}

.title {
  font-size: 14px;
}
</style>