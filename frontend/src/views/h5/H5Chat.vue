<template>
  <div class="h5-chat">
    <div class="messages">
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="msg-item"
        :class="{ 'msg-user': msg.sender === 'user', 'msg-agent': msg.sender === 'agent' }"
      >
        <div class="msg-bubble">{{ msg.text }}</div>
        <div class="msg-time">{{ msg.time }}</div>
      </div>
    </div>

    <div class="input-section">
      <div class="input-tools">
        <button class="tool-btn" @click="showEmojiPicker = !showEmojiPicker">😊</button>
        <button class="tool-btn" @click="uploadFile">📁</button>
        <button class="tool-btn" @click="uploadImage">🖼️</button>
      </div>

      <div class="input-area">
        <input
          v-model="inputMessage"
          type="text"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
          class="msg-input"
        />
        <button class="send-btn" @click="sendMessage">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api'

const route = useRoute()
const showEmojiPicker = ref(false)
const inputMessage = ref('')
const messages = ref([
  { id: 1, sender: 'agent', text: '您好，请问有什么需要帮助的？', time: '14:30' },
])

// 游客用户 UID 的 localStorage Key
const GUEST_UID_KEY = 'mini_cs_guest_uid'

// 用户信息
interface UserInfo {
  uid: string
  externalUid?: string
  nickname?: string
  avatar?: string
  phone?: string
  projectId: string
  isGuest: boolean
}

const currentUser = ref<UserInfo | null>(null)

/**
 * 生成游客 UID
 */
const generateGuestUid = (): string => {
  return 'guest_' + Date.now() + '_' + Math.random().toString(36).substring(2, 11)
}

/**
 * 获取或创建游客 UID
 */
const getOrCreateGuestUid = (projectId: string): string => {
  const key = `${GUEST_UID_KEY}_${projectId}`
  let guestUid = localStorage.getItem(key)
  if (!guestUid) {
    guestUid = generateGuestUid()
    localStorage.setItem(key, guestUid)
  }
  return guestUid
}

/**
 * 初始化或合并用户
 */
const initUser = async (params: {
  projectId: string
  externalUid?: string
  nickname?: string
  avatar?: string
  phone?: string
}) => {
  const { projectId, externalUid, nickname, avatar, phone } = params
  
  // 获取本地存储的游客 UID
  const guestUid = getOrCreateGuestUid(projectId)
  
  try {
    // 调用后端 API 初始化/合并用户
    const response = await request.post('/api/portal/user/init', {
      projectId,
      guestUid,
      externalUid,
      nickname,
      avatar,
      phone
    })
    
    const userData = response.data
    
    // 如果返回了新的 UID（合并后），更新 localStorage
    if (userData.uid && userData.uid !== guestUid) {
      const key = `${GUEST_UID_KEY}_${projectId}`
      localStorage.setItem(key, userData.uid)
    }
    
    currentUser.value = {
      uid: userData.uid,
      externalUid: userData.externalUid,
      nickname: userData.nickname,
      avatar: userData.avatar,
      phone: userData.phone,
      projectId,
      isGuest: userData.isGuest
    }
    
    console.log('User initialized:', currentUser.value)
    
    // 如果发生了合并，可以加载历史消息
    if (userData.merged) {
      console.log('User merged from guest to registered user')
      // TODO: 加载合并后的历史消息
    }
    
  } catch (error) {
    console.error('Failed to init user:', error)
    // 失败时使用本地游客身份
    currentUser.value = {
      uid: guestUid,
      projectId,
      isGuest: true
    }
  }
}

onMounted(async () => {
  // 解析 URL 参数
  const projectId = route.query.project_id as string || route.query.projectId as string
  
  if (!projectId) {
    console.error('Missing project_id parameter')
    return
  }
  
  const params = {
    projectId,
    externalUid: route.query.uid as string || route.query.external_uid as string,
    avatar: route.query.avatar as string,
    nickname: route.query.nick_name as string || route.query.nickname as string,
    phone: route.query.phone as string,
  }
  
  console.log('URL params:', params)
  
  // 初始化用户
  await initUser(params)
  
  // TODO: 初始化 WuKongIM 连接
})

const sendMessage = () => {
  if (!inputMessage.value.trim()) return
  
  messages.value.push({
    id: messages.value.length + 1,
    sender: 'user',
    text: inputMessage.value,
    time: new Date().toLocaleTimeString(),
  })
  inputMessage.value = ''
}

const uploadFile = () => {
  // TODO: 实现文件上传
}

const uploadImage = () => {
  // TODO: 实现图片上传
}
</script>

<style scoped lang="css">
.h5-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #fff;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.msg-item {
  display: flex;
  flex-direction: column;
}

.msg-user {
  align-items: flex-end;
}

.msg-agent {
  align-items: flex-start;
}

.msg-bubble {
  max-width: 80%;
  padding: 8px 12px;
  border-radius: 4px;
  word-break: break-word;
  font-size: 14px;
}

.msg-user .msg-bubble {
  background-color: #1890ff;
  color: white;
}

.msg-agent .msg-bubble {
  background-color: #f0f0f0;
  color: #333;
}

.msg-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.input-section {
  background-color: #f5f5f5;
  border-top: 1px solid #e8e8e8;
  padding: 8px;
}

.input-tools {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.tool-btn {
  flex: 1;
  background-color: white;
  border: 1px solid #e8e8e8;
  padding: 8px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.input-area {
  display: flex;
  gap: 8px;
}

.msg-input {
  flex: 1;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 8px 12px;
  font-size: 14px;
  outline: none;
}

.msg-input:focus {
  border-color: #1890ff;
}

.send-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.send-btn:active {
  background-color: #0050b3;
}
</style>
