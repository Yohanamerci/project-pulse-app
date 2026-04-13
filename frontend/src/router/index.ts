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
      // TODO: implement ActivitiesPage.vue
      component: () => import('@/pages/DashboardPage.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN', 'INSTRUCTOR', 'STUDENT'] },
    },
    {
      path: '/evaluations',
      name: 'evaluations',
      // TODO: implement EvaluationsPage.vue
      component: () => import('@/pages/DashboardPage.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN', 'INSTRUCTOR', 'STUDENT'] },
    },
    {
      path: '/teams',
      name: 'teams',
      // TODO: implement TeamsPage.vue
      component: () => import('@/pages/DashboardPage.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN', 'INSTRUCTOR'] },
    },
    {
      path: '/sections',
      name: 'sections',
      // TODO: implement SectionsPage.vue
      component: () => import('@/pages/DashboardPage.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
      path: '/users',
      name: 'users',
      // TODO: implement UsersPage.vue
      component: () => import('@/pages/DashboardPage.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] },
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
