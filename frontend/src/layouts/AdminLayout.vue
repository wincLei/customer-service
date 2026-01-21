<template>
  <div class="admin-layout">
    <el-container>
      <!-- 左侧导航栏 -->
      <el-aside width="60px" class="sidebar">
        <div class="nav-icon" @click="currentView = 'dashboard'" :class="{ active: currentView === 'dashboard' }">
          <i class="el-icon-data-analysis"></i>
        </div>
        <div class="nav-icon" @click="currentView = 'chat'" :class="{ active: currentView === 'chat' }">
          <i class="el-icon-chat"></i>
        </div>
        <div class="nav-icon" @click="currentView = 'settings'" :class="{ active: currentView === 'settings' }">
          <i class="el-icon-setting"></i>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部栏 -->
        <el-header class="header">
          <div class="header-left">
            <h1>客服系统管理后台</h1>
          </div>
          <div class="header-right">
            <span class="user-info">{{ username }}</span>
            <el-button type="danger" size="small" @click="logout">退出登录</el-button>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentView = ref('dashboard')
const username = ref('客服员工')

const logout = () => {
  localStorage.removeItem('auth_token')
  router.push('/login')
}
</script>

<style scoped lang="css">
.admin-layout {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.sidebar {
  background-color: #001a33;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  box-shadow: 1px 0 4px rgba(0, 0, 0, 0.15);
}

.nav-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 16px;
  transition: all 0.3s;
  font-size: 20px;
}

.nav-icon:hover {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.15);
}

.nav-icon.active {
  color: #1890ff;
  background-color: rgba(24, 144, 255, 0.15);
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left h1 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  color: #666;
  font-size: 14px;
}

.main-content {
  padding: 16px;
  background-color: #f0f2f5;
}
</style>
