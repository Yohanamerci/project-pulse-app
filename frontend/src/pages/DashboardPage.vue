<template>
  <v-container class="pa-6">

    <!-- ── Hero welcome banner ── -->
    <div class="pp-page-hero mb-8 pa-8">
      <div class="pp-page-hero-grid" />
      <div class="position-absolute" style="width:340px;height:340px;right:-80px;top:-80px;border-radius:50%;background:radial-gradient(circle,rgba(0,212,255,0.14) 0%,rgba(124,58,237,0.10) 50%,transparent 70%);pointer-events:none" />
      <div class="position-absolute" style="width:200px;height:200px;left:40%;bottom:-60px;border-radius:50%;background:radial-gradient(circle,rgba(52,211,153,0.10) 0%,transparent 65%);pointer-events:none" />

      <div class="position-relative" style="z-index:1">
        <p class="text-caption mb-1" style="color:rgba(255,255,255,0.45);letter-spacing:1.5px;text-transform:uppercase">
          Project Pulse
        </p>
        <h1 class="text-h4 font-weight-bold mb-2" style="color:#fff;letter-spacing:-0.5px">
          Welcome back,
          <span style="background:linear-gradient(90deg,#00D4FF,#A78BFA);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text">
            {{ authStore.userInfo?.username }}
          </span>
        </h1>
        <div class="d-flex align-center ga-2">
          <span class="text-body-2" style="color:rgba(255,255,255,0.55)">Signed in as</span>
          <v-chip :color="roleColor" size="small" label variant="elevated" class="font-weight-bold">
            {{ authStore.userInfo?.role }}
          </v-chip>
        </div>
      </div>
    </div>

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
