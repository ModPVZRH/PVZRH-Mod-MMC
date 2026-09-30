import { i18n } from './index.js'

const URI_KEY = {
  '/system': 'menu.system',
  '/system/user': 'menu.user',
  '/system/role': 'menu.role',
  '/system/menu': 'menu.permission',
  '/system/file': 'menu.file',
  '/business/mods': 'menu.mods',
  '/business/category': 'menu.category',
  '/business/tag': 'menu.tag',
  '/business/announcement': 'menu.announcement',
  '/business/client-version': 'menu.clientVersion',
}

const NAME_KEY = {
  系统管理: 'menu.system',
  用户管理: 'menu.user',
  角色管理: 'menu.role',
  权限管理: 'menu.permission',
  文件管理: 'menu.file',
  管理: 'menu.mods',
  模组管理: 'menu.mods',
  分类管理: 'menu.category',
  标签管理: 'menu.tag',
  公告管理: 'menu.announcement',
  客户端版本: 'menu.clientVersion',
  版本管理: 'menu.clientVersion',
  查询: 'menu.query',
  新增: 'menu.add',
  修改: 'menu.edit',
  删除: 'menu.delete',
  超级管理: 'menu.superAdmin',
  上传: 'menu.upload',
  首页: 'common.home',
  个人中心: 'layout.account',
  登录: 'login.submit',
  注册: 'register.submit',
}

export function translateMenuName(name, uri) {
  const key = (uri && URI_KEY[uri]) || NAME_KEY[name]
  return key ? i18n.global.t(key) : name
}

export function translateRouteTitle(title) {
  return translateMenuName(title)
}
