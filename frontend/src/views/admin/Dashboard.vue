<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalConversations }}</div>
            <div class="stat-label">总会话数</div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.activeConversations }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.averageRating }}</div>
            <div class="stat-label">平均评分</div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-number">{{ stats.onlineAgents }}</div>
            <div class="stat-label">在线客服</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日概览</span>
            </div>
          </template>
          <div id="chart-container" style="width: 100%; height: 300px"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近会话</span>
            </div>
          </template>
          <el-table :data="recentConversations" size="small">
            <el-table-column prop="userName" label="用户名" width="120"></el-table-column>
            <el-table-column prop="status" label="状态" width="80"></el-table-column>
            <el-table-column prop="duration" label="时长" width="80"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const stats = ref({
  totalConversations: 1250,
  activeConversations: 45,
  averageRating: 4.8,
  onlineAgents: 12,
})

const recentConversations = ref([
  { userName: '李先生', status: '进行中', duration: '5分钟' },
  { userName: '王女士', status: '已结束', duration: '12分钟' },
  { userName: '张先生', status: '进行中', duration: '3分钟' },
])

onMounted(() => {
  // TODO: 初始化 D3.js 图表
})
</script>

<style scoped lang="css">
.dashboard {
  padding: 16px 0;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px 0;
}

.stat-number {
  font-size: 28px;
  font-weight: 600;
  color: #1890ff;
}

.stat-label {
  margin-top: 8px;
  color: #666;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
