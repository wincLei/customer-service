<template>
  <div class="portal-layout">
    <div class="container">
      <!-- 顶部栏 -->
      <div class="portal-header">
        <h1>在线客服</h1>
        <p>欢迎咨询我们的服务</p>
      </div>

      <!-- 主内容 -->
      <div class="portal-content">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="css">
.portal-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.container {
  width: 100%;
  max-width: 800px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.portal-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px 20px;
  text-align: center;
}

.portal-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}

.portal-header p {
  margin: 8px 0 0 0;
  font-size: 14px;
  opacity: 0.9;
}

.portal-content {
  padding: 30px 20px;
}
</style>
