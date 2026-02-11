<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>{{ $t('dashboard.title') }}</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          :range-separator="$t('dashboard.rangeTo')"
          :start-placeholder="$t('dashboard.startDate')"
          :end-placeholder="$t('dashboard.endDate')"
          size="default"
          @change="refreshData"
        />
        <el-button type="primary" :icon="Refresh" @click="refreshData">{{ $t('common.refresh') }}</el-button>
      </div>
    </div>

    <!-- 只保留客服状态卡片 -->
    <div class="agent-status-panel">
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>{{ $t('dashboard.agentStatus') }}</span>
            <el-tag size="small">{{ $t('dashboard.online') }} {{ onlineAgents.length }}/{{ allAgents.length }}</el-tag>
          </div>
        </template>
        <div class="agent-list">
          <div v-for="agent in allAgents" :key="agent.id" class="agent-item">
            <el-badge :value="agent.activeCount" :max="99" :hidden="agent.activeCount === 0">
              <el-avatar :size="40">{{ agent.nickname?.charAt(0) || 'A' }}</el-avatar>
            </el-badge>
            <div class="agent-info">
              <div class="agent-name">{{ agent.nickname }}</div>
              <div class="agent-meta">
                <el-tag :type="agent.status === 'online' ? 'success' : 'info'" size="small">
                  {{ agent.status === 'online' ? $t('dashboard.online') : $t('dashboard.offline') }}
                </el-tag>
                <span class="conversation-count">{{ $t('dashboard.ongoing') }} {{ agent.activeCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">

import { ref, onMounted, onUnmounted } from 'vue'
import { StorageKeys } from '@/constants'
import { logger } from '@/utils/logger'
import { Refresh } from '@element-plus/icons-vue'


interface Agent {
  id: number
  username: string
  nickname: string
  status: string
  activeCount: number
}

const projectId = ref(1)
const dateRange = ref<[Date, Date]>()


const allAgents = ref<Agent[]>([])
const onlineAgents = ref<Agent[]>([])

let refreshTimer: any = null


// 获取客服列表
const fetchAgents = async () => {
  try {
    const token = localStorage.getItem(StorageKeys.AUTH_TOKEN)
    const agentsRes = await fetch(`/api/admin/users/agents?projectId=${projectId.value}`, {
      headers: {
        'Authorization': token ? `Bearer ${token}` : ''
      }
    })
    const agentsData = await agentsRes.json()
    if (agentsData.agents) {
      allAgents.value = agentsData.agents
      onlineAgents.value = allAgents.value.filter((a: Agent) => a.status === 'online')
    }
  } catch (error) {
    logger.error('获取客服列表失败:', error)
  }
}

// 刷新数据
const refreshData = () => {
  fetchAgents()
}

onMounted(() => {
  refreshData()
  // 每30秒自动刷新
  refreshTimer = setInterval(refreshData, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon.queue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-icon.active {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-icon.messages {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  color: #909399;
}

.stat-trend.up {
  color: #67c23a;
}

.stat-trend.down {
  color: #f56c6c;
}

/* 图表区域 */
.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 满意度统计 */
.satisfaction-stats {
  padding: 20px 0;
}

.satisfaction-summary {
  text-align: center;
  margin-bottom: 30px;
}

.avg-score {
  display: inline-block;
}

.score-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.score-value {
  font-size: 48px;
  font-weight: 600;
  color: #ff9900;
  margin-bottom: 8px;
}

.satisfaction-breakdown {
  max-width: 500px;
  margin: 0 auto;
}

.breakdown-item {
  margin-bottom: 16px;
}

.breakdown-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
}

.breakdown-count {
  font-size: 12px;
  color: #909399;
}

/* 客服状态 */
.agent-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.agent-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.agent-item:hover {
  background: #e8eaf0;
}

.agent-info {
  flex: 1;
}

.agent-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.agent-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.conversation-count {
  color: #909399;
}

@media (max-width: 1400px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .charts-row .el-col {
    margin-bottom: 20px;
  }
}
</style>
