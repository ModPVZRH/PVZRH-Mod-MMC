<template>
  <el-dropdown class="header-trigger">
    <div class="wrapper">
      <UserAvatar
        class="avatar-image"
        :src="avatar"
        :username="username"
        :nickname="nickname"
        :size="28"
      />
      <span class="name">{{ nickname }}</span>
    </div>
    <template #dropdown>
      <el-dropdown-menu class="avatar-menu">
        <el-dropdown-item @click="toAccount">
          <span>{{ t('layout.account') }}</span>
        </el-dropdown-item>
        <el-dropdown-item @click="onLogout">
          <span>{{ t('layout.logout') }}</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>
<script setup>
import {computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {loginApi} from "@/api/login-api.js";
import {useUserStore} from "@/stores/user.js";
import {useMenuStore} from "@/stores/menu.js";
import {useRouter} from "vue-router";
import UserAvatar from "@/components/user-avatar.vue";

const {t} = useI18n()

const userStore = useUserStore()
const username = computed(() => userStore.username)
const nickname = computed(() => userStore.nickname)
const avatar = computed(() => userStore.avatar)

async function onLogout() {
  try {
    let res = await loginApi.logout()
    if(res.code === 0){
      ElMessage.success(t('layout.logoutSuccess'))
    }
  } catch (e) {
    console.log(e)
  } finally {
    useUserStore().reset()
    useMenuStore().reset()
    location.reload()
  }
}

// ------------------------ 个人中心 ------------------------
const router = useRouter()

function toAccount(menuId) {
  router.push({
    path: '/account',
    // query: {menuId}
  })
}
</script>

<style scoped lang="scss">
.wrapper {
  cursor: pointer;
  display: flex;
  align-items: center;

  .avatar-image {
    margin-right: 8px;
  }

  .name {
    font-size: 13px;
    color: #334155;
    font-weight: 500;
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>