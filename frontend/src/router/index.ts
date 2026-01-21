import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { authService } from '@/api/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/admin/dashboard',
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
    meta: { requiresAuth: true, role: 'agent' },
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表板' },
      },
      {
        path: 'chat',
        component: () => import('@/views/admin/Workbench.vue'),
        meta: { title: '工作台' },
      },
      {
        path: 'settings',
        component: () => import('@/views/admin/Settings.vue'),
        meta: { title: '设置' },
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
      next()
    }
  } else {
    // 如果已登录且访问登录页，重定向到仪表板
    if (isAuthenticated && to.path === '/login') {
      next('/admin/dashboard')
    } else {
      next()
    }
  }
})

export default router
