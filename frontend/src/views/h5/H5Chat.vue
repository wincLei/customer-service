<template>
  <div class="h5-chat">
    <!-- IM 连接状态指示器 -->
    <div class="im-status" :class="{ connected: imConnected, connecting: imConnecting }">
      <span class="status-dot"></span>
      <span class="status-text">{{ imConnected ? '已连接' : imConnecting ? '连接中...' : '未连接' }}</span>
    </div>

    <!-- 消息列表区域 -->
    <div class="messages" ref="messagesContainer">
      <!-- 日期分隔线 -->
      <template v-for="(group, index) in groupedMessages" :key="index">
        <div class="date-divider">{{ group.date }}</div>
        <div
          v-for="msg in group.messages"
          :key="msg.id"
          class="msg-item"
          :class="{ 'msg-user': msg.senderType === 'user', 'msg-agent': msg.senderType === 'agent' }"
        >
          <!-- 客服头像（左侧） -->
          <div v-if="msg.senderType === 'agent'" class="msg-avatar">
            <img :src="agentAvatar" alt="客服" />
          </div>
          
          <!-- 消息内容 -->
          <div class="msg-content">
            <div class="msg-bubble" :class="{ 'image-bubble': msg.msgType === 'image' }">
              <!-- 文本消息 -->
              <template v-if="msg.msgType === 'text'">
                {{ msg.content }}
              </template>
              <!-- 图片消息 -->
              <template v-else-if="msg.msgType === 'image'">
                <img :src="msg.content" @click="previewImage(msg.content)" class="msg-image" />
              </template>
              <!-- 文件消息 -->
              <template v-else-if="msg.msgType === 'file'">
                <div class="file-msg" @click="downloadFile(msg)">
                  <span class="file-icon">📄</span>
                  <span class="file-name">{{ msg.fileName }}</span>
                </div>
              </template>
            </div>
            <div class="msg-time">{{ msg.time }}</div>
          </div>
          
          <!-- 用户头像（右侧） -->
          <div v-if="msg.senderType === 'user'" class="msg-avatar">
            <img :src="userAvatar" alt="用户" />
          </div>
        </div>
      </template>
      
      <!-- 加载中提示 -->
      <div v-if="loading" class="loading-tip">
        <span class="loading-spinner"></span>
        消息加载中...
      </div>
    </div>

    <!-- 底部 Logo 水印 -->
    <div class="watermark">极简开源客服系统</div>

    <!-- 输入区域 -->
    <div class="input-section">
      <div class="input-row">
        <!-- 图片上传按钮 -->
        <button class="tool-btn" @click="triggerImageUpload">
          <span class="tool-icon">🖼️</span>
        </button>
        <input
          ref="imageInput"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleImageUpload"
        />
        
        <!-- 输入框 -->
        <input
          v-model="inputMessage"
          type="text"
          placeholder="请输入内容"
          @keyup.enter="sendTextMessage"
          class="msg-input"
        />
        
        <!-- 表情按钮 -->
        <button class="tool-btn" @click="toggleEmojiPicker">
          <span class="tool-icon">😊</span>
        </button>
        
        <!-- 发送按钮 -->
        <button 
          class="send-btn" 
          :class="{ active: inputMessage.trim() }"
          :disabled="!inputMessage.trim()"
          @click="sendTextMessage"
        >
          发送
        </button>
      </div>
    </div>

    <!-- 表情选择器弹窗 -->
    <div v-if="showEmojiPicker" class="emoji-picker" @click.stop>
      <div class="emoji-grid">
        <span
          v-for="emoji in emojis"
          :key="emoji"
          class="emoji-item"
          @click="insertEmoji(emoji)"
        >
          {{ emoji }}
        </span>
      </div>
    </div>

    <!-- 更多功能弹窗 -->
    <div v-if="showMorePanel" class="more-panel">
      <div class="more-item" @click="submitTicket">
        <span class="more-icon">📝</span>
        <span>提交工单</span>
      </div>
      <div class="more-item" @click="viewFAQ">
        <span class="more-icon">❓</span>
        <span>常见问题</span>
      </div>
    </div>

    <!-- 工单提交弹窗 -->
    <div v-if="showTicketDialog" class="ticket-dialog-overlay" @click.self="showTicketDialog = false">
      <div class="ticket-dialog">
        <div class="ticket-header">
          <h3>提交工单</h3>
          <button class="close-btn" @click="showTicketDialog = false">✕</button>
        </div>
        <div class="ticket-form">
          <div class="form-group">
            <label>标题 <span class="required">*</span></label>
            <input v-model="ticketForm.title" placeholder="请输入工单标题" />
          </div>
          <div class="form-group">
            <label>问题描述 <span class="required">*</span></label>
            <textarea v-model="ticketForm.description" placeholder="请详细描述您遇到的问题" rows="4"></textarea>
          </div>
          <div class="form-group">
            <label>联系方式</label>
            <input v-model="ticketForm.contactInfo" placeholder="手机号或邮箱（方便我们联系您）" />
          </div>
          <div class="form-group">
            <label>优先级</label>
            <select v-model="ticketForm.priority">
              <option value="low">低</option>
              <option value="medium">中</option>
              <option value="high">高</option>
              <option value="urgent">紧急</option>
            </select>
          </div>
          <button class="submit-ticket-btn" @click="handleSubmitTicket" :disabled="!ticketForm.title || !ticketForm.description">
            提交工单
          </button>
        </div>
      </div>
    </div>

    <!-- 图片预览弹窗 -->
    <div v-if="previewImageUrl" class="image-preview-overlay" @click="previewImageUrl = ''">
      <img :src="previewImageUrl" class="preview-image" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api'
import { WKIM, WKIMEvent } from 'easyjssdk'
import { DeviceType, WKChannelType } from '@/constants'

const route = useRoute()
const router = useRouter()

// IM 实例
let imInstance: ReturnType<typeof WKIM.init> | null = null
const imConnected = ref(false)
const imConnecting = ref(false)
const imToken = ref<string | null>(null)  // IM Token（从 user/init 接口获取）

// 状态
const loading = ref(false)
const inputMessage = ref('')
const showEmojiPicker = ref(false)
const showMorePanel = ref(false)
const showTicketDialog = ref(false)
const previewImageUrl = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const imageInput = ref<HTMLInputElement | null>(null)

// 用户信息
interface UserInfo {
  id?: number
  uid: string
  externalUid?: string
  nickname?: string
  avatar?: string
  phone?: string
  projectId: string
  isGuest: boolean
}

const currentUser = ref<UserInfo | null>(null)
const projectId = ref('')
const conversationId = ref<number | null>(null)
const agentUid = ref<string | null>(null)  // 已分配客服的 UID，用于 Personal Channel 通信

// 计算 WuKongIM 用户 UID，格式: {projectId}_{userId}
// 这个格式与后端保持一致，使用数据库自增 ID 保证全局唯一
const imUid = computed(() => {
  if (currentUser.value?.id && projectId.value) {
    return `${projectId.value}_${currentUser.value.id}`
  }
  return null
})

// 客服信息
const agentAvatar = ref('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%231890ff"/><text x="50" y="60" text-anchor="middle" fill="white" font-size="40">客</text></svg>')
const userAvatar = ref('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%2387ceeb"/><text x="50" y="60" text-anchor="middle" fill="white" font-size="40">我</text></svg>')

// 消息列表
interface Message {
  id: string | number
  senderType: 'user' | 'agent' | 'system'
  msgType: 'text' | 'image' | 'file'
  content: string
  fileName?: string
  fileUrl?: string
  time: string
  date: string
  timestamp: number
}

const messages = ref<Message[]>([])

// 按日期分组消息
const groupedMessages = computed(() => {
  const groups: { date: string; messages: Message[] }[] = []
  let currentDate = ''
  
  for (const msg of messages.value) {
    if (msg.date !== currentDate) {
      currentDate = msg.date
      groups.push({ date: currentDate, messages: [] })
    }
    groups[groups.length - 1].messages.push(msg)
  }
  
  return groups
})

// 表情列表
const emojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
  '🙂', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗',
  '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭',
  '🤔', '🤐', '🤨', '😐', '😑', '😶', '😏', '😒',
  '🙄', '😬', '😮', '😯', '😲', '😳', '🥺', '😦',
  '👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙',
  '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '💔'
]

// 工单表单
const ticketForm = ref({
  title: '',
  description: '',
  contactInfo: '',
  priority: 'medium'
})

// 游客 UID 存储 Key
const GUEST_UID_KEY = 'mini_cs_guest_uid'

const generateGuestUid = (): string => {
  return 'guest_' + Date.now() + '_' + Math.random().toString(36).substring(2, 11)
}

const getOrCreateGuestUid = (pid: string): string => {
  const key = `${GUEST_UID_KEY}_${pid}`
  let guestUid = localStorage.getItem(key)
  if (!guestUid) {
    guestUid = generateGuestUid()
    localStorage.setItem(key, guestUid)
  }
  return guestUid
}

const formatTime = (date: Date): string => {
  const h = date.getHours().toString().padStart(2, '0')
  const m = date.getMinutes().toString().padStart(2, '0')
  return `${h}:${m}`
}

const formatDate = (date: Date): string => {
  const month = (date.getMonth() + 1).toString()
  const day = date.getDate().toString()
  const h = date.getHours().toString().padStart(2, '0')
  const m = date.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${h}:${m}`
}

const initUser = async () => {
  // 从 URL 参数获取配置，支持多种命名风格
  const pid = route.query.project_id as string || route.query.projectId as string || '1'
  projectId.value = pid
  
  // 优先使用 URL 传入的 guestUid，否则从本地存储获取或生成新的
  const urlGuestUid = route.query.guestUid as string || route.query.guest_uid as string
  const guestUid = urlGuestUid || getOrCreateGuestUid(pid)
  
  // 如果 URL 传入了 guestUid，保存到本地存储
  if (urlGuestUid) {
    const key = `${GUEST_UID_KEY}_${pid}`
    localStorage.setItem(key, urlGuestUid)
  }
  
  try {
    const response = await request.post('/portal/user/init', {
      projectId: parseInt(pid),
      guestUid,
      externalUid: route.query.uid as string || route.query.external_uid as string || route.query.externalUid as string,
      nickname: route.query.nick_name as string || route.query.nickname as string,
      avatar: route.query.avatar as string,
      phone: route.query.phone as string,
      deviceFlag: DeviceType.H5  // H5 端使用 H5 设备类型
    }) as any
    
    if (response.code === 0 && response.data) {
      const userData = response.data
      
      if (userData.uid && userData.uid !== guestUid) {
        const key = `${GUEST_UID_KEY}_${pid}`
        localStorage.setItem(key, userData.uid)
      }
      
      currentUser.value = {
        id: userData.id,
        uid: userData.uid,
        externalUid: userData.externalUid,
        nickname: userData.nickname,
        avatar: userData.avatar,
        phone: userData.phone,
        projectId: pid,
        isGuest: userData.isGuest
      }
      
      // 保存 IM Token（从 user/init 接口直接获取，无需单独调用）
      if (userData.imToken) {
        imToken.value = userData.imToken
        console.log('IM Token obtained from user init')
      }
      
      if (userData.avatar) {
        userAvatar.value = userData.avatar
      }
    }
  } catch (error) {
    console.error('Failed to init user:', error)
    currentUser.value = {
      uid: guestUid,
      projectId: pid,
      isGuest: true
    }
  }
}

const initConversation = async () => {
  if (!currentUser.value?.id) return
  
  try {
    const response = await request.post('/portal/conversation/init', {
      projectId: parseInt(projectId.value),
      userId: currentUser.value.id
    }) as any
    
    if (response.code === 0 && response.data) {
      conversationId.value = response.data.id
      
      // 保存已分配客服的 UID（如果有）
      if (response.data.agentUid) {
        agentUid.value = response.data.agentUid
        console.log('Agent assigned:', agentUid.value)
      }
      
      await loadHistory()
      
      if (response.data.isNew) {
        const welcomeMsg = response.data.welcomeMessage || '您好，请问有什么需要帮助的？'
        addMessage({ senderType: 'agent', msgType: 'text', content: welcomeMsg })
      }
      
      initIMConnection()
    }
  } catch (error) {
    console.error('Failed to init conversation:', error)
    addMessage({ senderType: 'agent', msgType: 'text', content: '您好，请问有什么需要帮助的？' })
  }
}

const loadHistory = async () => {
  // 访客频道: channel_id = {projectId}_{userId}, channel_type = 10
  if (!imUid.value) {
    console.log('No imUid (projectId_userId), skip loading history')
    return
  }
  
  loading.value = true
  try {
    // 通过后端代理调用 WuKongIM API 获取历史消息
    // Visitor Channel: channel_id = {projectId}_{userId}, channel_type = 10
    const response = await request.post('/portal/im/messages/sync', {
      loginUid: imUid.value,
      channelId: imUid.value,  // 访客频道 ID = {projectId}_{userId}
      channelType: WKChannelType.VISITOR,
      limit: 50
    }) as any
    
    if (response.code === 0 && response.data) {
      messages.value = response.data.map((msg: any) => {
        // 解析 WuKongIM 消息格式
        const payload = msg.payload || {}
        const timestamp = (msg.timestamp || msg.message_time || 0) * 1000  // WuKongIM 时间戳是秒
        const msgDate = new Date(timestamp)
        
        // WuKongIM 返回的字段是 snake_case 格式（from_uid）
        // 判断发送者类型（使用 imUid 格式比较）
        const msgFromUid = msg.from_uid || msg.fromUid
        const senderType = msgFromUid === imUid.value ? 'user' : 'agent'
        
        console.log('Message from:', msgFromUid, 'imUid:', imUid.value, 'senderType:', senderType)
        
        return {
          id: msg.message_id || msg.messageId || msg.client_msg_no || msg.clientMsgNo,
          senderType,
          msgType: payload.type === 2 ? 'image' : payload.type === 3 ? 'file' : 'text',
          content: payload.content || payload.url || '',
          fileName: payload.fileName || payload.file_name,
          time: formatTime(msgDate),
          date: formatDate(msgDate),
          timestamp
        }
      })
      scrollToBottom()
    }
  } catch (error) {
    console.error('Failed to load history from WuKongIM:', error)
  } finally {
    loading.value = false
  }
}

// IM 事件处理函数（需要保持引用以便移除）
const handleIMConnect = (result: any) => {
  console.log('IM Connected:', result)
  imConnected.value = true
  imConnecting.value = false
}

const handleIMDisconnect = (disconnectInfo: any) => {
  console.log('IM Disconnected:', disconnectInfo.code, disconnectInfo.reason)
  imConnected.value = false
  imConnecting.value = false
}

const handleIMMessage = (message: any) => {
  console.log('IM Message Received:', message)
  
  // 解析消息内容
  const payload = message.payload || {}
  // 使用 imUid ({projectId}_{userId}) 格式判断发送者
  const senderType = message.fromUID?.startsWith('agent_') ? 'agent' : 
                     message.fromUID === imUid.value ? 'user' : 'agent'
  
  // 如果是自己发的消息，跳过（已经通过本地添加）
  if (message.fromUID === imUid.value) {
    return
  }
  
  // 添加接收到的消息
  const now = new Date()
  const newMsg: Message = {
    id: message.messageId || Date.now(),
    senderType: senderType,
    msgType: payload.type === 2 ? 'image' : payload.type === 3 ? 'file' : 'text',
    content: payload.content || payload.url || '',
    fileName: payload.fileName,
    time: formatTime(now),
    date: formatDate(now),
    timestamp: now.getTime()
  }
  
  messages.value.push(newMsg)
  scrollToBottom()
}

const handleIMError = (error: any) => {
  console.error('IM Error:', error.message || error)
  imConnecting.value = false
}

const initIMConnection = async () => {
  if (!currentUser.value || imInstance) return
  
  // 检查是否已有 IM Token（从 user/init 接口获取）
  if (!imToken.value) {
    console.error('No IM token available, cannot connect to IM')
    return
  }
  
  // 检查 imUid 是否已计算好
  if (!imUid.value) {
    console.error('No imUid (projectId_userId) available, cannot connect to IM')
    return
  }
  
  imConnecting.value = true
  
  try {
    // 获取 IM WebSocket 地址（从环境变量或配置）
    const wsUrl = import.meta.env.VITE_WUKONGIM_WS_URL || 'ws://localhost:5200'
    
    // 初始化 SDK（使用已获取的 imToken）
    // uid 必须使用 {projectId}_{userId} 格式，与后端获取 token 时一致
    imInstance = WKIM.init(wsUrl, {
      uid: imUid.value,  // 使用 {projectId}_{userId} 格式
      token: imToken.value,
      deviceFlag: DeviceType.H5  // 连接时使用相同的 deviceFlag
    })
    
    // 注册事件监听
    imInstance.on(WKIMEvent.Connect, handleIMConnect)
    imInstance.on(WKIMEvent.Disconnect, handleIMDisconnect)
    imInstance.on(WKIMEvent.Message, handleIMMessage)
    imInstance.on(WKIMEvent.Error, handleIMError)
    
    // 连接服务器
    await imInstance.connect()
    console.log('IM connection initiated with token from user init')
    
  } catch (error) {
    console.error('Failed to init IM connection:', error)
    imConnecting.value = false
  }
}

// 断开 IM 连接
const disconnectIM = () => {
  if (imInstance) {
    // 移除事件监听
    imInstance.off(WKIMEvent.Connect, handleIMConnect)
    imInstance.off(WKIMEvent.Disconnect, handleIMDisconnect)
    imInstance.off(WKIMEvent.Message, handleIMMessage)
    imInstance.off(WKIMEvent.Error, handleIMError)
    
    imInstance = null
    imConnected.value = false
  }
}

const addMessage = (msg: Partial<Message>) => {
  const now = new Date()
  const newMsg: Message = {
    id: Date.now(),
    senderType: msg.senderType || 'user',
    msgType: msg.msgType || 'text',
    content: msg.content || '',
    fileName: msg.fileName,
    time: formatTime(now),
    date: formatDate(now),
    timestamp: now.getTime()
  }
  messages.value.push(newMsg)
  scrollToBottom()
  return newMsg
}

const sendTextMessage = async () => {
  const text = inputMessage.value.trim()
  if (!text || !imInstance || !imConnected.value) {
    if (!imConnected.value) {
      console.warn('IM not connected, cannot send message')
    }
    return
  }
  
  inputMessage.value = ''
  showEmojiPicker.value = false
  
  // 先在本地显示消息
  addMessage({ senderType: 'user', msgType: 'text', content: text })
  
  // 通过 WuKongIM SDK 发送消息到访客频道
  // Visitor Channel (channel_type=10), channel_id = {projectId}_{userId}
  const visitorChannelId = imUid.value
  if (!visitorChannelId) {
    console.error('No imUid (projectId_userId) for visitor channel')
    return
  }
  
  try {
    const payload = { type: WKChannelType.PERSONAL, content: text }
    // 使用数字 10 作为访客频道类型
    const result = await imInstance.send(visitorChannelId, WKChannelType.VISITOR as any, payload)
    console.log('Message sent to visitor channel:', result)
  } catch (error) {
    console.error('Failed to send message via IM:', error)
  }
}

const insertEmoji = (emoji: string) => {
  inputMessage.value += emoji
}

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
  showMorePanel.value = false
}

const triggerImageUpload = () => {
  imageInput.value?.click()
}

const handleImageUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  
  try {
    const tokenRes = await request.get('/portal/oss/token') as any
    
    if (tokenRes.code === 0 && tokenRes.data) {
      const { accessKeyId, policy, signature, host, dir } = tokenRes.data
      
      const formData = new FormData()
      const key = `${dir}${Date.now()}_${file.name}`
      
      formData.append('key', key)
      formData.append('policy', policy)
      formData.append('OSSAccessKeyId', accessKeyId)
      formData.append('signature', signature)
      formData.append('file', file)
      
      const uploadRes = await fetch(host, { method: 'POST', body: formData })
      
      if (uploadRes.ok) {
        const imageUrl = `${host}/${key}`
        addMessage({ senderType: 'user', msgType: 'image', content: imageUrl })
        
        // 通过 WuKongIM SDK 发送图片消息到访客频道
        if (imInstance && imConnected.value && currentUser.value?.uid) {
          const visitorChannelId = currentUser.value.uid
          const payload = { type: 2, url: imageUrl }
          await imInstance.send(visitorChannelId, WKChannelType.VISITOR as any, payload)
        }
      }
    }
  } catch (error) {
    console.error('Failed to upload image:', error)
    alert('图片上传失败，请重试')
  }
  
  input.value = ''
}

const previewImage = (url: string) => {
  previewImageUrl.value = url
}

const downloadFile = (msg: Message) => {
  if (msg.fileUrl) {
    window.open(msg.fileUrl, '_blank')
  }
}

const submitTicket = () => {
  showMorePanel.value = false
  showTicketDialog.value = true
}

const handleSubmitTicket = async () => {
  if (!ticketForm.value.title || !ticketForm.value.description) {
    alert('请填写工单标题和描述')
    return
  }
  
  try {
    const response = await request.post('/portal/ticket/create', {
      projectId: parseInt(projectId.value),
      userId: currentUser.value?.id,
      title: ticketForm.value.title,
      description: ticketForm.value.description,
      contactInfo: ticketForm.value.contactInfo,
      priority: ticketForm.value.priority
    }) as any
    
    if (response.code === 0) {
      alert('工单提交成功！我们会尽快处理。')
      showTicketDialog.value = false
      
      const title = ticketForm.value.title
      ticketForm.value = { title: '', description: '', contactInfo: '', priority: 'medium' }
      
      addMessage({
        senderType: 'system',
        msgType: 'text',
        content: `您已成功提交工单【${title}】，工单号：${response.data.id}`
      })
    }
  } catch (error) {
    console.error('Failed to submit ticket:', error)
    alert('工单提交失败，请重试')
  }
}

const viewFAQ = () => {
  showMorePanel.value = false
  router.push(`/portal?project_id=${projectId.value}`)
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const handleClickOutside = () => {
  if (showEmojiPicker.value || showMorePanel.value) {
    showEmojiPicker.value = false
    showMorePanel.value = false
  }
}

onMounted(async () => {
  document.addEventListener('click', handleClickOutside)
  await initUser()
  await initConversation()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  // 断开 IM 连接
  disconnectIM()
})
</script>

<style scoped lang="css">
.h5-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f5f5f5;
  position: relative;
}

/* IM 连接状态指示器 */
.im-status {
  position: absolute;
  top: 8px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 12px;
  z-index: 10;
}

.im-status .status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ff4d4f;
}

.im-status.connecting .status-dot {
  background: #faad14;
  animation: pulse 1s infinite;
}

.im-status.connected .status-dot {
  background: #52c41a;
}

.im-status .status-text {
  color: #fff;
  font-size: 11px;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  padding-bottom: 40px;
}

.date-divider {
  text-align: center;
  color: #999;
  font-size: 12px;
  margin: 16px 0;
}

.msg-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 8px;
}

.msg-user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.msg-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 4px;
  object-fit: cover;
}

.msg-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.msg-user .msg-content {
  align-items: flex-end;
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 8px;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.5;
}

.msg-agent .msg-bubble {
  background-color: #fff;
  color: #333;
  border-top-left-radius: 2px;
}

.msg-user .msg-bubble {
  background-color: #1890ff;
  color: white;
  border-top-right-radius: 2px;
}

.msg-bubble.image-bubble {
  padding: 4px;
  background: transparent;
}

.msg-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  cursor: pointer;
}

.msg-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.file-msg {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.file-icon {
  font-size: 20px;
}

.file-name {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.loading-tip {
  text-align: center;
  color: #999;
  font-size: 12px;
  padding: 16px;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid #ddd;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 8px;
  vertical-align: middle;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.watermark {
  position: absolute;
  bottom: 70px;
  left: 50%;
  transform: translateX(-50%);
  color: #ccc;
  font-size: 12px;
  pointer-events: none;
}

.input-section {
  background-color: #fff;
  border-top: 1px solid #e8e8e8;
  padding: 8px 12px;
  padding-bottom: env(safe-area-inset-bottom, 8px);
}

.input-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tool-btn {
  width: 36px;
  height: 36px;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tool-btn:active {
  background-color: #f0f0f0;
}

.tool-icon {
  font-size: 22px;
}

.msg-input {
  flex: 1;
  height: 36px;
  border: 1px solid #e8e8e8;
  border-radius: 18px;
  padding: 0 16px;
  font-size: 14px;
  outline: none;
  background-color: #f5f5f5;
}

.msg-input:focus {
  border-color: #1890ff;
  background-color: #fff;
}

.send-btn {
  min-width: 56px;
  height: 36px;
  background-color: #e0e0e0;
  border: none;
  border-radius: 18px;
  color: #999;
  font-size: 14px;
  cursor: not-allowed;
}

.send-btn.active {
  background-color: #1890ff;
  color: white;
  cursor: pointer;
}

.send-btn.active:active {
  background-color: #0050b3;
}

.emoji-picker {
  position: absolute;
  bottom: 60px;
  left: 12px;
  right: 12px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
  padding: 12px;
  z-index: 100;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
}

.emoji-item {
  font-size: 24px;
  text-align: center;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
}

.emoji-item:active {
  background-color: #f0f0f0;
}

.more-panel {
  position: absolute;
  bottom: 60px;
  left: 12px;
  right: 12px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
  padding: 16px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  z-index: 100;
}

.more-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 12px;
  border-radius: 8px;
}

.more-item:active {
  background-color: #f0f0f0;
}

.more-icon {
  font-size: 28px;
}

.more-item span:last-child {
  font-size: 12px;
  color: #666;
}

.ticket-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 20px;
}

.ticket-dialog {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 400px;
  max-height: 80vh;
  overflow-y: auto;
}

.ticket-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.ticket-header h3 {
  margin: 0;
  font-size: 16px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #999;
  cursor: pointer;
}

.ticket-form {
  padding: 16px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}

.required {
  color: #f56c6c;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  border-color: #1890ff;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.submit-ticket-btn {
  width: 100%;
  height: 44px;
  background-color: #1890ff;
  border: none;
  border-radius: 6px;
  color: white;
  font-size: 16px;
  cursor: pointer;
}

.submit-ticket-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.submit-ticket-btn:not(:disabled):active {
  background-color: #0050b3;
}

.image-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 300;
}

.preview-image {
  max-width: 95%;
  max-height: 95%;
  object-fit: contain;
}
</style>
