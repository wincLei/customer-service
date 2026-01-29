<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">极简客服系统</h2>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-item">
          <div class="input-wrapper">
            <span class="input-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </span>
            <input
              v-model="form.username"
              type="text"
              placeholder="账号"
              class="form-input"
              :disabled="loading"
              required
            />
            <span class="input-badge">账号</span>
          </div>
        </div>

        <div class="form-item">
          <div class="input-wrapper">
            <span class="input-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
            </span>
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="密码"
              class="form-input"
              :disabled="loading"
              required
            />
            <span class="input-badge">密码</span>
          </div>
        </div>

        <div class="form-item">
          <div class="captcha-group">
            <div class="input-wrapper flex-1">
              <span class="input-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                </svg>
              </span>
              <input
                v-model="form.captcha"
                type="text"
                placeholder="请计算并输入结果"
                class="form-input"
                :disabled="loading"
                required
              />
            </div>
            <div class="captcha-display" @click="refreshCaptcha">
              <span v-if="captchaQuestion" class="captcha-question">{{ captchaQuestion }}</span>
              <span v-else class="captcha-placeholder">点击获取</span>
            </div>
            <button 
              type="button" 
              class="refresh-button" 
              @click="refreshCaptcha"
              :disabled="loading"
              title="刷新验证码"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"></path>
              </svg>
            </button>
          </div>
        </div>

        <div class="form-item">
          <label class="checkbox-wrapper">
            <input
              v-model="form.rememberMe"
              type="checkbox"
              :disabled="loading"
            />
            <span>记住我</span>
          </label>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <button
          type="submit"
          class="login-button"
          :disabled="loading"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登录中...</span>
        </button>

        <div class="login-footer">
          还没有注册账号？<a href="javascript:;" @click="handleRegister">立即注册</a>
        </div>
      </form>
    </div>

    <div class="copyright">
      © 2025 | 
      <a href="javascript:;">京ICP备18028455号-2</a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authService } from '@/api/auth'
import api from '@/api/index'
import { getPermissionStore } from '@/stores/permission'

const router = useRouter()
const permissionStore = getPermissionStore()
const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const captchaQuestion = ref('')  // 数学题目
const captchaKey = ref('')        // 验证码key

const form = ref({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false,
})

// 获取验证码
const refreshCaptcha = async () => {
  try {
    const response = await api.get('/admin/auth/captcha')
    if (response.data?.question) {
      captchaQuestion.value = response.data.question
      captchaKey.value = response.data.key || ''
      form.value.captcha = ''  // 清空输入
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    // 使用模拟验证码用于测试
    captchaQuestion.value = '1 + 1 = ?'
    captchaKey.value = 'test-key'
  }
}

// 登录处理
const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  if (!form.value.captcha) {
    errorMessage.value = '请输入验证码答案'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const response = await authService.login({
      username: form.value.username,
      password: form.value.password,
      captcha: form.value.captcha,
      captchaKey: captchaKey.value,
    })

    if (response?.token) {
      const token = response.token
      const responseUser = response.user || {}
      
      // 构建完整的用户信息
      const userInfo = {
        id: responseUser.id || 0,
        username: responseUser.username || form.value.username,
        email: responseUser.email || '',
        role: responseUser.role || 'agent',
        avatar: responseUser.avatar || '',
        permissions: responseUser.permissions || { menus: [], actions: [] },
        projectIds: responseUser.projectIds || []  // 关联的项目ID列表
      }

      localStorage.setItem('auth_token', token)
      localStorage.setItem('user_info', JSON.stringify(userInfo))
      
      // 更新权限store
      permissionStore.setUser(userInfo)

      if (form.value.rememberMe) {
        localStorage.setItem('remember_username', form.value.username)
      } else {
        localStorage.removeItem('remember_username')
      }

      // 根据菜单权限决定跳转目标
      console.log('登录成功，用户角色:', userInfo.role, '权限:', userInfo.permissions)
      let targetPath = '/admin/settings'  // 默认跳转到设置页
      
      const menus = userInfo.permissions?.menus || []
      if (menus.includes('dashboard')) {
        targetPath = '/admin/dashboard'
      } else if (menus.includes('workbench')) {
        targetPath = '/admin/workbench'
      } else if (menus.includes('projects')) {
        targetPath = '/admin/projects'
      }
      
      // 使用 replace 而不是 push，避免返回到登录页
      await router.replace(targetPath)
      
      // 如果路由跳转没有生效，强制刷新页面
      setTimeout(() => {
        if (window.location.pathname === '/login') {
          window.location.href = targetPath
        }
      }, 100)
    } else {
      errorMessage.value = '登录失败'
      refreshCaptcha()
      form.value.captcha = ''
    }
  } catch (error: any) {
    console.error('登录错误:', error)
    const message = error?.message || error?.data?.message || '登录失败，请重试'
    errorMessage.value = message
    refreshCaptcha()
    form.value.captcha = ''
  } finally {
    loading.value = false
  }
}

// 注册
const handleRegister = () => {
  errorMessage.value = '注册功能开发中'
}

onMounted(() => {
  // 检查是否已登录
  const token = localStorage.getItem('auth_token')
  if (token) {
    console.log('已登录，跳转到工作台')
    router.replace('/admin/dashboard')
    return
  }
  
  // 加载验证码
  refreshCaptcha()
  
  // 加载记住的用户名
  const savedUsername = localStorage.getItem('remember_username')
  if (savedUsername) {
    form.value.username = savedUsername
    form.value.rememberMe = true
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.login-box {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 480px;
  padding: 45px 40px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

.login-title {
  text-align: center;
  font-size: 32px;
  font-weight: 600;
  color: #333;
  margin: 0 0 40px 0;
  letter-spacing: 2px;
}

.login-form {
  margin-top: 0;
}

.form-item {
  margin-bottom: 20px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 15px;
  color: #999;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  height: 48px;
  padding: 0 45px 0 45px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
  background: #fff;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input:disabled {
  background: #f5f7fa;
  cursor: not-allowed;
}

.input-badge {
  position: absolute;
  right: 15px;
  background: #ff4d4f;
  color: white;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  pointer-events: none;
}

.captcha-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.flex-1 {
  flex: 1;
}

.captcha-display {
  min-width: 110px;
  width: 110px;
  height: 48px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid #667eea;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  transition: all 0.3s;
  flex-shrink: 0;
  padding: 0 8px;
}

.captcha-display:hover {
  border-color: #764ba2;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.3);
  transform: translateY(-1px);
}

.captcha-question {
  color: #333;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  font-family: 'Courier New', monospace;
  white-space: nowrap;
}

.captcha-placeholder {
  color: #909399;
  font-size: 12px;
}

.refresh-button {
  width: 40px;
  height: 48px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  flex-shrink: 0;
}

.refresh-button:hover:not(:disabled) {
  border-color: #667eea;
  background: #667eea;
  color: white;
  transform: rotate(180deg);
}

.refresh-button:active:not(:disabled) {
  transform: rotate(180deg) scale(0.95);
}

.refresh-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.refresh-button svg {
  transition: inherit;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  user-select: none;
}

.checkbox-wrapper input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.error-message {
  padding: 10px 15px;
  background: #fef0f0;
  border: 1px solid #fde2e2;
  border-radius: 8px;
  color: #f56c6c;
  font-size: 13px;
  margin-bottom: 16px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 8px;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-footer {
  text-align: center;
  color: #666;
  font-size: 14px;
  margin-top: 20px;
}

.login-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.login-footer a:hover {
  color: #764ba2;
  text-decoration: underline;
}

.copyright {
  position: relative;
  z-index: 1;
  margin-top: 30px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
  text-align: center;
}

.copyright a {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
}

.copyright a:hover {
  color: #fff;
  text-decoration: underline;
}

/* 移动端响应式设计 */
@media (max-width: 640px) {
  .login-container {
    padding: 15px;
  }

  .login-box {
    max-width: 100%;
    padding: 30px 25px;
  }

  .login-title {
    font-size: 24px;
    margin-bottom: 30px;
  }

  .form-input {
    font-size: 16px; /* 防止iOS自动缩放 */
    height: 44px;
  }

  .captcha-group {
    gap: 6px;
  }

  .captcha-display {
    min-width: 90px;
    width: 90px;
    height: 44px;
    padding: 0 6px;
  }

  .captcha-question {
    font-size: 14px;
    letter-spacing: 0.5px;
  }

  .refresh-button {
    width: 36px;
    height: 44px;
  }

  .refresh-button svg {
    width: 16px;
    height: 16px;
  }

  .login-button {
    height: 44px;
    font-size: 15px;
  }

  .input-badge {
    font-size: 10px;
    padding: 2px 6px;
  }

  .copyright {
    margin-top: 20px;
    font-size: 12px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 375px) {
  .login-box {
    padding: 25px 20px;
  }

  .login-title {
    font-size: 22px;
  }

  .captcha-display {
    min-width: 80px;
    width: 80px;
  }

  .captcha-question {
    font-size: 13px;
  }

  .form-input {
    padding: 0 40px 0 40px;
  }
}
</style>
