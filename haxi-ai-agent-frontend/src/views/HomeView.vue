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
  <div class="home">
    <div class="container">
      <header class="header">
        <h1 class="title">Haxi AI Agent</h1>
        <p class="subtitle">智能交互 · 无限可能</p>
      </header>

      <div class="cards">
        <div
          v-for="workbench in workbenches"
          :key="workbench.id"
          class="card"
          :style="{ '--accent-color': workbench.color }"
          @click="navigateTo(workbench.route)"
        >
          <div class="card-icon">{{ workbench.icon }}</div>
          <div class="card-content">
            <h2 class="card-title">{{ workbench.name }}</h2>
            <p class="card-desc">{{ workbench.description }}</p>
          </div>
          <div class="card-arrow">→</div>
          <div class="card-glow"></div>
        </div>
      </div>

      <footer class="footer">
        <p>选择上方工作台开始体验</p>
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
    var(--bg-primary);
}

.container {
  max-width: 800px;
  width: 100%;
  padding: 40px 20px;
}

.header {
  text-align: center;
  margin-bottom: 60px;
}

.title {
  font-size: 3rem;
  font-weight: 700;
  background: linear-gradient(135deg, #f8fafc 0%, #94a3b8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
  letter-spacing: -0.02em;
}

.subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  letter-spacing: 0.1em;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

.card {
  position: relative;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 32px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.card:hover {
  transform: translateY(-4px);
  border-color: var(--accent-color);
  box-shadow: var(--shadow), 0 0 30px color-mix(in srgb, var(--accent-color) 30%, transparent);
}

.card-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--accent-color), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card:hover .card-glow {
  opacity: 1;
}

.card-icon {
  font-size: 2.5rem;
  margin-bottom: 16px;
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.card-desc {
  font-size: 0.9rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.card-arrow {
  position: absolute;
  right: 24px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.5rem;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.card:hover .card-arrow {
  color: var(--accent-color);
  transform: translateY(-50%) translateX(4px);
}

.footer {
  text-align: center;
  margin-top: 48px;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

@media (max-width: 640px) {
  .title {
    font-size: 2rem;
  }

  .cards {
    grid-template-columns: 1fr;
  }
}
</style>
