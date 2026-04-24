<template>
  <v-app>

    <!-- ── Global animated neon blobs (behind everything) ── -->
    <div class="pp-bg-canvas" aria-hidden="true">
      <div class="pp-bg-blob pp-bg-blob--1" :style="{ background: blobBg1 }" />
      <div class="pp-bg-blob pp-bg-blob--2" :style="{ background: blobBg2 }" />
      <div class="pp-bg-blob pp-bg-blob--3" :style="{ background: blobBg3 }" />
      <div class="pp-bg-blob pp-bg-blob--4" :style="{ background: blobBg4 }" />
    </div>

    <!-- ── Navigation Drawer ── -->
    <v-navigation-drawer
      v-if="authStore.isLoggedIn"
      v-model="drawer"
      class="pp-drawer"
    >
      <!-- Dot grid overlay -->
      <div class="pp-drawer-grid" />

      <!-- Branding header -->
      <div class="pa-4 pb-3 position-relative" style="z-index:1">
        <div class="d-flex align-center ga-3">
          <v-avatar size="36" rounded="lg" style="background: linear-gradient(135deg, #00D4FF, #7C3AED); box-shadow: 0 0 14px rgba(0,212,255,0.35)">
            <v-icon icon="mdi-pulse" size="20" color="white" />
          </v-avatar>
          <div>
            <div class="text-subtitle-2 font-weight-bold" style="color: #F9FAFB">Project Pulse</div>
            <div class="text-caption" style="color: rgba(255,255,255,0.4)">Activity Tracker</div>
          </div>
        </div>
      </div>

      <v-divider style="border-color: rgba(255,255,255,0.08)" />

      <!-- Nav items -->
      <v-list density="compact" nav class="mt-2 px-2 position-relative" style="z-index:1">
        <v-list-item
          v-for="item in visibleItems"
          :key="item.route"
          :prepend-icon="item.icon"
          :title="item.title"
          :value="item.route"
          :active="route.path === item.route || route.path.startsWith(item.route + '/')"
          active-color="primary"
          rounded="lg"
          class="mb-1 pp-nav-item"
          style="color: rgba(255,255,255,0.75)"
          @click="navigate(item.route)"
        />
      </v-list>

      <!-- User info + sign-out at bottom -->
      <template #append>
        <v-divider style="border-color: rgba(255,255,255,0.08)" />
        <div class="pa-3 position-relative" style="z-index:1">
          <div
            v-if="authStore.userInfo"
            class="d-flex align-center ga-2 px-2 py-2 rounded-lg mb-2"
            style="background: rgba(255,255,255,0.05)"
          >
            <v-avatar :color="roleColor" size="32" rounded="lg">
              <span class="font-weight-bold text-white" style="font-size: 11px">
                {{ authStore.userInfo.username.slice(0, 2).toUpperCase() }}
              </span>
            </v-avatar>
            <div style="min-width: 0; flex: 1">
              <div class="text-body-2 font-weight-medium text-truncate" style="color: #F9FAFB">
                {{ authStore.userInfo.username }}
              </div>
              <div class="text-caption" style="color: rgba(255,255,255,0.4)">
                {{ authStore.userInfo.role }}
              </div>
            </div>
          </div>
          <v-btn
            block
            variant="tonal"
            color="grey-lighten-1"
            prepend-icon="mdi-logout"
            density="comfortable"
            rounded="lg"
            @click="handleLogout"
          >
            Sign Out
          </v-btn>
        </div>
      </template>
    </v-navigation-drawer>

    <!-- ── App Bar ── -->
    <v-app-bar
      v-if="authStore.isLoggedIn"
      elevation="0"
      :class="isDark ? 'pp-appbar-dark' : 'pp-appbar-light'"
    >
      <v-app-bar-nav-icon @click="drawer = !drawer" />
      <v-app-bar-title>
        <span class="font-weight-semibold" :style="isDark ? 'color:#F9FAFB' : ''">{{ currentPageTitle }}</span>
      </v-app-bar-title>
      <template #append>
        <v-chip
          v-if="authStore.userInfo"
          class="mr-2 d-none d-sm-flex"
          :color="roleColor"
          variant="tonal"
          size="small"
          label
        >
          <v-icon start icon="mdi-account-circle" size="14" />
          {{ authStore.userInfo.username }}
        </v-chip>

        <!-- Theme toggle -->
        <v-btn
          :prepend-icon="isDark ? 'mdi-weather-sunny' : 'mdi-weather-night'"
          variant="elevated"
          :color="isDark ? 'warning' : 'secondary'"
          size="small"
          rounded="pill"
          class="mr-2"
          style="font-weight: 600; letter-spacing: 0.3px"
          @click="toggleTheme"
        >
          {{ isDark ? 'Light Mode' : 'Dark Mode' }}
        </v-btn>
      </template>
    </v-app-bar>

    <!-- ── Main Content ── -->
    <v-main class="pp-main">
      <router-view />
    </v-main>

  </v-app>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useTheme } from 'vuetify'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const route  = useRoute()
const router = useRouter()
const drawer = ref(true)

// ── Theme toggle ──
const vuetifyTheme = useTheme()
vuetifyTheme.global.name.value = (localStorage.getItem('pp-theme') ?? 'light') as 'light' | 'dark'
const isDark = computed(() => vuetifyTheme.global.name.value === 'dark')

function toggleTheme() {
  const next = isDark.value ? 'light' : 'dark'
  vuetifyTheme.global.name.value = next
  localStorage.setItem('pp-theme', next)
}

// ── Background blobs (match login page right-panel blobs) ──
const blobBg1 = computed(() =>
  isDark.value
    ? 'radial-gradient(circle, rgba(0,212,255,0.13) 0%, transparent 65%)'
    : 'radial-gradient(circle, rgba(29,78,216,0.07) 0%, transparent 65%)'
)
const blobBg2 = computed(() =>
  isDark.value
    ? 'radial-gradient(circle, rgba(124,58,237,0.15) 0%, transparent 65%)'
    : 'radial-gradient(circle, rgba(79,70,229,0.06) 0%, transparent 65%)'
)
const blobBg3 = computed(() =>
  isDark.value
    ? 'radial-gradient(circle, rgba(236,72,153,0.11) 0%, transparent 65%)'
    : 'radial-gradient(circle, rgba(236,72,153,0.05) 0%, transparent 65%)'
)
const blobBg4 = computed(() =>
  isDark.value
    ? 'radial-gradient(circle, rgba(16,185,129,0.10) 0%, transparent 65%)'
    : 'radial-gradient(circle, rgba(5,150,105,0.05) 0%, transparent 65%)'
)

const allItems = [
  { title: 'Dashboard',    icon: 'mdi-view-dashboard',   route: '/dashboard',   roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
  { title: 'Activities',   icon: 'mdi-calendar-check',   route: '/activities',  roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
  { title: 'Evaluations',  icon: 'mdi-star-check',        route: '/evaluations', roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
  { title: 'Teams',        icon: 'mdi-account-multiple',  route: '/teams',       roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
  { title: 'Sections',     icon: 'mdi-school',            route: '/sections',    roles: ['ADMIN', 'INSTRUCTOR'] },
  { title: 'Users',        icon: 'mdi-account-cog',       route: '/users',       roles: ['ADMIN', 'INSTRUCTOR'] },
  { title: 'Rubrics',      icon: 'mdi-clipboard-list',    route: '/rubrics',     roles: ['ADMIN', 'INSTRUCTOR'] },
  { title: 'Edit Profile', icon: 'mdi-account-edit',      route: '/profile',     roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
]

const visibleItems = computed(() => {
  const role = authStore.userInfo?.role
  if (!role) return []
  return allItems.filter(i => i.roles.includes(role))
})

const currentPageTitle = computed(() => {
  const found = allItems.find(i => route.path === i.route || route.path.startsWith(i.route + '/'))
  if (found) return found.title
  if (route.path.startsWith('/students/')) return 'Student Performance'
  return 'Project Pulse'
})

const roleColor = computed(() => ({
  ADMIN: 'error', INSTRUCTOR: 'warning', STUDENT: 'primary',
} as Record<string, string>)[authStore.userInfo?.role ?? ''] ?? 'grey')

function navigate(path: string) {
  router.push(path)
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style>
/* ── Base background — matches login page ── */
html, body {
  margin: 0;
  padding: 0;
}
.v-theme--dark html,
.v-theme--dark body,
html:has(.v-theme--dark) {
  background-color: #050B18 !important;
}

/* ── Main content area — transparent so blobs show through ── */
.pp-main {
  position: relative;
  z-index: 1;
}
.v-theme--dark .pp-main {
  background-color: transparent !important;
}
.v-theme--light .pp-main {
  background: linear-gradient(145deg, #F0F4FF 0%, #F8FAFC 60%, #F5F0FF 100%) !important;
  background-attachment: fixed !important;
}

/* ── Animated background blobs (fixed, behind everything) ── */
.pp-bg-canvas {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}
.pp-bg-blob {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.pp-bg-blob--1 {
  width: 700px; height: 700px;
  top: -220px; right: -220px;
  animation: pp-bg-drift-1 16s ease-in-out infinite;
}
.pp-bg-blob--2 {
  width: 560px; height: 560px;
  bottom: -160px; left: -160px;
  animation: pp-bg-drift-2 18s ease-in-out infinite;
}
.pp-bg-blob--3 {
  width: 380px; height: 380px;
  top: 38%; left: 32%;
  animation: pp-bg-drift-3 11s ease-in-out infinite;
}
.pp-bg-blob--4 {
  width: 320px; height: 320px;
  top: 8%; left: 55%;
  animation: pp-bg-drift-4 20s ease-in-out infinite;
}

@keyframes pp-bg-drift-1 {
  0%, 100% { transform: translate(0, 0); }
  50%       { transform: translate(-32px, 28px); }
}
@keyframes pp-bg-drift-2 {
  0%, 100% { transform: translate(0, 0); }
  50%       { transform: translate(28px, -22px); }
}
@keyframes pp-bg-drift-3 {
  0%, 100% { transform: translate(0, 0); }
  50%       { transform: translate(20px, 26px); }
}
@keyframes pp-bg-drift-4 {
  0%, 100% { transform: translate(0, 0); }
  50%       { transform: translate(-24px, 18px); }
}

/* ── Navigation Drawer — deep space glass ── */
.pp-drawer {
  background: rgba(5, 11, 24, 0.92) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border-right: 1px solid rgba(124, 58, 237, 0.18) !important;
}
.pp-drawer-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(255,255,255,0.07) 1px, transparent 1px);
  background-size: 24px 24px;
  pointer-events: none;
  z-index: 0;
}

/* ── App Bar ── */
.pp-appbar-dark {
  background: rgba(5, 11, 24, 0.80) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border-bottom: 1px solid rgba(124, 58, 237, 0.18) !important;
}
.pp-appbar-light {
  background: rgba(255, 255, 255, 0.80) !important;
  backdrop-filter: blur(16px) !important;
  -webkit-backdrop-filter: blur(16px) !important;
  border-bottom: 1px solid rgba(99, 102, 241, 0.12) !important;
}

/* ── All cards in main content — glassmorphism ── */
.v-theme--dark .pp-main .v-card {
  position: relative !important;
  overflow: hidden !important;
  background: rgba(255, 255, 255, 0.035) !important;
  backdrop-filter: blur(24px) !important;
  -webkit-backdrop-filter: blur(24px) !important;
  border: 1px solid rgba(124, 58, 237, 0.22) !important;
  box-shadow: 0 4px 32px rgba(0, 0, 0, 0.45),
              0 0 0 1px rgba(0, 212, 255, 0.06) !important;
}
.v-theme--light .pp-main .v-card {
  position: relative !important;
  overflow: hidden !important;
  background: rgba(255, 255, 255, 0.75) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border: 1px solid rgba(99, 102, 241, 0.12) !important;
  box-shadow: 0 4px 24px rgba(29, 78, 216, 0.08) !important;
}

/* Rainbow accent bar on all cards */
.pp-main .v-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: linear-gradient(90deg, #00D4FF, #7C3AED, #EC4899);
  z-index: 1;
}

/* ── Slim custom scrollbar ── */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb {
  background: rgba(124, 58, 237, 0.25);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(124, 58, 237, 0.45);
}

/* ── Smooth card hover lift ── */
.v-card.v-card--hover {
  transition: box-shadow 0.2s ease, transform 0.2s ease !important;
}
.v-card.v-card--hover:hover {
  transform: translateY(-2px);
}

/* ── Global neon page hero banner ── */
.pp-page-hero {
  background: linear-gradient(150deg, #0F0C29 0%, #302B63 55%, #24243E 100%);
  border-radius: 16px;
  padding: 22px 26px;
  position: relative;
  overflow: hidden;
}
.pp-page-hero-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(255,255,255,0.10) 1px, transparent 1px);
  background-size: 26px 26px;
  pointer-events: none;
}
.pp-page-hero-glow {
  position: absolute;
  width: 380px;
  height: 220px;
  right: -40px;
  top: -40px;
  border-radius: 50%;
  pointer-events: none;
}
.pp-page-hero-icon {
  width: 52px !important;
  height: 52px !important;
  min-width: 52px !important;
  border-radius: 14px !important;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
  flex-shrink: 0;
}
</style>
