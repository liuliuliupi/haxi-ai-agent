<script setup lang="ts">
import { ref, onUnmounted, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createSSEConnection, SSEManager } from '@/utils/sse'

interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

interface LocationInfo {
  city: string
  province: string
  location: string
}

const router = useRouter()
const messages = ref<Message[]>([])
const inputMessage = ref('')
const isLoading = ref(false)
const chatId = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const currentAssistantIndex = ref(-1)
let sseManager: SSEManager | null = null
const userLocation = ref<LocationInfo | null>(null)
const showLocationTip = ref(false)

onMounted(async () => {
  await fetchUserLocation()
})

async function fetchUserLocation() {
  try {
    const response = await fetch('/api/ai/location/ip')
    const data = await response.json()
    
    if (data.city || data.province) {
      userLocation.value = {
        city: data.city || '',
        province: data.province || '',
        location: data.location || ''
      }
      showLocationTip.value = true
      
      setTimeout(() => {
        showLocationTip.value = false
      }, 5000)
    }
  } catch (error) {
    console.error('获取位置信息失败:', error)
  }
}

function getWelcomeMessage(): string {
  if (!userLocation.value) {
    return '告诉我您想去的地方、时间和预算，我来为您规划最佳行程'
  }
  
  const location = userLocation.value.city || userLocation.value.province
  return `检测到您在${location}，告诉我您想去哪里、什么时候出发和预算，我来为您规划最佳行程`
}

function useQuickQuery(type: string) {
  if (!userLocation.value) return
  
  const location = userLocation.value.city || userLocation.value.province
  const queries: Record<string, string> = {
    '周末游': `我从${location}出发，想安排一个周末 2-3 天的短途旅行，请推荐合适的目的地和行程规划`,
    '周边景点': `我在${location}，请推荐周边值得游玩的景点和一日游路线`,
    '美食推荐': `我想了解${location}的特色美食和必吃餐厅推荐`
  }
  
  inputMessage.value = queries[type] || ''
  sendMessage()
}

// 将文本中的换行符转换为 <br> 标签
function formatText(text: string): string {
  if (!text) return ''
  // 将换行符替换为 <br>
  return text.replace(/\n/g, '<br>')
}

function generateChatId(): string {
  return 'travel_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

function sendMessage() {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage: Message = {
    id: Date.now().toString(),
    role: 'user',
    content: inputMessage.value.trim(),
    timestamp: Date.now()
  }
  messages.value.push(userMessage)

  const aiMessage: Message = {
    id: (Date.now() + 1).toString(),
    role: 'assistant',
    content: '',
    timestamp: Date.now()
  }
  messages.value.push(aiMessage)
  currentAssistantIndex.value = messages.value.length - 1

  const messageText = inputMessage.value.trim()
  inputMessage.value = ''
  isLoading.value = true
  scrollToBottom()

  sseManager = createSSEConnection(
    '/api/ai/travel_app/chat/sse',
    {
      message: messageText,
      chatId: chatId.value
    },
    (data: string) => {
      if (data !== null && data !== undefined) {
        const lastMsg = messages.value[currentAssistantIndex.value]
        if (lastMsg) {
          lastMsg.content += data
          messages.value = [...messages.value]
          scrollToBottom()
        }
      }
    },
    (error: Error) => {
      console.error('SSE Error:', error)
      isLoading.value = false
      const lastMsg = messages.value[currentAssistantIndex.value]
      if (lastMsg && !lastMsg.content) {
        lastMsg.content = '抱歉，连接出现错误，请重试。'
        messages.value = [...messages.value]
      }
    },
    () => {
      isLoading.value = false
    }
  )
}

function goHome() {
  if (sseManager) {
    sseManager.close()
  }
  router.push('/')
}

function clearChat() {
  messages.value = []
  chatId.value = generateChatId()
  currentAssistantIndex.value = -1
}

chatId.value = generateChatId()

onUnmounted(() => {
  if (sseManager) {
    sseManager.close()
  }
})
</script>

<template>
  <div class="workbench" itemscope itemtype="https://schema.org/ChatApplication">
    <meta itemprop="name" content="AI 自由行行程规划大师" />
    <meta itemprop="description" content="智能规划您的旅行行程，打造个性化出行方案" />
    
    <!-- 位置提示 -->
    <transition name="slide-down">
      <div v-if="showLocationTip && userLocation" class="location-tip">
        <span class="location-icon">📍</span>
        <span class="location-text">
          已自动定位到：<strong>{{ userLocation.province }}{{ userLocation.city }}</strong>
          <span class="tip-hint">规划行程时将考虑您的当前位置</span>
        </span>
      </div>
    </transition>
    
    <header class="header">
      <div class="header-left">
        <button class="back-btn" @click="goHome">←</button>
        <div class="title-wrap">
          <h1 class="title" itemprop="name">AI自由行行程规划大师</h1>
          <span class="chat-id">会话ID: {{ chatId }}</span>
        </div>
      </div>
      <div class="header-right">
        <button class="clear-btn" @click="clearChat">新建会话</button>
      </div>
    </header>

    <div class="chat-container" ref="messagesContainer" itemprop="interactionService">
      <div class="welcome" v-if="messages.length === 0">
        <div class="welcome-icon">✈️</div>
        <h2>开启您的自由行之旅</h2>
        <p>{{ getWelcomeMessage() }}</p>
        
        <!-- 快捷提问按钮 -->
        <div v-if="userLocation" class="quick-actions">
          <button class="quick-btn" @click="useQuickQuery('周末游')">
            🚗 从{{ userLocation.city || userLocation.province }}出发周末游
          </button>
          <button class="quick-btn" @click="useQuickQuery('周边景点')">
            🏞️ {{ userLocation.city || userLocation.province }}周边景点推荐
          </button>
          <button class="quick-btn" @click="useQuickQuery('美食推荐')">
            🍜 {{ userLocation.city || userLocation.province }}美食推荐
          </button>
        </div>
      </div>

      <div
        v-for="message in messages"
        :key="message.id"
        class="message"
        :class="message.role"
        itemscope
        :itemtype="message.role === 'user' ? 'https://schema.org/UserComments' : 'https://schema.org/CreativeWork'"
      >
        <meta v-if="message.role === 'user'" itemprop="author" content="用户" />
        <meta v-else itemprop="author" content="AI助手" />
        <meta itemprop="dateCreated" :content="new Date(message.timestamp).toISOString()" />
        <div class="message-avatar">
          <span v-if="message.role === 'user'">👤</span>
          <span v-else>✈️</span>
        </div>
        <div class="message-content">
          <div class="bubble">
            <template v-if="message.role === 'assistant'">
              <span v-if="message.content" class="content-text" itemprop="text" v-html="formatText(message.content)"></span>
              <span v-else class="typing-indicator">正在输入<span class="dot">.</span><span class="dot">.</span><span class="dot">.</span></span>
            </template>
            <span v-else class="content-text" itemprop="text">{{ message.content }}</span>
          </div>
          <span class="timestamp">{{ new Date(message.timestamp).toLocaleTimeString() }}</span>
        </div>
      </div>
    </div>

    <div class="input-area" itemprop="potentialAction" itemscope itemtype="https://schema.org/SendMessageAction">
      <meta itemprop="name" content="发送消息" />
      <div class="input-wrapper">
        <textarea
          v-model="inputMessage"
          :placeholder="userLocation ? `描述您的旅行需求...（例如：从${userLocation.city || userLocation.province}出发）` : '描述您的旅行需求...'"
          @keydown.enter.exact.prevent="sendMessage"
          :disabled="isLoading"
          itemprop="message" 
        ></textarea>
        <button 
          class="send-btn" 
          @click="sendMessage"
          :disabled="isLoading || !inputMessage.trim()"
          itemprop="target"
        >
          <span v-if="!isLoading">发送</span>
          <span v-else class="loading-dot">...</span>
        </button>
      </div>
    </div>

    <footer class="workbench-footer">
      <div class="copyright">
        <p>© 2026 Haxi AI Agent. 保留所有权利。</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.workbench {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-primary);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--bg-tertiary);
  color: var(--text-primary);
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.back-btn:hover {
  background: var(--primary-color);
}

.title-wrap {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-id {
  font-size: 0.75rem;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-right {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.clear-btn {
  padding: 8px 16px;
  border-radius: 8px;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  font-size: 0.875rem;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.clear-btn:hover {
  background: var(--primary-color);
  color: var(--text-primary);
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--text-secondary);
  padding: 40px 20px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  margin-top: 20px;
  max-width: 600px;
}

.quick-btn {
  padding: 10px 16px;
  background: rgba(79, 70, 229, 0.1);
  border: 1px solid rgba(79, 70, 229, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.quick-btn:hover {
  background: rgba(79, 70, 229, 0.2);
  border-color: rgba(79, 70, 229, 0.5);
  transform: translateY(-2px);
}

.location-tip {
  position: absolute;
  top: 70px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.95), rgba(139, 92, 246, 0.95));
  backdrop-filter: blur(10px);
  padding: 12px 24px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(79, 70, 229, 0.3);
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 100;
  animation: slideDown 0.3s ease-out;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.location-icon {
  font-size: 1.2rem;
  animation: bounce 1s ease-in-out infinite;
}

.location-text {
  color: #fff;
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.location-text strong {
  font-weight: 600;
  font-size: 0.95rem;
}

.tip-hint {
  font-size: 0.8rem;
  opacity: 0.9;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translate(-50%, -20px);
  }
  to {
    opacity: 1;
    transform: translate(-50%, 0);
  }
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

.welcome-icon {
  font-size: 4rem;
  margin-bottom: 24px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.welcome h2 {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin-bottom: 12px;
  max-width: 90%;
}

.welcome p {
  font-size: 1rem;
  max-width: 400px;
  line-height: 1.6;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 85%;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: var(--primary-color);
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #4f46e5, #8b5cf6);
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.message.user .message-content {
  align-items: flex-end;
}

.bubble {
  padding: 14px 18px;
  border-radius: 16px;
  font-size: 0.95rem;
  line-height: 1.6;
  position: relative;
  animation: bubbleIn 0.3s ease;
  word-wrap: break-word;
  max-width: 100%;
}

@keyframes bubbleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.message.user .bubble {
  background: var(--primary-color);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .bubble {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-bottom-left-radius: 4px;
}

.bubble p {
  margin: 0;
  white-space: normal;
  word-wrap: break-word;
}

.content-text {
  white-space: normal;
  word-wrap: break-word;
  display: block;
  line-height: 1.8;
}

.timestamp {
  font-size: 0.7rem;
  color: var(--text-secondary);
  white-space: nowrap;
}

.typing-indicator {
  display: inline-block;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.typing-indicator .dot {
  animation: blink 1.4s infinite both;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {
  0%, 80%, 100% { opacity: 0; }
  40% { opacity: 1; }
}

.input-area {
  padding: 16px 24px 16px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
}

.input-wrapper {
  display: flex;
  gap: 12px;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
}

.input-wrapper textarea {
  flex: 1;
  padding: 14px 18px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: 0.95rem;
  resize: none;
  min-height: 48px;
  max-height: 120px;
  transition: border-color 0.2s ease;
  min-width: 0;
  -webkit-overflow-scrolling: touch;
  overflow-y: auto;
}

.input-wrapper textarea:focus {
  outline: none;
  border-color: var(--primary-color);
}

.input-wrapper textarea::placeholder {
  color: var(--text-secondary);
}

.send-btn {
  padding: 12px 24px;
  border-radius: 12px;
  background: var(--primary-color);
  color: white;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-1px);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-dot {
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.workbench-footer {
  padding: 8px 24px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
}

.copyright {
  text-align: center;
  font-size: 0.8rem;
  color: var(--text-secondary);
  opacity: 0.8;
}

.copyright p {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .header {
    padding: 14px 20px;
  }
  
  .title {
    font-size: 1.15rem;
  }
  
  .chat-container {
    padding: 20px;
  }
  
  .input-area {
    padding: 14px 20px 14px;
  }
  
  .input-wrapper {
    max-width: 700px;
  }
}

@media (max-width: 768px) {
  .header {
    padding: 12px 16px;
  }
  
  .header-left {
    gap: 12px;
  }
  
  .back-btn {
    width: 36px;
    height: 36px;
    font-size: 1.1rem;
  }
  
  .title {
    font-size: 1.1rem;
  }
  
  .chat-id {
    font-size: 0.7rem;
  }
  
  .clear-btn {
    padding: 6px 12px;
    font-size: 0.8rem;
  }
  
  .chat-container {
    padding: 16px;
    gap: 16px;
  }
  
  .welcome-icon {
    font-size: 3rem;
    margin-bottom: 20px;
  }
  
  .welcome h2 {
    font-size: 1.3rem;
  }
  
  .welcome p {
    font-size: 0.95rem;
    max-width: 90%;
  }
  
  .message {
    max-width: 90%;
    gap: 10px;
  }
  
  .message-avatar {
    width: 32px;
    height: 32px;
    font-size: 1.1rem;
  }
  
  .bubble {
    padding: 12px 16px;
    font-size: 0.9rem;
  }
  
  .input-area {
    padding: 12px 16px 12px;
  }
  
  .input-wrapper {
    gap: 10px;
  }
  
  .input-wrapper textarea {
    padding: 12px 16px;
    font-size: 0.9rem;
  }
  
  .send-btn {
    padding: 10px 20px;
    font-size: 0.9rem;
  }
  
  .workbench-footer {
    padding: 6px 16px;
  }
  
  .copyright {
    font-size: 0.75rem;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 10px 12px;
  }
  
  .header-left {
    gap: 8px;
  }
  
  .back-btn {
    width: 40px;
    height: 40px;
    font-size: 1rem;
    min-width: 40px;
  }
  
  .title {
    font-size: 1rem;
  }
  
  .chat-id {
    font-size: 0.65rem;
  }
  
  .clear-btn {
    padding: 6px 12px;
    font-size: 0.75rem;
    min-height: 32px;
  }
  
  .chat-container {
    padding: 12px;
    gap: 14px;
    -webkit-overflow-scrolling: touch;
  }
  
  .welcome {
    padding: 20px 10px;
  }
  
  .welcome-icon {
    font-size: 2.5rem;
    margin-bottom: 16px;
  }
  
  .welcome h2 {
    font-size: 1.2rem;
  }
  
  .welcome p {
    font-size: 0.9rem;
  }
  
  .message {
    max-width: 95%;
    gap: 8px;
  }
  
  .message-avatar {
    width: 32px;
    height: 32px;
    font-size: 1rem;
  }
  
  .bubble {
    padding: 12px 16px;
    font-size: 0.85rem;
  }
  
  .timestamp {
    font-size: 0.65rem;
  }
  
  .input-area {
    padding: 10px 12px 10px;
  }
  
  .input-wrapper {
    gap: 8px;
  }
  
  .input-wrapper textarea {
    padding: 12px 16px;
    font-size: 0.85rem;
    min-height: 48px;
  }
  
  .send-btn {
    padding: 10px 20px;
    font-size: 0.85rem;
    min-height: 48px;
  }
  
  .workbench-footer {
    padding: 4px 12px;
  }
  
  .copyright {
    font-size: 0.7rem;
  }
}

@media (max-width: 360px) {
  .title {
    font-size: 0.95rem;
  }
  
  .chat-id {
    font-size: 0.6rem;
  }
  
  .welcome h2 {
    font-size: 1.1rem;
  }
  
  .bubble {
    padding: 8px 12px;
    font-size: 0.8rem;
  }
  
  .input-wrapper textarea {
    padding: 8px 12px;
    font-size: 0.8rem;
  }
  
  .send-btn {
    padding: 6px 12px;
    font-size: 0.8rem;
  }
}
</style>
