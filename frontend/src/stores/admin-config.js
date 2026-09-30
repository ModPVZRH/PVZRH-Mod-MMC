import {defineStore} from 'pinia'
import {applyLanguage} from '@/i18n/index.js'

export const defaultConfig = {
  language: 'zh-cn',// 'en' : 'zh-cn'
  themeColor: '#409eff',
  sideMenuWidth: '200px',
  websiteName: '模组管理系统',
  footerShow: true,
  pageTabShow:true,
  breadCrumbShow: true,
}

export const useAdminConfigStore = defineStore('ModConfig', {
  state: () => ({
    ...defaultConfig
  }),
  actions: {
    reset() {
      for (const k in defaultConfig) {
        this[k] = defaultConfig[k]
      }
      applyLanguage(defaultConfig.language)
    }
  },
  persist: {
    key: 'ModConfig',
    storage: localStorage,
  },
})

export const themeColors = [
  {primaryColor: '#409eff'},
  {primaryColor: '#1677ff'},
  {primaryColor: '#2F54EB'},
  {primaryColor: '#00b96b'},
  {primaryColor: '#F5222D'},
  {primaryColor: '#13c2c2'},
  {primaryColor: '#EB2F96'},
  {primaryColor: '#722ED1'},
  {primaryColor: '#52c41a'},
  {primaryColor: '#009688'},
  {primaryColor: '#fa541c'}
];