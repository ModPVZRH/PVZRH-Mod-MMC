<template>
  <el-container class="mod-layout">
    <el-aside class="mod-layout-aside"
              width="auto">
      <SideMenu :collapse="collapse"/>
    </el-aside>
    <el-container class="mod-layout-container">
      <el-header class="mod-layout-header">
        <div class="mod-layout-header-action">
          <div class="mod-layout-header-left">
            <span class="collapse-button">
              <el-icon v-if="collapse" @click="() => (collapse = !collapse)">
                <expand/>
              </el-icon>
              <el-icon v-else @click="() => (collapse = !collapse)">
                <fold/>
              </el-icon>
            </span>
            <el-tooltip :content="t('layout.home')" placement="bottom">
              <span class="home-button" @click="goHome">
                <el-icon>
                  <home-filled/>
                </el-icon>
              </span>
            </el-tooltip>
            <span class="menu-breadcrumb" v-if="breadCrumbShow">
              <Breadcrumb/>
            </span>
          </div>
          <!---用户区域 -->
          <div class="mod-layout-header-right">
            <HeaderUser></HeaderUser>
          </div>
        </div>
        <span v-if="pageTagShow"> <PageTab/></span>
      </el-header>

      <el-main class="mod-layout-main">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" :key="route.name"/>
          </keep-alive>
        </router-view>
      </el-main>

      <el-footer class="mod-layout-footer" v-show="footerShow">
        <div>{{ t('app.copyright') }}</div>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import {computed, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {useAdminConfigStore} from "@/stores/admin-config.js";
import SideMenu from "@/layout/side-menu/side-menu.vue";
import {useRoute, useRouter} from "vue-router";
import {HOME_PAGE} from "@/constants/index.js";
import PageTab from "@/layout/page-tab/page-tab.vue";
import Breadcrumb from "@/layout/breadcrumb/breadcrumb.vue";
import HeaderUser from "@/layout/header-user/header-user.vue";

const {t} = useI18n()
const collapse = ref(false)


const breadCrumbShow = computed(() => useAdminConfigStore().breadCrumbShow)
const pageTagShow = computed(() => useAdminConfigStore().pageTabShow)
const footerShow = computed(() => useAdminConfigStore().footerShow)

const router = useRouter()

function goHome() {
  router.push({name: HOME_PAGE})
}

const route = useRoute()
</script>

<style lang="scss" scoped>
.mod-layout {
  height: 100vh;
  width: 100%;
  overflow: hidden;
  background: var(--admin-bg);

  .mod-layout-aside {
    height: 100vh;
    overflow: hidden;
    box-shadow: 4px 0 24px rgba(15, 23, 42, 0.08);
    z-index: 30;
  }

  .mod-layout-container {
    min-width: 0;
    flex: 1;
    height: 100vh;
    overflow: hidden;
    background: var(--admin-bg);

    .mod-layout-header {
      background: rgba(255, 255, 255, 0.92);
      backdrop-filter: blur(10px);
      padding: 0;
      z-index: 21;
      height: auto;
      flex-shrink: 0;
      box-shadow: 0 1px 0 rgba(15, 23, 42, 0.06);

      .mod-layout-header-action {
        height: var(--admin-header-height);
        display: flex;
        align-items: center;
        padding: 0 8px 0 4px;

        .mod-layout-header-left {
          display: flex;
          align-items: center;
          flex: 1 1 0;

          .collapse-button,
          .home-button {
            width: 32px;
            height: 32px;
            margin-left: 8px;
            border-radius: 8px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            color: #475569;
            transition: background-color 0.2s, color 0.2s;

            &:hover {
              background-color: #f1f5f9;
              color: var(--el-color-primary);
            }
          }

          .menu-breadcrumb {
            margin-left: 12px;
          }
        }

        .mod-layout-header-right {
          display: flex;
          justify-content: flex-end;
          padding-right: 8px;
        }
      }
    }

    .mod-layout-main {
      min-width: 0;
      min-height: 0;
      flex: 1;
      overflow-x: hidden;
      overflow-y: auto;
      padding: 16px 20px;
      background: var(--admin-bg);
    }

    .mod-layout-footer {
      position: relative;
      padding: 0;
      display: flex;
      justify-content: center;
      align-items: center;
      background-color: transparent;
      height: 40px;
      flex-shrink: 0;
      color: #94a3b8;
      font-size: 12px;
      border-top: none;
    }
  }
}
</style>
