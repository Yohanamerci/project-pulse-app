<template>
  <v-app>

    <!-- ── Navigation Drawer ── -->
    <v-navigation-drawer
      v-if="authStore.isLoggedIn"
      v-model="drawer"
      color="#111827"
    >
      <!-- Branding header -->
      <div class="pa-4 pb-3">
        <div class="d-flex align-center ga-3">
          <v-avatar color="primary" size="36" rounded="lg">
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
      <v-list density="compact" nav class="mt-2 px-2">
        <v-list-item
          v-for="item in visibleItems"
          :key="item.route"
          :prepend-icon="item.icon"
          :title="item.title"
          :value="item.route"
          :active="route.path === item.route || route.path.startsWith(item.route + '/')"
          active-color="primary"
          rounded="lg"
          class="mb-1"
          style="color: rgba(255,255,255,0.75)"
          @click="navigate(item.route)"
        />
      </v-list>

      <!-- User info + sign-out at bottom -->
      <template #append>
        <v-divider style="border-color: rgba(255,255,255,0.08)" />
        <div class="pa-3">
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
              <div
                class="text-body-2 font-weight-medium text-truncate"
                style="color: #F9FAFB"
              >{{ authStore.userInfo.username }}</div>
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
      color="surface"
      border="b"
    >
      <v-app-bar-nav-icon @click="drawer = !drawer" />
      <v-app-bar-title>
        <span class="font-weight-semibold text-high-emphasis">{{ currentPageTitle }}</span>
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
          :icon="isDark ? 'mdi-weather-sunny' : 'mdi-weather-night'"
          variant="text"
          size="small"
          class="mr-1"
          @click="toggleTheme"
        >
          <v-tooltip activator="parent" location="bottom">
            {{ isDark ? 'Light mode' : 'Dark mode' }}
          </v-tooltip>
        </v-btn>
      </template>
    </v-app-bar>

    <!-- ── Main Content ── -->
    <v-main>
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
/* ── Global base — uses Vuetify CSS vars so light/dark both work ── */
html, body {
  background-color: rgb(var(--v-theme-background)) !important;
}

.v-main {
  background-color: rgb(var(--v-theme-background)) !important;
}

/* ── Slim custom scrollbar ── */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.18);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

/* ── Smooth card hover lift ── */
.v-card[href],
.v-card.v-card--hover {
  transition: box-shadow 0.2s ease, transform 0.2s ease !important;
}
.v-card.v-card--hover:hover {
  transform: translateY(-2px);
}
</style>
