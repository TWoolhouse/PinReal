import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      alias: [
        "/index.html"
      ],
      name: 'home',
      component: Home
    },
    {
      path: '/proximity',
      name: 'proximity',
      component: () => import('../views/Proximity.vue')
    },
    {
      path: '/landmark',
      name: 'landmark',
      component: () => import('../views/Landmark.vue')
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('../views/User.vue')
    },
    {
      path: '/post',
      name: 'post',
      component: () => import('../views/Post.vue')
    }
  ]
})

export default router
