import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/pages/LoginPage.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      redirect: '/dashboard',
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/pages/DashboardPage.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/activities',
      name: 'activities',
      component: () => import('@/pages/ActivitiesPage.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/evaluations',
      name: 'evaluations',
      component: () => import('@/pages/EvaluationsPage.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/teams',
      name: 'teams',
      component: () => import('@/pages/TeamsPage.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/sections',
      name: 'sections',
      component: () => import('@/pages/SectionsPage.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('@/pages/UsersPage.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/rubrics',
      name: 'rubrics',
      component: () => import('@/pages/RubricsPage.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard',
    },
  ],
})

// Navigation guard
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const isLoggedIn = !!token

  if (to.meta.requiresAuth && !isLoggedIn) {
    return { name: 'login' }
  }
  if (to.name === 'login' && isLoggedIn) {
    return { name: 'dashboard' }
  }
})

export default router
