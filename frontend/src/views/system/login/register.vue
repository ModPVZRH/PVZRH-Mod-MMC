<template>
  <div class="register-container">
    <div class="register-lang">
      <LanguageSwitcher light />
    </div>
    <div class="waves">
      <div class="wave wave1"></div>
      <div class="wave wave2"></div>
      <div class="wave wave3"></div>
    </div>
    <div class="register-box">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
      <div class="brand">
        <img src="/logo.png" alt="logo" class="brand-logo" />
        <div>
          <h1 class="title">{{ $t('register.title') }}</h1>
          <p class="subtitle">{{ $t('register.subtitle') }}</p>
        </div>
      </div>

      <el-form ref="formRef" :model="formData" :rules="rules" class="register-form">
        <el-form-item prop="username" class="custom-form-item">
          <el-input v-model="formData.username" :placeholder="$t('register.username')" class="custom-input" />
        </el-form-item>

        <el-form-item prop="nickname" class="custom-form-item">
          <el-input v-model="formData.nickname" :placeholder="$t('register.nickname')" class="custom-input" />
        </el-form-item>

        <el-form-item prop="password" class="custom-form-item">
          <el-input type="password" v-model="formData.password" :placeholder="$t('register.password')" class="custom-input" />
        </el-form-item>

        <el-form-item prop="newPassword" class="custom-form-item">
          <el-input type="password" v-model="formData.newPassword" :placeholder="$t('register.confirmPassword')" class="custom-input" />
        </el-form-item>

        <el-form-item prop="captchaCode" class="custom-form-item">
          <div class="captcha-row">
            <el-input v-model="formData.captchaCode" :placeholder="$t('register.captcha')" class="custom-input" />
            <img :src="captchaBase64Image" @click="refreshCaptcha" :alt="$t('register.captcha')" class="captcha-img">
          </div>
        </el-form-item>
      </el-form>

      <div class="action-buttons">
        <el-button type="primary" class="register-btn" @click="register">
          {{ $t('register.submit') }}
        </el-button>

        <div class="divider">
          <span>{{ $t('register.hasAccount') }}</span>
        </div>

        <el-button type="info" class="login-btn" @click="router.push('/login')">
          {{ $t('register.backLogin') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { loginApi } from '@/api/login-api'
import { systemSettingApi } from '@/api/system-setting-api'
import { ElMessage, ElNotification } from 'element-plus'
import { router } from '@/router/index.js'
import LanguageSwitcher from '@/components/language-switcher.vue'

const { t } = useI18n()

const formData = reactive({
  username: '',
  nickname: '',
  password: '',
  newPassword: '',
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
  nickname: [
    { required: true, message: t('register.nicknameRequired'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('login.passwordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('login.passwordLength'), trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_]+$/,
      message: t('login.passwordPattern'),
      trigger: 'blur'
    }
  ],
  newPassword: [
    { required: true, message: t('register.confirmPasswordRequired'), trigger: 'blur' },
    { min: 6, max: 20, message: t('login.passwordLength'), trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_.]+$/,
      message: t('login.passwordPattern'),
      trigger: 'blur'
    }
  ],
  captchaCode: [
    { required: true, message: t('login.captchaRequired'), trigger: 'blur' }
  ],
  phone: [
    { required: true, message: t('register.phoneRequired'), trigger: 'blur' }
  ]
}))

const captchaBase64Image = ref('')

const formRef = ref()

onMounted(async () => {
  try {
    const res = await systemSettingApi.getPublic()
    if (res?.data?.registerEnabled === false) {
      ElMessage.warning(t('register.closed'))
      router.replace('/login')
      return
    }
  } catch (e) {
    console.error(e)
  }
  refreshCaptcha()
})

async function refreshCaptcha() {
  try {
    let captchaResult = await loginApi.getCaptcha()
    if (captchaResult.code === 0) {
      captchaBase64Image.value = captchaResult.data.captchaBase64Image
      formData.captchaOwner = captchaResult.data.captchaOwner
    }
  } catch (e) {
    console.error(e)
  }
}

async function register() {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await loginApi.register(formData)
        if (res.code === 0) {
          console.log(res.data)
          ElMessage.success(t('register.success'))
          router.push('/login')
        } else {
          ElMessage.error(res.msg)
        }
      } catch (error) {
        console.error('请求失败:', error)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.register-container {
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

.register-lang {
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

.register-box {
  width: 440px;
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
  margin-bottom: 24px;
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
    background: linear-gradient(45deg, #84fab020, #8fd3f420);
    top: -100px;
    right: -100px;
  }

  &.circle-2 {
    width: 150px;
    height: 150px;
    background: linear-gradient(45deg, #8fd3f420, #84fab020);
    bottom: -75px;
    left: -75px;
  }

  &.circle-3 {
    width: 100px;
    height: 100px;
    background: linear-gradient(45deg, #84fab030, #8fd3f430);
    top: 50%;
    right: -50px;
    transform: translateY(-50%);
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

.register-form {
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
  position: relative;
  z-index: 1;

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
