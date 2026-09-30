<template>
  <el-config-provider :locale="locale">
    <div
         class="app-shell"
         v-loading="loading"
         :element-loading-text="t('app.loading')"
         :element-loading-spinner="svg"
         element-loading-svg-view-box="-10, -10, 50, 50">
      <RouterView/>
    </div>
  </el-config-provider>
</template>
<script setup>
import {computed, watch} from 'vue'
import {useI18n} from 'vue-i18n'
import {usemodLoadingStore} from "@/stores/mod-loading.js";
import {useAdminConfigStore} from "@/stores/admin-config.js";
import {en, zhCn} from "element-plus/es/locale/index";
import {applyLanguage} from '@/i18n/index.js'

const {t} = useI18n()
const configStore = useAdminConfigStore()
const locale = computed(() => (configStore.language === 'zh-cn' ? zhCn : en))
const loading = computed(() => usemodLoadingStore().loading)
const themeColor = computed(() => configStore.themeColor)
const svg = `
        <path class="path" d="
          M 30 15
          L 28 17
          M 25.61 25.61
          A 15 15, 0, 0, 1, 15 30
          A 15 15, 0, 1, 1, 27.99 7.5
          L 15 15
        " style="stroke-width: 4px; fill: rgba(0, 0, 0, 0)"/>`
// 监听 themeColor 的变化并更新 CSS 变量
watch(themeColor, () => {
  document.documentElement.style.setProperty('--el-color-primary', themeColor.value);
}, {immediate: true});
watch(() => configStore.language, (lang) => {
  applyLanguage(lang)
}, {immediate: true});
</script>
<style lang="scss">
html,
body,
#app {
  height: 100%;
  margin: 0;
}

.el-config-provider,
.app-shell {
  height: 100%;
}

body {
  background-color: var(--admin-bg, #f3f5f9);
  margin: 0;
}

.drawer-footer {
  position: relative;
  margin: 0;
  padding: 0;
  border-top: none;
  text-align: right;
  box-shadow: none;
}

/* 全局滚动条的样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.3);
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.5);
}
</style>
