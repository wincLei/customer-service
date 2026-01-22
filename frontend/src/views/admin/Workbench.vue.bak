<template>
  <div class="workbench">
    <el-container>
      <!-- 左侧：会话列表 -->
      <el-aside width="300px" class="conversation-list">
        <div class="list-header">
          <h3>会话列表</h3>
          <el-button-group>
            <el-button
              v-for="tab in tabs"
              :key="tab.value"
              :type="activeTab === tab.value ? 'primary' : 'default'"
              size="small"
              @click="activeTab = tab.value"
            >
              {{ tab.label }}
            </el-button>
          </el-button-group>
        </div>

        <div class="conversation-items">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            class="conversation-item"
            :class="{ active: selectedConversation?.id === conv.id }"
            @click="selectConversation(conv)"
          >
            <div class="item-header">
              <img :src="conv.userAvatar" :alt="conv.userName" class="avatar" />
              <div class="item-info">
                <div class="item-name">{{ conv.userName }}</div>
                <div class="item-time">{{ conv.timestamp }}</div>
              </div>
              <span v-if="conv.unread > 0" class="unread-badge">{{ conv.unread }}</span>
            </div>
            <div class="item-message">{{ conv.lastMessage }}</div>
          </div>
        </div>
      </el-aside>

      <!-- 中间：聊天窗口 -->
      <el-container class="chat-container">
        <!-- 聊天头部 -->
        <div class="chat-header" v-if="selectedConversation">
          <div class="header-left">
            <img :src="selectedConversation.userAvatar" :alt="selectedConversation.userName" class="avatar" />
            <div class="header-info">
              <h3>{{ selectedConversation.userName }}</h3>
              <span class="status">{{ selectedConversation.status }}</span>
            </div>
          </div>
          <div class="header-actions">
            <el-button type="default" size="small">转接</el-button>
            <el-button type="primary" size="small">标签</el-button>
          </div>
        </div>

        <!-- 消息区域 -->
        <div class="messages-container" v-if="selectedConversation">
          <div
            v-for="msg in selectedConversation.messages"
            :key="msg.id"
            class="message"
            :class="{ 'message-user': msg.sender === 'user', 'message-agent': msg.sender === 'agent' }"
          >
            <img v-if="msg.sender === 'user'" :src="selectedConversation.userAvatar" class="msg-avatar" />
            <div class="message-content">
              <div class="message-text">{{ msg.text }}</div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-container" v-if="selectedConversation">
          <div class="toolbar">
            <el-button type="text" icon="😊">表情</el-button>
            <el-button type="text" icon="📁">文件</el-button>
            <el-button type="text" icon="🖼️">图片</el-button>
          </div>
          <div class="input-area">
            <el-input
              v-model="messageInput"
              type="textarea"
              placeholder="输入消息..."
              :rows="3"
              @keyup.enter.ctrl="sendMessage"
            ></el-input>
            <el-button type="primary" @click="sendMessage">发送</el-button>
          </div>
        </div>
      </el-container>

      <!-- 右侧：用户信息 -->
      <el-aside width="350px" class="user-info-panel" v-if="selectedConversation">
        <el-tabs>
          <el-tab-pane label="用户资料">
            <div class="info-section">
              <div class="info-item">
                <span class="info-label">用户昵称</span>
                <span class="info-value">{{ selectedConversation.userName }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">设备类型</span>
                <span class="info-value">{{ selectedConversation.deviceType }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">IP归属地</span>
                <span class="info-value">{{ selectedConversation.city }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">用户标签</span>
                <div class="tags">
                  <el-tag v-for="tag in selectedConversation.tags" :key="tag">{{ tag }}</el-tag>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="知识库">
            <el-input placeholder="搜索文章..." size="small"></el-input>
            <div class="kb-articles">
              <div v-for="article in kbArticles" :key="article.id" class="kb-article">
                <div class="article-title">{{ article.title }}</div>
                <div class="article-excerpt">{{ article.excerpt }}</div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="快捷回复">
            <div class="quick-replies">
              <div v-for="reply in quickReplies" :key="reply.id" class="quick-reply" @click="useQuickReply(reply)">
                {{ reply.content }}
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-aside>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const tabs = [
  { label: '全部', value: 'all' },
  { label: '排队中', value: 'queued' },
  { label: '我的', value: 'mine' },
]

const activeTab = ref('all')
const messageInput = ref('')

const conversations = ref([
  {
    id: 1,
    userName: '李先生',
    userAvatar: 'https://via.placeholder.com/40',
    lastMessage: '请问这个产品有优惠吗？',
    timestamp: '14:30',
    unread: 2,
    status: '在线',
    deviceType: '移动端',
    city: '北京',
    tags: ['意向客户', '高价值'],
    messages: [
      { id: 1, sender: 'user', text: '你好，请问这个产品有优惠吗？', time: '14:30' },
      { id: 2, sender: 'agent', text: '您好，我们目前有八折优惠', time: '14:31' },
    ],
  },
])

const selectedConversation = ref(conversations.value[0])

const kbArticles = ref([
  { id: 1, title: '如何使用优惠券', excerpt: '优惠券可在结账时使用...' },
  { id: 2, title: '产品保修政策', excerpt: '我们提供24个月的保修...' },
])

const quickReplies = ref([
  { id: 1, content: '感谢您的咨询，我们稍后会联系您' },
  { id: 2, content: '您的订单已接收，我们会尽快处理' },
])

const selectConversation = (conv: any) => {
  selectedConversation.value = conv
}

const sendMessage = () => {
  if (!messageInput.value.trim()) return
  
  if (selectedConversation.value) {
    selectedConversation.value.messages.push({
      id: selectedConversation.value.messages.length + 1,
      sender: 'agent',
      text: messageInput.value,
      time: new Date().toLocaleTimeString(),
    })
    messageInput.value = ''
  }
}

const useQuickReply = (reply: any) => {
  messageInput.value = reply.content
}
</script>

<style scoped lang="css">
.workbench {
  height: calc(100vh - 100px);
  display: flex;
}

.conversation-list {
  background: white;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.list-header h3 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #333;
}

.conversation-items {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  padding: 12px 8px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.conversation-item:hover {
  background-color: #f5f7fa;
}

.conversation-item.active {
  background-color: #e6f7ff;
  border-left: 3px solid #1890ff;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.item-time {
  font-size: 12px;
  color: #999;
}

.unread-badge {
  background-color: #ff4d4f;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 12px;
}

.item-message {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 48px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  background: white;
  border-bottom: 1px solid #e8e8e8;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-info h3 {
  margin: 0;
  font-size: 14px;
  color: #333;
}

.status {
  font-size: 12px;
  color: #52c41a;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f5f7fa;
}

.message {
  display: flex;
  margin-bottom: 16px;
  gap: 8px;
}

.message-user {
  justify-content: flex-start;
}

.message-agent {
  justify-content: flex-end;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.message-content {
  max-width: 60%;
}

.message-text {
  padding: 8px 12px;
  border-radius: 4px;
  word-break: break-word;
}

.message-user .message-text {
  background-color: white;
  color: #333;
}

.message-agent .message-text {
  background-color: #1890ff;
  color: white;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.input-container {
  background: white;
  border-top: 1px solid #e8e8e8;
  padding: 12px 16px;
}

.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.input-area {
  display: flex;
  gap: 8px;
}

.user-info-panel {
  background: white;
  border-left: 1px solid #e8e8e8;
  overflow-y: auto;
}

.info-section {
  padding: 12px 0;
}

.info-item {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #999;
}

.info-value {
  font-size: 13px;
  color: #333;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.kb-articles {
  padding: 12px 0;
}

.kb-article {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.kb-article:hover {
  background-color: #f5f7fa;
}

.article-title {
  font-size: 13px;
  color: #1890ff;
  font-weight: 500;
}

.article-excerpt {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.quick-replies {
  padding: 12px 0;
}

.quick-reply {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
  font-size: 13px;
  color: #333;
}

.quick-reply:hover {
  background-color: #f5f7fa;
}
</style>
