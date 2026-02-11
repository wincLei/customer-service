<template>
  <div class="mobile-layout">
    <!-- 顶部导航 -->
    <div class="mobile-header">
      <div class="header-left">
        <button class="back-btn" @click="goBack">
          <span>‹</span>
        </button>
        <button class="close-btn" @click="closeChat">
          <span>✕</span>
        </button>
      </div>
      <div class="header-center">
        <h1>{{ projectName }}</h1>
      </div>
      <div class="header-right">
        <!-- 预留空间保持居中 -->
      </div>
    </div>

    <!-- 内容区 -->
    <div class="mobile-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/api'
import { logger } from '@/utils/logger'

const router = useRouter()
const route = useRoute()

// 项目信息
const projectName = ref('在线客服')

// 加载项目配置
const loadProjectConfig = async () => {
  const projectId = route.query.project_id || route.query.projectId || '1'
  try {
    const response = await request.get('/pub/config', {
      params: { projectId }
    }) as any
    
    if (response.code === 0 && response.data && response.data.projectName) {
      projectName.value = response.data.projectName + '-在线客服'
    }
  } catch (error) {
    logger.error('Failed to load project config:', error)
  }
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/portal')
  }
}

const closeChat = () => {
  // 关闭聊天窗口，返回来源页面或首页
  const projectId = route.query.project_id || route.query.projectId || '1'
  router.push(`/portal?project_id=${projectId}`)
}

onMounted(() => {
  loadProjectConfig()
})
</script>

<style scoped lang="css">
.mobile-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.mobile-header {
  background-color: #1890ff;
  color: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 44px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 60px;
}

.back-btn,
.close-btn {
  background: transparent;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  padding: 4px 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-btn:active,
.close-btn:active {
  opacity: 0.7;
}

.header-center {
  flex: 1;
  text-align: center;
}

.header-center h1 {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
}

.header-right {
  min-width: 60px;
}

.mobile-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
