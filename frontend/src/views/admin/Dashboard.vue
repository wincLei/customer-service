<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>数据概览</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="default"
          @change="refreshData"
        />
        <el-button type="primary" :icon="Refresh" @click="refreshData">刷新</el-button>
      </div>
    </div>

    <!-- 实时统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon queue">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">排队中</div>
            <div class="stat-value">{{ stats.queueCount }}</div>
            <div class="stat-trend up">+{{ stats.queueTrend }}%</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon active">
            <el-icon :size="32"><ChatDotRound /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">进行中会话</div>
            <div class="stat-value">{{ stats.activeConversations }}</div>
            <div class="stat-trend">实时</div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon total">
            <el-icon :size="32"><MessageBox /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日会话数</div>
            <div class="stat-value">{{ stats.todayConversations }}</div>
            <div class="stat-trend" :class="stats.conversationTrend >= 0 ? 'up' : 'down'">
              {{ stats.conversationTrend >= 0 ? '+' : '' }}{{ stats.conversationTrend }}%
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-icon messages">
            <el-icon :size="32"><ChatLineRound /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日消息数</div>
            <div class="stat-value">{{ stats.todayMessages }}</div>
            <div class="stat-trend up">+{{ stats.messageTrend }}%</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 满意度统计 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>满意度统计</span>
            </div>
          </template>
          <div class="satisfaction-stats">
            <div class="satisfaction-summary">
              <div class="avg-score">
                <div class="score-label">平均评分</div>
                <div class="score-value">{{ stats.avgSatisfaction }}</div>
                <el-rate v-model="stats.avgSatisfaction" disabled show-score text-color="#ff9900" />
              </div>
            </div>
            <div class="satisfaction-breakdown">
              <div v-for="item in satisfactionData" :key="item.score" class="breakdown-item">
                <div class="breakdown-label">
                  {{ item.label }}
                  <span class="breakdown-count">{{ item.count }}次</span>
                </div>
                <el-progress :percentage="item.percentage" :color="item.color" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>客服状态</span>
              <el-tag size="small">在线 {{ onlineAgents.length }}/{{ allAgents.length }}</el-tag>
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
                    {{ agent.status === 'online' ? '在线' : '离线' }}
                  </el-tag>
                  <span class="conversation-count">进行中: {{ agent.activeCount }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Clock, ChatDotRound, MessageBox, ChatLineRound, Refresh } from '@element-plus/icons-vue'

interface Stats {
  queueCount: number
  queueTrend: number
  activeConversations: number
  todayConversations: number
  conversationTrend: number
  todayMessages: number
  messageTrend: number
  avgSatisfaction: number
}

interface Agent {
  id: number
  username: string
  nickname: string
  status: string
  activeCount: number
}

const projectId = ref(1)
const dateRange = ref<[Date, Date]>()

const stats = ref<Stats>({
  queueCount: 0,
  queueTrend: 0,
  activeConversations: 0,
  todayConversations: 0,
  conversationTrend: 0,
  todayMessages: 0,
  messageTrend: 0,
  avgSatisfaction: 4.5
})

const satisfactionData = ref([
  { score: 5, label: '非常满意', percentage: 60, count: 120, color: '#67c23a' },
  { score: 4, label: '满意', percentage: 25, count: 50, color: '#409eff' },
  { score: 3, label: '一般', percentage: 10, count: 20, color: '#e6a23c' },
  { score: 2, label: '不满意', percentage: 3, count: 6, color: '#f56c6c' },
  { score: 1, label: '非常不满意', percentage: 2, count: 4, color: '#909399' }
])

const allAgents = ref<Agent[]>([])
const onlineAgents = ref<Agent[]>([])

let refreshTimer: any = null

// 获取统计数据
const fetchStats = async () => {
  try {
    // 从统计API获取真实数据
    const statsRes = await fetch(`/api/admin/conversations/statistics?projectId=${projectId.value}`)
    const statsData = await statsRes.json()
    if (statsData.code === 0) {
      stats.value.queueCount = statsData.data.queueCount || 0
      stats.value.activeConversations = statsData.data.activeConversations || 0
      stats.value.todayConversations = statsData.data.todayConversations || 0
      stats.value.todayMessages = statsData.data.todayMessages || 0
      
      // 模拟趋势数据（可以后续添加趋势API）
      stats.value.queueTrend = Math.floor(Math.random() * 20)
      stats.value.conversationTrend = Math.floor(Math.random() * 30) - 10
      stats.value.messageTrend = Math.floor(Math.random() * 40)
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取客服列表
const fetchAgents = async () => {
  try {
    // 从真实API获取客服列表
    const agentsRes = await fetch(`/api/admin/users/agents?projectId=${projectId.value}`)
    const agentsData = await agentsRes.json()
    if (agentsData.agents) {
      allAgents.value = agentsData.agents
      onlineAgents.value = allAgents.value.filter(a => a.status === 'online')
    }
  } catch (error) {
    console.error('获取客服列表失败:', error)
  }
}

// 刷新数据
const refreshData = () => {
  fetchStats()
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
