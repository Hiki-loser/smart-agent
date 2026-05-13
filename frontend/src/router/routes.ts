import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  // ---- Public (no auth required) ----
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { requiresAuth: false, layout: 'blank', title: 'auth.login' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { requiresAuth: false, layout: 'blank', title: 'auth.register' },
  },

  // ---- Authenticated ----
  {
    path: '/',
    redirect: '/chat',
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/chat/ChatView.vue'),
    meta: { requiresAuth: true, layout: 'default', title: 'chat.title' },
  },
  {
    path: '/chat/:sessionId',
    name: 'ChatSession',
    component: () => import('@/views/chat/ChatView.vue'),
    meta: { requiresAuth: true, layout: 'default', title: 'chat.title' },
    props: true,
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/user/ProfileView.vue'),
    meta: { requiresAuth: true, layout: 'default', title: 'user.profile' },
  },
  {
    path: '/api-keys',
    name: 'ApiKeys',
    component: () => import('@/views/user/ApiKeysView.vue'),
    meta: { requiresAuth: true, layout: 'default', title: 'apiKey.title' },
  },

  // ---- Error pages ----
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/ForbiddenView.vue'),
    meta: { layout: 'blank', title: 'error.403' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFoundView.vue'),
    meta: { layout: 'blank', title: 'error.404' },
  },
]
