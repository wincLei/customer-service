<template>
  <div class="mobile-layout">
    <!-- 顶部导航 -->
    <div class="mobile-header">
      <h1>客服支持</h1>
      <div class="header-action">
        <span class="status">客服在线</span>
        <button @click="endConversation" class="end-btn">结束会话</button>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="mobile-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const endConversation = () => {
  // TODO: 实现结束会话逻辑
  router.push('/portal')
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.mobile-header h1 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.header-action {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status {
  font-size: 12px;
  opacity: 0.9;
}

.end-btn {
  background-color: rgba(255, 255, 255, 0.3);
  border: none;
  color: white;
  padding: 4px 12px;
  border-radius: 3px;
  font-size: 12px;
  cursor: pointer;
}

.end-btn:active {
  background-color: rgba(255, 255, 255, 0.4);
}

.mobile-content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
</style>
