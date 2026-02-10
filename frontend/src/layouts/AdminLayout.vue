<template>
  <div class="admin-layout">
    <el-container>
      <!-- 左侧导航栏 -->
      <el-aside width="60px" class="sidebar">
        <!-- 数据概览 - 需要dashboard菜单权限 -->
        <div v-if="hasMenu('dashboard')" class="nav-icon" @click="navigateTo('dashboard')" :class="{ active: currentView === 'dashboard' }">
          <i class="el-icon-data-analysis"></i>
          <div class="nav-label">{{ $t('nav.dashboard') }}</div>
        </div>
        
        <!-- 工作台 - 需要workbench菜单权限 -->
        <div v-if="hasMenu('workbench')" class="nav-icon" @click="navigateTo('workbench')" :class="{ active: currentView === 'workbench' }">
          <i class="el-icon-chat-line-square"></i>
          <div class="nav-label">{{ $t('nav.workbench') }}</div>
        </div>

        <!-- 项目管理 - 需要projects菜单权限 -->
        <div v-if="hasMenu('projects')" class="nav-icon" @click="navigateTo('projects')" :class="{ active: currentView === 'projects' }">
          <i class="el-icon-folder"></i>
          <div class="nav-label">{{ $t('nav.project') }}</div>
        </div>

        <!-- 知识库 - 需要knowledge菜单权限 -->
        <div v-if="hasMenu('knowledge')" class="nav-icon" @click="navigateTo('knowledge')" :class="{ active: currentView === 'knowledge' }">
          <i class="el-icon-document"></i>
          <div class="nav-label">{{ $t('nav.knowledgeBase') }}</div>
        </div>

        <!-- 客户端 - 包含客户管理、标签管理、快捷回复子菜单 -->
        <el-popover
          v-if="hasMenu('customers') || hasMenu('customer-tags') || hasMenu('quick-replies')"
          placement="right-start"
          :width="140"
          trigger="hover"
          :show-arrow="false"
          popper-class="system-submenu-popover"
        >
          <template #reference>
            <div class="nav-icon" :class="{ active: isClientMenuActive }">
              <i class="el-icon-user-filled"></i>
              <div class="nav-label">{{ $t('nav.client') }}</div>
            </div>
          </template>
          <div class="submenu-list">
            <div 
              v-if="hasMenu('customers')" 
              class="submenu-item" 
              :class="{ active: currentView === 'customers' }"
              @click="navigateTo('customers')"
            >
              <i class="el-icon-user"></i>
              <span>{{ $t('nav.customerManagement') }}</span>
            </div>
            <div 
              v-if="hasMenu('customer-tags')" 
              class="submenu-item" 
              :class="{ active: currentView === 'customer-tags' }"
              @click="navigateTo('customer-tags')"
            >
              <i class="el-icon-price-tag"></i>
              <span>{{ $t('nav.tagManagement') }}</span>
            </div>
            <div 
              v-if="hasMenu('quick-replies')" 
              class="submenu-item" 
              :class="{ active: currentView === 'quick-replies' }"
              @click="navigateTo('quick-replies')"
            >
              <i class="el-icon-chat-line-round"></i>
              <span>{{ $t('nav.quickReply') }}</span>
            </div>
          </div>
        </el-popover>

        <!-- 工单管理 - 需要tickets菜单权限 -->
        <div v-if="hasMenu('tickets')" class="nav-icon" @click="navigateTo('tickets')" :class="{ active: currentView === 'tickets' }">
          <i class="el-icon-tickets"></i>
          <div class="nav-label">{{ $t('nav.ticket') }}</div>
        </div>

        <!-- 系统管理 - 包含用户、角色、菜单、客服子菜单 -->
        <el-popover
          v-if="hasMenu('system') || hasMenu('users') || hasMenu('roles') || hasMenu('menus') || hasMenu('agents')"
          placement="right-start"
          :width="140"
          trigger="hover"
          :show-arrow="false"
          popper-class="system-submenu-popover"
        >
          <template #reference>
            <div class="nav-icon" :class="{ active: isSystemMenuActive }">
              <i class="el-icon-setting"></i>
              <div class="nav-label">{{ $t('nav.system') }}</div>
            </div>
          </template>
          <div class="submenu-list">
            <div 
              v-if="hasMenu('users')" 
              class="submenu-item" 
              :class="{ active: currentView === 'users' }"
              @click="navigateTo('users')"
            >
              <i class="el-icon-user"></i>
              <span>{{ $t('nav.userManagement') }}</span>
            </div>
            <div 
              v-if="hasMenu('agents')" 
              class="submenu-item" 
              :class="{ active: currentView === 'agents' }"
              @click="navigateTo('agents')"
            >
              <i class="el-icon-headset"></i>
              <span>{{ $t('nav.agentManagement') }}</span>
            </div>
            <div 
              v-if="hasMenu('roles')" 
              class="submenu-item"
              :class="{ active: currentView === 'roles' }"
              @click="navigateTo('roles')"
            >
              <i class="el-icon-key"></i>
              <span>{{ $t('nav.roleManagement') }}</span>
            </div>
            <div 
              v-if="hasMenu('menus')" 
              class="submenu-item"
              :class="{ active: currentView === 'menus' }"
              @click="navigateTo('menus')"
            >
              <i class="el-icon-menu"></i>
              <span>{{ $t('nav.menuManagement') }}</span>
            </div>
          </div>
        </el-popover>
        
        <!-- 设置 - 需要settings菜单权限 -->
        <div v-if="hasMenu('settings')" class="nav-icon" @click="navigateTo('settings')" :class="{ active: currentView === 'settings' }">
          <i class="el-icon-tools"></i>
          <div class="nav-label">{{ $t('nav.settings') }}</div>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部栏 -->
        <el-header height="50px" class="header">
          <div class="header-left">
            <span v-if="isAgent" class="system-title">{{ $t('nav.agentWorkbench') }}</span>
          </div>
          <div class="header-right">
            <span class="user-info">{{ username }}</span>
            <el-button type="text" size="small" @click="logout">{{ $t('nav.logout') }}</el-button>
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
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getPermissionStore } from '@/stores/permission'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const permissionStore = getPermissionStore()

const currentView = ref('workbench')
const username = computed(() => permissionStore.getUser.value?.username || t('nav.defaultUser'))

// 判断是否是客服角色
const isAgent = computed(() => {
  return permissionStore.hasRole('agent')
})

// 检查菜单权限
const hasMenu = (menu: string): boolean => {
  return permissionStore.hasMenu(menu)
}

// 系统菜单是否激活（用户、客服、角色、菜单任意一个激活时）
const isSystemMenuActive = computed(() => {
  return ['users', 'agents', 'roles', 'menus'].includes(currentView.value)
})

// 客户端菜单是否激活（客户管理、标签管理、快捷回复任意一个激活时）
const isClientMenuActive = computed(() => {
  return ['customers', 'customer-tags', 'quick-replies'].includes(currentView.value)
})

const navigateTo = (view: string) => {
  currentView.value = view
  router.push(`/admin/${view}`)
}

const logout = () => {
  permissionStore.clearUser()
  router.push('/login')
}

onMounted(() => {
  // 确保权限store已初始化
  permissionStore.init()
  
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

/* 系统子菜单样式 */
.submenu-list {
  padding: 5px 0;
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  transition: all 0.3s;
  border-radius: 4px;
  margin: 2px 5px;
}

.submenu-item i {
  margin-right: 8px;
  font-size: 16px;
}

.submenu-item:hover {
  background-color: #ecf5ff;
  color: #409eff;
}

.submenu-item.active {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}
</style>
