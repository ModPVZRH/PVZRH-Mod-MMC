import {createRouter, createWebHistory} from 'vue-router'
import {HOME_PAGE, LAYOUT} from "@/constants/index.js";
import {useUserStore} from "@/stores/user.js";
import {useMenuStore} from "@/stores/menu.js";
import {systemSettingApi} from "@/api/system-setting-api.js";

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [{
    path: '/', name: LAYOUT,
    redirect: {name: HOME_PAGE},
    component: () => import('@/layout/side-layout.vue'),
    meta: {menuId: '-1', title: '首页', icon: 'HomeOutlined', hideInMenu: true},
    children: [
      {
        path: '/home', name: HOME_PAGE,
        component: () => import('@/views/system/home.vue'),
        meta: {menuId: '-2', title: '首页', icon: 'HomeOutlined', hideInMenu: true},
      },
      {
        path: '/account',
        name: 'Account',
        component: () => import('@/views/system/account.vue'),
        meta: {title: '个人中心', hideInMenu: false}
      }
    ]
  }, {
    path: '/login', name: 'login',
    component: () => import('@/views/system/login/login.vue'),
    meta: {title: '登录', hideInMenu: true}
  }, {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/system/login/register.vue'),
    meta: {title: '注册', hideInMenu: true}
  },
    {path: '/:pathMatch(.*)*', name: '404', component: () => import('@/views/system/404.vue')},
    {path: '/403', name: '403', component: () => import('@/views/system/403.vue')}
  ],
  strict: true,
  scrollBehavior: () => ({left: 0, top: 0})
})


// ----------------------- 路由加载前 -----------------------
router.beforeEach(async (to, from, next) => {
  // 公共页面，任何时候都可以跳转
  if (to.name === '404') {
    next()
    return
  }
  // 验证登录
  const token = useUserStore().token
  if (!token) {
    useUserStore().reset()
    if (to.name === 'login') next()
    else if (to.path === '/register') {
      try {
        const res = await systemSettingApi.getPublic()
        if (res?.data?.registerEnabled === false) {
          next({path: '/login'})
          return
        }
      } catch (e) {
        // 读不到开关时保持原注册页可用，真正提交仍由后端拦截
      }
      next()
    }
    else next({path: '/login'})
    return
  }

  if (to.name === HOME_PAGE) {
    next()
    return
  }

  // 设置tabNav
  useMenuStore().setTabNav(from, to)

  next()
})

// ----------------------- 路由加载后 -----------------------
router.afterEach(() => {
})

// ----------------------- 构建router对象 -----------------------
export async function buildRoutes(menuList) {
  const routerList = []
  const modules = import.meta.glob('../views/**/**.vue')
  for (const e of menuList) {
    if (!e.menuId) continue
    let route = {
      path: e.uri.startsWith('/') ? e.uri : `/${e.uri}`,
      meta: {
        menuId: e.menuId,
        title: e.menuName,
        icon: e.icon
      }
    }

    let componentPath = e.componentPath && e.componentPath.startsWith('/') ? e.componentPath : '/' + e.componentPath
    let relativePath = `../views${componentPath}`
    route.component = modules[relativePath]

    routerList.push(route)
  }
  // 添加到路由里
  router.addRoute({
    path: '/',
    meta: {},
    component: () => import('@/layout/side-layout.vue'),
    children: routerList
  })
}
