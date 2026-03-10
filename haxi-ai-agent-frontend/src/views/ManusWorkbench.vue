<script setup lang="ts">
import { ref, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { createSSEConnection, SSEManager } from '@/utils/sse'

interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
  isStep?: boolean
  stepNumber?: number
}

const router = useRouter()
const messages = ref<Message[]>([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)
let sseManager: SSEManager | null = null
let currentStepNumber = 0

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

  const messageText = inputMessage.value.trim()
  inputMessage.value = ''
  isLoading.value = true
  currentStepNumber = 0
  scrollToBottom()

  sseManager = createSSEConnection(
    '/api/ai/manus/chat',
    {
      message: messageText
    },
    (data: string) => {
      if (data && data.trim()) {
        if (data.includes('\n\n') || (data.startsWith('步骤') || data.startsWith('Step') || data.startsWith('1.') || data.startsWith('['))) {
          currentStepNumber++
          const stepMessage: Message = {
            id: Date.now() + '_' + currentStepNumber,
            role: 'assistant',
            content: data.trim(),
            timestamp: Date.now(),
            isStep: true,
            stepNumber: currentStepNumber
          }
          messages.value.push(stepMessage)
        } else {
          const lastMsg = messages.value[messages.value.length - 1]
          if (lastMsg && lastMsg.role === 'assistant') {
            lastMsg.content += data
          } else {
            currentStepNumber++
            const stepMessage: Message = {
              id: Date.now() + '_' + currentStepNumber,
              role: 'assistant',
              content: data,
              timestamp: Date.now(),
              isStep: true,
              stepNumber: currentStepNumber
            }
            messages.value.push(stepMessage)
          }
        }
        messages.value = [...messages.value]
        scrollToBottom()
      }
    },
    (error: Error) => {
      console.error('SSE Error:', error)
      isLoading.value = false
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
  currentStepNumber = 0
}

onUnmounted(() => {
  if (sseManager) {
    sseManager.close()
  }
})
</script>

<template>
  <div class="workbench manus" itemscope itemtype="https://schema.org/ChatApplication">
    <meta itemprop="name" content="全能智能体终端" />
    <meta itemprop="description" content="强大的通用AI助手，满足多样化工作需求" />
    <header class="header">
      <div class="header-left">
        <button class="back-btn" @click="goHome">←</button>
        <div class="title-wrap">
          <h1 class="title" itemprop="name">
            <span class="title-icon">⚡</span>
            全能智能体终端
          </h1>
          <span class="status-dot"></span>
        </div>
      </div>
      <div class="header-right">
        <button class="clear-btn" @click="clearChat">新建会话</button>
      </div>
    </header>

    <div class="chat-container" ref="messagesContainer" itemprop="interactionService">
      <div class="welcome" v-if="messages.length === 0">
        <div class="welcome-icon">🤖</div>
        <h2>全能智能体</h2>
        <p>我是您的AI助手，可以帮助您完成各种任务。请告诉我您的需求</p>
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
          <span v-else>⚡</span>
        </div>
        <div class="message-content">
          <div class="bubble" :class="{ 'step-bubble': message.isStep }">
            <span v-if="message.isStep && message.stepNumber" class="step-badge">步骤 {{ message.stepNumber }}</span>
            <span class="content-text" itemprop="text">{{ message.content }}</span>
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
          placeholder="输入您的需求..."
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

.workbench.manus {
  --accent-color: #8b5cf6;
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
  background: var(--accent-color);
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.title-icon {
  font-size: 1.4rem;
  flex-shrink: 0;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent-color);
  box-shadow: 0 0 10px var(--accent-color);
  animation: glow 2s ease-in-out infinite;
  flex-shrink: 0;
}

@keyframes glow {
  0%, 100% {
    box-shadow: 0 0 5px var(--accent-color);
  }
  50% {
    box-shadow: 0 0 15px var(--accent-color), 0 0 25px var(--accent-color);
  }
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
  background: var(--accent-color);
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

.welcome-icon {
  font-size: 4rem;
  margin-bottom: 24px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { 
    transform: scale(1);
    filter: brightness(1);
  }
  50% { 
    transform: scale(1.05);
    filter: brightness(1.2);
  }
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
  max-width: 90%;
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
  background: var(--accent-color);
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #8b5cf6, #d946ef);
  box-shadow: 0 0 15px rgba(139, 92, 246, 0.5);
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
  background: var(--accent-color);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .bubble {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-bottom-left-radius: 4px;
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.1);
}

.step-bubble {
  border-left: 3px solid var(--accent-color);
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), var(--bg-secondary));
}

.step-badge {
  display: inline-block;
  background: var(--accent-color);
  color: white;
  font-size: 0.7rem;
  padding: 2px 8px;
  border-radius: 10px;
  margin-bottom: 8px;
}

.content-text {
  white-space: pre-wrap;
  word-wrap: break-word;
  display: block;
}

.timestamp {
  font-size: 0.7rem;
  color: var(--text-secondary);
  white-space: nowrap;
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
  border-color: var(--accent-color);
}

.input-wrapper textarea::placeholder {
  color: var(--text-secondary);
}

.send-btn {
  padding: 12px 24px;
  border-radius: 12px;
  background: var(--accent-color);
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
  background: #7c3aed;
  transform: translateY(-1px);
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.5);
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
    gap: 6px;
  }
  
  .title-icon {
    font-size: 1.2rem;
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
    max-width: 95%;
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
  
  .step-badge {
    font-size: 0.65rem;
    padding: 1px 6px;
    margin-bottom: 6px;
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
    gap: 4px;
  }
  
  .title-icon {
    font-size: 1.1rem;
  }
  
  .status-dot {
    width: 6px;
    height: 6px;
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
    max-width: 98%;
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
  
  .step-badge {
    font-size: 0.65rem;
    padding: 2px 6px;
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
