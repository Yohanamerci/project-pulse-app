<template>
  <v-container class="pa-6">
    <v-row align="center" class="mb-6">
      <v-col>
        <div class="d-flex align-center ga-3">
          <v-icon icon="mdi-account-multiple" size="32" color="info" />
          <div>
            <h1 class="text-h4 font-weight-bold">Teams</h1>
            <p class="text-subtitle-1 text-medium-emphasis mb-0">
              Manage project teams and their members
            </p>
          </div>
        </div>
      </v-col>
      <v-col cols="auto" v-if="authStore.isAdmin">
        <v-btn color="info" prepend-icon="mdi-plus">
          Create Team
        </v-btn>
      </v-col>
    </v-row>

    <!-- Search -->
    <v-row class="mb-4">
      <v-col cols="12" sm="5">
        <v-text-field
          label="Search teams..."
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="compact"
          clearable
        />
      </v-col>
    </v-row>

    <!-- Team cards placeholder -->
    <v-row>
      <v-col v-for="n in 4" :key="n" cols="12" sm="6" md="4">
        <v-card rounded="lg" elevation="2" hover>
          <v-card-title class="d-flex align-center justify-space-between pa-4">
            <span>Team Alpha {{ n }}</span>
            <v-chip color="info" size="small">4 members</v-chip>
          </v-card-title>
          <v-card-text class="pt-0">
            <div class="text-caption text-medium-emphasis mb-2">Section: CS 4391 Spring 2026</div>
            <div class="d-flex align-center ga-1 flex-wrap">
              <v-avatar v-for="i in 4" :key="i" size="28" color="primary" class="text-caption">
                {{ i }}
              </v-avatar>
            </div>
          </v-card-text>
          <v-card-actions>
            <v-btn variant="text" color="info" size="small">View Details</v-btn>
            <v-spacer />
            <v-btn v-if="authStore.isAdmin" icon="mdi-pencil" variant="text" size="small" />
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- TODO banner -->
    <v-alert type="info" variant="tonal" class="mt-6" icon="mdi-code-tags">
      <strong>TODO:</strong> Wire up
      <code>GET /api/v1/teams</code>,
      <code>POST /api/v1/teams</code>,
      <code>PUT /api/v1/teams/{id}/members</code>.
      See UC-5 (create team), UC-6 (assign instructor — BR-1).
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
</script>
