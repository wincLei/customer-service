import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { authService } from '@/api/auth'
import { getPermissionStore } from '@/stores/permission'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    redirect: () => {
      // 根据用户权限重定向到第一个有权限的菜单
      const userInfo = localStorage.getItem('user_info')
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo)
          const menus = user.permissions?.menus || []
          if (menus.includes('dashboard')) return '/admin/dashboard'
          if (menus.includes('workbench')) return '/admin/workbench'
          if (menus.includes('projects')) return '/admin/projects'
          if (menus.includes('settings')) return '/admin/settings'
        } catch (e) {
          console.error('解析用户信息失败', e)
        }
      }
      return '/admin/settings'
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据概览', menu: 'dashboard' },
      },
      {
        path: 'workbench',
        name: 'Workbench',
        component: () => import('@/views/admin/Workbench.vue'),
        meta: { title: '客服工作台', menu: 'workbench' },
      },
      {
        path: 'projects',
        name: 'ProjectManagement',
        component: () => import('@/views/admin/ProjectManagement.vue'),
        meta: { title: '项目管理', menu: 'projects' },
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '用户管理', menu: 'users' },
      },
      {
        path: 'roles',
        name: 'RoleManagement',
        component: () => import('@/views/admin/RoleManagement.vue'),
        meta: { title: '角色管理', menu: 'roles' },
      },
      {
        path: 'menus',
        name: 'MenuManagement',
        component: () => import('@/views/admin/MenuManagement.vue'),
        meta: { title: '菜单管理', menu: 'menus' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/admin/Settings.vue'),
        meta: { title: '系统设置', menu: 'settings' },
      },
    ],
  },
  {
    path: '/portal',
    component: () => import('@/layouts/PortalLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('@/views/portal/FAQHome.vue'),
        meta: { title: '帮助中心' },
      },
      {
        path: 'chat',
        component: () => import('@/views/portal/WebChatWindow.vue'),
        meta: { title: '在线客服' },
      },
    ],
  },
  {
    path: '/mobile/chat',
    component: () => import('@/layouts/MobileLayout.vue'),
    children: [
      {
        path: '',
        component: () => import('@/views/h5/H5Chat.vue'),
        meta: { title: '客服对话' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  const isAuthenticated = authService.isAuthenticated()

  // 如果要访问需要认证的页面
  if (requiresAuth) {
    if (!isAuthenticated) {
      // 未登录，重定向到登录页
      next('/login')
    } else {
      // 检查菜单权限
      const menu = to.meta.menu as string | undefined
      if (menu) {
        const permissionStore = getPermissionStore()
        if (!permissionStore.hasMenu(menu)) {
          // 无权限访问，重定向到第一个有权限的菜单
          const userInfo = localStorage.getItem('user_info')
          if (userInfo) {
            try {
              const user = JSON.parse(userInfo)
              const menus = user.permissions?.menus || []
              if (menus.includes('dashboard')) {
                next('/admin/dashboard')
              } else if (menus.includes('workbench')) {
                next('/admin/workbench')
              } else if (menus.includes('settings')) {
                next('/admin/settings')
              } else {
                next('/login')
              }
              return
            } catch (e) {
              console.error('解析用户信息失败', e)
            }
          }
          next('/login')
          return
        }
      }
      next()
    }
  } else {
    // 如果已登录且访问登录页，重定向到有权限的页面
    if (isAuthenticated && to.path === '/login') {
      const userInfo = localStorage.getItem('user_info')
      if (userInfo) {
        try {
          const user = JSON.parse(userInfo)
          const menus = user.permissions?.menus || []
          if (menus.includes('dashboard')) {
            next('/admin/dashboard')
          } else if (menus.includes('workbench')) {
            next('/admin/workbench')
          } else {
            next('/admin/settings')
          }
          return
        } catch (e) {
          console.error('解析用户信息失败', e)
        }
      }
      next('/admin/settings')
    } else {
      next()
    }
  }
})

export default router
