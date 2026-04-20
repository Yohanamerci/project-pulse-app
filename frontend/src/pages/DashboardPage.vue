<template>
  <v-container class="pa-6">

    <!-- Welcome header -->
    <v-row class="mb-6">
      <v-col>
        <h1 class="text-h4 font-weight-bold mb-1">
          Welcome back, {{ authStore.userInfo?.username }}
        </h1>
        <p class="text-subtitle-1 text-medium-emphasis">
          Signed in as
          <v-chip size="x-small" :color="roleColor" class="ml-1">{{ authStore.userInfo?.role }}</v-chip>
        </p>
      </v-col>
    </v-row>

    <!-- Quick-nav cards -->
    <v-row>
      <v-col
        v-for="card in visibleCards"
        :key="card.title"
        cols="12"
        sm="6"
        md="4"
      >
        <v-card
          hover
          rounded="lg"
          elevation="2"
          class="h-100"
          @click="router.push(card.route)"
        >
          <v-card-text class="d-flex align-start pa-6 ga-4">
            <v-avatar :color="card.color" variant="tonal" size="52" rounded="lg">
              <v-icon :icon="card.icon" size="28" />
            </v-avatar>
            <div>
              <div class="text-subtitle-1 font-weight-bold mb-1">{{ card.title }}</div>
              <div class="text-body-2 text-medium-emphasis">{{ card.description }}</div>
            </div>
          </v-card-text>
          <v-card-actions class="px-6 pb-4 pt-0">
            <v-btn
              variant="tonal"
              :color="card.color"
              size="small"
              :append-icon="'mdi-arrow-right'"
              @click.stop="router.push(card.route)"
            >
              Open
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const roleColor = computed(() => ({
  ADMIN: 'error', INSTRUCTOR: 'warning', STUDENT: 'primary'
} as Record<string, string>)[authStore.userInfo?.role ?? ''] ?? 'grey')

const allCards = [
  {
    title: 'Weekly Activities',
    description: 'Submit and view weekly activity reports (WAR)',
    icon: 'mdi-calendar-check',
    color: 'primary',
    route: '/activities',
    roles: ['ADMIN', 'INSTRUCTOR', 'STUDENT'],
  },
  {
    title: 'Peer Evaluations',
    description: 'Submit peer evaluations for your teammates',
    icon: 'mdi-star-check',
    color: 'success',
    route: '/evaluations',
    roles: ['ADMIN', 'INSTRUCTOR', 'STUDENT'],
  },
  {
    title: 'Teams',
    description: 'Manage and view project teams',
    icon: 'mdi-account-multiple',
    color: 'info',
    route: '/teams',
    roles: ['ADMIN', 'INSTRUCTOR'],
  },
  {
    title: 'Sections',
    description: 'Manage course sections and active weeks',
    icon: 'mdi-school',
    color: 'warning',
    route: '/sections',
    roles: ['ADMIN'],
  },
  {
    title: 'Users',
    description: 'Manage instructors and students',
    icon: 'mdi-account-cog',
    color: 'error',
    route: '/users',
    roles: ['ADMIN'],
  },
]

const visibleCards = computed(() => {
  const role = authStore.userInfo?.role
  if (!role) return []
  return allCards.filter(c => c.roles.includes(role))
})
</script>
