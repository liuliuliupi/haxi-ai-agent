<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const workbenches = [
  {
    id: 'travel',
    name: 'AI自由行行程规划大师',
    description: '智能规划您的旅行行程，打造个性化出行方案',
    icon: '✈️',
    route: '/travel',
    color: '#4f46e5'
  },
  {
    id: 'manus',
    name: '全能智能体终端',
    description: '强大的通用AI助手，满足多样化工作需求',
    icon: '🤖',
    route: '/manus',
    color: '#8b5cf6'
  }
]

function navigateTo(route: string) {
  router.push(route)
}
</script>

<template>
  <div class="home" itemscope itemtype="https://schema.org/WebSite">
    <meta itemprop="name" content="Haxi AI Agent" />
    <meta itemprop="description" content="智能交互平台，提供AI自由行行程规划和全能智能体终端" />
    <div class="container">
      <header class="header">
        <h1 class="title" itemprop="name">Haxi AI Agent</h1>
        <p class="subtitle" itemprop="description">智能交互 · 无限可能</p>
      </header>

      <main class="main-content">
        <div class="cards">
          <div
            v-for="workbench in workbenches"
            :key="workbench.id"
            class="card"
            :style="{ '--accent-color': workbench.color }"
            @click="navigateTo(workbench.route)"
            itemscope itemtype="https://schema.org/Service"
          >
            <meta itemprop="serviceType" content="AI Service" />
            <div class="card-icon">{{ workbench.icon }}</div>
            <div class="card-content">
              <h2 class="card-title" itemprop="name">{{ workbench.name }}</h2>
              <p class="card-desc" itemprop="description">{{ workbench.description }}</p>
            </div>
            <div class="card-arrow">→</div>
            <div class="card-glow"></div>
          </div>
        </div>
      </main>

      <footer class="footer">
        <div class="footer-content">
          <p class="footer-text">选择上方工作台开始体验</p>
          <div class="copyright">
            <p>© 2026 Haxi AI Agent. 保留所有权利。</p>
          </div>
        </div>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: 
    radial-gradient(ellipse at 20% 20%, rgba(79, 70, 229, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(139, 92, 246, 0.15) 0%, transparent 50%),
    linear-gradient(135deg, rgba(79, 70, 229, 0.05) 0%, rgba(139, 92, 246, 0.05) 100%),
    var(--bg-primary);
  position: relative;
  overflow: hidden;
}

/* 科技网格背景 */
.home::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(79, 70, 229, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(79, 70, 229, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: gridMove 20s linear infinite;
  pointer-events: none;
}

/* 旅行元素装饰 */
.home::after {
  content: '';
  position: absolute;
  top: 10%;
  right: 10%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 4s ease-in-out infinite;
  pointer-events: none;
}

@keyframes gridMove {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(50px, 50px);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.container {
  max-width: 1200px;
  width: 100%;
  padding: 40px 20px;
  margin: 0 auto;
}

.header {
  text-align: center;
  margin-bottom: 60px;
  position: relative;
  z-index: 1;
}

.title {
  font-size: 3.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #f8fafc 0%, #94a3b8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 16px;
  letter-spacing: -0.02em;
  animation: titleGlow 3s ease-in-out infinite alternate;
}

.subtitle {
  font-size: 1.2rem;
  color: var(--text-secondary);
  letter-spacing: 0.1em;
  animation: subtitleFade 4s ease-in-out infinite;
}

@keyframes titleGlow {
  from {
    text-shadow: 0 0 20px rgba(79, 70, 229, 0.3);
  }
  to {
    text-shadow: 0 0 30px rgba(139, 92, 246, 0.5);
  }
}

@keyframes subtitleFade {
  0%, 100% {
    opacity: 0.7;
    transform: translateY(0);
  }
  50% {
    opacity: 1;
    transform: translateY(-5px);
  }
}

.main-content {
  margin-bottom: 60px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 32px;
  max-width: 800px;
  margin: 0 auto;
}

.card {
  position: relative;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 40px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 280px;
  position: relative;
  z-index: 1;
  animation: cardFloat 6s ease-in-out infinite;
}

.card:nth-child(1) {
  animation-delay: 0s;
}

.card:nth-child(2) {
  animation-delay: 2s;
}

.card:hover {
  transform: translateY(-12px) scale(1.02);
  border-color: var(--accent-color);
  box-shadow: var(--shadow), 0 0 40px color-mix(in srgb, var(--accent-color) 40%, transparent);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--accent-color), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
  animation: glowMove 3s linear infinite;
}

.card:hover .card-glow {
  opacity: 1;
}

.card-icon {
  font-size: 3rem;
  margin-bottom: 20px;
  animation: iconBounce 4s ease-in-out infinite;
}

.card:hover .card-icon {
  transform: scale(1.1) rotate(5deg);
  transition: transform 0.3s ease;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
}

.card-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: var(--accent-color);
  transition: width 0.3s ease;
}

.card:hover .card-title::after {
  width: 100%;
}

.card-desc {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  flex: 1;
  opacity: 0.8;
  transition: opacity 0.3s ease;
}

.card:hover .card-desc {
  opacity: 1;
}

.card-arrow {
  position: absolute;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.8rem;
  color: var(--text-secondary);
  transition: all 0.3s ease;
  animation: arrowPulse 2s ease-in-out infinite;
}

.card:hover .card-arrow {
  color: var(--accent-color);
  transform: translateY(-50%) translateX(12px) rotate(10deg);
  animation: none;
}

/* 卡片背景效果 */
.card::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(79, 70, 229, 0.05), transparent);
  transform: rotate(45deg);
  transition: transform 0.6s ease;
  z-index: -1;
}

.card:hover::before {
  transform: rotate(45deg) scale(1.2);
}

@keyframes cardFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes iconBounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

@keyframes glowMove {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

@keyframes arrowPulse {
  0%, 100% {
    transform: translateY(-50%) scale(1);
  }
  50% {
    transform: translateY(-50%) scale(1.1);
  }
}

.footer {
  text-align: center;
  color: var(--text-secondary);
  font-size: 1rem;
}

.footer-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.footer-text {
  margin: 0;
}

.copyright {
  font-size: 0.9rem;
  opacity: 0.8;
  border-top: 1px solid var(--border-color);
  padding-top: 16px;
  width: 100%;
  text-align: center;
}

.copyright p {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .container {
    padding: 30px 20px;
  }
  
  .title {
    font-size: 3rem;
  }
  
  .cards {
    gap: 24px;
  }
  
  .card {
    padding: 32px;
    min-height: 250px;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 24px 16px;
  }
  
  .header {
    margin-bottom: 48px;
  }
  
  .title {
    font-size: 2.5rem;
  }
  
  .subtitle {
    font-size: 1.1rem;
  }
  
  .cards {
    grid-template-columns: 1fr;
    max-width: 500px;
  }
  
  .card {
    padding: 28px;
    min-height: 230px;
  }
  
  .card-title {
    font-size: 1.3rem;
  }
  
  .card-desc {
    font-size: 0.95rem;
  }
  
  .footer {
    margin-top: 40px;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 20px 12px;
  }
  
  .header {
    margin-bottom: 40px;
  }
  
  .title {
    font-size: 2rem;
    margin-bottom: 12px;
  }
  
  .subtitle {
    font-size: 1rem;
  }
  
  .card {
    padding: 24px;
    min-height: 200px;
    border-radius: 16px;
  }
  
  .card-icon {
    font-size: 2.5rem;
    margin-bottom: 16px;
  }
  
  .card-title {
    font-size: 1.2rem;
  }
  
  .card-arrow {
    font-size: 1.5rem;
    right: 16px;
  }
  
  .footer-content {
    gap: 12px;
  }
  
  .footer-text {
    font-size: 0.9rem;
  }
  
  .copyright {
    font-size: 0.8rem;
    padding-top: 12px;
  }
}

@media (max-width: 360px) {
  .title {
    font-size: 1.8rem;
  }
  
  .card {
    padding: 20px;
    min-height: 180px;
  }
  
  .card-icon {
    font-size: 2.2rem;
    margin-bottom: 12px;
  }
  
  .card-title {
    font-size: 1.1rem;
  }
  
  .card-desc {
    font-size: 0.9rem;
  }
  
  .card-arrow {
    font-size: 1.3rem;
    right: 12px;
  }
  
  /* 小屏幕设备上的动画调整 */
  .card {
    animation: cardFloat 8s ease-in-out infinite;
  }
  
  .home::before {
    background-size: 30px 30px;
    animation: gridMove 30s linear infinite;
  }
  
  .home::after {
    width: 120px;
    height: 120px;
  }
}
</style>
