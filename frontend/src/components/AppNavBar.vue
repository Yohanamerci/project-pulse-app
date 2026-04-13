<template>
  <!-- Top app bar -->
  <v-app-bar elevation="1" color="primary">
    <v-app-bar-nav-icon @click="drawer = !drawer" />
    <v-app-bar-title>Project Pulse</v-app-bar-title>
    <template #append>
      <span class="text-body-2 mr-3 d-none d-sm-block">
        {{ authStore.userInfo?.username }}
        <v-chip size="x-small" class="ml-1" color="white" text-color="primary">
          {{ authStore.userInfo?.role }}
        </v-chip>
      </span>
      <v-btn icon @click="handleLogout">
        <v-icon>mdi-logout</v-icon>
        <v-tooltip activator="parent" location="bottom">Logout</v-tooltip>
      </v-btn>
    </template>
  </v-app-bar>

  <!-- Side navigation drawer -->
  <v-navigation-drawer v-model="drawer" temporary>
    <v-list-item
      prepend-icon="mdi-pulse"
      title="Project Pulse"
      subtitle="Team Activity Tracker"
      nav
    />
    <v-divider />

    <v-list density="compact" nav>
      <v-list-item
        v-for="item in visibleItems"
        :key="item.title"
        :prepend-icon="item.icon"
        :title="item.title"
        :to="item.route"
        color="primary"
      />
    </v-list>

    <template #append>
      <v-divider />
      <v-list density="compact" nav>
        <v-list-item
          prepend-icon="mdi-logout"
          title="Logout"
          @click="handleLogout"
        />
      </v-list>
    </template>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const drawer = ref(false)

const allItems = [
  { title: 'Dashboard',    icon: 'mdi-view-dashboard',  route: '/dashboard', roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
  { title: 'Activities',   icon: 'mdi-calendar-check',  route: '/activities', roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
  { title: 'Evaluations',  icon: 'mdi-account-group',   route: '/evaluations', roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
  { title: 'Teams',        icon: 'mdi-account-multiple', route: '/teams', roles: ['ADMIN','INSTRUCTOR'] },
  { title: 'Sections',     icon: 'mdi-school',           route: '/sections', roles: ['ADMIN'] },
  { title: 'Users',        icon: 'mdi-account-cog',      route: '/users', roles: ['ADMIN'] },
]

const visibleItems = computed(() => {
  const role = authStore.userInfo?.role
  if (!role) return []
  return allItems.filter((i) => i.roles.includes(role))
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>
