<template>
  <v-container class="pa-6">
    <v-row align="center" class="mb-6">
      <v-col>
        <div class="d-flex align-center ga-3">
          <v-icon icon="mdi-calendar-check" size="32" color="primary" />
          <div>
            <h1 class="text-h4 font-weight-bold">Weekly Activity Reports</h1>
            <p class="text-subtitle-1 text-medium-emphasis mb-0">
              Submit and view your weekly activity logs (WAR)
            </p>
          </div>
        </div>
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" prepend-icon="mdi-plus">
          Submit Activity
        </v-btn>
      </v-col>
    </v-row>

    <!-- Week selector placeholder -->
    <v-row class="mb-4">
      <v-col cols="12" sm="4">
        <v-select
          label="Active Week"
          :items="['Week 5', 'Week 6', 'Week 7', 'Week 8']"
          variant="outlined"
          density="compact"
        />
      </v-col>
      <v-col cols="12" sm="4">
        <v-select
          label="Category"
          :items="['All', 'Development', 'Testing', 'Bug Fix', 'Documentation', 'Design', 'Meeting']"
          variant="outlined"
          density="compact"
        />
      </v-col>
    </v-row>

    <!-- Empty state -->
    <v-card variant="outlined" class="pa-8 text-center">
      <v-icon icon="mdi-calendar-blank-outline" size="64" color="grey-lighten-1" class="mb-4" />
      <h3 class="text-h6 font-weight-medium mb-2">No activities yet for this week</h3>
      <p class="text-body-2 text-medium-emphasis mb-4">
        Submit your weekly activity report to track development, testing, meetings, and more.
      </p>
      <v-btn color="primary" variant="tonal" prepend-icon="mdi-plus">
        Submit First Activity
      </v-btn>
    </v-card>

    <!-- TODO banner -->
    <v-alert type="info" variant="tonal" class="mt-6" icon="mdi-code-tags">
      <strong>TODO:</strong> Wire up
      <code>GET /api/v1/activities</code> and
      <code>POST /api/v1/activities</code> from the backend.
      See Use Cases UC-12 (submit WAR) and UC-13 (view WARs).
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
</script>
