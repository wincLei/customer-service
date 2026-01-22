import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { authService } from '@/api/auth'

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
      // 根据用户角色重定向
      const userInfo = localStorage.getItem('user_info')
      if (userInfo) {
        const user = JSON.parse(userInfo)
        // 管理员进入Dashboard，客服进入Workbench
        return user.role === 'admin' ? '/admin/dashboard' : '/admin/workbench'
      }
      return '/admin/dashboard'
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据概览', roles: ['admin'] },
      },
      {
        path: 'workbench',
        name: 'Workbench',
        component: () => import('@/views/admin/Workbench.vue'),
        meta: { title: '客服工作台', roles: ['agent'] },
      },
      {
        path: 'projects',
        name: 'ProjectManagement',
        component: () => import('@/views/admin/ProjectManagement.vue'),
        meta: { title: '项目管理', roles: ['admin'] },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/admin/Settings.vue'),
        meta: { title: '系统设置' },
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
      // 检查角色权限
      const roles = to.meta.roles as string[] | undefined
      if (roles && roles.length > 0) {
        const userInfo = localStorage.getItem('user_info')
        if (userInfo) {
          const user = JSON.parse(userInfo)
          if (!roles.includes(user.role)) {
            // 无权限访问，重定向到对应的默认页面
            if (user.role === 'admin') {
              next('/admin/dashboard')
            } else {
              next('/admin/workbench')
            }
            return
          }
        }
      }
      next()
    }
  } else {
    // 如果已登录且访问登录页，重定向到工作台
    if (isAuthenticated && to.path === '/login') {
      next('/admin/workbench')
    } else {
      next()
    }
  }
})

export default router
