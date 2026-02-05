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
          <el-tab-pane label="我的会话" name="my">
            <template #label>
              我的会话 <el-badge :value="myUnreadCount" v-if="myUnreadCount > 0" class="queue-badge" />
            </template>
          </el-tab-pane>
        </el-tabs>
        <!-- IM 连接状态指示器 -->
        <div class="im-status" :class="{ connected: imConnected, connecting: imConnecting }">
          <span class="status-dot"></span>
          <span class="status-text">{{ imConnected ? 'IM已连接' : imConnecting ? '连接中...' : 'IM未连接' }}</span>
          <!-- 声音设置按钮 -->
          <el-tooltip :content="soundEnabled ? '声音提醒已开启' : '声音提醒已关闭'" placement="top">
            <el-icon 
              class="sound-toggle" 
              :class="{ enabled: soundEnabled }"
              @click.stop="showSoundSettings = true"
            >
              <Bell v-if="soundEnabled" />
              <MuteNotification v-else />
            </el-icon>
          </el-tooltip>
        </div>
        
        <!-- 会话搜索框 -->
        <div class="conversation-search" v-if="activeTab === 'my'">
          <el-input
            v-model="searchKeyword"
            placeholder="输入 UID 搜索用户"
            size="small"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleClearSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
            <template #append>
              <el-button @click="handleSearch" :loading="searchLoading">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>
      
      <div class="list-content" ref="conversationListRef" @scroll="handleConversationScroll">
        <div 
          v-for="conv in conversations" 
          :key="conv.userUid || conv.id" 
          class="conversation-item"
          :class="{ active: selectedConversation?.userUid === conv.userUid }"
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
          <div class="conv-unread" v-if="conv.unreadCount && conv.unreadCount > 0">
            <el-badge :value="conv.unreadCount" :max="99" />
          </div>
        </div>
        
        <!-- 会话列表加载更多提示 -->
        <div v-if="loadingMoreConversations" class="loading-more-conv">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-else-if="!hasMoreConversations && myConversations.length > 0 && activeTab === 'my'" class="no-more-conv">
          没有更多会话了
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
        <div class="message-list" ref="messageListRef" @scroll="handleMessageScroll">
          <!-- 加载更多提示 -->
          <div v-if="loadingMoreMessages" class="loading-more">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="!hasMoreMessages && messages.length > 0" class="no-more-messages">
            没有更多消息了
          </div>
          
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
              <!-- 文本消息 -->
              <div v-if="msg.contentType === 'text'" class="message-text">{{ msg.content }}</div>
              <!-- 图片消息 -->
              <div v-else-if="msg.contentType === 'image'" class="message-image" @click="previewImage(msg.content)">
                <img :src="msg.content" alt="图片消息" />
              </div>
              <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
            </div>
          </div>
          
          <div v-if="messages.length === 0" class="empty-messages">
            <el-empty description="暂无消息" :image-size="60" />
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <!-- 知识库弹窗遮罩 -->
          <div class="panel-overlay" v-if="showKbPanel" @click="showKbPanel = false"></div>
          <!-- 知识库弹窗 -->
          <div class="kb-panel" v-if="showKbPanel" @click.stop>
            <div class="kb-header">
              <span>知识库</span>
              <el-icon @click="showKbPanel = false" style="cursor: pointer"><Close /></el-icon>
            </div>
            <div class="kb-search">
              <el-input v-model="kbSearchQuery" placeholder="搜索知识库..." size="small" clearable>
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
            </div>
            <div class="kb-list">
              <div 
                v-for="item in filteredKbItems" 
                :key="item.id" 
                class="kb-item"
                @click="insertKbContent(item.content)"
              >
                <div class="kb-title">{{ item.title }}</div>
                <div class="kb-content">{{ item.content }}</div>
              </div>
              <el-empty v-if="filteredKbItems.length === 0" description="暂无匹配内容" :image-size="40" />
            </div>
          </div>
          
          <!-- 快捷回复弹窗遮罩 -->
          <div class="panel-overlay" v-if="showQuickReply" @click="showQuickReply = false"></div>
          <!-- 快捷回复弹窗 -->
          <div class="quick-reply-panel" v-if="showQuickReply" @click.stop>
            <div class="kb-header">
              <span>快捷回复</span>
              <el-icon @click="showQuickReply = false" style="cursor: pointer"><Close /></el-icon>
            </div>
            <div class="kb-list">
              <div 
                v-for="reply in quickReplies" 
                :key="reply.id" 
                class="kb-item"
                @click="insertQuickReply(reply.content)"
              >
                <div class="kb-title">{{ reply.title }}</div>
                <div class="kb-content">{{ reply.content }}</div>
              </div>
              <el-empty v-if="quickReplies.length === 0" description="暂无快捷回复" :image-size="40" />
            </div>
          </div>
          
          <div class="input-toolbar">
            <el-button size="small" @click="showKbPanel = !showKbPanel">
              <el-icon><Notebook /></el-icon> 知识库
            </el-button>
            <el-button size="small" @click="showQuickReply = !showQuickReply">
              <el-icon><ChatDotSquare /></el-icon> 快捷回复
            </el-button>
            <el-button size="small" @click="triggerImageUpload" :loading="uploadingImage">
              <el-icon><Picture /></el-icon> 图片
            </el-button>
            <el-button size="small" @click="openUserTickets" :loading="loadingTickets" class="ticket-btn">
              <el-icon><Tickets /></el-icon> 工单
              <span v-if="hasUnreadUserTicket" class="ticket-unread-dot"></span>
            </el-button>
            <input
              ref="imageInputRef"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleImageUpload"
            />
          </div>
          <div class="input-box">
            <el-input
              ref="inputRef"
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
              <label>外部UID:</label>
              <span>{{ selectedConversation.externalUid || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>邮箱:</label>
              <span>{{ selectedConversation.email || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>手机号:</label>
              <span>{{ selectedConversation.phone || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>设备类型:</label>
              <el-tag type="info" size="small">
                {{ selectedConversation.deviceType || 'PC端' }}
              </el-tag>
            </div>
            <div class="detail-item">
              <label>用户标签:</label>
              <div class="tags" v-loading="loadingTags">
                <template v-if="userTags.length > 0">
                  <el-tag 
                    v-for="tag in userTags" 
                    :key="tag.id"
                    size="small"
                    :style="{ 
                      backgroundColor: tag.color || '#409eff',
                      borderColor: tag.color || '#409eff',
                      color: '#fff'
                    }"
                  >
                    {{ tag.name }}
                  </el-tag>
                </template>
                <span v-else class="no-tags-text">暂无标签</span>
                <el-button size="small" @click="openAddTagDialog" class="manage-tag-btn">
                  管理
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
    <el-dialog v-model="showAddTagDialog" title="管理用户标签" width="450px">
      <div class="tag-dialog-content">
        <!-- 可选标签列表 -->
        <div class="available-tags-section">
          <div class="section-title">项目标签（点击添加）</div>
          <div class="available-tags" v-loading="loadingTags">
            <template v-if="unselectedTags.length > 0">
              <el-tag
                v-for="tag in unselectedTags"
                :key="tag.id"
                class="available-tag"
                effect="plain"
                size="default"
                @click="addTagToUser(tag)"
                :style="{ 
                  borderColor: tag.color || '#409eff',
                  color: tag.color || '#409eff'
                }"
              >
                {{ tag.name }}
              </el-tag>
            </template>
            <div v-else class="no-tags-hint">暂无可添加的标签</div>
          </div>
        </div>
        
        <!-- 已选标签 -->
        <div class="selected-tags-section">
          <div class="section-title">已添加的标签</div>
          <div class="selected-tags">
            <template v-if="userTags.length > 0">
              <el-tag
                v-for="tag in userTags"
                :key="tag.id"
                closable
                @close="removeTag(tag)"
                :style="{ 
                  backgroundColor: tag.color || '#409eff',
                  borderColor: tag.color || '#409eff',
                  color: '#fff'
                }"
              >
                {{ tag.name }}
              </el-tag>
            </template>
            <div v-else class="no-tags-hint">暂未添加标签</div>
          </div>
        </div>
        
        <!-- 自定义添加新标签 -->
        <div class="new-tag-section">
          <div class="section-title">创建新标签</div>
          <div class="new-tag-input">
            <el-input v-model="newTagName" placeholder="输入新标签名称" size="small" style="flex: 1" />
            <el-button type="primary" size="small" @click="createAndAddTag" :disabled="!newTagName.trim()">
              创建并添加
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAddTagDialog = false">关闭</el-button>
      </template>
    </el-dialog>
    
    <!-- 声音设置对话框 -->
    <el-dialog v-model="showSoundSettings" title="声音提醒设置" width="400px">
      <div class="sound-settings">
        <div class="sound-switch">
          <span>开启声音提醒</span>
          <el-switch v-model="soundEnabled" @change="toggleSound" />
        </div>
        <div class="sound-options" v-if="soundEnabled">
          <div class="sound-label">选择提示音：</div>
          <div class="sound-list">
            <div 
              v-for="sound in soundOptions" 
              :key="sound.id"
              class="sound-option"
              :class="{ active: selectedSound === sound.id }"
              @click="selectSound(sound.id)"
            >
              <el-icon><Bell /></el-icon>
              <span>{{ sound.name }}</span>
              <el-icon v-if="selectedSound === sound.id" class="check-icon"><Check /></el-icon>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showSoundSettings = false">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 用户工单弹窗 -->
    <el-dialog v-model="showUserTickets" :title="currentTicketDetail ? '工单详情' : '用户工单'" width="700px">
      <!-- 工单列表视图 -->
      <div v-if="!currentTicketDetail" class="user-tickets-content" v-loading="loadingTickets">
        <div v-if="userTickets.length > 0" class="tickets-list">
          <div 
            v-for="ticket in userTickets" 
            :key="ticket.id" 
            class="ticket-item"
            @click="viewTicketDetail(ticket)"
          >
            <div class="ticket-header">
              <span class="ticket-title">{{ ticket.title }}</span>
              <div class="ticket-tags">
                <el-tag :type="getTicketStatusType(ticket.status)" size="small">
                  {{ getTicketStatusLabel(ticket.status) }}
                </el-tag>
                <el-tag :type="getTicketPriorityType(ticket.priority)" size="small">
                  {{ getTicketPriorityLabel(ticket.priority) }}
                </el-tag>
              </div>
            </div>
            <div class="ticket-desc">{{ ticket.description?.substring(0, 100) }}{{ ticket.description?.length > 100 ? '...' : '' }}</div>
            <div class="ticket-footer">
              <span class="ticket-time">创建时间：{{ formatTicketTime(ticket.createdAt) }}</span>
              <span class="ticket-id">#{{ ticket.id }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="该用户暂无工单" :image-size="80" />
      </div>
      
      <!-- 工单详情视图 -->
      <div v-else class="ticket-detail-view" v-loading="loadingTicketDetail">
        <!-- 工单基本信息 -->
        <div class="ticket-info-section">
          <div class="ticket-detail-header">
            <h4>{{ currentTicketDetail.ticket.title }}</h4>
            <div class="ticket-detail-tags">
              <el-tag :type="getTicketStatusType(currentTicketDetail.ticket.status)" size="small">
                {{ getTicketStatusLabel(currentTicketDetail.ticket.status) }}
              </el-tag>
              <el-tag :type="getTicketPriorityType(currentTicketDetail.ticket.priority)" size="small">
                {{ getTicketPriorityLabel(currentTicketDetail.ticket.priority) }}
              </el-tag>
            </div>
          </div>
          <div class="ticket-detail-desc">{{ currentTicketDetail.ticket.description }}</div>
          <div class="ticket-detail-meta">
            <span>创建时间：{{ formatTicketTime(currentTicketDetail.ticket.createdAt) }}</span>
            <span v-if="currentTicketDetail.ticket.contactInfo">联系方式：{{ currentTicketDetail.ticket.contactInfo }}</span>
          </div>
        </div>
        
        <!-- 回复记录 -->
        <div class="ticket-events-section">
          <div class="section-title">处理记录</div>
          <div class="events-list" v-if="currentTicketDetail.events?.length > 0">
            <div 
              v-for="event in currentTicketDetail.events" 
              :key="event.id" 
              class="event-item"
              :class="event.operatorType"
            >
              <div class="event-header">
                <span class="event-sender">{{ event.operatorType === 'agent' ? '客服' : '用户' }}</span>
                <span class="event-time">{{ formatTicketTime(event.createdAt) }}</span>
              </div>
              <div class="event-content">{{ event.content }}</div>
            </div>
          </div>
          <div v-else class="no-events">暂无处理记录</div>
        </div>
        
        <!-- 快速回复区域 -->
        <div class="quick-reply-section" v-if="currentTicketDetail.ticket.status !== 'closed'">
          <el-input
            v-model="ticketReplyContent"
            type="textarea"
            :rows="3"
            placeholder="输入回复内容..."
          />
          <div class="reply-actions">
            <el-select v-model="ticketNewStatus" placeholder="更新状态" size="small" style="width: 120px">
              <el-option label="不变" value="" />
              <el-option label="处理中" value="processing" />
              <el-option label="已解决" value="resolved" />
              <el-option label="已关闭" value="closed" />
            </el-select>
            <el-button type="primary" @click="submitTicketReply" :loading="submittingTicketReply" :disabled="!ticketReplyContent.trim()">
              发送回复
            </el-button>
          </div>
        </div>
        <div v-else class="ticket-closed-notice">
          <el-alert title="该工单已关闭" type="info" :closable="false" />
        </div>
      </div>
      
      <template #footer>
        <el-button v-if="currentTicketDetail" @click="currentTicketDetail = null">返回列表</el-button>
        <el-button @click="closeTicketDialog">关闭</el-button>
        <el-button v-if="!currentTicketDetail" type="primary" @click="goToTicketManagement">前往工单管理</el-button>
      </template>
    </el-dialog>
    
    <!-- 图片预览弹窗 -->
    <div v-if="previewImageUrl" class="image-preview-overlay" @click="previewImageUrl = ''">
      <img :src="previewImageUrl" class="preview-image" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Close, Notebook, ChatDotSquare, Search, Loading, Bell, MuteNotification, Check, Picture, Tickets } from '@element-plus/icons-vue'
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
  lastMessageSeq?: number  // 最后一条消息的序号，用于标记已读
  unreadCount?: number
  projectId?: number
  isGuest?: boolean
  externalUid?: string  // 外部用户ID
  email?: string
  phone?: string
}

// 标签类型
interface CustomerTag {
  id: number
  name: string
  color?: string
  description?: string
  projectId?: number
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
const userTags = ref<CustomerTag[]>([])  // 用户已有的标签
const availableTags = ref<CustomerTag[]>([])  // 项目下可选的标签
const conversationHistory = ref<any[]>([])
const messageListRef = ref<HTMLElement | null>(null)
const inputRef = ref<InstanceType<typeof import('element-plus')['ElInput']> | null>(null)
const conversationListRef = ref<HTMLElement | null>(null)  // 会话列表容器
const imageInputRef = ref<HTMLInputElement | null>(null)  // 图片上传输入框
const uploadingImage = ref(false)  // 图片上传中状态
const previewImageUrl = ref('')  // 预览图片URL
const showAddTagDialog = ref(false)
const newTagName = ref('')
const loadingTags = ref(false)  // 标签加载状态

// 工单相关状态
interface Ticket {
  id: number
  title: string
  description: string
  status: string
  priority: string
  contactInfo?: string
  createdAt: string
  updatedAt: string
}
interface TicketEvent {
  id: number
  ticketId: number
  operatorId: number
  operatorType: string
  action: string
  content: string
  createdAt: string
}
interface TicketDetail {
  ticket: Ticket
  events: TicketEvent[]
}
const showUserTickets = ref(false)
const loadingTickets = ref(false)
const userTickets = ref<Ticket[]>([])
const currentTicketDetail = ref<TicketDetail | null>(null)
const loadingTicketDetail = ref(false)
const ticketReplyContent = ref('')
const ticketNewStatus = ref('')
const submittingTicketReply = ref(false)
const hasUnreadUserTicket = ref(false)  // 是否有用户回复的未读工单
const AGENT_TICKET_READ_KEY = 'mini_cs_agent_ticket_read_'  // 客服端工单已读状态 key

// 消息分页加载相关状态
const loadingMoreMessages = ref(false)
const hasMoreMessages = ref(true)
const oldestMessageSeq = ref<number>(0)  // 当前最旧消息的序号

// 会话列表分页状态
const conversationPage = ref(0)  // 当前页码（从0开始）
const conversationPageSize = 20  // 每页大小
const loadingMoreConversations = ref(false)
const hasMoreConversations = ref(true)
const totalConversations = ref(0)

// 会话搜索相关状态
const searchKeyword = ref('')  // 搜索关键词
const searchLoading = ref(false)  // 搜索加载状态
const isSearchMode = ref(false)  // 是否处于搜索模式

// 声音提醒相关状态
const soundEnabled = ref(true)  // 是否开启声音提醒
const selectedSound = ref('ding')  // 当前选择的声音
const showSoundSettings = ref(false)  // 是否显示声音设置弹窗
const soundOptions = [
  { id: 'ding', name: '叮咚', frequency: [880, 660], duration: 0.15 },
  { id: 'notification', name: '通知', frequency: [523, 659, 784], duration: 0.12 },
  { id: 'message', name: '消息', frequency: [800], duration: 0.1 },
  { id: 'alert', name: '提示', frequency: [440, 550, 660], duration: 0.1 },
  { id: 'pop', name: '气泡', frequency: [600, 400], duration: 0.08 }
]
let audioContext: AudioContext | null = null

// 知识库相关
const kbSearchQuery = ref('')
const kbItems = ref<{id: number; title: string; content: string}[]>([])
const filteredKbItems = computed(() => {
  if (!kbSearchQuery.value) return kbItems.value
  return kbItems.value.filter(item => 
    item.title.includes(kbSearchQuery.value) || item.content.includes(kbSearchQuery.value)
  )
})

// 快捷回复
const quickReplies = ref<{id: number; title: string; content: string}[]>([])

// 加载知识库文章（支持多个项目）
const fetchKbArticles = async (projectIds: number[]) => {
  try {
    const projectIdsParam = projectIds.join(',')
    const response = await fetch(`/api/admin/kb/articles?projectIds=${projectIdsParam}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    const data = await response.json()
    if (data.code === 0 && data.data) {
      kbItems.value = data.data.map((item: any) => ({
        id: item.id,
        title: item.title,
        content: item.content || item.summary || ''
      }))
    }
  } catch (error) {
    console.error('加载知识库失败:', error)
  }
}

// 加载快捷回复（支持多个项目）
const fetchQuickReplies = async (projectIds: number[]) => {
  try {
    const projectIdsParam = projectIds.join(',')
    const response = await fetch(`/api/admin/quick-replies?projectIds=${projectIdsParam}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    const data = await response.json()
    if (data.code === 0 && data.data) {
      quickReplies.value = data.data.map((item: any) => ({
        id: item.id,
        title: item.title,
        content: item.content
      }))
    }
  } catch (error) {
    console.error('加载快捷回复失败:', error)
  }
}

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

// 计算当前显示的会话列表（选中的会话置顶）
const conversations = computed(() => {
  const list = activeTab.value === 'pending' ? pendingQueue.value : myConversations.value
  
  // 如果有选中的会话，将其置顶
  if (selectedConversation.value) {
    const selectedUid = selectedConversation.value.userUid
    const selectedIndex = list.findIndex(c => c.userUid === selectedUid)
    
    if (selectedIndex > 0) {
      // 选中的会话不在第一位，需要重新排序
      const selected = list[selectedIndex]
      const others = list.filter((_, i) => i !== selectedIndex)
      return [selected, ...others]
    }
  }
  
  return list
})

// 计算排队中有未读消息的用户数
const queueCount = computed(() => pendingQueue.value.filter(c => c.unreadCount && c.unreadCount > 0).length)

// 计算"我的会话"中有未读消息的用户数
const myUnreadCount = computed(() => myConversations.value.filter(c => c.unreadCount && c.unreadCount > 0).length)

// 获取"我的会话"列表 - 从后端 API 加载（首次加载）
const fetchMyConversations = async (keyword?: string) => {
  try {
    // 重置分页状态
    conversationPage.value = 0
    hasMoreConversations.value = true
    
    // 将 projectIds 数组转换为查询参数
    const projectIdsParam = projectIds.value.join(',')
    let url = `/api/admin/workbench/users?projectIds=${projectIdsParam}&page=0&pageSize=${conversationPageSize}`
    
    // 如果有搜索关键词，添加到请求参数
    if (keyword && keyword.trim()) {
      url += `&keyword=${encodeURIComponent(keyword.trim())}`
      isSearchMode.value = true
    } else {
      isSearchMode.value = false
    }
    
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 0 && data.data) {
      // 分页接口返回 { list, total, page, pageSize, hasMore }
      myConversations.value = data.data.list || []
      totalConversations.value = data.data.total || 0
      hasMoreConversations.value = data.data.hasMore || false
      console.log('My conversations loaded:', myConversations.value.length, 'total:', totalConversations.value, 'hasMore:', hasMoreConversations.value, 'keyword:', keyword)
    } else {
      console.error('获取会话列表失败:', data.message)
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
  }
}

// 加载更多会话（上拉分页）
const loadMoreConversations = async () => {
  if (loadingMoreConversations.value || !hasMoreConversations.value || activeTab.value !== 'my') {
    return
  }
  
  loadingMoreConversations.value = true
  
  try {
    const nextPage = conversationPage.value + 1
    const projectIdsParam = projectIds.value.join(',')
    let url = `/api/admin/workbench/users?projectIds=${projectIdsParam}&page=${nextPage}&pageSize=${conversationPageSize}`
    
    // 如果处于搜索模式，添加搜索关键词
    if (isSearchMode.value && searchKeyword.value.trim()) {
      url += `&keyword=${encodeURIComponent(searchKeyword.value.trim())}`
    }
    
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 0 && data.data) {
      const newConversations = data.data.list || []
      
      if (newConversations.length > 0) {
        // 去重：过滤掉已存在的会话
        const existingIds = new Set(myConversations.value.map(c => c.userUid))
        const uniqueConversations = newConversations.filter((c: UserConversation) => !existingIds.has(c.userUid))
        
        // 追加到列表
        myConversations.value = [...myConversations.value, ...uniqueConversations]
        conversationPage.value = nextPage
      }
      
      hasMoreConversations.value = data.data.hasMore || false
      console.log('Loaded more conversations, page:', nextPage, 'total now:', myConversations.value.length, 'hasMore:', hasMoreConversations.value)
    }
  } catch (error) {
    console.error('加载更多会话失败:', error)
  } finally {
    loadingMoreConversations.value = false
  }
}

// 搜索会话
const handleSearch = async () => {
  if (searchLoading.value) return
  
  searchLoading.value = true
  try {
    await fetchMyConversations(searchKeyword.value)
  } finally {
    searchLoading.value = false
  }
}

// 清除搜索
const handleClearSearch = async () => {
  searchKeyword.value = ''
  isSearchMode.value = false
  await fetchMyConversations()
}

// 处理会话列表滚动事件（上拉加载）
const handleConversationScroll = () => {
  const container = conversationListRef.value
  if (!container || activeTab.value !== 'my') return
  
  // 当滚动到底部附近时（距离底部 50px 内），加载更多
  const scrollBottom = container.scrollHeight - container.scrollTop - container.clientHeight
  if (scrollBottom < 50) {
    loadMoreConversations()
  }
}

// 检查用户是否已在"我的会话"中
const isUserInMyConversations = (userUid: string): boolean => {
  return myConversations.value.some(conv => conv.userUid === userUid)
}

// 选择会话
const selectConversation = async (conv: UserConversation) => {
  // 如果 userId 为 0 但有 userUid，尝试解析获取真实的 userId
  if ((!conv.userId || conv.userId === 0) && conv.userUid) {
    const parsed = parseUserUid(conv.userUid)
    if (parsed) {
      conv.userId = parsed.userId
      conv.projectId = parsed.projectId
      console.log('Parsed userId from userUid:', conv.userUid, '-> userId:', parsed.userId, 'projectId:', parsed.projectId)
    }
  }
  
  selectedConversation.value = conv
  
  // 使用 userUid 获取历史消息
  if (conv.userUid) {
    await fetchMessagesByUserUid(conv.userUid)
  }
  
  showUserPanel.value = true
  
  // 加载用户标签
  if (conv.userId && conv.userId > 0) {
    await fetchUserTags(conv.userId)
    // 检查用户是否有未读工单
    await checkUserHasUnreadTicket()
  } else {
    userTags.value = []
    hasUnreadUserTicket.value = false
  }
  
  // 标记为已读（只有在 userId 有效时才调用后端）
  if (conv.unreadCount && conv.unreadCount > 0) {
    if (conv.userId && conv.userId > 0) {
      await markConversationAsRead(conv.userId, conv.lastMessageSeq)
    }
    // 本地清除未读
    conv.unreadCount = 0
    
    // 更新列表中的未读数
    const myConvIndex = myConversations.value.findIndex(c => c.userUid === conv.userUid)
    if (myConvIndex !== -1) {
      myConversations.value[myConvIndex] = { ...myConversations.value[myConvIndex], unreadCount: 0 }
    }
  }
}

// 标记会话为已读
const markConversationAsRead = async (userId: number, messageSeq?: number) => {
  try {
    await fetch(`/api/admin/workbench/users/${userId}/read`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify({ messageSeq: messageSeq || null })
    })
    // 更新本地状态 - 两个列表都检查
    const myConvIndex = myConversations.value.findIndex(c => c.userId === userId)
    if (myConvIndex !== -1) {
      myConversations.value[myConvIndex] = {
        ...myConversations.value[myConvIndex],
        unreadCount: 0
      }
    }
    const pendingConvIndex = pendingQueue.value.findIndex(c => c.userId === userId)
    if (pendingConvIndex !== -1) {
      pendingQueue.value[pendingConvIndex] = {
        ...pendingQueue.value[pendingConvIndex],
        unreadCount: 0
      }
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 解析单条消息
const parseMessage = (msg: any): Message & { messageSeq?: number } => {
  const payload = msg.payload || {}
  const timestamp = (msg.timestamp || msg.message_time || 0) * 1000
  const msgFromUid = msg.from_uid || msg.fromUid || ''
  const isAgent = msgFromUid.startsWith('agent_')
  
  return {
    id: msg.message_id || msg.messageId || msg.client_msg_no || msg.clientMsgNo || Date.now(),
    conversationId: 0,
    senderId: isAgent ? agentId.value : 0,
    senderType: isAgent ? 'agent' : 'user',
    contentType: payload.type === 2 ? 'image' : 'text',
    content: payload.content || payload.url || '',
    createdAt: new Date(timestamp).toISOString(),
    messageSeq: msg.message_seq || msg.messageSeq || 0
  }
}

// 获取消息列表（通过 userUid，用于排队用户）
const fetchMessagesByUserUid = async (userUid: string) => {
  // 重置分页状态
  hasMoreMessages.value = true
  oldestMessageSeq.value = 0
  
  try {
    // 通过后端代理调用 WuKongIM API 获取历史消息
    // Visitor Channel: channel_id = 用户 UID, channel_type = 10
    // 首次加载不传 pullMode，默认获取最新消息
    const response = await fetch('/api/admin/im/messages/sync', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify({
        loginUid: getAgentUid(),
        channelId: userUid,
        channelType: WKChannelType.VISITOR,  // Visitor Channel
        limit: 50
      })
    })
    
    const data = await response.json()
    
    if (data.code === 200 && data.data) {
      const parsedMessages = data.data.map(parseMessage)
      messages.value = parsedMessages
      
      // 记录最旧消息的序号，用于分页
      if (parsedMessages.length > 0) {
        const seqs = parsedMessages.map((m: any) => m.messageSeq || 0).filter((s: number) => s > 0)
        if (seqs.length > 0) {
          oldestMessageSeq.value = Math.min(...seqs)
        }
        hasMoreMessages.value = parsedMessages.length >= 50
      } else {
        hasMoreMessages.value = false
      }
      
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('获取消息失败:', error)
  }
}

// 加载更多历史消息（向上滚动时触发）
const loadMoreMessages = async () => {
  if (!selectedConversation.value?.userUid || loadingMoreMessages.value || !hasMoreMessages.value) {
    return
  }
  
  if (oldestMessageSeq.value <= 1) {
    hasMoreMessages.value = false
    return
  }
  
  loadingMoreMessages.value = true
  
  try {
    // pullMode=0 配合 startMessageSeq 向上拉取更旧的消息
    const response = await fetch('/api/admin/im/messages/sync', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify({
        loginUid: getAgentUid(),
        channelId: selectedConversation.value.userUid,
        channelType: WKChannelType.VISITOR,
        startMessageSeq: oldestMessageSeq.value,
        limit: 30,
        pullMode: 0  // 向上拉取更旧消息
      })
    })
    
    const data = await response.json()
    
    if (data.code === 200 && data.data && data.data.length > 0) {
      const olderMessages = data.data.map(parseMessage)
      
      // 去重：过滤掉已存在的消息
      const existingIds = new Set(messages.value.map(m => m.id))
      const uniqueOlderMessages = olderMessages.filter((m: any) => !existingIds.has(m.id))
      
      if (uniqueOlderMessages.length === 0) {
        hasMoreMessages.value = false
        return
      }
      
      // 保存当前滚动位置
      const container = messageListRef.value
      const previousScrollHeight = container?.scrollHeight || 0
      
      // 将旧消息添加到列表前面
      messages.value = [...uniqueOlderMessages, ...messages.value]
      
      // 更新最旧消息序号 - 使用新加载的消息中的最小序号
      const seqs = uniqueOlderMessages.map((m: any) => m.messageSeq || 0).filter((s: number) => s > 0)
      if (seqs.length > 0) {
        const newOldestSeq = Math.min(...seqs)
        // 确保序号在减小，防止重复请求
        if (newOldestSeq < oldestMessageSeq.value) {
          oldestMessageSeq.value = newOldestSeq
        } else {
          hasMoreMessages.value = false
        }
      }
      
      hasMoreMessages.value = olderMessages.length >= 30
      
      // 恢复滚动位置，保持用户当前查看的消息不动
      await nextTick()
      if (container) {
        const newScrollHeight = container.scrollHeight
        container.scrollTop = newScrollHeight - previousScrollHeight
      }
    } else {
      hasMoreMessages.value = false
    }
  } catch (error) {
    console.error('加载更多消息失败:', error)
  } finally {
    loadingMoreMessages.value = false
  }
}

// 处理消息列表滚动事件
const handleMessageScroll = () => {
  const container = messageListRef.value
  if (!container) return
  
  // 当滚动到顶部附近时（距离顶部 50px 内），加载更多消息
  if (container.scrollTop < 50) {
    loadMoreMessages()
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
  showKbPanel.value = false
  showQuickReply.value = false
}

// 触发图片上传
const triggerImageUpload = () => {
  imageInputRef.value?.click()
}

// 图片预览
const previewImage = (url: string) => {
  previewImageUrl.value = url
}

// ========== 工单相关方法 ==========
// 检查工单是否有未读的用户回复
const checkTicketUnread = (ticket: Ticket): boolean => {
  if (!selectedConversation.value?.userId) return false
  const key = `${AGENT_TICKET_READ_KEY}${selectedConversation.value.userId}_${ticket.id}`
  const readTime = localStorage.getItem(key)
  if (!readTime) {
    // 从未查看过，检查是否有用户回复（通过 updatedAt 判断）
    return ticket.status !== 'closed'
  }
  // 比较工单更新时间与已读时间
  const ticketUpdateTime = new Date(ticket.updatedAt).getTime()
  return ticketUpdateTime > parseInt(readTime)
}

// 标记工单为已读
const markTicketRead = (ticketId: number) => {
  if (!selectedConversation.value?.userId) return
  const key = `${AGENT_TICKET_READ_KEY}${selectedConversation.value.userId}_${ticketId}`
  localStorage.setItem(key, Date.now().toString())
}

// 检查当前用户是否有未读工单
const checkUserHasUnreadTicket = async () => {
  if (!selectedConversation.value?.userId) {
    hasUnreadUserTicket.value = false
    return
  }
  
  try {
    const response = await fetch(`/api/admin/ticket/user/${selectedConversation.value.userId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    const data = await response.json()
    if (data.code === 0 && data.data) {
      // 检查是否有未读的工单
      hasUnreadUserTicket.value = data.data.some((ticket: Ticket) => checkTicketUnread(ticket))
    } else {
      hasUnreadUserTicket.value = false
    }
  } catch (error) {
    console.error('检查未读工单失败:', error)
    hasUnreadUserTicket.value = false
  }
}

// 打开用户工单列表
const openUserTickets = async () => {
  if (!selectedConversation.value?.userId) {
    ElMessage.warning('请先选择会话')
    return
  }
  
  showUserTickets.value = true
  currentTicketDetail.value = null
  loadingTickets.value = true
  
  try {
    const response = await fetch(`/api/admin/ticket/user/${selectedConversation.value.userId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    const data = await response.json()
    if (data.code === 0 && data.data) {
      userTickets.value = data.data
      // 打开工单列表后，清除未读状态
      hasUnreadUserTicket.value = false
    } else {
      userTickets.value = []
    }
  } catch (error) {
    console.error('加载用户工单失败:', error)
    userTickets.value = []
    ElMessage.error('加载工单失败')
  } finally {
    loadingTickets.value = false
  }
}

// 查看工单详情（在弹窗内）
const viewTicketDetail = async (ticket: Ticket) => {
  loadingTicketDetail.value = true
  ticketReplyContent.value = ''
  ticketNewStatus.value = ''
  
  // 标记工单为已读
  markTicketRead(ticket.id)
  
  try {
    const response = await fetch(`/api/admin/ticket/${ticket.id}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    const data = await response.json()
    if (data.code === 0 && data.data) {
      currentTicketDetail.value = data.data
    }
  } catch (error) {
    console.error('加载工单详情失败:', error)
    ElMessage.error('加载工单详情失败')
  } finally {
    loadingTicketDetail.value = false
  }
}

// 提交工单回复
const submitTicketReply = async () => {
  if (!ticketReplyContent.value.trim() || !currentTicketDetail.value) return
  
  submittingTicketReply.value = true
  
  try {
    // 发送回复
    const replyResponse = await fetch(`/api/admin/ticket/${currentTicketDetail.value.ticket.id}/reply`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify({ content: ticketReplyContent.value.trim() })
    })
    const replyData = await replyResponse.json()
    
    if (replyData.code !== 0) {
      ElMessage.error(replyData.message || '回复失败')
      return
    }
    
    // 如果需要更新状态
    if (ticketNewStatus.value) {
      await fetch(`/api/admin/ticket/${currentTicketDetail.value.ticket.id}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        },
        body: JSON.stringify({ status: ticketNewStatus.value })
      })
    }
    
    ElMessage.success('回复成功')
    ticketReplyContent.value = ''
    ticketNewStatus.value = ''
    
    // 重新加载工单详情
    await viewTicketDetail(currentTicketDetail.value.ticket)
    
    // 同时更新列表中的工单状态
    const ticketIndex = userTickets.value.findIndex(t => t.id === currentTicketDetail.value!.ticket.id)
    if (ticketIndex > -1 && ticketNewStatus.value) {
      userTickets.value[ticketIndex].status = ticketNewStatus.value
    }
  } catch (error) {
    console.error('回复工单失败:', error)
    ElMessage.error('回复失败，请重试')
  } finally {
    submittingTicketReply.value = false
  }
}

// 关闭工单弹窗
const closeTicketDialog = () => {
  showUserTickets.value = false
  currentTicketDetail.value = null
  ticketReplyContent.value = ''
  ticketNewStatus.value = ''
}

// 获取工单状态类型
const getTicketStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'open': 'warning',
    'pending': 'warning',
    'processing': 'primary',
    'resolved': 'success',
    'closed': 'info'
  }
  return statusMap[status] || 'info'
}

// 获取工单状态标签
const getTicketStatusLabel = (status: string) => {
  const labelMap: Record<string, string> = {
    'open': '待处理',
    'pending': '待处理',
    'processing': '处理中',
    'resolved': '已解决',
    'closed': '已关闭'
  }
  return labelMap[status] || status
}

// 获取工单优先级类型
const getTicketPriorityType = (priority: string) => {
  const priorityMap: Record<string, string> = {
    'low': 'info',
    'medium': '',
    'high': 'warning',
    'urgent': 'danger'
  }
  return priorityMap[priority] || ''
}

// 获取工单优先级标签
const getTicketPriorityLabel = (priority: string) => {
  const labelMap: Record<string, string> = {
    'low': '低',
    'medium': '中',
    'high': '高',
    'urgent': '紧急'
  }
  return labelMap[priority] || priority
}

// 格式化工单时间
const formatTicketTime = (time: string) => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 跳转到工单管理页面
const goToTicketManagement = () => {
  showUserTickets.value = false
  currentTicketDetail.value = null
  if (selectedConversation.value?.userId) {
    window.open(`/admin/tickets?userId=${selectedConversation.value.userId}`, '_blank')
  } else {
    window.open('/admin/tickets', '_blank')
  }
}

// 处理图片上传
const handleImageUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // 验证文件类型，只允许上传图片
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.warning('只支持上传图片文件（JPG、PNG、GIF、WEBP、BMP）')
    input.value = ''
    return
  }

  // 检查 IM 连接和用户 UID
  if (!imInstance || !imConnected.value) {
    ElMessage.warning('IM 未连接，请稍后重试')
    return
  }
  
  if (!selectedConversation.value?.userUid) {
    ElMessage.warning('请先选择会话')
    return
  }

  uploadingImage.value = true
  
  try {
    // 获取 OSS 上传凭证
    const tokenRes = await fetch('/api/admin/oss/token', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    const tokenData = await tokenRes.json()
    
    if (tokenData.code === 0 && tokenData.data) {
      const { policy, signature, x_oss_credential, x_oss_date, host, dir, domain } = tokenData.data
      
      const formData = new FormData()
      const key = `${dir}${Date.now()}_${file.name}`
      
      // V4 签名方式的表单字段
      formData.append('key', key)
      formData.append('success_action_status', '200')
      formData.append('policy', policy)
      formData.append('x-oss-signature', signature)
      formData.append('x-oss-signature-version', 'OSS4-HMAC-SHA256')
      formData.append('x-oss-credential', x_oss_credential)
      formData.append('x-oss-date', x_oss_date)
      formData.append('file', file)  // file 必须为最后一个表单域
      
      const uploadRes = await fetch(host, { method: 'POST', body: formData })
      
      if (uploadRes.ok) {
        const imageUrl = `${domain}/${key}`
        
        // 先在本地添加消息（乐观更新）
        const tempMsg: Message = {
          id: Date.now(),
          conversationId: selectedConversation.value.userId,
          senderId: agentId.value,
          senderType: 'agent',
          contentType: 'image',
          content: imageUrl,
          createdAt: new Date().toISOString()
        }
        messages.value.push(tempMsg)
        nextTick(() => scrollToBottom())
        
        // 通过 WuKongIM SDK 发送图片消息到用户的访客频道
        const payload = { type: 2, url: imageUrl }
        const result = await imInstance.send(
          selectedConversation.value.userUid,
          WKChannelType.VISITOR as any,
          payload
        )
        
        console.log('Image sent to visitor channel:', result)
        
        if (result.reasonCode !== 1) {
          ElMessage.error('图片发送失败')
          messages.value = messages.value.filter(m => m.id !== tempMsg.id)
        } else {
          // 发送成功，更新会话列表中的最新消息
          const convIndex = myConversations.value.findIndex(c => c.userUid === selectedConversation.value?.userUid)
          if (convIndex !== -1) {
            myConversations.value[convIndex] = {
              ...myConversations.value[convIndex],
              lastMessage: '[图片]',
              lastMessageTime: new Date().toISOString()
            }
          }
        }
      } else {
        console.error('Upload failed:', uploadRes.status, await uploadRes.text())
        ElMessage.error('图片上传失败，请重试')
      }
    } else {
      console.error('Failed to get OSS token:', tokenData)
      ElMessage.error('获取上传凭证失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败，请重试')
  } finally {
    uploadingImage.value = false
    input.value = ''
  }
}

// 插入知识库内容
const insertKbContent = (content: string) => {
  inputMessage.value = content
  showKbPanel.value = false
  nextTick(() => {
    inputRef.value?.focus()
  })
}

// 插入快捷回复
const insertQuickReply = (content: string) => {
  inputMessage.value = content
  showQuickReply.value = false
  nextTick(() => {
    inputRef.value?.focus()
  })
}

// ================= 用户标签功能 =================

// 计算未选中的标签（可添加的标签）
const unselectedTags = computed(() => {
  const selectedIds = new Set(userTags.value.map(t => t.id))
  return availableTags.value.filter(t => !selectedIds.has(t.id))
})

// 加载项目下的可用标签
const fetchAvailableTags = async (projectId: number) => {
  try {
    const response = await fetch(`/api/admin/customer-tags?projectId=${projectId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    const data = await response.json()
    if (data.code === 0 && data.data) {
      availableTags.value = data.data
    }
  } catch (error) {
    console.error('加载项目标签失败:', error)
  }
}

// 加载用户已有的标签
const fetchUserTags = async (userId: number) => {
  loadingTags.value = true
  try {
    const response = await fetch(`/api/admin/customers/${userId}/tags`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    const data = await response.json()
    if (data.code === 0 && data.data) {
      userTags.value = data.data
    } else {
      userTags.value = []
    }
  } catch (error) {
    console.error('加载用户标签失败:', error)
    userTags.value = []
  } finally {
    loadingTags.value = false
  }
}

// 打开标签管理对话框
const openAddTagDialog = async () => {
  if (!selectedConversation.value) return
  
  showAddTagDialog.value = true
  newTagName.value = ''
  
  // 加载项目下的可用标签
  if (selectedConversation.value.projectId) {
    await fetchAvailableTags(selectedConversation.value.projectId)
  }
}

// 添加标签到用户
const addTagToUser = async (tag: CustomerTag) => {
  if (!selectedConversation.value?.userId) return
  
  try {
    const response = await fetch(`/api/admin/customers/${selectedConversation.value.userId}/tags`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify({ tagId: tag.id })
    })
    const data = await response.json()
    if (data.code === 0) {
      // 添加到已选标签列表
      if (!userTags.value.find(t => t.id === tag.id)) {
        userTags.value.push(tag)
      }
      ElMessage.success('标签已添加')
    } else {
      ElMessage.error(data.message || '添加标签失败')
    }
  } catch (error) {
    console.error('添加标签失败:', error)
    ElMessage.error('添加标签失败')
  }
}

// 删除标签
const removeTag = async (tag: CustomerTag) => {
  if (!selectedConversation.value?.userId) return
  
  try {
    const response = await fetch(`/api/admin/customers/${selectedConversation.value.userId}/tags/${tag.id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    })
    const data = await response.json()
    if (data.code === 0) {
      userTags.value = userTags.value.filter(t => t.id !== tag.id)
      ElMessage.success('标签已移除')
    } else {
      ElMessage.error(data.message || '移除标签失败')
    }
  } catch (error) {
    console.error('移除标签失败:', error)
    ElMessage.error('移除标签失败')
  }
}

// 创建新标签并添加到用户
const createAndAddTag = async () => {
  if (!newTagName.value.trim() || !selectedConversation.value) return
  
  const projectId = selectedConversation.value.projectId
  if (!projectId) {
    ElMessage.error('无法确定项目ID')
    return
  }
  
  try {
    // 先创建标签
    const createResponse = await fetch('/api/admin/customer-tags', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify({
        projectId: projectId,
        name: newTagName.value.trim(),
        color: '#409EFF'
      })
    })
    const createData = await createResponse.json()
    
    if (createData.code === 0 && createData.data) {
      const newTag = createData.data
      // 添加到可用标签列表
      availableTags.value.push(newTag)
      // 然后添加到用户
      await addTagToUser(newTag)
      newTagName.value = ''
    } else {
      ElMessage.error(createData.message || '创建标签失败')
    }
  } catch (error) {
    console.error('创建标签失败:', error)
    ElMessage.error('创建标签失败')
  }
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

// ================= 声音提醒功能 =================

// 获取或创建 AudioContext
const getAudioContext = (): AudioContext | null => {
  try {
    if (!audioContext) {
      audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
    }
    // 如果被挂起，尝试恢复
    if (audioContext.state === 'suspended') {
      audioContext.resume()
    }
    return audioContext
  } catch (error) {
    console.warn('Web Audio API 不支持:', error)
    return null
  }
}

// 使用 Web Audio API 播放提示音
const playSoundWithWebAudio = (frequencies: number[], duration: number) => {
  const ctx = getAudioContext()
  if (!ctx) return
  
  const now = ctx.currentTime
  
  frequencies.forEach((freq, index) => {
    const oscillator = ctx.createOscillator()
    const gainNode = ctx.createGain()
    
    oscillator.connect(gainNode)
    gainNode.connect(ctx.destination)
    
    oscillator.frequency.value = freq
    oscillator.type = 'sine'
    
    // 音量渐变
    gainNode.gain.setValueAtTime(0, now + index * duration)
    gainNode.gain.linearRampToValueAtTime(0.3, now + index * duration + 0.02)
    gainNode.gain.linearRampToValueAtTime(0, now + (index + 1) * duration)
    
    oscillator.start(now + index * duration)
    oscillator.stop(now + (index + 1) * duration + 0.05)
  })
}

// 播放提示音
const playNotificationSound = () => {
  if (!soundEnabled.value) return
  
  try {
    const soundOption = soundOptions.find(s => s.id === selectedSound.value)
    if (!soundOption) return
    
    playSoundWithWebAudio(soundOption.frequency, soundOption.duration)
  } catch (error) {
    console.error('播放提示音出错:', error)
  }
}

// 预览声音
const previewSound = (soundId: string) => {
  const soundOption = soundOptions.find(s => s.id === soundId)
  if (!soundOption) return
  
  try {
    playSoundWithWebAudio(soundOption.frequency, soundOption.duration)
  } catch (error) {
    console.error('播放预览音出错:', error)
  }
}

// 切换声音开关
const toggleSound = () => {
  // v-model 已经更新了 soundEnabled，这里只需要保存设置
  localStorage.setItem('workbench_sound_enabled', String(soundEnabled.value))
  ElMessage.success(soundEnabled.value ? '已开启声音提醒' : '已关闭声音提醒')
}

// 选择声音
const selectSound = (soundId: string) => {
  selectedSound.value = soundId
  localStorage.setItem('workbench_sound_type', soundId)
  previewSound(soundId)
}

// 初始化声音设置
const initSoundSettings = () => {
  const savedEnabled = localStorage.getItem('workbench_sound_enabled')
  if (savedEnabled !== null) {
    soundEnabled.value = savedEnabled === 'true'
  }
  
  const savedSound = localStorage.getItem('workbench_sound_type')
  if (savedSound && soundOptions.some(s => s.id === savedSound)) {
    selectedSound.value = savedSound
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

// 解析 userUid（格式: {projectId}_{userId}）
const parseUserUid = (userUid: string): { projectId: number; userId: number } | null => {
  if (!userUid) return null
  const parts = userUid.split('_')
  if (parts.length >= 2) {
    const projectId = parseInt(parts[0], 10)
    const userId = parseInt(parts[1], 10)
    if (!isNaN(projectId) && !isNaN(userId)) {
      return { projectId, userId }
    }
  }
  return null
}

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
  const messageSeq = message.messageSeq || message.messageSeQ || 0
  
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
        lastMessageTime: new Date().toISOString(),
        lastMessageSeq: messageSeq  // 保存最新消息序号
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
        
        // 调用后端接口清除未读（传递 messageSeq）
        if (conv.userId) {
          markConversationAsRead(conv.userId, messageSeq)
        }
      } else {
        // 场景2：客服没有打开该用户的聊天框 - 未读数+1
        myConversations.value[convIndex] = {
          ...myConversations.value[convIndex],
          unreadCount: (conv.unreadCount || 0) + 1
        }
        console.log('Unread count updated for conversation:', conv.userUid, 'new count:', myConversations.value[convIndex].unreadCount)
        
        // 播放新消息提示音
        playNotificationSound()
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
        lastMessageSeq: messageSeq,  // 保存最新消息序号
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
        // 播放新消息提示音
        playNotificationSound()
      }
    } else {
      // 新用户加入排队 - 从 userUid 解析 projectId 和 userId
      const parsed = parseUserUid(userUid)
      const newUserConv: UserConversation = {
        id: Date.now(),
        userId: parsed?.userId || 0,
        userUid: userUid,
        userName: userUid,
        deviceType: 'H5',
        lastMessage: messageContent,
        lastMessageTime: new Date().toISOString(),
        lastMessageSeq: messageSeq,  // 保存消息序号
        unreadCount: 1,
        projectId: parsed?.projectId || projectIds.value[0] || 1
      }
      pendingQueue.value.unshift(newUserConv)
      console.log('New user added to pending queue:', userUid, 'parsed userId:', parsed?.userId, 'projectId:', parsed?.projectId)
      
      // 播放新用户提示音
      playNotificationSound()
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
  initSoundSettings()  // 初始化声音设置
  
  // 加载"我的会话"列表
  await fetchMyConversations()
  
  // 加载知识库和快捷回复（使用所有关联的项目ID）
  if (projectIds.value.length > 0) {
    await fetchKbArticles(projectIds.value)
    await fetchQuickReplies(projectIds.value)
  }
  
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

/* 会话搜索框样式 */
.conversation-search {
  margin-top: 10px;
}

.conversation-search :deep(.el-input-group__append) {
  padding: 0;
}

.conversation-search :deep(.el-input-group__append .el-button) {
  border: none;
  margin: 0;
  padding: 8px 12px;
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

/* 加载更多提示样式 */
.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: #909399;
  font-size: 13px;
}

.no-more-messages {
  text-align: center;
  padding: 12px;
  color: #c0c4cc;
  font-size: 12px;
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

/* 图片消息样式 */
.message-image {
  max-width: 200px;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.message-image img {
  width: 100%;
  height: auto;
  display: block;
  transition: transform 0.2s;
}

.message-image:hover img {
  transform: scale(1.02);
}

/* 图片预览遮罩层 */
.image-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  cursor: pointer;
}

.preview-image {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
  border-radius: 4px;
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

/* 知识库和快捷回复面板样式 */
.kb-panel,
.quick-reply-panel {
  position: absolute;
  bottom: 100%;
  left: 16px;
  right: 16px;
  max-height: 300px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.1);
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.kb-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  color: #303133;
}

.kb-search {
  padding: 10px 16px;
  border-bottom: 1px solid #f0f2f5;
}

.kb-list {
  flex: 1;
  overflow-y: auto;
  max-height: 200px;
}

.kb-item {
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f0f2f5;
}

.kb-item:hover {
  background-color: #ecf5ff;
}

.kb-item:last-child {
  border-bottom: none;
}

.kb-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.kb-content {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.input-area {
  position: relative;
}

/* 弹窗遮罩层 */
.panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9;
  background-color: transparent;
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

/* 右侧面板tabs样式调整 */
.side-panel :deep(.el-tabs__nav-wrap) {
  padding-left: 20px;
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

.no-tags-text {
  color: #909399;
  font-size: 13px;
}

.manage-tag-btn {
  padding: 4px 8px;
  font-size: 12px;
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

/* 会话列表加载更多样式 */
.loading-more-conv {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: #909399;
  font-size: 13px;
}

.no-more-conv {
  text-align: center;
  padding: 12px;
  color: #c0c4cc;
  font-size: 12px;
}

/* 声音设置相关样式 */
.sound-toggle {
  margin-left: auto;
  cursor: pointer;
  font-size: 16px;
  opacity: 0.6;
  transition: all 0.2s;
}

.sound-toggle:hover {
  opacity: 1;
}

.sound-toggle.enabled {
  opacity: 1;
  color: #67c23a;
}

.sound-settings {
  padding: 10px 0;
}

.sound-switch {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
  margin-bottom: 16px;
}

.sound-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.sound-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sound-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.sound-option:hover {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
}

.sound-option.active {
  background-color: #ecf5ff;
  border-color: #409eff;
  color: #409eff;
}

.sound-option .check-icon {
  margin-left: auto;
  color: #409eff;
}

/* 标签对话框样式 */
.tag-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 10px;
}

.available-tags-section,
.selected-tags-section {
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f2f5;
}

.available-tags,
.selected-tags {
  min-height: 40px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 8px 0;
}

.available-tag {
  cursor: pointer;
  transition: all 0.2s;
  margin: 0 !important;
}

.available-tag:hover {
  transform: scale(1.05);
}

.selected-tags .el-tag {
  margin: 0 !important;
}

.no-tags-hint {
  color: #909399;
  font-size: 13px;
  padding: 8px 0;
}

.new-tag-section .new-tag-input {
  display: flex;
  gap: 10px;
}

/* 用户工单弹窗样式 */
.user-tickets-content {
  min-height: 200px;
  max-height: 400px;
  overflow-y: auto;
}

.tickets-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ticket-item {
  padding: 14px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.ticket-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.ticket-title {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
  flex: 1;
  margin-right: 12px;
}

.ticket-tags {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.ticket-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
}

.ticket-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.ticket-id {
  font-family: monospace;
}

/* 工单详情视图样式 */
.ticket-detail-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}

.back-btn {
  padding: 4px 8px;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.ticket-info-section {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.info-label {
  color: #909399;
  min-width: 56px;
}

.info-value {
  color: #606266;
}

.ticket-desc-section {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.ticket-events-section {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.events-header {
  padding: 10px 12px;
  background: #f8f9fa;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
}

.events-list {
  max-height: 200px;
  overflow-y: auto;
}

.event-item {
  padding: 10px 12px;
  border-bottom: 1px solid #f0f2f5;
}

.event-item:last-child {
  border-bottom: none;
}

.event-item.agent {
  background: #ecf5ff;
}

.event-item.user {
  background: #fff;
}

.event-item.system {
  background: #fafafa;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.event-creator {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.event-time {
  font-size: 12px;
  color: #909399;
}

.event-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  white-space: pre-wrap;
}

.no-events {
  padding: 20px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.quick-reply-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.reply-header {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.reply-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.status-select {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-label {
  font-size: 13px;
  color: #606266;
}

/* 工单按钮红点样式 */
.ticket-btn {
  position: relative;
}

.ticket-unread-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  border: 1px solid #fff;
}
</style>
