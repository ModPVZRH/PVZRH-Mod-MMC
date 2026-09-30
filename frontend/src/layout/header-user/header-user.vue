<template>
  <el-space :size="10">
    <div class="setting">
      <el-icon @click="showSetting" class="operate-icon">
        <Setting/>
      </el-icon>
    </div>
    <LanguageSwitcher/>
    <div class="user-space-item">
      <HeaderAvatar></HeaderAvatar>
    </div>

    <HeaderSetting ref="doShow"/>
  </el-space>
</template>

<script setup>
import {ref} from 'vue'
import HeaderSetting from "@/layout/header-user/header-setting.vue";
import HeaderAvatar from "@/layout/header-user/header-avatar.vue";
import LanguageSwitcher from '@/components/language-switcher.vue'

// 设置
const doShow = ref(null)

function showSetting() {
  if (doShow.value) {
    doShow.value.show()
  }
}


</script>

<style lang="scss" scoped>
.setting {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  cursor: pointer;

  &:hover {
    background: #f1f5f9;
    color: var(--el-color-primary);
  }
}

.user-space-item {
  padding: 4px 8px 4px 4px;
  border-radius: 999px;
  transition: background-color 0.2s;

  &:hover {
    background: #f1f5f9;
  }
}
</style>
