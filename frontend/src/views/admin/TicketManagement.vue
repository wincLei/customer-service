<template>
  <div class="ticket-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>工单管理</h2>
      <p class="subtitle">管理用户提交的工单和问题反馈</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><Tickets /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total || 0 }}</div>
          <div class="stat-label">全部工单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon open">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.open || 0 }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon processing">
          <el-icon><Loading /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.processing || 0 }}</div>
          <div class="stat-label">处理中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon resolved">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.resolved || 0 }}</div>
          <div class="stat-label">已解决</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索工单标题或内容..."
        clearable
        @keyup.enter="handleSearch"
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filterStatus" placeholder="状态" clearable @change="handleSearch">
        <el-option label="待处理" value="open" />
        <el-option label="处理中" value="processing" />
        <el-option label="已解决" value="resolved" />
        <el-option label="已关闭" value="closed" />
      </el-select>
      <el-select v-model="filterPriority" placeholder="优先级" clearable @change="handleSearch">
        <el-option label="紧急" value="urgent" />
        <el-option label="高" value="high" />
        <el-option label="中" value="medium" />
        <el-option label="低" value="low" />
      </el-select>
      <el-button type="primary" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
    </div>

    <!-- 工单列表 -->
    <div class="ticket-list">
      <el-table :data="tickets" v-loading="loading" stripe>
        <el-table-column prop="id" label="工单号" width="100" />
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="showTicketDetail(row)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" @click="showTicketDetail(row)">
                查看
              </el-button>
              <el-button
                size="small"
                type="primary"
                @click="openReplyDialog(row)"
                :disabled="row.status === 'closed'"
              >
                回复
              </el-button>
              <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, row)">
                <el-button size="small">
                  更多 <el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="processing" :disabled="row.status !== 'open'">
                      标记处理中
                    </el-dropdown-item>
                    <el-dropdown-item command="resolved" :disabled="row.status === 'closed'">
                      标记已解决
                    </el-dropdown-item>
                    <el-dropdown-item command="closed" :disabled="row.status === 'closed'">
                      关闭工单
                    </el-dropdown-item>
                    <el-dropdown-item command="assign" divided>
                      分配客服
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalElements"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 工单详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="工单详情"
      size="600px"
      direction="rtl"
    >
      <div class="ticket-detail" v-if="currentTicket">
        <!-- 工单头部 -->
        <div class="detail-header">
          <h3>{{ currentTicket.ticket.title }}</h3>
          <div class="detail-meta">
            <el-tag :type="getStatusType(currentTicket.ticket.status)" size="small">
              {{ getStatusLabel(currentTicket.ticket.status) }}
            </el-tag>
            <el-tag :type="getPriorityType(currentTicket.ticket.priority)" size="small">
              {{ getPriorityLabel(currentTicket.ticket.priority) }}
            </el-tag>
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ currentTicket.userName }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatTime(currentTicket.ticket.createdAt) }}
            </span>
          </div>
        </div>

        <!-- 工单描述 -->
        <div class="detail-content">
          <h4>问题描述</h4>
          <p>{{ currentTicket.ticket.description }}</p>
        </div>

        <!-- 事件时间线 -->
        <div class="detail-timeline">
          <h4>处理记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="event in currentTicket.events"
              :key="event.id"
              :timestamp="formatTime(event.createdAt)"
              :type="getEventType(event.action)"
            >
              <div class="event-content">
                <span class="event-label">{{ getEventLabel(event) }}</span>
                <p v-if="event.content">{{ event.content }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 快捷回复 -->
        <div class="detail-reply" v-if="currentTicket.ticket.status !== 'closed'">
          <h4>快捷回复</h4>
          <el-input
            v-model="quickReplyContent"
            type="textarea"
            :rows="3"
            placeholder="输入回复内容..."
          />
          <div class="reply-actions">
            <el-button type="primary" @click="submitQuickReply" :loading="replyLoading">
              发送回复
            </el-button>
            <el-button @click="updateTicketStatus('resolved')">
              标记已解决
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复工单" width="500px">
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="工单标题">
          <span>{{ replyForm.title }}</span>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replyLoading">
          发送
        </el-button>
      </template>
    </el-dialog>

    <!-- 分配客服对话框 -->
    <el-dialog v-model="assignDialogVisible" title="分配客服" width="400px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="选择客服">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择客服" style="width: 100%">
            <el-option
              v-for="agent in agents"
              :key="agent.id"
              :label="agent.nickname"
              :value="agent.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :loading="assignLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Tickets,
  Bell,
  Loading,
  CircleCheck,
  ArrowDown,
  User,
  Clock
} from '@element-plus/icons-vue'
import request from '@/api'

// 状态数据
const stats = ref({
  total: 0,
  open: 0,
  processing: 0,
  resolved: 0,
  closed: 0
})

// 列表数据
const tickets = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalElements = ref(0)

// 搜索筛选
const searchKeyword = ref('')
const filterStatus = ref('')
const filterPriority = ref('')

// 详情抽屉
const detailDrawerVisible = ref(false)
const currentTicket = ref<any>(null)
const quickReplyContent = ref('')

// 回复对话框
const replyDialogVisible = ref(false)
const replyLoading = ref(false)
const replyForm = ref({
  id: 0,
  title: '',
  content: ''
})

// 分配对话框
const assignDialogVisible = ref(false)
const assignLoading = ref(false)
const assignForm = ref({
  ticketId: 0,
  assigneeId: null as number | null
})
const agents = ref<any[]>([])

// 获取工单列表
const fetchTickets = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/admin/ticket/list', {
      params: {
        keyword: searchKeyword.value || undefined,
        status: filterStatus.value || undefined,
        priority: filterPriority.value || undefined,
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    tickets.value = res.data.content || []
    totalElements.value = res.data.totalElements || 0
  } catch (error) {
    console.error('获取工单列表失败', error)
    ElMessage.error('获取工单列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const res = await request.get('/api/admin/ticket/stats')
    stats.value = res.data
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 获取客服列表
const fetchAgents = async () => {
  try {
    const res = await request.get('/api/admin/user/agents')
    agents.value = res.data || []
  } catch (error) {
    console.error('获取客服列表失败', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchTickets()
}

// 分页
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchTickets()
}

const handlePageChange = (val: number) => {
  currentPage.value = val
  fetchTickets()
}

// 显示工单详情
const showTicketDetail = async (row: any) => {
  try {
    const res = await request.get(`/api/admin/ticket/${row.id}`)
    currentTicket.value = res.data
    detailDrawerVisible.value = true
  } catch (error) {
    ElMessage.error('获取工单详情失败')
  }
}

// 打开回复对话框
const openReplyDialog = (row: any) => {
  replyForm.value = {
    id: row.id,
    title: row.title,
    content: ''
  }
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyForm.value.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  replyLoading.value = true
  try {
    await request.post(`/api/admin/ticket/${replyForm.value.id}/reply`, {
      content: replyForm.value.content
    })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    fetchTickets()
    fetchStats()
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    replyLoading.value = false
  }
}

// 快捷回复
const submitQuickReply = async () => {
  if (!quickReplyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  replyLoading.value = true
  try {
    await request.post(`/api/admin/ticket/${currentTicket.value.ticket.id}/reply`, {
      content: quickReplyContent.value
    })
    ElMessage.success('回复成功')
    quickReplyContent.value = ''
    showTicketDetail({ id: currentTicket.value.ticket.id })
    fetchTickets()
    fetchStats()
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    replyLoading.value = false
  }
}

// 更新工单状态
const updateTicketStatus = async (status: string) => {
  const ticketId = currentTicket.value?.ticket?.id || assignForm.value.ticketId
  try {
    await request.put(`/api/admin/ticket/${ticketId}/status`, { status })
    ElMessage.success('状态更新成功')
    if (detailDrawerVisible.value) {
      showTicketDetail({ id: ticketId })
    }
    fetchTickets()
    fetchStats()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 处理下拉菜单命令
const handleCommand = (command: string, row: any) => {
  if (command === 'assign') {
    assignForm.value = {
      ticketId: row.id,
      assigneeId: null
    }
    fetchAgents()
    assignDialogVisible.value = true
  } else {
    updateTicketStatusById(row.id, command)
  }
}

// 按ID更新状态
const updateTicketStatusById = async (id: number, status: string) => {
  try {
    await request.put(`/api/admin/ticket/${id}/status`, { status })
    ElMessage.success('状态更新成功')
    fetchTickets()
    fetchStats()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 提交分配
const submitAssign = async () => {
  if (!assignForm.value.assigneeId) {
    ElMessage.warning('请选择客服')
    return
  }
  
  assignLoading.value = true
  try {
    await request.put(`/api/admin/ticket/${assignForm.value.ticketId}/assign`, {
      assigneeId: assignForm.value.assigneeId
    })
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    fetchTickets()
  } catch (error) {
    ElMessage.error('分配失败')
  } finally {
    assignLoading.value = false
  }
}

// 工具函数
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    open: 'danger',
    processing: 'warning',
    resolved: 'success',
    closed: 'info'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    open: '待处理',
    processing: '处理中',
    resolved: '已解决',
    closed: '已关闭'
  }
  return labels[status] || status
}

const getPriorityType = (priority: string) => {
  const types: Record<string, string> = {
    urgent: 'danger',
    high: 'warning',
    medium: '',
    low: 'info'
  }
  return types[priority] || ''
}

const getPriorityLabel = (priority: string) => {
  const labels: Record<string, string> = {
    urgent: '紧急',
    high: '高',
    medium: '中',
    low: '低'
  }
  return labels[priority] || priority
}

const getEventType = (action: string) => {
  const types: Record<string, string> = {
    create: 'primary',
    reply: 'success',
    status_change: 'warning',
    assign: 'info'
  }
  return types[action] || ''
}

const getEventLabel = (event: any) => {
  const labels: Record<string, string> = {
    create: '创建工单',
    reply: event.operatorType === 'user' ? '用户回复' : '客服回复',
    status_change: '状态变更',
    assign: '工单分配'
  }
  return labels[event.action] || event.action
}

onMounted(() => {
  fetchTickets()
  fetchStats()
})
</script>

<style scoped>
.ticket-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.page-header .subtitle {
  margin: 8px 0 0;
  color: #666;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 24px;
}

.stat-icon.total {
  background: #e6f7ff;
  color: #1890ff;
}

.stat-icon.open {
  background: #fff2e8;
  color: #fa541c;
}

.stat-icon.processing {
  background: #fffbe6;
  color: #faad14;
}

.stat-icon.resolved {
  background: #f6ffed;
  color: #52c41a;
}

.stat-info .stat-value {
  font-size: 28px;
  font-weight: 600;
  line-height: 1;
}

.stat-info .stat-label {
  margin-top: 4px;
  color: #666;
  font-size: 14px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  width: 300px;
}

.ticket-list {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.ticket-detail {
  padding: 0 16px;
}

.detail-header h3 {
  margin: 0 0 12px;
  font-size: 18px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 14px;
}

.detail-content {
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.detail-content h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #999;
}

.detail-content p {
  margin: 0;
  line-height: 1.6;
}

.detail-timeline {
  margin-bottom: 24px;
}

.detail-timeline h4 {
  margin: 0 0 16px;
  font-size: 14px;
  color: #999;
}

.event-content .event-label {
  font-weight: 500;
}

.event-content p {
  margin: 4px 0 0;
  color: #666;
}

.detail-reply h4 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #999;
}

.reply-actions {
  margin-top: 12px;
  display: flex;
  gap: 12px;
}

@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .search-bar {
    flex-wrap: wrap;
  }
  
  .search-input {
    width: 100%;
  }
}
</style>
