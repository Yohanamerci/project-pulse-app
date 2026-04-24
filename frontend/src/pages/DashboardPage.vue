<template>
  <v-container class="pa-6">

    <!-- ── Hero welcome banner ── -->
    <v-sheet
      rounded="xl"
      class="pa-8 mb-8 overflow-hidden position-relative"
      style="background: linear-gradient(135deg, #1D4ED8 0%, #4F46E5 100%)"
    >
      <!-- Decorative circles -->
      <div
        class="position-absolute"
        style="right: -80px; top: -80px; width: 280px; height: 280px;
               border-radius: 50%; background: rgba(255,255,255,0.05); pointer-events: none"
      />
      <div
        class="position-absolute"
        style="right: 80px; bottom: -100px; width: 180px; height: 180px;
               border-radius: 50%; background: rgba(255,255,255,0.04); pointer-events: none"
      />

      <div class="position-relative">
        <h1 class="text-h4 font-weight-bold text-white mb-2">
          Welcome back, {{ authStore.userInfo?.username }}
        </h1>
        <div class="d-flex align-center ga-2">
          <span class="text-body-1" style="color: rgba(255,255,255,0.7)">Signed in as</span>
          <v-chip
            :color="roleColor"
            size="small"
            label
            variant="elevated"
            class="font-weight-medium"
          >
            {{ authStore.userInfo?.role }}
          </v-chip>
        </div>
      </div>
    </v-sheet>

    <!-- ── Quick-nav cards ── -->
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
          rounded="xl"
          elevation="0"
          border
          class="h-100 cursor-pointer"
          @click="router.push(card.route)"
        >
          <!-- Colored accent top bar -->
          <div
            class="rounded-t-xl"
            :style="`height: 3px; background: rgb(var(--v-theme-${card.color}))`"
          />
          <v-card-text class="d-flex align-start pa-6 ga-4">
            <v-avatar :color="card.color" variant="tonal" size="52" rounded="xl">
              <v-icon :icon="card.icon" size="26" />
            </v-avatar>
            <div>
              <div class="text-subtitle-1 font-weight-bold mb-1">{{ card.title }}</div>
              <div class="text-body-2 text-medium-emphasis">{{ card.description }}</div>
            </div>
          </v-card-text>
          <v-card-actions class="px-6 pb-5 pt-0">
            <v-btn
              variant="tonal"
              :color="card.color"
              size="small"
              rounded="lg"
              append-icon="mdi-arrow-right"
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
