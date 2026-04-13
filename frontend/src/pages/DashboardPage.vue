<template>
  <v-container class="pa-6">
    <v-row>
      <v-col cols="12">
        <h1 class="text-h4 font-weight-bold mb-1">
          Welcome, {{ authStore.userInfo?.username }}
        </h1>
        <p class="text-subtitle-1 text-medium-emphasis mb-6">
          Role: {{ authStore.userInfo?.role }}
        </p>
      </v-col>
    </v-row>

    <v-row>
      <v-col
        v-for="card in visibleCards"
        :key="card.title"
        cols="12"
        sm="6"
        md="4"
      >
        <v-card
          :to="card.route"
          hover
          rounded="lg"
          elevation="2"
        >
          <v-card-text class="d-flex align-center pa-6">
            <v-icon :icon="card.icon" size="40" :color="card.color" class="mr-4" />
            <div>
              <div class="text-h6 font-weight-bold">{{ card.title }}</div>
              <div class="text-body-2 text-medium-emphasis">{{ card.description }}</div>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const allCards = [
  {
    title: 'Weekly Activities',
    description: 'Submit and view weekly activity reports',
    icon: 'mdi-calendar-check',
    color: 'primary',
    route: '/activities',
    roles: ['ADMIN', 'INSTRUCTOR', 'STUDENT'],
  },
  {
    title: 'Peer Evaluations',
    description: 'Submit peer evaluations for your team',
    icon: 'mdi-account-group',
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
    description: 'Manage course sections',
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
  return allCards.filter((c) => c.roles.includes(role))
})
</script>
