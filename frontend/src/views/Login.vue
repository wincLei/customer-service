<template>
  <div class="min-h-screen bg-slate-100 flex items-center justify-center px-6 py-10">
    <div class="w-full max-w-5xl bg-white shadow-2xl rounded-2xl overflow-hidden border border-slate-200">
      <div class="grid grid-cols-1 md:grid-cols-2">
        <!-- 左侧品牌区 -->
        <div class="relative bg-gradient-to-br from-slate-900 via-slate-900 to-slate-800 text-white p-10">
          <div class="flex items-center gap-3">
            <div class="h-10 w-10 rounded-xl bg-blue-500/90 flex items-center justify-center font-bold">CS</div>
            <div>
              <div class="text-lg font-semibold tracking-wide">客服系统</div>
              <div class="text-xs text-slate-300">Customer Service Console</div>
            </div>
          </div>

          <div class="mt-10 space-y-5">
            <h2 class="text-2xl font-semibold">高效 · 稳定 · 现代</h2>
            <p class="text-slate-300 text-sm leading-6">
              参考 CRMChat 的极简扁平化风格，三栏式工作台布局，聚焦会话处理效率与团队协作。
            </p>
            <div class="flex items-center gap-3 text-xs text-slate-300">
              <span class="px-2 py-1 rounded-md bg-white/10">多端接入</span>
              <span class="px-2 py-1 rounded-md bg-white/10">低延迟通讯</span>
              <span class="px-2 py-1 rounded-md bg-white/10">统一架构</span>
            </div>
          </div>

          <div class="absolute bottom-6 left-10 right-10 text-xs text-slate-400">
            © 2026 Customer Service Platform
          </div>
        </div>

        <!-- 右侧表单区 -->
        <div class="p-10">
          <div class="mb-8">
            <h1 class="text-2xl font-semibold text-slate-900">登录管理后台</h1>
            <p class="text-slate-500 text-sm mt-2">请输入账号与密码继续</p>
          </div>

          <form @submit.prevent="handleLogin" class="space-y-5">
            <div>
              <label class="block text-xs font-medium text-slate-600 mb-2">用户名</label>
              <div class="relative">
                <input
                  v-model="form.username"
                  type="text"
                  placeholder="请输入用户名"
                  class="w-full px-4 py-3 border border-slate-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500/40 focus:border-blue-500 transition bg-white"
                  :disabled="loading"
                  required
                />
              </div>
            </div>

            <div>
              <label class="block text-xs font-medium text-slate-600 mb-2">密码</label>
              <div class="relative">
                <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  class="w-full px-4 py-3 border border-slate-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500/40 focus:border-blue-500 transition bg-white"
                  :disabled="loading"
                  required
                />
                <button
                  type="button"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-slate-500 hover:text-slate-700"
                  @click="showPassword = !showPassword"
                  :disabled="loading"
                >
                  {{ showPassword ? '隐藏' : '显示' }}
                </button>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <label class="flex items-center gap-2 text-xs text-slate-600">
                <input
                  v-model="form.rememberMe"
                  type="checkbox"
                  class="h-4 w-4 text-blue-600 rounded border-slate-300 focus:ring-blue-500"
                  :disabled="loading"
                />
                记住账号
              </label>
              <button type="button" class="text-xs text-slate-500 hover:text-blue-600" :disabled="loading">
                忘记密码
              </button>
            </div>

            <div v-if="errorMessage" class="rounded-xl border border-red-200 bg-red-50 text-red-700 text-xs px-3 py-2">
              {{ errorMessage }}
            </div>

            <button
              type="submit"
              class="w-full h-11 rounded-xl bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
              :disabled="loading"
            >
              <span v-if="!loading">登录</span>
              <span v-else class="flex items-center gap-2">
                <svg class="animate-spin h-4 w-4" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none" />
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                登录中...
              </span>
            </button>

            <div class="rounded-xl border border-blue-100 bg-blue-50 text-blue-700 text-xs px-3 py-2 text-center">
              演示账号：admin / admin123
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)

const form = ref({
  username: '',
  password: '',
  rememberMe: false,
})

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const response = await authService.login({
      username: form.value.username,
      password: form.value.password,
    })

    if (response?.token) {
      const token = response.token
      const userInfo = response.user || {}

      localStorage.setItem('auth_token', token)
      localStorage.setItem('user_info', JSON.stringify(userInfo))

      if (form.value.rememberMe) {
        localStorage.setItem('remember_username', form.value.username)
      } else {
        localStorage.removeItem('remember_username')
      }

      router.push('/admin/dashboard')
    } else {
      errorMessage.value = '登录失败'
    }
  } catch (error: any) {
    const message = error?.message || error?.data?.message || '登录失败，请重试'
    errorMessage.value = message
  } finally {
    loading.value = false
  }
}

// 如果有保存的用户名，则自动填充
const savedUsername = localStorage.getItem('remember_username')
if (savedUsername) {
  form.value.username = savedUsername
  form.value.rememberMe = true
}
</script>

<style scoped>
/* 细节阴影优化 */
</style>
