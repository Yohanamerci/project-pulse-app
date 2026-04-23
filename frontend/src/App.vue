<template>
  <v-app>

    <!-- ── Navigation Drawer ── direct child of v-app so Vuetify layout works -->
    <v-navigation-drawer
      v-if="authStore.isLoggedIn"
      v-model="drawer"
      color="grey-darken-4"
    >
      <!-- Branding -->
      <v-list-item
        prepend-icon="mdi-pulse"
        title="Project Pulse"
        subtitle="Team Activity Tracker"
        nav
        class="py-4"
      />
      <v-divider />

      <!-- Nav items -->
      <v-list density="compact" nav class="mt-2">
        <v-list-item
          v-for="item in visibleItems"
          :key="item.route"
          :prepend-icon="item.icon"
          :title="item.title"
          :value="item.route"
          :active="route.path === item.route"
          active-color="primary"
          rounded="lg"
          class="mb-1"
          @click="navigate(item.route)"
        />
      </v-list>

      <!-- Logout at bottom -->
      <template #append>
        <v-divider />
        <v-list density="compact" nav class="mb-2">
          <v-list-item
            prepend-icon="mdi-logout"
            title="Logout"
            rounded="lg"
            @click="handleLogout"
          />
        </v-list>
      </template>
    </v-navigation-drawer>

    <!-- ── App Bar ── direct child of v-app -->
    <v-app-bar v-if="authStore.isLoggedIn" elevation="1" color="primary">
      <v-app-bar-nav-icon @click="drawer = !drawer" />
      <v-app-bar-title class="font-weight-bold">Project Pulse</v-app-bar-title>
      <template #append>
        <v-chip
          v-if="authStore.userInfo"
          class="mr-2 d-none d-sm-flex"
          color="white"
          variant="outlined"
          size="small"
        >
          <v-icon start icon="mdi-account-circle" />
          {{ authStore.userInfo.username }} · {{ authStore.userInfo.role }}
        </v-chip>
        <v-btn icon="mdi-logout" @click="handleLogout">
          <v-icon>mdi-logout</v-icon>
          <v-tooltip activator="parent" location="bottom">Logout</v-tooltip>
        </v-btn>
      </template>
    </v-app-bar>

    <!-- ── Main Content ── router-view renders here -->
    <v-main>
      <router-view />
    </v-main>

  </v-app>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const route  = useRoute()
const router = useRouter()
const drawer = ref(true)

const allItems = [
  { title: 'Dashboard',   icon: 'mdi-view-dashboard',   route: '/dashboard',   roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
  { title: 'Activities',  icon: 'mdi-calendar-check',   route: '/activities',  roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
  { title: 'Evaluations', icon: 'mdi-star-check',        route: '/evaluations', roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
  { title: 'Teams',       icon: 'mdi-account-multiple',  route: '/teams',       roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
  { title: 'Sections',    icon: 'mdi-school',            route: '/sections',    roles: ['ADMIN', 'INSTRUCTOR'] },
  { title: 'Users',       icon: 'mdi-account-cog',       route: '/users',       roles: ['ADMIN', 'INSTRUCTOR'] },
  { title: 'Rubrics',    icon: 'mdi-clipboard-list',    route: '/rubrics',     roles: ['ADMIN', 'INSTRUCTOR'] },
  { title: 'Edit Profile', icon: 'mdi-account-edit',   route: '/profile',     roles: ['ADMIN','INSTRUCTOR','STUDENT'] },
]

const visibleItems = computed(() => {
  const role = authStore.userInfo?.role
  if (!role) return []
  return allItems.filter(i => i.roles.includes(role))
})

function navigate(path: string) {
  router.push(path)
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>
