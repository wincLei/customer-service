<template>
  <div class="workbench">
    <!-- 会话列表 -->
    <div class="conversation-list">
      <div class="list-header">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="排队中" name="pending">
            <template #label>
              排队中 <el-badge :value="queueCount" v-if="queueCount > 0" class="queue-badge" />
            </template>
          </el-tab-pane>
          <el-tab-pane label="我的会话" name="my"></el-tab-pane>
        </el-tabs>
      </div>
      
      <div class="list-content">
        <div 
          v-for="conv in conversations" 
          :key="conv.id" 
          class="conversation-item"
          :class="{ active: selectedConversation?.id === conv.id }"
          @click="selectConversation(conv)"
        >
          <div class="conv-avatar">
            <el-avatar :size="40">U</el-avatar>
          </div>
          <div class="conv-info">
            <div class="conv-header">
              <span class="conv-user">用户{{ conv.userId }}</span>
              <span class="conv-time">{{ formatTime(conv.lastMessageTime) }}</span>
            </div>
            <div class="conv-message">{{ conv.lastMessage || '暂无消息' }}</div>
          </div>
          <div class="conv-actions" v-if="activeTab === 'pending'">
            <el-button size="small" type="primary" plain @click.stop="acceptConversation(conv)">
              接入
            </el-button>
          </div>
        </div>
        
        <el-empty v-if="conversations.length === 0" description="暂无会话" :image-size="80" />
      </div>
    </div>

    <!-- 聊天窗口 -->
    <div class="chat-window">
      <template v-if="selectedConversation">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="user-info-bar">
            <el-avatar :size="32">U</el-avatar>
            <span class="user-name">用户{{ selectedConversation.userId }}</span>
          </div>
          <div class="chat-actions">
            <el-button size="small" @click="showUserPanel = !showUserPanel">
              <el-icon><User /></el-icon> 用户信息
            </el-button>
            <el-button size="small" type="danger" @click="closeConversation">
              <el-icon><Close /></el-icon> 结束会话
            </el-button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <div 
            v-for="msg in messages" 
            :key="msg.id"
            class="message-item"
            :class="msg.senderType"
          >
            <div class="message-avatar">
              <el-avatar :size="32">{{ msg.senderType === 'agent' ? 'A' : 'U' }}</el-avatar>
            </div>
            <div class="message-content">
              <div class="message-text">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
            </div>
          </div>
          
          <div v-if="messages.length === 0" class="empty-messages">
            <el-empty description="暂无消息" :image-size="60" />
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-toolbar">
            <el-button size="small" @click="showKbPanel = !showKbPanel">
              <el-icon><Notebook /></el-icon> 知识库
            </el-button>
            <el-button size="small" @click="showQuickReply = !showQuickReply">
              <el-icon><ChatDotSquare /></el-icon> 快捷回复
            </el-button>
          </div>
          <div class="input-box">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入消息，Ctrl+Enter发送..."
              @keydown.ctrl.enter="sendMessage"
            />
            <el-button type="primary" @click="sendMessage" :disabled="!inputMessage.trim()">
              发送
            </el-button>
          </div>
        </div>
      </template>
      
      <el-empty v-else description="请选择会话开始聊天" :image-size="120" />
    </div>

    <!-- 右侧用户信息面板 -->
    <div class="side-panel" v-if="selectedConversation && showUserPanel">
      <div class="panel-header">
        <h3>用户信息</h3>
        <el-icon @click="showUserPanel = false" style="cursor: pointer"><Close /></el-icon>
      </div>
      
      <el-tabs v-model="activeSideTab">
        <el-tab-pane label="基本信息" name="user">
          <div class="user-detail">
            <div class="detail-item">
              <label>用户ID:</label>
              <span>{{ selectedConversation.userId }}</span>
            </div>
            <div class="detail-item">
              <label>昵称:</label>
              <span>用户{{ selectedConversation.userId }}</span>
            </div>
            <div class="detail-item">
              <label>会话状态:</label>
              <el-tag :type="selectedConversation.status === 'active' ? 'success' : 'info'" size="small">
                {{ selectedConversation.status === 'active' ? '进行中' : selectedConversation.status }}
              </el-tag>
            </div>
            <div class="detail-item">
              <label>用户标签:</label>
              <div class="tags">
                <el-tag 
                  v-for="tag in userTags" 
                  :key="tag"
                  closable
                  @close="removeTag(tag)"
                  size="small"
                  style="margin: 4px;"
                >
                  {{ tag }}
                </el-tag>
                <el-button size="small" @click="showAddTagDialog = true">
                  <el-icon><Plus /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="会话记录" name="history">
          <div class="history-list">
            <div class="history-item" v-for="h in conversationHistory" :key="h.id">
              <div class="history-title">会话 #{{ h.id }}</div>
              <div class="history-time">{{ formatTime(h.createdAt) }}</div>
            </div>
            
            <el-empty v-if="conversationHistory.length === 0" description="暂无历史记录" :image-size="60" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 添加标签对话框 -->
    <el-dialog v-model="showAddTagDialog" title="添加标签" width="400px">
      <el-input v-model="newTagName" placeholder="输入标签名称" />
      <template #footer>
        <el-button @click="showAddTagDialog = false">取消</el-button>
        <el-button type="primary" @click="addTag">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Close, Notebook, ChatDotSquare, Plus } from '@element-plus/icons-vue'

// 类型定义
interface Conversation {
  id: number
  userId: number
  userName?: string
  agentId?: number
  status: string
  lastMessage?: string
  lastMessageTime?: string
  createdAt?: string
}

interface Message {
  id: number
  conversationId: number
  senderId: number
  senderType: 'agent' | 'user'
  contentType: string
  content: string
  createdAt: string
}

// 状态
const activeTab = ref('pending')
const selectedConversation = ref<Conversation | null>(null)
const conversations = ref<Conversation[]>([])
const messages = ref<Message[]>([])
const inputMessage = ref('')
const queueCount = ref(0)
const showUserPanel = ref(false)
const showKbPanel = ref(false)
const showQuickReply = ref(false)
const activeSideTab = ref('user')
const userTags = ref<string[]>([])
const conversationHistory = ref<any[]>([])
const messageListRef = ref<HTMLElement | null>(null)
const showAddTagDialog = ref(false)
const newTagName = ref('')

// 项目和客服ID（从localStorage获取）
const projectId = ref(1)
const agentId = ref(1)

// 从localStorage获取当前登录的客服ID
const initAgentId = () => {
  const userInfo = localStorage.getItem('user_info')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      if (user.id) {
        agentId.value = user.id
      } else if (user.username) {
        // 临时方案：根据用户名提取ID
        const match = user.username.match(/\d+$/)
        if (match) {
          agentId.value = parseInt(match[0])
        }
      }
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
}

// 获取会话列表
const fetchConversations = async () => {
  try {
    const endpoint = activeTab.value === 'pending' 
      ? `/api/admin/conversations/pending?projectId=${projectId.value}`
      : `/api/admin/conversations/my?agentId=${agentId.value}`
      
    const response = await fetch(endpoint, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      if (activeTab.value === 'pending') {
        conversations.value = data.data.conversations || []
        queueCount.value = data.data.queueCount || 0
      } else {
        conversations.value = data.data || []
      }
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
  }
}

// 选择会话
const selectConversation = async (conv: Conversation) => {
  selectedConversation.value = conv
  await fetchMessages(conv.id)
  showUserPanel.value = true
}

// 获取消息列表
const fetchMessages = async (conversationId: number) => {
  try {
    const response = await fetch(`/api/admin/messages/conversation/${conversationId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    const data = await response.json()
    
    if (data.code === 200) {
      messages.value = data.data || []
      
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('获取消息失败:', error)
  }
}

// 接入会话
const acceptConversation = async (conv: Conversation) => {
  try {
    const response = await fetch(`/api/admin/conversations/${conv.id}/accept?agentId=${agentId.value}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('已接入会话')
      fetchConversations()
      selectConversation(conv)
      activeTab.value = 'my'
    } else {
      ElMessage.error(data.message || '接入失败')
    }
  } catch (error) {
    console.error('接入会话失败:', error)
    ElMessage.error('接入失败')
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !selectedConversation.value) return
  
  try {
    const response = await fetch('/api/admin/messages/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify({
        projectId: projectId.value,
        conversationId: selectedConversation.value.id,
        senderId: agentId.value,
        senderType: 'agent',
        contentType: 'text',
        content: inputMessage.value
      })
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      inputMessage.value = ''
      await fetchMessages(selectedConversation.value.id)
    } else {
      ElMessage.error(data.message || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败')
  }
}

// 结束会话
const closeConversation = async () => {
  if (!selectedConversation.value) return
  
  try {
    const response = await fetch(`/api/admin/conversations/${selectedConversation.value.id}/close`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200) {
      ElMessage.success('会话已结束')
      selectedConversation.value = null
      messages.value = []
      fetchConversations()
    } else {
      ElMessage.error(data.message || '操作失败')
    }
  } catch (error) {
    console.error('结束会话失败:', error)
    ElMessage.error('操作失败')
  }
}

// 添加标签
const addTag = () => {
  if (newTagName.value.trim()) {
    userTags.value.push(newTagName.value.trim())
    newTagName.value = ''
    showAddTagDialog.value = false
    ElMessage.success('标签已添加')
  }
}

// 删除标签
const removeTag = (tag: string) => {
  userTags.value = userTags.value.filter(t => t !== tag)
  ElMessage.success('标签已删除')
}

// 切换标签页
const handleTabClick = () => {
  fetchConversations()
}

// 滚动到底部
const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  return `${month}-${day} ${hours}:${minutes}`
}

// 初始化
onMounted(() => {
  initAgentId()
  fetchConversations()
  
  // 定时刷新会话列表
  setInterval(fetchConversations, 5000)
})

// 监听选中会话变化
watch(selectedConversation, (newVal) => {
  if (newVal) {
    // 定时刷新消息
    const timer = setInterval(() => {
      if (selectedConversation.value) {
        fetchMessages(selectedConversation.value.id)
      } else {
        clearInterval(timer)
      }
    }, 3000)
  }
})
</script>

<style scoped lang="css">
.workbench {
  display: flex;
  height: calc(100vh - 50px);
  background-color: #f5f7fa;
}

.conversation-list {
  width: 320px;
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 10px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.list-header :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.queue-badge {
  margin-left: 5px;
}

.list-content {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: all 0.2s;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-item.active {
  background-color: #ecf5ff;
  border-left: 3px solid #409eff;
}

.conv-avatar {
  margin-right: 12px;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.conv-user {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.conv-time {
  font-size: 12px;
  color: #909399;
}

.conv-message {
  font-size: 13px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-actions {
  margin-left: 10px;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.chat-header {
  height: 56px;
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fafafa;
}

.user-info-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-weight: 500;
  color: #303133;
  font-size: 15px;
}

.chat-actions {
  display: flex;
  gap: 10px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f7fa;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
}

.message-item.agent {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 12px;
}

.message-content {
  max-width: 60%;
}

.message-item.agent .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-text {
  padding: 12px 16px;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  word-break: break-word;
  line-height: 1.6;
  font-size: 14px;
}

.message-item.agent .message-text {
  background-color: #409eff;
  color: #fff;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

.empty-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.input-area {
  border-top: 1px solid #e4e7ed;
  background-color: #fff;
}

.input-toolbar {
  padding: 10px 16px;
  border-bottom: 1px solid #f0f2f5;
  display: flex;
  gap: 10px;
}

.input-box {
  padding: 16px;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-box :deep(.el-textarea) {
  flex: 1;
}

.input-box :deep(.el-textarea__inner) {
  resize: none;
}

.side-panel {
  width: 340px;
  background-color: #fff;
  border-left: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.user-detail {
  padding: 20px;
}

.detail-item {
  margin-bottom: 20px;
}

.detail-item label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}

.detail-item span {
  color: #303133;
  font-size: 14px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.history-list {
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  padding: 12px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.history-item:hover {
  background-color: #f5f7fa;
}

.history-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.history-time {
  font-size: 12px;
  color: #909399;
}
</style>
