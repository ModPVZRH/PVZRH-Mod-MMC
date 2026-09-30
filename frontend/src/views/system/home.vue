<template>
  <div class="home-container">
    <!-- Welcome Section -->
    <el-row :gutter="20" class="welcome-row">
      <el-col :span="24">
        <el-card shadow="never" class="welcome-card">
          <div class="welcome-content">
            <div class="user-avatar">
              <UserAvatar
                :size="64"
                :src="userInfo.avatar"
                :username="userInfo.username"
                :nickname="userInfo.nickname"
              />
            </div>
            <div class="welcome-info">
              <h2 class="greeting">{{ t('home.greetingWithName', { greeting, name: userInfo.nickname }) }}</h2>
              <p class="welcome-text">{{ $t('home.welcome') }}</p>
            </div>
            <div class="user-info">
              <div class="info-item">
                <span class="label">{{ $t('home.username') }}</span>
                <span class="value">{{ userInfo.username }}</span>
              </div>
              <div class="info-item">
                <span class="label">{{ $t('home.role') }}</span>
                <span class="value">{{ userInfo.roles?.join(', ') || $t('home.noRole') }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Statistics Cards -->
    <el-row :gutter="20" class="stats-row" v-if="hasPerm('system:role:get')">
      <el-col :span="6" v-for="(item, index) in statsData" :key="index">
        <el-card shadow="never" class="stats-wrap" :body-style="{ padding: '18px 20px' }">
          <div class="stats-card">
            <div class="stats-icon" :style="{ background: item.color }">
              <el-icon>
                <component :is="item.icon"/>
              </el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-title">{{ item.title }}</div>
              <div class="stats-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- System Info -->
    <el-row :gutter="20" class="system-row">
      <el-col :span="12">
        <el-card shadow="never" class="system-card">
          <template #header>
            <div class="card-header">
              <span>{{ $t('home.systemInfo') }}</span>
            </div>
          </template>
          <div class="system-info">
            <div class="info-item">
              <span class="label">{{ $t('home.systemName') }}</span>
              <span class="value">{{ $t('app.name') }}</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('home.systemVersion') }}</span>
              <span class="value">1.0.0</span>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('home.backend') }}</span>
              <div class="value-container">
                <div>Spring Boot 3 + Spring Security</div>
                <div>MySQL + Redis</div>
              </div>
            </div>
            <div class="info-item">
              <span class="label">{{ $t('home.frontend') }}</span>
              <span class="value">Vue 3 + Element Plus + Axios</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="quick-nav-card" v-if="hasPerm('system:role:get')">
          <template #header>
            <div class="card-header">
              <span>{{ $t('home.quickNav') }}</span>
            </div>
          </template>
          <div class="quick-nav">
            <el-button
                v-for="(nav, index) in quickNavs"
                :key="index"
                :type="nav.type"
                @click="handleNavClick(nav.path)"
            >
              <el-icon><component :is="nav.icon" /></el-icon>
              {{ nav.name }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
  import {computed, onMounted, reactive} from 'vue'
  import {useRouter} from 'vue-router'
  import {useI18n} from 'vue-i18n'
  import {User, Setting, Menu, Files, Edit, List, Upload} from '@element-plus/icons-vue'
  import {userApi} from '@/api/user-api'
  import {roleApi} from '@/api/role-api'
  import {fileApi} from '@/api/file-api'
  import {modsApi} from '@/api/mods-api'
  import {useUserStore} from '@/stores/user'
import {hasPerm} from "@/utils/permission.js";
import UserAvatar from "@/components/user-avatar.vue";
  const {t} = useI18n()
  const router = useRouter()
  const userStore = useUserStore()

  // 用户信息
  const userInfo = computed(() => {
    return {
      username: userStore.username,
      nickname: userStore.nickname || userStore.username,
      avatar: userStore.avatar,
      roles: userStore.roles.map(role => role.roleName),
      gender: userStore.gender
    }
  })

  // 问候语
  const greeting = computed(() => {
    const hour = new Date().getHours()
    if (hour < 6) return t('home.greetLateNight')
    if (hour < 9) return t('home.greetMorning')
    if (hour < 12) return t('home.greetForenoon')
    if (hour < 14) return t('home.greetNoon')
    if (hour < 17) return t('home.greetAfternoon')
    if (hour < 19) return t('home.greetEvening')
    return t('home.greetNight')
  })

  const statsCounts = reactive({
    mods: '0',
    users: '0',
    roles: '0',
    files: '0'
  })

  // Statistics Data
  const statsData = computed(() => [
    {
      title: t('home.modsTotal'),
      value: statsCounts.mods,
      icon: 'List',
      color: 'linear-gradient(135deg, #4f8cff, #2563eb)'
    },
    {
      title: t('home.usersTotal'),
      value: statsCounts.users,
      icon: 'User',
      color: 'linear-gradient(135deg, #36cfc9, #0891b2)'
    },
    {
      title: t('home.rolesTotal'),
      value: statsCounts.roles,
      icon: 'Setting',
      color: 'linear-gradient(135deg, #73d13d, #16a34a)'
    },
    {
      title: t('home.filesTotal'),
      value: statsCounts.files,
      icon: 'Files',
      color: 'linear-gradient(135deg, #ff9a43, #f97316)'
    }
  ])

  // 快捷导航
  const quickNavs = computed(() => [
    {
      name: t('menu.mods'),
      path: '/business/mods',
      icon: 'List',
      type: 'primary'
    },
    {
      name: t('menu.user'),
      path: '/system/user',
      icon: 'User',
      type: 'success'
    },
    {
      name: t('menu.role'),
      path: '/system/role',
      icon: 'Setting',
      type: 'warning'
    },
    {
      name: t('menu.file'),
      path: '/system/file',
      icon: 'Files',
      type: 'info'
    }
  ])

  // 获取统计数据
  const fetchStatsData = async () => {
    try {
      // 获取用户总数
      const [modsRes, userRes, roleRes, fileRes] = await Promise.all([
        hasPerm('business:mods:get') ? modsApi.page({pageNum: 1, pageSize: 1}) : Promise.resolve(null),
        userApi.page({pageNum: 1, pageSize: 1}),
        roleApi.page({pageNum: 1, pageSize: 1}),
        fileApi.page({pageNum: 1, pageSize: 1})
      ])
      if (modsRes?.data) {
        statsCounts.mods = modsRes.data.total.toString()
      }
      if (userRes?.data) {
        statsCounts.users = userRes.data.total.toString()
      }
      if (roleRes?.data) {
        statsCounts.roles = roleRes.data.total.toString()
      }
      if (fileRes?.data) {
        statsCounts.files = fileRes.data.total.toString()
      }
    } catch (error) {
      console.error('获取统计数据失败:', error)
    }
  }

  // 导航点击处理
  const handleNavClick = (path) => {
    router.push(path)
  }

  onMounted(() => {
    if (hasPerm('system:role:get')) {
      fetchStatsData()
    }
  })
</script>

<style scoped lang="scss">
  .home-container {
    padding: 4px 0 8px;
    min-height: 100%;

    .welcome-row {
      margin-bottom: 20px;

      .welcome-card {
        background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 48%, #38bdf8 100%);
        color: #fff;

        :deep(.el-card__body) {
          padding: 0;
        }

        .welcome-content {
          display: flex;
          align-items: center;
          padding: 28px 32px;

          .user-avatar {
            margin-right: 24px;
          }

          .welcome-info {
            flex: 1;

            .greeting {
              font-size: 26px;
              color: #fff;
              margin: 0 0 8px 0;
              font-weight: 700;
            }

            .welcome-text {
              color: rgba(255, 255, 255, 0.82);
              margin: 0;
            }
          }

          .user-info {
            margin-left: 40px;
            min-width: 180px;
            padding: 12px 16px;
            background: rgba(255, 255, 255, 0.14);
            border-radius: 12px;

            .info-item {
              margin-bottom: 8px;

              &:last-child {
                margin-bottom: 0;
              }

              .label {
                display: block;
                color: rgba(255, 255, 255, 0.7);
                margin-bottom: 2px;
                font-size: 12px;
              }

              .value {
                color: #fff;
                font-weight: 600;
              }
            }
          }
        }
      }
    }

    .stats-row {
      margin-bottom: 20px;

      .stats-card {
        display: flex;
        align-items: center;

        .stats-icon {
          width: 48px;
          height: 48px;
          border-radius: 14px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;

          .el-icon {
            font-size: 24px;
            color: #fff;
          }
        }

        .stats-info {
          flex: 1;

          .stats-title {
            font-size: 14px;
            color: #909399;
            margin-bottom: 8px;
          }

          .stats-value {
            font-size: 24px;
            font-weight: bold;
          }
        }
      }
    }

    .system-row {
      .system-card, .quick-nav-card {
        .card-header {
          font-weight: bold;
        }

        .system-info {
          .info-item {
            margin-bottom: 16px;
            display: flex;

            .label {
              color: #909399;
              flex: 0 0 80px;
              text-align: right;
              margin-right: 12px;
              padding-top: 2px;
            }

            .value {
              color: #303133;
              flex: 1;
            }

            .value-container {
              flex: 1;
              color: #303133;

              div {
                line-height: 1.5;

                &:not(:last-child) {
                  margin-bottom: 4px;
                }
              }
            }
          }
        }

        .quick-nav {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 16px;

          :deep(.el-button) {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            margin: 0;

            .el-icon {
              margin-right: 8px;
            }
          }

          :deep(.el-button + .el-button) {
            margin-left: 0;
          }
        }
      }
    }
  }
</style>