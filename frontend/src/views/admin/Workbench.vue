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
        <!-- IM 连接状态指示器 -->
        <div class="im-status" :class="{ connected: imConnected, connecting: imConnecting }">
          <span class="status-dot"></span>
          <span class="status-text">{{ imConnected ? 'IM已连接' : imConnecting ? '连接中...' : 'IM未连接' }}</span>
        </div>
      </div>
      
      <div class="list-content">
        <div 
          v-for="conv in conversations" 
          :key="conv.id" 
          class="conversation-item"
          :class="{ active: selectedConversation?.userId === conv.userId }"
          @click="selectConversation(conv)"
        >
          <div class="conv-avatar">
            <el-avatar :size="40" :src="conv.avatar">{{ conv.userName?.charAt(0) || 'U' }}</el-avatar>
          </div>
          <div class="conv-info">
            <div class="conv-header">
              <span class="conv-user">
                {{ conv.userName || conv.userId }}
                <el-tag size="small" type="info" class="device-tag">{{ conv.deviceType || 'PC端' }}</el-tag>
              </span>
              <span class="conv-time">{{ formatTime(conv.lastMessageTime) }}</span>
            </div>
            <div class="conv-message">{{ conv.lastMessage || '暂无消息' }}</div>
          </div>
          <div class="conv-actions" v-if="activeTab === 'pending' && conv.unreadCount && conv.unreadCount > 0">
            <el-button size="small" type="primary" plain @click.stop="acceptConversation(conv)">
              接入
            </el-button>
          </div>
          <div class="conv-unread" v-else-if="conv.unreadCount && conv.unreadCount > 0">
            <el-badge :value="conv.unreadCount" :max="99" />
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
            <el-avatar :size="32" :src="selectedConversation.avatar">{{ selectedConversation.userName?.charAt(0) || 'U' }}</el-avatar>
            <span class="user-name">{{ selectedConversation.userName || selectedConversation.userId }}</span>
            <el-tag size="small" type="info">{{ selectedConversation.deviceType || 'PC端' }}</el-tag>
          </div>
          <div class="chat-actions">
            <el-button size="small" @click="showUserPanel = !showUserPanel">
              <el-icon><User /></el-icon> 用户信息
            </el-button>
            <el-button size="small" type="info" @click="closeConversation">
              <el-icon><Close /></el-icon> 关闭
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
              placeholder="输入消息，Enter发送，Shift+Enter换行"
              @keydown.enter.exact.prevent="sendMessage"
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
              <span>{{ selectedConversation.userName || '用户' + selectedConversation.userId }}</span>
            </div>
            <div class="detail-item">
              <label>设备类型:</label>
              <el-tag type="info" size="small">
                {{ selectedConversation.deviceType || 'PC端' }}
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
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Close, Notebook, ChatDotSquare, Plus } from '@element-plus/icons-vue'
import { WKIM, WKIMEvent } from 'easyjssdk'
import { DeviceType, WKChannelType } from '@/constants'

// IM 相关状态
let imInstance: ReturnType<typeof WKIM.init> | null = null
const imConnected = ref(false)
const imConnecting = ref(false)

// 类型定义
interface UserConversation {
  id: number
  userId: number
  userUid?: string  // 用户的 WuKongIM UID
  userName?: string
  avatar?: string
  deviceType?: string  // 设备类型 (PC端/H5)
  lastMessage?: string
  lastMessageTime?: string
  unreadCount?: number
  projectId?: number
  isGuest?: boolean
  phone?: string
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
const activeTab = ref('my')  // 默认显示"我的会话"
const selectedConversation = ref<UserConversation | null>(null)
const myConversations = ref<UserConversation[]>([])  // 我的会话列表
const pendingQueue = ref<UserConversation[]>([])  // 排队中列表（IM 实时推送）
const messages = ref<Message[]>([])
const inputMessage = ref('')
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
const projectIds = ref<number[]>([1])  // 客服关联的项目ID列表
const agentId = ref(1)

// 从localStorage获取当前登录的客服ID和项目ID列表
const initAgentInfo = () => {
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
      // 获取关联的项目ID列表
      if (user.projectIds && Array.isArray(user.projectIds) && user.projectIds.length > 0) {
        projectIds.value = user.projectIds
      }
      console.log('Agent info loaded:', { agentId: agentId.value, projectIds: projectIds.value })
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
}

// 计算当前显示的会话列表
const conversations = computed(() => {
  return activeTab.value === 'pending' ? pendingQueue.value : myConversations.value
})

// 计算排队中的数量
const queueCount = computed(() => pendingQueue.value.length)

// 获取"我的会话"列表 - 从后端 API 加载
const fetchMyConversations = async () => {
  try {
    // 将 projectIds 数组转换为查询参数
    const projectIdsParam = projectIds.value.join(',')
    const response = await fetch(`/api/admin/workbench/users?projectIds=${projectIdsParam}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 0) {
      myConversations.value = data.data || []
      console.log('My conversations loaded:', myConversations.value.length)
      console.log('Raw API response data:', JSON.stringify(data.data))
    } else {
      console.error('获取会话列表失败:', data.message)
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
  }
}

// 检查用户是否已在"我的会话"中
const isUserInMyConversations = (userUid: string): boolean => {
  return myConversations.value.some(conv => conv.userUid === userUid)
}

// 选择会话
const selectConversation = async (conv: UserConversation) => {
  selectedConversation.value = conv
  await fetchMessages(conv.userId)
  showUserPanel.value = true
  
  // 标记为已读
  if (conv.unreadCount && conv.unreadCount > 0) {
    await markConversationAsRead(conv.userId)
  }
}

// 标记会话为已读
const markConversationAsRead = async (userId: number) => {
  try {
    await fetch(`/api/admin/workbench/users/${userId}/read`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    // 更新本地状态 - 两个列表都检查
    const myConv = myConversations.value.find(c => c.userId === userId)
    if (myConv) {
      myConv.unreadCount = 0
    }
    const pendingConv = pendingQueue.value.find(c => c.userId === userId)
    if (pendingConv) {
      pendingConv.unreadCount = 0
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 获取消息列表（从 WuKongIM 加载历史消息）
const fetchMessages = async (userId: number) => {
  // 在两个列表中查找用户
  const conv = myConversations.value.find(c => c.userId === userId) 
    || pendingQueue.value.find(c => c.userId === userId)
    || selectedConversation.value
  if (!conv?.userUid) {
    console.warn('Cannot load messages: userUid not found')
    return
  }
  
  try {
    // 通过后端代理调用 WuKongIM API 获取历史消息
    // Visitor Channel: channel_id = 用户 UID, channel_type = 10
    const response = await fetch('/api/admin/im/messages/sync', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify({
        loginUid: getAgentUid(),
        channelId: conv.userUid,
        channelType: WKChannelType.VISITOR,  // Visitor Channel
        limit: 50
      })
    })
    
    const data = await response.json()
    
    if (data.code === 200 && data.data) {
      messages.value = data.data.map((msg: any) => {
        // 解析 WuKongIM 消息格式
        const payload = msg.payload || {}
        // WuKongIM 返回的时间戳可能是秒或毫秒
        const timestamp = (msg.timestamp || msg.message_time || 0) * 1000
        
        // 判断发送者类型 - 兼容 snake_case 和 camelCase
        const msgFromUid = msg.from_uid || msg.fromUid || ''
        const isAgent = msgFromUid.startsWith('agent_')
        
        console.log('History message from:', msgFromUid, 'isAgent:', isAgent)
        
        return {
          id: msg.message_id || msg.messageId || msg.client_msg_no || msg.clientMsgNo || Date.now(),
          conversationId: conv.userId,
          senderId: isAgent ? agentId.value : conv.userId,
          senderType: isAgent ? 'agent' : 'user',
          contentType: payload.type === 2 ? 'image' : 'text',
          content: payload.content || payload.url || '',
          createdAt: new Date(timestamp).toISOString()
        }
      })
      
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('获取消息失败:', error)
  }
}

// 接入会话（从排队中接入用户，移动到我的会话）
const acceptConversation = async (conv: UserConversation) => {
  try {
    // 标记为已读
    await markConversationAsRead(conv.userId)
    
    // 从排队中移除
    pendingQueue.value = pendingQueue.value.filter(c => c.userId !== conv.userId)
    
    // 添加到我的会话（如果不存在）
    if (!isUserInMyConversations(conv.userUid || '')) {
      myConversations.value.unshift(conv)
    }
    
    ElMessage.success('已接入会话')
    selectConversation(conv)
    activeTab.value = 'my'
  } catch (error) {
    console.error('接入会话失败:', error)
    ElMessage.error('接入失败')
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !selectedConversation.value) return
  
  // 检查 IM 连接和用户 UID
  if (!imInstance || !imConnected.value) {
    ElMessage.warning('IM 未连接，请稍后重试')
    return
  }
  
  if (!selectedConversation.value.userUid) {
    ElMessage.warning('无法获取用户信息，请刷新会话')
    return
  }
  
  const messageText = inputMessage.value.trim()
  inputMessage.value = '' // 立即清空输入框
  
  // 先在本地添加消息（乐观更新）
  const tempMsg: Message = {
    id: Date.now(),
    conversationId: selectedConversation.value.userId,  // 使用 userId 作为临时 ID
    senderId: agentId.value,
    senderType: 'agent',
    contentType: 'text',
    content: messageText,
    createdAt: new Date().toISOString()
  }
  messages.value.push(tempMsg)
  nextTick(() => scrollToBottom())
  
  try {
    // 通过 WuKongIM SDK 发送消息到用户的访客频道
    // Visitor Channel (channel_type=10), channel_id = 用户 UID
    const payload = { type: 1, content: messageText }
    const result = await imInstance.send(
      selectedConversation.value.userUid,
      WKChannelType.VISITOR as any,
      payload
    )
    
    console.log('Message sent to visitor channel:', result)
    
    if (result.reasonCode !== 1) {
      ElMessage.error('消息发送失败')
      messages.value = messages.value.filter(m => m.id !== tempMsg.id)
    } else {
      // 发送成功，更新会话列表中的最新消息
      const convIndex = myConversations.value.findIndex(c => c.userUid === selectedConversation.value?.userUid)
      if (convIndex !== -1) {
        myConversations.value[convIndex] = {
          ...myConversations.value[convIndex],
          lastMessage: messageText,
          lastMessageTime: new Date().toISOString()
        }
      }
      
      // 通知后端更新会话活动时间
      fetch('/api/admin/conversations/activity', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        },
        body: JSON.stringify({
          userId: selectedConversation.value.userId
        })
      }).catch(err => console.warn('Activity update failed:', err))
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败')
    messages.value = messages.value.filter(m => m.id !== tempMsg.id)
  }
}

// 清空当前会话选择（暂时不需要结束会话功能）
const closeConversation = () => {
  selectedConversation.value = null
  messages.value = []
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
  // 切换时清空选中
  selectedConversation.value = null
  messages.value = []
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

// ================= WuKongIM 集成 =================

// 客服 UID（格式: agent_{userId}）
const getAgentUid = () => `agent_${agentId.value}`

// IM 事件处理函数
const handleIMConnect = async (result: any) => {
  console.log('Agent IM Connected:', result)
  imConnected.value = true
  imConnecting.value = false
  
  // 使用访客频道模式，客服在用户初始化会话时已被添加为订阅者
  // 不需要订阅队列频道
  
  ElMessage.success('IM 已连接')
}

const handleIMDisconnect = (disconnectInfo: any) => {
  console.log('Agent IM Disconnected:', disconnectInfo.code, disconnectInfo.reason)
  imConnected.value = false
  imConnecting.value = false
}

const handleIMMessage = (message: any) => {
  console.log('Agent IM Message Received:', message)
  
  // 解析消息 - 注意：WuKongIM SDK 使用小写字段名
  const payload = message.payload || {}
  const fromUid = message.fromUID || message.fromUid || ''
  const channelId = message.channelID || message.channelId || ''
  const channelType = message.channelType || 0
  const messageContent = payload.content || payload.url || ''
  
  // 忽略客服自己发的消息
  if (fromUid.startsWith('agent_')) {
    console.log('Ignoring agent message from:', fromUid)
    return
  }
  
  // 只处理访客频道消息
  if (channelType !== WKChannelType.VISITOR) {
    console.log('Ignoring non-visitor channel message, channelType:', channelType)
    return
  }
  
  const userUid = channelId
  console.log('=== Message Routing Debug ===')
  console.log('userUid from channelId:', userUid, 'type:', typeof userUid)
  console.log('myConversations count:', myConversations.value.length)
  console.log('myConversations userUids:', myConversations.value.map(c => ({ userUid: c.userUid, type: typeof c.userUid })))
  console.log('First conversation full object:', JSON.stringify(myConversations.value[0]))
  
  // 检查用户是否在"我的会话"中
  const isInMyConversations = isUserInMyConversations(userUid)
  const currentConv = selectedConversation.value
  const isCurrentlySelected = currentConv && currentConv.userUid === userUid
  
  console.log('isInMyConversations:', isInMyConversations, 'isCurrentlySelected:', isCurrentlySelected)
  
  if (isInMyConversations) {
    // 用户在"我的会话"中
    const convIndex = myConversations.value.findIndex(c => c.userUid === userUid)
    if (convIndex !== -1) {
      // 更新会话的最近消息和时间（使用响应式方式）
      myConversations.value[convIndex] = {
        ...myConversations.value[convIndex],
        lastMessage: messageContent,
        lastMessageTime: new Date().toISOString()
      }
      const conv = myConversations.value[convIndex]
      
      if (isCurrentlySelected) {
        // 场景1：客服已打开该用户的聊天框 - 直接添加消息到聊天框
        const newMsg: Message = {
          id: Date.now(),
          conversationId: conv.userId,
          senderId: 0,
          senderType: 'user',
          contentType: payload.type === 2 ? 'image' : 'text',
          content: messageContent,
          createdAt: new Date().toISOString()
        }
        messages.value.push(newMsg)
        nextTick(() => scrollToBottom())
        console.log('Message added to chat window for selected conversation')
      } else {
        // 场景2：客服没有打开该用户的聊天框 - 未读数+1
        myConversations.value[convIndex] = {
          ...myConversations.value[convIndex],
          unreadCount: (conv.unreadCount || 0) + 1
        }
        console.log('Unread count updated for conversation:', conv.userUid, 'new count:', myConversations.value[convIndex].unreadCount)
      }
    }
  } else {
    // 用户不在"我的会话"中 - 检查是否在排队中
    const pendingIndex = pendingQueue.value.findIndex(c => c.userUid === userUid)
    const currentConvInPending = selectedConversation.value && selectedConversation.value.userUid === userUid
    
    if (pendingIndex >= 0) {
      // 用户已在排队中，更新排队列表
      pendingQueue.value[pendingIndex] = {
        ...pendingQueue.value[pendingIndex],
        lastMessage: messageContent,
        lastMessageTime: new Date().toISOString(),
        unreadCount: currentConvInPending ? 0 : (pendingQueue.value[pendingIndex].unreadCount || 0) + 1
      }
      
      // 如果客服已打开这个排队用户的聊天框，添加消息到聊天窗口
      if (currentConvInPending) {
        const newMsg: Message = {
          id: Date.now(),
          conversationId: pendingQueue.value[pendingIndex].userId || 0,
          senderId: 0,
          senderType: 'user',
          contentType: payload.type === 2 ? 'image' : 'text',
          content: messageContent,
          createdAt: new Date().toISOString()
        }
        messages.value.push(newMsg)
        nextTick(() => scrollToBottom())
        console.log('Message added to chat window for pending user')
      } else {
        console.log('Pending queue updated for user:', userUid)
      }
    } else {
      // 新用户加入排队
      const newUserConv: UserConversation = {
        id: Date.now(),
        userId: 0,
        userUid: userUid,
        userName: userUid,
        deviceType: 'H5',
        lastMessage: messageContent,
        lastMessageTime: new Date().toISOString(),
        unreadCount: 1,
        projectId: projectIds.value[0] || 1
      }
      pendingQueue.value.unshift(newUserConv)
      console.log('New user added to pending queue:', userUid)
    }
  }
}

const handleIMError = (error: any) => {
  console.error('Agent IM Error:', error.message || error)
  imConnecting.value = false
}

// 初始化 IM 连接
const initIMConnection = async () => {
  console.log('[IM] initIMConnection called, agentId:', agentId.value, 'imInstance:', !!imInstance)
  if (imInstance) {
    console.log('[IM] Already has imInstance, skipping')
    return
  }
  if (!agentId.value) {
    console.log('[IM] No agentId, skipping')
    return
  }
  
  imConnecting.value = true
  
  try {
    // 1. 获取 IM Token（使用 DeviceType.WEB 常量）
    console.log('[IM] Fetching token for uid:', getAgentUid(), 'deviceFlag:', DeviceType.WEB)
    const response = await fetch('/api/admin/im/token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify({
        uid: getAgentUid(),
        name: `客服 ${agentId.value}`,
        deviceFlag: DeviceType.WEB  // 传给后端用于注册 token
      })
    })
    
    console.log('[IM] Token API response status:', response.status)
    const data = await response.json()
    console.log('[IM] Token API response:', data)
    
    if (!data.success || !data.token) {
      console.error('[IM] Failed to get agent IM token:', data.error || data)
      imConnecting.value = false
      return
    }
    
    console.log('[IM] Agent IM Token obtained:', data.token.substring(0, 8) + '...')
    
    // 2. 初始化 SDK（使用相同的 deviceFlag）
    const wsUrl = import.meta.env.VITE_WUKONGIM_WS_URL || 'ws://localhost:5200'
    console.log('[IM] Connecting to WebSocket:', wsUrl, 'with deviceFlag:', DeviceType.WEB)
    
    imInstance = WKIM.init(wsUrl, {
      uid: getAgentUid(),
      token: data.token,
      deviceFlag: DeviceType.WEB  // 连接时使用相同的 deviceFlag
    })
    
    // 3. 注册事件监听
    imInstance.on(WKIMEvent.Connect, handleIMConnect)
    imInstance.on(WKIMEvent.Disconnect, handleIMDisconnect)
    imInstance.on(WKIMEvent.Message, handleIMMessage)
    imInstance.on(WKIMEvent.Error, handleIMError)
    
    // 4. 连接服务器
    await imInstance.connect()
    console.log('Agent IM connection initiated')
    
  } catch (error) {
    console.error('Failed to init agent IM connection:', error)
    imConnecting.value = false
  }
}

// 断开 IM 连接
const disconnectIM = () => {
  if (imInstance) {
    imInstance.off(WKIMEvent.Connect, handleIMConnect)
    imInstance.off(WKIMEvent.Disconnect, handleIMDisconnect)
    imInstance.off(WKIMEvent.Message, handleIMMessage)
    imInstance.off(WKIMEvent.Error, handleIMError)
    imInstance = null
    imConnected.value = false
  }
}

// ================= 生命周期 =================

// 初始化
onMounted(async () => {
  initAgentInfo()
  
  // 加载"我的会话"列表
  await fetchMyConversations()
  
  // 初始化 IM 连接
  await initIMConnection()
})

// 清理
onUnmounted(() => {
  disconnectIM()
})

// 监听选中会话变化
watch(selectedConversation, (newVal) => {
  if (newVal) {
    // 消息通过 IM 推送实时接收，不需要定时器
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
  display: flex;
  flex-direction: column;
}

.list-header :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

/* IM 连接状态指示器样式 */
.im-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  margin-top: 8px;
  border-radius: 4px;
  background-color: #fef0f0;
  font-size: 12px;
  color: #f56c6c;
}

.im-status.connected {
  background-color: #f0f9eb;
  color: #67c23a;
}

.im-status.connecting {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.im-status .status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: currentColor;
}

.im-status.connecting .status-dot {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
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
  display: flex;
  align-items: center;
  gap: 6px;
}

.device-tag {
  font-size: 11px;
  padding: 0 4px;
  height: 18px;
  line-height: 16px;
}

.conv-unread {
  margin-left: 10px;
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
