import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue')
    },
    {
      path: '/travel',
      name: 'travel',
      component: () => import('@/views/TravelWorkbench.vue')
    },
    {
      path: '/manus',
      name: 'manus',
      component: () => import('@/views/ManusWorkbench.vue')
    }
  ]
})

export default router
