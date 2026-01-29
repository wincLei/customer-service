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
        <h1>在线客服</h1>
      </div>
      <div class="header-right">
        <!-- 预留空间保持居中 -->
      </div>
    </div>

    <!-- 客服信息栏 -->
    <div class="agent-bar">
      <div class="agent-avatar">
        <img :src="agentAvatar" alt="客服" />
      </div>
      <div class="agent-info">
        <span class="agent-name">{{ agentName }}</span>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="mobile-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 客服信息
const agentName = ref('益玩客服')
const agentAvatar = ref('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%231890ff"/><text x="50" y="35" text-anchor="middle" fill="white" font-size="24">益玩</text><text x="50" y="70" text-anchor="middle" fill="white" font-size="20">客服</text></svg>')

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

/* 客服信息栏 */
.agent-bar {
  background-color: #1890ff;
  color: white;
  padding: 8px 16px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.agent-avatar {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}

.agent-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.agent-info {
  flex: 1;
}

.agent-name {
  font-size: 14px;
  font-weight: 500;
}

.mobile-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
