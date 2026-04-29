<template>
  <div class="h5-chat">
    <!-- IM 连接状态指示器 -->
    <div
      class="im-status"
      :class="{ connected: imConnected, connecting: imConnecting }"
    >
      <span class="status-dot"></span>
      <span class="status-text">{{
        imConnected ? $t('h5chat.connected') : imConnecting ? $t('h5chat.connecting') : $t('h5chat.disconnected')
      }}</span>
    </div>

    <!-- 消息列表区域 -->
    <div
      class="messages"
      ref="messagesContainer"
      @scroll="handleMessagesScroll"
    >
      <!-- 加载更多提示 -->
      <div v-if="loadingMoreMessages" class="loading-more">
        <span class="loading-spinner"></span>
        {{ $t('h5chat.loadMore') }}
      </div>
      <div
        v-else-if="!hasMoreMessages && messages.length > 0"
        class="no-more-messages"
      >
        {{ $t('h5chat.noMoreMessages') }}
      </div>

      <!-- 日期分隔线 -->
      <template v-for="(group, index) in groupedMessages" :key="index">
        <div class="date-divider">{{ group.date }}</div>
        <div
          v-for="msg in group.messages"
          :key="msg.id"
          class="msg-item"
          :class="{
            'msg-user': msg.senderType === 'user',
            'msg-agent': msg.senderType === 'agent',
          }"
        >
          <!-- 客服头像（左侧） -->
          <div v-if="msg.senderType === 'agent'" class="msg-avatar">
            <img :src="agentAvatar" :alt="$t('h5chat.agentAvatar')" />
          </div>

          <!-- 消息内容 -->
          <div class="msg-content">
            <div
              class="msg-bubble"
              :class="{ 'image-bubble': msg.msgType === 'image' }"
            >
              <!-- 文本消息 -->
              <template v-if="msg.msgType === 'text'">
                {{ msg.content }}
              </template>
              <!-- 图片消息 -->
              <template v-else-if="msg.msgType === 'image'">
                <img
                  :src="msg.content"
                  @click="previewImage(msg.content)"
                  class="msg-image"
                />
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
            <img :src="userAvatar" :alt="$t('h5chat.userAvatar')" />
          </div>
        </div>
      </template>

      <!-- 加载中提示 -->
      <div v-if="loading" class="loading-tip">
        <span class="loading-spinner"></span>
        {{ $t('h5chat.messageLoading') }}
      </div>
    </div>

    <!-- 底部 Logo 水印 -->
    <div class="watermark">{{ $t('h5chat.brand') }}</div>

    <!-- 输入区域 -->
    <div class="input-section">
      <!-- 工具栏 -->
      <div class="input-toolbar">
        <button class="toolbar-btn" @click="triggerImageUpload">
          <span class="toolbar-icon">🖼️</span>
          <span class="toolbar-text">{{ $t('h5chat.imageTool') }}</span>
        </button>
        <input
          ref="imageInput"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleImageUpload"
        />
        
        <button class="toolbar-btn" @click="submitTicket">
          <span class="toolbar-icon">📝</span>
          <span class="toolbar-text">{{ $t('h5chat.submitTicket') }}</span>
        </button>
        
        <button class="toolbar-btn" @click="openMyTickets">
          <span class="toolbar-icon">📋</span>
          <span class="toolbar-text">{{ $t('h5chat.myTickets') }}</span>
          <span v-if="hasUnreadTicketReply" class="unread-dot"></span>
        </button>
        
        <button class="toolbar-btn" @click.stop="toggleEmojiPicker">
          <span class="toolbar-icon">😊</span>
          <span class="toolbar-text">{{ $t('h5chat.emoji') }}</span>
        </button>
      </div>
      
      <!-- 频率限制提示 -->
      <div v-if="rateLimitTip" class="rate-limit-tip">{{ rateLimitTip }}</div>
      
      <!-- 输入行 -->
      <div class="input-row">
        <input
          v-model="inputMessage"
          type="text"
          :placeholder="$t('h5chat.inputPlaceholder')"
          :maxlength="MAX_CHARS_PER_MESSAGE"
          @keyup.enter="sendTextMessage"
          class="msg-input"
        />
        <span v-if="inputMessage.length > MAX_CHARS_PER_MESSAGE * 0.8" class="char-count" :class="{ warning: inputMessage.length >= MAX_CHARS_PER_MESSAGE }">
          {{ inputMessage.length }}/{{ MAX_CHARS_PER_MESSAGE }}
        </span>
        <button
          class="send-btn"
          :class="{ active: inputMessage.trim() }"
          :disabled="!inputMessage.trim()"
          @click="sendTextMessage"
        >
          {{ $t('h5chat.send') }}
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
        <span>{{ $t('h5chat.submitTicket') }}</span>
      </div>
      <div class="more-item" @click="viewFAQ">
        <span class="more-icon">❓</span>
        <span>{{ $t('h5chat.faq') }}</span>
      </div>
    </div>

    <!-- 工单提交弹窗 -->
    <div
      v-if="showTicketDialog"
      class="ticket-dialog-overlay"
      @click.self="showTicketDialog = false"
    >
      <div class="ticket-dialog">
        <div class="ticket-header">
          <h3>{{ $t('h5chat.submitTicket') }}</h3>
          <button class="close-btn" @click="showTicketDialog = false">✕</button>
        </div>
        <div class="ticket-form">
          <div class="form-group">
            <label>{{ $t('h5chat.ticketTitle') }} <span class="required">*</span></label>
            <input v-model="ticketForm.title" :placeholder="$t('h5chat.ticketTitlePlaceholder')" />
          </div>
          <div class="form-group">
            <label>{{ $t('h5chat.ticketDescription') }} <span class="required">*</span></label>
            <textarea
              v-model="ticketForm.description"
              :placeholder="$t('h5chat.ticketDescriptionPlaceholder')"
              rows="4"
            ></textarea>
          </div>
          <!-- 自动回复关键词匹配提示 -->
          <div v-if="matchedAutoReplies.length > 0" class="auto-reply-hint">
            <div class="auto-reply-hint-header">
              <span class="hint-icon">💡</span>
              <span>{{ $t('h5chat.autoReplySuggestTitle') }}</span>
            </div>
            <div
              v-for="(reply, idx) in matchedAutoReplies"
              :key="idx"
              class="auto-reply-hint-item"
            >{{ reply }}</div>
          </div>

          <div class="form-group">
            <label>{{ $t('h5chat.ticketContact') }}</label>
            <input
              v-model="ticketForm.contactInfo"
              :placeholder="$t('h5chat.ticketContactPlaceholder')"
            />
          </div>
          <div class="form-group">
            <label>{{ $t('h5chat.ticketPriority') }}</label>
            <select v-model="ticketForm.priority">
              <option value="low">{{ $t('ticket.priority.low') }}</option>
              <option value="medium">{{ $t('ticket.priority.medium') }}</option>
              <option value="high">{{ $t('ticket.priority.high') }}</option>
              <option value="urgent">{{ $t('ticket.priority.urgent') }}</option>
            </select>
          </div>
          <button
            class="submit-ticket-btn"
            @click="handleSubmitTicket"
            :disabled="!ticketForm.title || !ticketForm.description"
          >
            {{ $t('h5chat.submitTicket') }}
          </button>
        </div>
      </div>
    </div>

    <!-- 我的工单列表弹窗 -->
    <div
      v-if="showMyTickets"
      class="ticket-dialog-overlay"
      @click.self="showMyTickets = false"
    >
      <div class="ticket-dialog ticket-list-dialog">
        <div class="ticket-header">
          <h3>{{ $t('h5chat.myTickets') }}</h3>
          <button class="close-btn" @click="showMyTickets = false">✕</button>
        </div>
        <div class="ticket-list-content" v-if="!loadingTickets">
          <div v-if="myTickets.length > 0" class="ticket-list">
            <div 
              v-for="ticket in myTickets" 
              :key="ticket.id" 
              class="ticket-list-item"
              @click="openTicketDetail(ticket)"
            >
              <div class="ticket-item-header">
                <span class="ticket-item-title">{{ ticket.title }}</span>
                <span v-if="ticket.hasNewReply" class="new-reply-badge">{{ $t('h5chat.newReply') }}</span>
              </div>
              <div class="ticket-item-tags">
                <span class="ticket-tag" :class="'status-' + ticket.status">
                  {{ getStatusLabel(ticket.status) }}
                </span>
                <span class="ticket-tag" :class="'priority-' + ticket.priority">
                  {{ getPriorityLabel(ticket.priority) }}
                </span>
              </div>
              <div class="ticket-item-time">
                {{ formatTicketTime(ticket.createdAt) }}
              </div>
            </div>
          </div>
          <div v-else class="empty-tickets">
            <span class="empty-icon">📭</span>
            <span>{{ $t('h5chat.noTickets') }}</span>
          </div>
        </div>
        <div v-else class="loading-tickets">
          <span class="loading-spinner"></span>
          {{ $t('h5chat.loadMore') }}
        </div>
      </div>
    </div>

    <!-- 工单详情弹窗 -->
    <div
      v-if="showTicketDetail"
      class="ticket-dialog-overlay"
      @click.self="showTicketDetail = false"
    >
      <div class="ticket-dialog ticket-detail-dialog">
        <div class="ticket-header">
          <button class="back-btn" @click="backToTicketList">← {{ $t('h5chat.back') }}</button>
          <h3>{{ $t('h5chat.ticketDetail') }}</h3>
          <button class="close-btn" @click="showTicketDetail = false">✕</button>
        </div>
        <div class="ticket-detail-content" v-if="currentTicketDetail">
          <!-- 工单基本信息 -->
          <div class="ticket-info-card">
            <div class="ticket-detail-title">{{ currentTicketDetail.ticket.title }}</div>
            <div class="ticket-detail-tags">
              <span class="ticket-tag" :class="'status-' + currentTicketDetail.ticket.status">
                {{ getStatusLabel(currentTicketDetail.ticket.status) }}
              </span>
              <span class="ticket-tag" :class="'priority-' + currentTicketDetail.ticket.priority">
                {{ getPriorityLabel(currentTicketDetail.ticket.priority) }}
              </span>
            </div>
            <div class="ticket-detail-desc">{{ currentTicketDetail.ticket.description }}</div>
            <div class="ticket-detail-time">
              {{ $t('h5chat.createdTime') }}{{ formatTicketTime(currentTicketDetail.ticket.createdAt) }}
            </div>
          </div>
          
          <!-- 回复记录 -->
          <div class="ticket-events">
            <div class="events-title">{{ $t('h5chat.processHistory') }}</div>
            <div v-if="currentTicketDetail.events.length > 0" class="events-list">
              <div 
                v-for="event in currentTicketDetail.events" 
                :key="event.id" 
                class="event-item"
                :class="{ 'event-agent': event.operatorType === 'agent', 'event-user': event.operatorType === 'user', 'event-system': event.operatorType === 'system' }"
              >
                <div class="event-header">
                  <span class="event-sender">{{ event.operatorType === 'agent' ? $t('h5chat.agentLabel') : event.operatorType === 'system' ? $t('h5chat.systemLabel') : $t('h5chat.userLabel') }}</span>
                  <span class="event-time">{{ formatTicketTime(event.createdAt) }}</span>
                </div>
                <div class="event-content">{{ event.content }}</div>
              </div>
            </div>
            <div v-else class="no-events">{{ $t('h5chat.noProcessHistory') }}</div>
          </div>
          
          <!-- 回复输入框 -->
          <div class="ticket-reply-section" v-if="currentTicketDetail.ticket.status !== 'closed'">
            <textarea 
              v-model="ticketReplyContent" 
              :placeholder="$t('h5chat.replyPlaceholder')"
              rows="3"
            ></textarea>
            <button 
              class="reply-btn" 
              @click="submitTicketReply"
              :disabled="!ticketReplyContent.trim() || submittingReply"
            >
              {{ submittingReply ? $t('h5chat.sending') : $t('h5chat.sendReply') }}
            </button>
          </div>
          <div v-else class="ticket-closed-tip">
            {{ $t('h5chat.ticketClosedTip') }}
          </div>
        </div>
        <div v-else class="loading-tickets">
          <span class="loading-spinner"></span>
          {{ $t('h5chat.loadMore') }}
        </div>
      </div>
    </div>

    <!-- 图片预览弹窗 -->
    <div
      v-if="previewImageUrl"
      class="image-preview-overlay"
      @click="previewImageUrl = ''"
    >
      <img :src="previewImageUrl" class="preview-image" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from "vue";
import { logger } from '@/utils/logger';
import { useRoute, useRouter } from "vue-router";
import { useI18n } from 'vue-i18n';
import portalApi, { setPortalToken } from "@/api/portal";
import { WKIM, WKIMEvent } from "easyjssdk";
import { DeviceType, WKChannelType, StorageKeys, IMPayloadType, IM_INITIAL_LOAD_LIMIT, IM_LOAD_MORE_LIMIT, MAX_CHARS_PER_MESSAGE, MAX_CHARS_PER_MINUTE, RATE_LIMIT_WINDOW, WELCOME_INTERVAL, ALLOWED_IMAGE_TYPES } from "@/constants";

const route = useRoute();
const router = useRouter();
const { t } = useI18n();

// IM 实例
let imInstance: ReturnType<typeof WKIM.init> | null = null;
const imConnected = ref(false);
const imConnecting = ref(false);
const imToken = ref<string | null>(null); // IM Token（从 user/init 接口获取）

// 状态
const loading = ref(false);
const inputMessage = ref("");
const showEmojiPicker = ref(false);
const showMorePanel = ref(false);
const showTicketDialog = ref(false);
const previewImageUrl = ref("");
const messagesContainer = ref<HTMLElement | null>(null);
const imageInput = ref<HTMLInputElement | null>(null);

// 工单相关状态
const showMyTickets = ref(false);
const showTicketDetail = ref(false);
const loadingTickets = ref(false);
const myTickets = ref<any[]>([]);
const currentTicketDetail = ref<any>(null);
const ticketReplyContent = ref("");
const submittingReply = ref(false);
const hasUnreadTicketReply = ref(false);  // 是否有未读工单回复
const TICKET_READ_KEY = StorageKeys.TICKET_READ_PREFIX;  // 工单已读状态本地存储 key

// 分页加载相关状态
const loadingMoreMessages = ref(false);
const hasMoreMessages = ref(true);
const oldestMessageSeq = ref<number>(0); // 当前最早消息的序号

// 消息发送频率限制（常量来自 @/constants/appConfig）
const recentSentChars = ref<{ timestamp: number; count: number }[]>([]); // 最近发送的字数记录
const rateLimitTip = ref(''); // 频率限制提示信息

// 用户信息
interface UserInfo {
  id?: number;
  uid: string;
  externalUid?: string;
  nickname?: string;
  avatar?: string;
  phone?: string;
  projectId: string;
  isGuest: boolean;
}

const currentUser = ref<UserInfo | null>(null);
const projectId = ref("");
const conversationId = ref<number | null>(null);
const agentUid = ref<string | null>(null); // 已分配客服的 UID，用于 Personal Channel 通信

// 计算 WuKongIM 用户 UID，格式: {projectId}_{userId}
// 这个格式与后端保持一致，使用数据库自增 ID 保证全局唯一
const imUid = computed(() => {
  if (currentUser.value?.id && projectId.value) {
    return `${projectId.value}_${currentUser.value.id}`;
  }
  return null;
});

// 客服信息
const agentAvatar = ref(
  `data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%231890ff"/><text x="50" y="60" text-anchor="middle" fill="white" font-size="40">${t('h5chat.agentAvatarChar')}</text></svg>`,
);
const userAvatar = ref(
  `data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%2387ceeb"/><text x="50" y="60" text-anchor="middle" fill="white" font-size="40">${t('h5chat.userAvatarChar')}</text></svg>`,
);

// 消息列表
interface Message {
  id: string | number;
  senderType: "user" | "agent" | "system";
  msgType: "text" | "image" | "file";
  content: string;
  fileName?: string;
  fileUrl?: string;
  time: string;
  date: string;
  timestamp: number;
}

const messages = ref<Message[]>([]);

// 按日期分组消息
const groupedMessages = computed(() => {
  const groups: { date: string; messages: Message[] }[] = [];
  let currentDate = "";

  for (const msg of messages.value) {
    if (msg.date !== currentDate) {
      currentDate = msg.date;
      groups.push({ date: currentDate, messages: [] });
    }
    groups[groups.length - 1].messages.push(msg);
  }

  return groups;
});

// 表情列表
const emojis = [
  "😀",
  "😃",
  "😄",
  "😁",
  "😆",
  "😅",
  "🤣",
  "😂",
  "🙂",
  "😊",
  "😇",
  "🥰",
  "😍",
  "🤩",
  "😘",
  "😗",
  "😋",
  "😛",
  "😜",
  "🤪",
  "😝",
  "🤑",
  "🤗",
  "🤭",
  "🤔",
  "🤐",
  "🤨",
  "😐",
  "😑",
  "😶",
  "😏",
  "😒",
  "🙄",
  "😬",
  "😮",
  "😯",
  "😲",
  "😳",
  "🥺",
  "😦",
  "👍",
  "👎",
  "👌",
  "✌️",
  "🤞",
  "🤟",
  "🤘",
  "🤙",
  "❤️",
  "🧡",
  "💛",
  "💚",
  "💙",
  "💜",
  "🖤",
  "💔",
];

// 工单表单
const ticketForm = ref({
  title: "",
  description: "",
  contactInfo: "",
  priority: "medium",
});

// 自动回复规则（工单表单关键词提示用）
interface AutoReplyRuleSuggestion {
  keywords: string;
  replyContent: string;
}
const autoReplyRules = ref<AutoReplyRuleSuggestion[]>([]);
const matchedAutoReplies = ref<string[]>([]);
let autoReplyDebounceTimer: ReturnType<typeof setTimeout> | null = null;

// 游客 UID 存储 Key
const GUEST_UID_KEY = StorageKeys.GUEST_UID_PREFIX;

const generateGuestUid = (): string => {
  return (
    "guest_" + Date.now() + "_" + Math.random().toString(36).substring(2, 11)
  );
};

const getOrCreateGuestUid = (pid: string): string => {
  const key = `${GUEST_UID_KEY}_${pid}`;
  let guestUid = localStorage.getItem(key);
  if (!guestUid) {
    guestUid = generateGuestUid();
    localStorage.setItem(key, guestUid);
  }
  return guestUid;
};

const formatTime = (date: Date): string => {
  const h = date.getHours().toString().padStart(2, "0");
  const m = date.getMinutes().toString().padStart(2, "0");
  return `${h}:${m}`;
};

const formatDate = (date: Date): string => {
  const month = (date.getMonth() + 1).toString();
  const day = date.getDate().toString();
  const h = date.getHours().toString().padStart(2, "0");
  const m = date.getMinutes().toString().padStart(2, "0");
  return t('h5chat.dateFormat', { month, day, h, m });
};

const initUser = async () => {
  // 从 URL 参数获取配置，支持多种命名风格
  const pid =
    (route.query.project_id as string) ||
    (route.query.projectId as string) ||
    "1";
  projectId.value = pid;

  // 优先使用 URL 传入的 guestUid，否则从本地存储获取或生成新的
  const urlGuestUid =
    (route.query.guestUid as string) || (route.query.guest_uid as string);
  const guestUid = urlGuestUid || getOrCreateGuestUid(pid);

  // 如果 URL 传入了 guestUid，保存到本地存储
  if (urlGuestUid) {
    const key = `${GUEST_UID_KEY}_${pid}`;
    localStorage.setItem(key, urlGuestUid);
  }

  try {
    const response = (await portalApi.post("/portal/user/init", {
      projectId: parseInt(pid),
      guestUid,
      externalUid:
        (route.query.uid as string) ||
        (route.query.external_uid as string) ||
        (route.query.externalUid as string),
      nickname:
        (route.query.nick_name as string) || (route.query.nickname as string),
      avatar: route.query.avatar as string,
      phone: route.query.phone as string,
      deviceFlag: route.query.device_type || DeviceType.H5, // H5 端使用 H5 设备类型
    })) as any;

    if (response.code === 0 && response.data) {
      const userData = response.data;

      if (userData.uid && userData.uid !== guestUid) {
        const key = `${GUEST_UID_KEY}_${pid}`;
        localStorage.setItem(key, userData.uid);
      }

      currentUser.value = {
        id: userData.id,
        uid: userData.uid,
        externalUid: userData.externalUid,
        nickname: userData.nickname,
        avatar: userData.avatar,
        phone: userData.phone,
        projectId: pid,
        isGuest: userData.isGuest,
      };

      // 保存 Portal Token（用于后续接口认证）
      if (userData.portalToken) {
        setPortalToken(userData.portalToken);
        logger.debug("Portal Token saved");
      }

      // 保存 IM Token（从 user/init 接口直接获取，无需单独调用）
      if (userData.imToken) {
        imToken.value = userData.imToken;
        logger.debug("IM Token obtained from user init");
      }

      if (userData.avatar) {
        userAvatar.value = userData.avatar;
      }
    }
  } catch (error) {
    logger.error("Failed to init user:", error);
    currentUser.value = {
      uid: guestUid,
      projectId: pid,
      isGuest: true,
    };
  }
};

const initConversation = async () => {
  if (!currentUser.value?.id) return;

  try {
    const response = (await portalApi.post("/portal/conversation/init", {
      projectId: parseInt(projectId.value),
      userId: currentUser.value.id,
    })) as any;

    if (response.code === 0 && response.data) {
      conversationId.value = response.data.id;

      // 保存已分配客服的 UID（如果有）
      if (response.data.agentUid) {
        agentUid.value = response.data.agentUid;
        logger.debug("Agent assigned:", agentUid.value);
      }

      await loadHistory();

      // 判断是否需要展示欢迎语：新会话 或 距离上次访问超过5分钟
      const storageKey = `${StorageKeys.H5_LAST_VISIT_PREFIX}${projectId.value}_${currentUser.value?.id}`;
      const lastVisit = localStorage.getItem(storageKey);
      const now = Date.now();
      const shouldShowWelcome =
        response.data.isNew ||
        !lastVisit ||
        now - parseInt(lastVisit) > WELCOME_INTERVAL;

      if (shouldShowWelcome) {
        const welcomeMsg =
          response.data.welcomeMessage || t('h5chat.defaultWelcome');
        addMessage({
          senderType: "agent",
          msgType: "text",
          content: welcomeMsg,
        });
      }

      // 更新最后访问时间
      localStorage.setItem(storageKey, now.toString());

      initIMConnection();
    }
  } catch (error) {
    logger.error("Failed to init conversation:", error);
    addMessage({
      senderType: "agent",
      msgType: "text",
      content: t('h5chat.defaultWelcome'),
    });
  }
};

// 解析单条消息
const parseHistoryMessage = (msg: any): Message & { messageSeq?: number } => {
  const payload = msg.payload || {};
  const timestamp = (msg.timestamp || msg.message_time || 0) * 1000;
  const msgDate = new Date(timestamp);
  const msgFromUid = msg.from_uid || msg.fromUid;
  const senderType = msgFromUid === imUid.value ? "user" : "agent";

  return {
    id: msg.message_id || msg.messageId || msg.client_msg_no || msg.clientMsgNo,
    senderType,
    msgType:
      payload.type === IMPayloadType.IMAGE ? "image" : payload.type === IMPayloadType.FILE ? "file" : "text",
    content: payload.content || payload.url || "",
    fileName: payload.fileName || payload.file_name,
    time: formatTime(msgDate),
    date: formatDate(msgDate),
    timestamp,
    messageSeq: msg.message_seq || msg.messageSeq || 0,
  };
};

const loadHistory = async () => {
  // 访客频道: channel_id = {projectId}_{userId}, channel_type = 10
  if (!imUid.value) {
    logger.debug("No imUid (projectId_userId), skip loading history");
    return;
  }

  // 重置分页状态
  hasMoreMessages.value = true;
  oldestMessageSeq.value = 0;

  loading.value = true;
  try {
    // 通过后端代理调用 WuKongIM API 获取历史消息
    // Visitor Channel: channel_id = {projectId}_{userId}, channel_type = 10
    // 首次加载不传 pullMode，默认获取最新消息
    const response = (await portalApi.post("/portal/im/messages/sync", {
      loginUid: imUid.value,
      channelId: imUid.value, // 访客频道 ID = {projectId}_{userId}
      channelType: WKChannelType.VISITOR,
      limit: IM_INITIAL_LOAD_LIMIT,
    })) as any;

    if (response.code === 0 && response.data) {
      const parsedMessages = response.data.map(parseHistoryMessage);
      messages.value = parsedMessages;

      // 记录最早消息的序号，用于分页
      if (parsedMessages.length > 0) {
        const seqs = parsedMessages
          .map((m: any) => m.messageSeq || 0)
          .filter((s: number) => s > 0);
        if (seqs.length > 0) {
          oldestMessageSeq.value = Math.min(...seqs);
        }
        hasMoreMessages.value = parsedMessages.length >= IM_INITIAL_LOAD_LIMIT;
      } else {
        hasMoreMessages.value = false;
      }

      scrollToBottom();
    }
  } catch (error) {
    logger.error("Failed to load history from WuKongIM:", error);
  } finally {
    loading.value = false;
  }
};

// 加载更多历史消息（向上滚动时触发）
const loadMoreMessages = async () => {
  if (!imUid.value || loadingMoreMessages.value || !hasMoreMessages.value) {
    return;
  }

  if (oldestMessageSeq.value <= 1) {
    hasMoreMessages.value = false;
    return;
  }

  loadingMoreMessages.value = true;

  try {
    // pullMode=0 配合 startMessageSeq 向上拉取更旧的消息
    const response = (await portalApi.post("/portal/im/messages/sync", {
      loginUid: imUid.value,
      channelId: imUid.value,
      channelType: WKChannelType.VISITOR,
      startMessageSeq: oldestMessageSeq.value,
      limit: IM_LOAD_MORE_LIMIT,
      pullMode: 0, // 向上拉取更旧消息
    })) as any;

    if (response.code === 0 && response.data && response.data.length > 0) {
      const olderMessages = response.data.map(parseHistoryMessage);

      // 去重：过滤掉已存在的消息
      const existingIds = new Set(messages.value.map((m) => m.id));
      const uniqueOlderMessages = olderMessages.filter(
        (m: any) => !existingIds.has(m.id),
      );

      if (uniqueOlderMessages.length === 0) {
        hasMoreMessages.value = false;
        return;
      }

      // 保存当前滚动位置
      const container = messagesContainer.value;
      const previousScrollHeight = container?.scrollHeight || 0;

      // 将旧消息添加到列表前面
      messages.value = [...uniqueOlderMessages, ...messages.value];

      // 更新最早消息序号 - 使用新加载的消息中的最小序号
      const seqs = uniqueOlderMessages
        .map((m: any) => m.messageSeq || 0)
        .filter((s: number) => s > 0);
      if (seqs.length > 0) {
        const newOldestSeq = Math.min(...seqs);
        // 确保序号在减小，防止重复请求
        if (newOldestSeq < oldestMessageSeq.value) {
          oldestMessageSeq.value = newOldestSeq;
        } else {
          hasMoreMessages.value = false;
        }
      }

      hasMoreMessages.value = olderMessages.length >= 30;

      // 恢复滚动位置，保持用户当前查看的消息不动
      await nextTick();
      if (container) {
        const newScrollHeight = container.scrollHeight;
        container.scrollTop = newScrollHeight - previousScrollHeight;
      }
    } else {
      hasMoreMessages.value = false;
    }
  } catch (error) {
    logger.error("Failed to load more messages:", error);
  } finally {
    loadingMoreMessages.value = false;
  }
};

// 处理消息列表滚动事件
const handleMessagesScroll = () => {
  const container = messagesContainer.value;
  if (!container) return;

  // 当滚动到顶部附近时（距离顶部 50px 内），加载更多消息
  if (container.scrollTop < 50) {
    loadMoreMessages();
  }
};

// IM 事件处理函数（需要保持引用以便移除）
const handleIMConnect = (result: any) => {
  logger.info("IM Connected:", result);
  imConnected.value = true;
  imConnecting.value = false;
};

const handleIMDisconnect = (disconnectInfo: any) => {
  logger.info("IM Disconnected:", disconnectInfo.code, disconnectInfo.reason);
  imConnected.value = false;
  imConnecting.value = false;
};

const handleIMMessage = (message: any) => {
  logger.debug("IM Message Received:", message);

  // 解析消息内容
  const payload = message.payload || {};
  // 使用 imUid ({projectId}_{userId}) 格式判断发送者
  const senderType = message.fromUID?.startsWith("agent_")
    ? "agent"
    : message.fromUID === imUid.value
      ? "user"
      : "agent";

  // 如果是自己发的消息，跳过（已经通过本地添加）
  if (message.fromUID === imUid.value) {
    return;
  }

  // 根据 messageId 去重，避免重复显示
  const messageId =
    message.messageId || message.messageID || message.message_id;
  if (
    messageId &&
    messages.value.some((m) => m.id === messageId || m.id === String(messageId))
  ) {
    logger.debug("Duplicate message ignored:", messageId);
    return;
  }

  // 添加接收到的消息
  const now = new Date();
  const newMsg: Message = {
    id: messageId || Date.now(),
    senderType: senderType,
    msgType:
      payload.type === IMPayloadType.IMAGE ? "image" : payload.type === IMPayloadType.FILE ? "file" : "text",
    content: payload.content || payload.url || "",
    fileName: payload.fileName,
    time: formatTime(now),
    date: formatDate(now),
    timestamp: now.getTime(),
  };

  messages.value.push(newMsg);
  scrollToBottom();
};

const handleIMError = (error: any) => {
  logger.error("IM Error:", error.message || error);
  imConnecting.value = false;
};

const initIMConnection = async () => {
  if (!currentUser.value || imInstance) return;

  // 检查是否已有 IM Token（从 user/init 接口获取）
  if (!imToken.value) {
    logger.error("No IM token available, cannot connect to IM");
    return;
  }

  // 检查 imUid 是否已计算好
  if (!imUid.value) {
    logger.error(
      "No imUid (projectId_userId) available, cannot connect to IM",
    );
    return;
  }

  imConnecting.value = true;

  try {
    // 获取 IM WebSocket 地址（从环境变量或配置）
    // 优先使用运行时配置，其次使用构建时环境变量，最后使用默认值
    const wsUrl = (window as any).__RUNTIME_CONFIG__?.WUKONGIM_WS_URL || import.meta.env.VITE_WUKONGIM_WS_URL || "ws://localhost:5200";

    // 初始化 SDK（使用已获取的 imToken）
    // uid 必须使用 {projectId}_{userId} 格式，与后端获取 token 时一致
    imInstance = WKIM.init(wsUrl, {
      uid: imUid.value, // 使用 {projectId}_{userId} 格式
      token: imToken.value,
      deviceFlag: DeviceType.H5, // 连接时使用相同的 deviceFlag
    });

    // 注册事件监听
    imInstance.on(WKIMEvent.Connect, handleIMConnect);
    imInstance.on(WKIMEvent.Disconnect, handleIMDisconnect);
    imInstance.on(WKIMEvent.Message, handleIMMessage);
    imInstance.on(WKIMEvent.Error, handleIMError);

    // 连接服务器
    await imInstance.connect();
    logger.info("IM connection initiated with token from user init");
  } catch (error) {
    logger.error("Failed to init IM connection:", error);
    imConnecting.value = false;
  }
};

// 断开 IM 连接
const disconnectIM = () => {
  if (imInstance) {
    // 移除事件监听
    imInstance.off(WKIMEvent.Connect, handleIMConnect);
    imInstance.off(WKIMEvent.Disconnect, handleIMDisconnect);
    imInstance.off(WKIMEvent.Message, handleIMMessage);
    imInstance.off(WKIMEvent.Error, handleIMError);

    imInstance = null;
    imConnected.value = false;
  }
};

const addMessage = (msg: Partial<Message>) => {
  const now = new Date();
  const newMsg: Message = {
    id: Date.now(),
    senderType: msg.senderType || "user",
    msgType: msg.msgType || "text",
    content: msg.content || "",
    fileName: msg.fileName,
    time: formatTime(now),
    date: formatDate(now),
    timestamp: now.getTime(),
  };
  messages.value.push(newMsg);
  scrollToBottom();
  return newMsg;
};

// 检查消息发送频率限制
const checkRateLimit = (text: string): boolean => {
  // 检查单条消息字数限制
  if (text.length > MAX_CHARS_PER_MESSAGE) {
    rateLimitTip.value = t('h5chat.charLimitExceeded', { max: MAX_CHARS_PER_MESSAGE, current: text.length });
    setTimeout(() => { rateLimitTip.value = ''; }, 3000);
    return false;
  }

  // 清理超过时间窗口的记录
  const now = Date.now();
  recentSentChars.value = recentSentChars.value.filter(
    (r) => now - r.timestamp < RATE_LIMIT_WINDOW
  );

  // 计算时间窗口内已发送的总字数
  const totalCharsInWindow = recentSentChars.value.reduce(
    (sum, r) => sum + r.count, 0
  );

  if (totalCharsInWindow + text.length > MAX_CHARS_PER_MINUTE) {
    rateLimitTip.value = t('h5chat.rateLimitExceeded');
    setTimeout(() => { rateLimitTip.value = ''; }, 3000);
    return false;
  }

  return true;
};

const sendTextMessage = async () => {
  const text = inputMessage.value.trim();
  if (!text || !imInstance || !imConnected.value) {
    if (!imConnected.value) {
      logger.warn("IM not connected, cannot send message");
    }
    return;
  }

  // 检查频率限制
  if (!checkRateLimit(text)) {
    return;
  }

  // 记录本次发送字数
  recentSentChars.value.push({ timestamp: Date.now(), count: text.length });

  inputMessage.value = "";
  showEmojiPicker.value = false;

  // 先在本地显示消息
  addMessage({ senderType: "user", msgType: "text", content: text });

  // 通过 WuKongIM SDK 发送消息到访客频道
  // Visitor Channel (channel_type=10), channel_id = {projectId}_{userId}
  const visitorChannelId = imUid.value;
  if (!visitorChannelId) {
    logger.error("No imUid (projectId_userId) for visitor channel");
    return;
  }

  try {
    const payload = { type: WKChannelType.PERSONAL, content: text };
    // 使用数字 10 作为访客频道类型
    const result = await imInstance.send(
      visitorChannelId,
      WKChannelType.VISITOR as any,
      payload,
    );
    logger.info("Message sent to visitor channel:", result);
  } catch (error) {
    logger.error("Failed to send message via IM:", error);
  }
};

const insertEmoji = (emoji: string) => {
  inputMessage.value += emoji;
};

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value;
  showMorePanel.value = false;
};

const triggerImageUpload = () => {
  imageInput.value?.click();
};

const handleImageUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  // 验证文件类型，只允许上传图片
  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    alert(t('h5chat.imageOnlyTip'));
    input.value = '';
    return;
  }

  try {
    const tokenRes = (await portalApi.get("/portal/oss/token")) as any;

    if (tokenRes.code === 0 && tokenRes.data) {
      // V4 签名格式的响应字段
      const { policy, signature, x_oss_credential, x_oss_date, host, dir, domain } =
        tokenRes.data;

      const formData = new FormData();
      const key = `${dir}${Date.now()}_${file.name}`;

      // V4 签名方式的表单字段
      formData.append("key", key);
      formData.append("success_action_status", "200");
      formData.append("policy", policy);
      formData.append("x-oss-signature", signature);
      formData.append("x-oss-signature-version", "OSS4-HMAC-SHA256");
      formData.append("x-oss-credential", x_oss_credential);
      formData.append("x-oss-date", x_oss_date);
      formData.append("file", file); // file 必须为最后一个表单域

      const uploadRes = await fetch(host, { method: "POST", body: formData });

      if (uploadRes.ok) {
        const imageUrl = `${domain}/${key}`;
        addMessage({ senderType: "user", msgType: "image", content: imageUrl });

        // 通过 WuKongIM SDK 发送图片消息到访客频道
        if (imInstance && imConnected.value && imUid.value) {
          const visitorChannelId = imUid.value;
          const payload = { type: 2, url: imageUrl };
          await imInstance.send(
            visitorChannelId,
            WKChannelType.VISITOR as any,
            payload,
          );
        }
      } else {
        logger.error(
          "Upload failed:",
          uploadRes.status,
          await uploadRes.text(),
        );
        alert(t('h5chat.imageUploadFailed'));
      }
    } else {
      logger.error("Failed to get OSS token:", tokenRes);
      alert(t('h5chat.ossTokenFailed'));
    }
  } catch (error) {
    logger.error("Failed to upload image:", error);
    alert(t('h5chat.imageUploadFailed'));
  }

  input.value = "";
};

const previewImage = (url: string) => {
  previewImageUrl.value = url;
};

const downloadFile = (msg: Message) => {
  if (msg.fileUrl) {
    window.open(msg.fileUrl, "_blank");
  }
};

const loadAutoReplyRules = async () => {
  if (!projectId.value) return;
  try {
    const response = (await portalApi.get('/pub/auto-reply/rules', {
      params: { projectId: parseInt(projectId.value) },
    })) as any;
    if (response.code === 0) {
      autoReplyRules.value = response.data || [];
    }
  } catch {
    // 静默失败，不影响主功能
  }
};

const matchAutoReplyRules = () => {
  if (autoReplyDebounceTimer) clearTimeout(autoReplyDebounceTimer);
  autoReplyDebounceTimer = setTimeout(() => {
    const text = (ticketForm.value.title + ' ' + ticketForm.value.description).toLowerCase().trim();
    if (!text) {
      matchedAutoReplies.value = [];
      return;
    }
    const matched: string[] = [];
    for (const rule of autoReplyRules.value) {
      const keywords = rule.keywords.split(',').map(k => k.trim().toLowerCase()).filter(Boolean);
      if (keywords.some(kw => text.includes(kw))) {
        matched.push(rule.replyContent);
      }
    }
    matchedAutoReplies.value = matched;
  }, 300);
};

// 打开工单对话框时加载自动回复规则
watch(showTicketDialog, (val) => {
  if (val) {
    if (autoReplyRules.value.length === 0) {
      loadAutoReplyRules();
    }
  } else {
    matchedAutoReplies.value = [];
  }
});

// 监听标题和描述变化，实时匹配关键词
watch(
  () => ticketForm.value.title + ticketForm.value.description,
  () => matchAutoReplyRules()
);

const submitTicket = () => {
  showMorePanel.value = false;
  showTicketDialog.value = true;
};

const handleSubmitTicket = async () => {
  if (!ticketForm.value.title || !ticketForm.value.description) {
    alert(t('h5chat.ticketFormRequired'));
    return;
  }

  try {
    const response = (await portalApi.post("/portal/ticket/create", {
      projectId: parseInt(projectId.value),
      userId: currentUser.value?.id,
      title: ticketForm.value.title,
      description: ticketForm.value.description,
      contactInfo: ticketForm.value.contactInfo,
      priority: ticketForm.value.priority,
    })) as any;

    if (response.code === 0) {
      alert(t('h5chat.ticketSubmitSuccess'));
      showTicketDialog.value = false;

      const title = ticketForm.value.title;
      const newTicketId = response.data.id;
      
      // 立即标记新工单为已读，避免显示红点
      markTicketAsRead(newTicketId);
      
      ticketForm.value = {
        title: "",
        description: "",
        contactInfo: "",
        priority: "medium",
      };

      addMessage({
        senderType: "system",
        msgType: "text",
        content: t('h5chat.ticketSubmitMessage', { title, id: newTicketId }),
      });
      
      // 刷新工单列表
      await fetchMyTickets();
    }
  } catch (error) {
    logger.error("Failed to submit ticket:", error);
    alert(t('h5chat.ticketSubmitFailed'));
  }
};

// ========== 工单列表和详情相关方法 ==========
// 获取状态标签
const getStatusLabel = (status: string) => {
  return t(`ticket.status.${status}`);
};

// 获取优先级标签
const getPriorityLabel = (priority: string) => {
  return t(`ticket.priority.${priority}`);
};

// 格式化工单时间
const formatTicketTime = (time: string) => {
  if (!time) return '-';
  const date = new Date(time);
  const month = (date.getMonth() + 1).toString();
  const day = date.getDate().toString();
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return t('h5chat.dateFormat', { month, day, h: hours, m: minutes });
};

// 获取工单最后已读时间
const getTicketReadTime = (ticketId: number): number => {
  const key = `${TICKET_READ_KEY}${currentUser.value?.id}_${ticketId}`;
  const time = localStorage.getItem(key);
  return time ? parseInt(time) : 0;
};

// 标记工单为已读
const markTicketAsRead = (ticketId: number) => {
  const key = `${TICKET_READ_KEY}${currentUser.value?.id}_${ticketId}`;
  localStorage.setItem(key, Date.now().toString());
};

// 检查工单是否有新回复
const checkTicketNewReply = (ticket: any): boolean => {
  if (!ticket.updatedAt) return false;
  const readTime = getTicketReadTime(ticket.id);
  const updateTime = new Date(ticket.updatedAt).getTime();
  return updateTime > readTime;
};

// 获取我的工单列表
const fetchMyTickets = async () => {
  if (!currentUser.value?.id) return;
  
  loadingTickets.value = true;
  try {
    const response = (await portalApi.get(`/portal/ticket/list?userId=${currentUser.value.id}`)) as any;
    if (response.code === 0 && response.data) {
      // 为每个工单添加 hasNewReply 标记
      myTickets.value = response.data.map((ticket: any) => ({
        ...ticket,
        hasNewReply: checkTicketNewReply(ticket)
      }));
      
      // 更新是否有未读回复
      hasUnreadTicketReply.value = myTickets.value.some((t: any) => t.hasNewReply);
    }
  } catch (error) {
    logger.error("Failed to fetch tickets:", error);
  } finally {
    loadingTickets.value = false;
  }
};

// 打开我的工单列表
const openMyTickets = async () => {
  showMyTickets.value = true;
  await fetchMyTickets();
};

// 打开工单详情
const openTicketDetail = async (ticket: any) => {
  showMyTickets.value = false;
  showTicketDetail.value = true;
  currentTicketDetail.value = null;
  
  try {
    const response = (await portalApi.get(`/portal/ticket/${ticket.id}`)) as any;
    if (response.code === 0 && response.data) {
      currentTicketDetail.value = response.data;
      // 标记为已读
      markTicketAsRead(ticket.id);
      // 更新列表中的已读状态
      const ticketIndex = myTickets.value.findIndex((t: any) => t.id === ticket.id);
      if (ticketIndex > -1) {
        myTickets.value[ticketIndex].hasNewReply = false;
      }
      // 更新全局未读状态
      hasUnreadTicketReply.value = myTickets.value.some((t: any) => t.hasNewReply);
    }
  } catch (error) {
    logger.error("Failed to fetch ticket detail:", error);
    alert(t('h5chat.ticketDetailFailed'));
  }
};

// 返回工单列表
const backToTicketList = () => {
  showTicketDetail.value = false;
  showMyTickets.value = true;
  ticketReplyContent.value = "";
};

// 提交工单回复
const submitTicketReply = async () => {
  if (!ticketReplyContent.value.trim() || !currentTicketDetail.value) return;
  
  submittingReply.value = true;
  try {
    const response = (await portalApi.post(`/portal/ticket/${currentTicketDetail.value.ticket.id}/reply`, {
      userId: currentUser.value?.id,
      content: ticketReplyContent.value.trim()
    })) as any;
    
    if (response.code === 0) {
      // 添加到事件列表
      currentTicketDetail.value.events.push(response.data);
      ticketReplyContent.value = "";
      // 更新工单状态
      markTicketAsRead(currentTicketDetail.value.ticket.id);
    } else {
      alert(response.message || t('h5chat.replyFailed'));
    }
  } catch (error) {
    logger.error("Failed to reply ticket:", error);
    alert(t('h5chat.replyRetryFailed'));
  } finally {
    submittingReply.value = false;
  }
};

const viewFAQ = () => {
  showMorePanel.value = false;
  router.push(`/portal?project_id=${projectId.value}`);
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const handleClickOutside = () => {
  if (showEmojiPicker.value || showMorePanel.value) {
    showEmojiPicker.value = false;
    showMorePanel.value = false;
  }
};

onMounted(async () => {
  document.addEventListener("click", handleClickOutside);
  await initUser();
  await initConversation();
  // 加载工单列表以检查是否有未读回复
  await fetchMyTickets();
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
  // 断开 IM 连接
  disconnectIM();
});
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
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
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

/* 加载更多提示样式 */
.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: #999;
  font-size: 12px;
}

.no-more-messages {
  text-align: center;
  padding: 12px;
  color: #ccc;
  font-size: 12px;
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
  to {
    transform: rotate(360deg);
  }
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
  position: relative;
  z-index: 10;
}

/* 工具栏样式 */
.input-toolbar {
  display: flex;
  gap: 4px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 8px;
}

.toolbar-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  flex: 1;
  position: relative;
}

.toolbar-btn:active {
  background-color: #e8e8e8;
}

.toolbar-icon {
  font-size: 18px;
  margin-bottom: 2px;
}

.toolbar-text {
  font-size: 11px;
  color: #666;
}

.unread-dot {
  position: absolute;
  top: 4px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
}

/* 频率限制提示样式 */
.rate-limit-tip {
  text-align: center;
  font-size: 12px;
  color: #ff4d4f;
  padding: 4px 0;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 字数统计样式 */
.char-count {
  font-size: 11px;
  color: #999;
  flex-shrink: 0;
}

.char-count.warning {
  color: #ff4d4f;
}

.input-row {
  display: flex;
  align-items: center;
  gap: 8px;
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

.auto-reply-hint {
  margin-bottom: 16px;
  padding: 12px 14px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 6px;
  border-left: 4px solid #faad14;
}

.auto-reply-hint-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #ad6800;
  margin-bottom: 8px;
}

.hint-icon {
  font-size: 15px;
}

.auto-reply-hint-item {
  font-size: 13px;
  color: #595959;
  line-height: 1.6;
  padding: 8px 10px;
  background: #fff;
  border-radius: 4px;
  margin-top: 6px;
  white-space: pre-wrap;
  word-break: break-word;
  border: 1px solid #f5e8b0;
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

/* 工单列表弹窗样式 */
.ticket-list-dialog {
  max-height: 85vh;
}

.ticket-list-content {
  max-height: 60vh;
  overflow-y: auto;
}

.ticket-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ticket-list-item {
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.ticket-list-item:active {
  background: #f0f0f0;
}

.ticket-item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.ticket-item-title {
  font-weight: 500;
  font-size: 14px;
  color: #333;
  flex: 1;
  margin-right: 8px;
}

.new-reply-badge {
  background: #ff4d4f;
  color: white;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
  flex-shrink: 0;
}

.ticket-item-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 6px;
}

.ticket-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f0f0f0;
  color: #666;
}

.ticket-tag.status-open,
.ticket-tag.status-pending {
  background: #fff7e6;
  color: #d46b08;
}

.ticket-tag.status-processing {
  background: #e6f7ff;
  color: #1890ff;
}

.ticket-tag.status-resolved {
  background: #f6ffed;
  color: #52c41a;
}

.ticket-tag.status-closed {
  background: #f5f5f5;
  color: #999;
}

.ticket-tag.priority-low {
  background: #f5f5f5;
  color: #666;
}

.ticket-tag.priority-medium {
  background: #e6f7ff;
  color: #1890ff;
}

.ticket-tag.priority-high {
  background: #fff7e6;
  color: #fa8c16;
}

.ticket-tag.priority-urgent {
  background: #fff1f0;
  color: #f5222d;
}

.ticket-item-time {
  font-size: 12px;
  color: #999;
}

.empty-tickets {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.loading-tickets {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #999;
}

/* 工单详情弹窗样式 */
.ticket-detail-dialog {
  max-height: 90vh;
}

.ticket-detail-dialog .ticket-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ticket-detail-dialog .ticket-header h3 {
  flex: 1;
  margin: 0;
  text-align: center;
}

.back-btn {
  background: none;
  border: none;
  font-size: 14px;
  color: #1890ff;
  cursor: pointer;
  padding: 4px 8px;
}

.ticket-detail-content {
  max-height: calc(90vh - 160px);
  overflow-y: auto;
}

.ticket-info-card {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 16px;
}

.ticket-detail-title {
  font-weight: 600;
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
}

.ticket-detail-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.ticket-detail-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.ticket-detail-time {
  font-size: 12px;
  color: #999;
}

/* 回复记录 */
.ticket-events {
  margin-bottom: 16px;
}

.events-title {
  font-weight: 500;
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.event-item {
  padding: 10px 12px;
  border-radius: 8px;
  background: #f5f5f5;
}

.event-item.event-agent {
  background: #e6f7ff;
  border-left: 3px solid #1890ff;
}

.event-item.event-user {
  background: #f0f9eb;
  border-left: 3px solid #52c41a;
}

.event-item.event-system {
  background: #fafafa;
  border-left: 3px solid #faad14;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.event-sender {
  font-weight: 500;
  font-size: 13px;
  color: #333;
}

.event-time {
  font-size: 11px;
  color: #999;
}

.event-content {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
}

.no-events {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

/* 回复输入区域 */
.ticket-reply-section {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.ticket-reply-section textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  resize: none;
  margin-bottom: 10px;
}

.ticket-reply-section textarea:focus {
  border-color: #1890ff;
}

.reply-btn {
  width: 100%;
  height: 40px;
  background-color: #1890ff;
  border: none;
  border-radius: 6px;
  color: white;
  font-size: 14px;
  cursor: pointer;
}

.reply-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.reply-btn:not(:disabled):active {
  background-color: #0050b3;
}

.ticket-closed-tip {
  text-align: center;
  padding: 16px;
  color: #999;
  font-size: 14px;
  background: #f5f5f5;
  border-radius: 8px;
}
</style>
