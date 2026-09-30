<template>
  <div class="login-container">
    <div class="login-lang">
      <LanguageSwitcher light />
    </div>
    <div class="waves">
      <div class="wave wave1"></div>
      <div class="wave wave2"></div>
      <div class="wave wave3"></div>
    </div>
    <div class="login-box">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="brand">
        <img src="/logo.png" alt="logo" class="brand-logo" />
        <div>
          <h1 class="title">{{ $t('login.title') }}</h1>
          <p class="subtitle">{{ $t('login.subtitle') }}</p>
        </div>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="form.username" type="text" :placeholder="$t('login.username')" :prefix-icon="User" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" :placeholder="$t('login.password')" :prefix-icon="Lock" />
        </el-form-item>

        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" type="text" :placeholder="$t('login.captcha')" :prefix-icon="EditPen" />
            <img :src="captchaBase64Image" @click="refreshCaptcha" :alt="$t('login.captcha')" class="captcha-img" />
          </div>
        </el-form-item>
      </el-form>

      <div class="action-buttons">
        <el-button @click="onLogin" type="primary" class="login-btn">
          {{ $t('login.submit') }}
        </el-button>

        <template v-if="registerEnabled">
          <div class="divider">
            <span>{{ $t('login.noAccount') }}</span>
          </div>

          <el-button type="info" @click="router.push('/register')" class="register-btn">
            {{ $t('login.register') }}
          </el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { User, Lock, EditPen } from '@element-plus/icons-vue'
import { reactive, ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import LanguageSwitcher from '@/components/language-switcher.vue'

const { t } = useI18n()

onMounted(() => {
  refreshCaptcha()
  loadRegisterSetting()
})

async function loadRegisterSetting() {
  try {
    const res = await systemSettingApi.getPublic()
    registerEnabled.value = res?.data?.registerEnabled !== false
  } catch (e) {
    registerEnabled.value = true
  }
}
const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
  captchaOwner: ''
})

const rules = computed(() => ({
  username: [
    { required: true, message: t('login.usernameRequired'), trigger: 'blur' },
    { min: 3, max: 20, message: t('login.usernameLength'), trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: t('login.usernamePattern'),
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: t('login.passwordRequired'), trigger: ['blur', 'change'] },
    { min: 6, max: 20, message: t('login.passwordLength'), trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_.]+$/,
      message: t('login.passwordPattern'),
      trigger: 'blur'
    }
  ],
  captchaCode: [
    { required: true, message: t('login.captchaRequired'), trigger: ['blur', 'change'] }
  ]
}))

const formRef = ref()
import { loginApi } from '@/api/login-api'
import { systemSettingApi } from '@/api/system-setting-api'
import { ElMessage } from 'element-plus'

const captchaBase64Image = ref('')
const registerEnabled = ref(false)

async function refreshCaptcha() {
  try {
    let captchaResult = await loginApi.getCaptcha()
    if (captchaResult.code === 0) {
      captchaBase64Image.value = captchaResult.data.captchaBase64Image
      form.captchaOwner = captchaResult.data.captchaOwner
    }
  } catch (e) {
    console.log(e)
  }
}

import { useUserStore } from '@/stores/user'
import { localClear, localRead, localSave } from '@/utils/local-util'

import { HOME_PAGE } from "@/constants/index.js";
import { usemodLoadingStore } from "@/stores/mod-loading.js";

import { useMenuStore } from "@/stores/menu.js";
import { router, buildRoutes } from '@/router/index.js'

async function onLogin() {
  formRef.value.validate().then(async () => {
    try {
      usemodLoadingStore().show()
      const res = await loginApi.login(form)
      if (res.code === 0) {
        localSave('token', res.data.token)
        // 导航到首页
        let resInfo = await loginApi.getUserInfo()
        let menuRouterList = resInfo.data.menus.filter((e) => e.uri)
        await buildRoutes(menuRouterList)
        useUserStore().setUserInfo(res.data.userInfo)
        useUserStore().setToken(res.data.token)
        useMenuStore().setMenu(resInfo.data.menus)
        router.replace({ name: HOME_PAGE }).then(() => {
          router.go(0);
        });
      } else {
        ElMessage.error(res.code + ': ' + res.msg)
        console.log(res.msg)
      }
    } catch (error) {
      // ElMessage.error('请求失败:' + error)
      console.log('请求失败:' + error)
    } finally {
      usemodLoadingStore().hide()
    }
  })
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at top left, #60a5fa 0%, transparent 32%),
    linear-gradient(135deg, #0f172a 0%, #1d4ed8 52%, #0ea5e9 100%);
  position: relative;
  overflow: hidden;
}

.login-lang {
  position: absolute;
  top: 24px;
  right: 24px;
  z-index: 10;
}

.waves {
  position: absolute;
  width: 100%;
  height: 100%;
  bottom: 0;
  left: 0;
  pointer-events: none;
}

.wave {
  position: absolute;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.75);
  transform-origin: center center;

  &.wave1 {
    animation: wave1 20s infinite ease-in-out;
    opacity: 0.15;
    background: rgba(255, 255, 255, 0.4);
  }

  &.wave2 {
    animation: wave2 25s infinite ease-in-out;
    opacity: 0.1;
    background: rgba(255, 255, 255, 0.3);
  }

  &.wave3 {
    animation: wave3 30s infinite ease-in-out;
    opacity: 0.05;
    background: rgba(255, 255, 255, 0.2);
  }
}

@keyframes wave1 {
  0% {
    transform: translate(-25%, -25%) rotate(0deg) scale(1.5);
    border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  }

  25% {
    transform: translate(25%, 25%) rotate(90deg) scale(2);
    border-radius: 70% 30% 30% 70% / 70% 70% 30% 30%;
  }

  50% {
    transform: translate(25%, -25%) rotate(180deg) scale(1.5);
    border-radius: 30% 70% 70% 30% / 70% 70% 30% 30%;
  }

  75% {
    transform: translate(-25%, 25%) rotate(270deg) scale(2);
    border-radius: 70% 30% 30% 70% / 30% 30% 70% 70%;
  }

  100% {
    transform: translate(-25%, -25%) rotate(360deg) scale(1.5);
    border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  }
}

@keyframes wave2 {
  0% {
    transform: translate(25%, 25%) rotate(0deg) scale(2);
    border-radius: 70% 30% 30% 70% / 30% 70% 30% 70%;
  }

  33% {
    transform: translate(-25%, -25%) rotate(120deg) scale(1.5);
    border-radius: 30% 70% 70% 30% / 70% 30% 70% 30%;
  }

  66% {
    transform: translate(-25%, 25%) rotate(240deg) scale(2);
    border-radius: 70% 30% 30% 70% / 30% 70% 30% 70%;
  }

  100% {
    transform: translate(25%, 25%) rotate(360deg) scale(2);
    border-radius: 70% 30% 30% 70% / 30% 70% 30% 70%;
  }
}

@keyframes wave3 {
  0% {
    transform: translate(0%, 0%) rotate(0deg) scale(1.8);
    border-radius: 40% 60% 60% 40% / 60% 40% 60% 40%;
  }

  33% {
    transform: translate(-25%, 25%) rotate(-120deg) scale(1.5);
    border-radius: 60% 40% 40% 60% / 40% 60% 40% 60%;
  }

  66% {
    transform: translate(25%, -25%) rotate(-240deg) scale(1.8);
    border-radius: 40% 60% 60% 40% / 60% 40% 60% 40%;
  }

  100% {
    transform: translate(0%, 0%) rotate(-360deg) scale(1.8);
    border-radius: 40% 60% 60% 40% / 60% 40% 60% 40%;
  }
}

.login-box {
  width: 420px;
  padding: 36px 32px 28px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.25);
  position: relative;
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  overflow: hidden;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  object-fit: cover;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;

  &.circle-1 {
    width: 200px;
    height: 200px;
    background: linear-gradient(45deg, #a1c4fd20, #c2e9fb20);
    top: -100px;
    right: -100px;
  }

  &.circle-2 {
    width: 150px;
    height: 150px;
    background: linear-gradient(45deg, #c2e9fb20, #a1c4fd20);
    bottom: -75px;
    left: -75px;
  }
}

.title {
  text-align: left;
  color: #0f172a;
  font-size: 22px;
  margin: 0 0 4px;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.login-form {
  position: relative;
  z-index: 1;
  margin-bottom: 20px;

  :deep(.el-input) {
    --el-input-height: 45px;
  }
}

.captcha-row {
  display: flex;
  gap: 12px;

  :deep(.el-input) {
    flex: 1;
  }

  .captcha-img {
    height: 45px;
    border-radius: 4px;
    cursor: pointer;
  }
}

.action-buttons {
  :deep(.el-button) {
    width: 100%;
    height: 45px;
    font-size: 16px;
    margin-bottom: 16px;
  }

  .divider {
    position: relative;
    text-align: center;
    margin: 16px 0;

    &::before,
    &::after {
      content: '';
      position: absolute;
      top: 50%;
      width: 40%;
      height: 1px;
      background: #dcdfe6;
    }

    &::before {
      left: 0;
    }

    &::after {
      right: 0;
    }

    span {
      background: transparent;
      padding: 0 12px;
      color: #909399;
      font-size: 14px;
    }
  }
}
</style>
