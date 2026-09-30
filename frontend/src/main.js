import {createApp} from 'vue'
import {createPinia} from 'pinia'

import App from './App.vue'
import {router, buildRoutes} from '@/router/index.js'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import {useUserStore} from "@/stores/user.js";
import {useMenuStore} from "@/stores/menu.js";
import {ElMessage} from "element-plus";
import {localRead} from "@/utils/local-util.js";
import {loginApi} from "@/api/login-api.js";
import i18n from '@/i18n/index.js'
import '@/styles/common.scss'
import '@/styles/theme.scss'

export async function getUserInfo() {
  try {
    const res = await loginApi.getUserInfo()
    let menuRouterList = res.data.menus.filter((e) => e.uri)
    await buildRoutes(menuRouterList)
    init()
    useUserStore().setUserInfo(res.data)
    useMenuStore().setMenu(res.data.menus)
  } catch (e) {
    console.log('getUserInfo before init error')
    ElMessage.error(e.data ? e.data.msg : e.message)
    init()
  }
}


let token = localRead('token')
if (!token) {
  init()
} else {
  getUserInfo()
}


function init() {
  const app = createApp(App)
  app.config.globalProperties.$icons = []
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.config.globalProperties.$icons.push(key)
    app.component(key, component)
  }
  const pinia = createPinia()
  pinia.use(piniaPluginPersistedstate)
  app.use(pinia)
  app.use(i18n)
  app.use(router)
  app.mount('#app')
}


