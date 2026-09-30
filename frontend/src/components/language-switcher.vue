<template>
  <el-dropdown trigger="click" @command="onChange">
    <span class="language-switcher" :class="{ light }">
      {{ currentLabel }}
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="zh-cn" :disabled="language === 'zh-cn'">
          中文
        </el-dropdown-item>
        <el-dropdown-item command="en" :disabled="language === 'en'">
          English
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useAdminConfigStore } from '@/stores/admin-config.js'
import { applyLanguage } from '@/i18n/index.js'

defineProps({
  light: {
    type: Boolean,
    default: false,
  },
})

const configStore = useAdminConfigStore()
const language = computed(() => configStore.language)

const currentLabel = computed(() => (language.value === 'en' ? 'EN' : '中文'))

function onChange(lang) {
  configStore.language = lang
  applyLanguage(lang)
}
</script>

<style scoped lang="scss">
.language-switcher {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 32px;
  padding: 0 10px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  user-select: none;

  &:hover {
    background: #f1f5f9;
    color: var(--el-color-primary);
  }

  &.light {
    color: #fff;
    background: rgba(255, 255, 255, 0.16);

    &:hover {
      background: rgba(255, 255, 255, 0.28);
      color: #fff;
    }
  }
}
</style>
