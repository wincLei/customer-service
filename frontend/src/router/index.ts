import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/admin',
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

router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
  
  if (requiresAuth) {
    // TODO: 检查认证状态
    const isAuthenticated = !!localStorage.getItem('auth_token')
    if (!isAuthenticated) {
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
