<template>
  <div class="admin-layout">
    <el-container>
      <!-- 左侧导航栏 -->
      <el-aside width="60px" class="sidebar">
        <!-- 管理员：显示数据概览 -->
        <div v-if="userRole === 'admin'" class="nav-icon" @click="navigateTo('dashboard')" :class="{ active: currentView === 'dashboard' }">
          <i class="el-icon-data-analysis"></i>
          <div class="nav-label">数据</div>
        </div>
        
        <!-- 客服：显示工作台 -->
        <div v-if="userRole === 'agent'" class="nav-icon" @click="navigateTo('workbench')" :class="{ active: currentView === 'workbench' }">
          <i class="el-icon-chat-line-square"></i>
          <div class="nav-label">工作台</div>
        </div>

        <!-- 管理员：项目管理 -->
        <div v-if="userRole === 'admin'" class="nav-icon" @click="navigateTo('projects')" :class="{ active: currentView === 'projects' }">
          <i class="el-icon-folder"></i>
          <div class="nav-label">项目</div>
        </div>
        
        <!-- 公共菜单 -->
        <div class="nav-icon" @click="navigateTo('settings')" :class="{ active: currentView === 'settings' }">
          <i class="el-icon-setting"></i>
          <div class="nav-label">设置</div>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部栏 -->
        <el-header height="50px" class="header">
          <div class="header-left">
            <span class="system-title">客服工作台</span>
          </div>
          <div class="header-right">
            <span class="user-info">{{ username }}</span>
            <el-button type="text" size="small" @click="logout">退出</el-button>
          </div>
        </el-header>

        <!-- 主要内容 -->
        <el-main class="main-content">
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const currentView = ref('workbench')
const username = ref('客服员工')
const userRole = ref<string>('agent')

const navigateTo = (view: string) => {
  currentView.value = view
  router.push(`/admin/${view}`)
}

const logout = () => {
  localStorage.removeItem('auth_token')
  localStorage.removeItem('user_info')
  router.push('/login')
}

const loadUserInfo = () => {
  const userInfo = localStorage.getItem('user_info')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      username.value = user.username || user.nickname || '用户'
      userRole.value = user.role || 'agent'
      console.log('加载用户信息:', { username: username.value, role: userRole.value })
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
}

onMounted(() => {
  // 加载用户信息
  loadUserInfo()
  
  // 根据当前路由设置激活状态
  const pathParts = route.path.split('/')
  if (pathParts.length > 2) {
    currentView.value = pathParts[2]
  }
})

// 监听路由变化
watch(() => route.path, (newPath) => {
  const pathParts = newPath.split('/')
  if (pathParts.length > 2) {
    currentView.value = pathParts[2]
  }
})

</script>

<style scoped lang="css">
.admin-layout {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.sidebar {
  background-color: #1e3a5f;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.nav-icon {
  width: 50px;
  height: 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  border-radius: 6px;
  margin-bottom: 10px;
  transition: all 0.3s;
  font-size: 22px;
}

.nav-label {
  font-size: 10px;
  margin-top: 2px;
}

.nav-icon:hover {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.1);
}

.nav-icon.active {
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.1);
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.system-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  color: #606266;
  font-size: 14px;
}

.main-content {
  background-color: #f0f2f5;
  padding: 0;
  overflow: hidden;
}
</style>
