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

const route = useRoute()
const showEmojiPicker = ref(false)
const inputMessage = ref('')
const messages = ref([
  { id: 1, sender: 'agent', text: '您好，请问有什么需要帮助的？', time: '14:30' },
])

onMounted(() => {
  // TODO: 解析 URL 参数并初始化用户信息
  const params = {
    token: route.query.token,
    projectId: route.query.project_id,
    uid: route.query.uid,
    avatar: route.query.avatar,
    nickName: route.query.nick_name,
    phone: route.query.phone,
    deviceType: route.query.device_type,
  }
  console.log('User params:', params)
  
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
