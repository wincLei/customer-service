<template>
  <div class="web-chat">
    <div class="chat-messages">
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="message"
        :class="{ 'message-user': msg.sender === 'user', 'message-agent': msg.sender === 'agent' }"
      >
        <div class="message-bubble">
          <div class="message-content">{{ msg.text }}</div>
          <div class="message-time">{{ msg.time }}</div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        placeholder="输入消息，Ctrl+Enter 发送..."
        :rows="3"
        @keyup.enter.ctrl="sendMessage"
      ></el-input>
      <div class="input-actions">
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const inputMessage = ref('')
const messages = ref([
  { id: 1, sender: 'agent', text: '您好，欢迎咨询我们！请问有什么可以帮助您的？', time: '14:30' },
])

const sendMessage = () => {
  if (!inputMessage.value.trim()) return
  
  messages.value.push({
    id: messages.value.length + 1,
    sender: 'user',
    text: inputMessage.value,
    time: new Date().toLocaleTimeString(),
  })
  inputMessage.value = ''
  
  // TODO: 发送消息到后端和 WuKongIM
}
</script>

<style scoped lang="css">
.web-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f5f5f5;
}

.message {
  display: flex;
  margin-bottom: 12px;
}

.message-user {
  justify-content: flex-end;
}

.message-agent {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 8px;
}

.message-user .message-bubble {
  background-color: #1890ff;
  color: white;
}

.message-agent .message-bubble {
  background-color: white;
  color: #333;
  border: 1px solid #e8e8e8;
}

.message-content {
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.7;
}

.chat-input {
  background: white;
  padding: 12px;
  border-top: 1px solid #e8e8e8;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
  gap: 8px;
}
</style>
