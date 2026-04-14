<template>
  <v-container class="pa-6">
    <v-row align="center" class="mb-6">
      <v-col>
        <div class="d-flex align-center ga-3">
          <v-icon icon="mdi-account-group" size="32" color="success" />
          <div>
            <h1 class="text-h4 font-weight-bold">Peer Evaluations</h1>
            <p class="text-subtitle-1 text-medium-emphasis mb-0">
              Submit weekly peer evaluations for your teammates
            </p>
          </div>
        </div>
      </v-col>
      <v-col cols="auto">
        <v-btn color="success" prepend-icon="mdi-plus" :disabled="authStore.isInstructor">
          Submit Evaluation
        </v-btn>
      </v-col>
    </v-row>

    <!-- Business Rules reminder -->
    <v-row class="mb-4">
      <v-col>
        <v-alert type="warning" variant="tonal" icon="mdi-alert-circle-outline" density="compact">
          <strong>BR-3:</strong> Peer evaluations cannot be edited after submission. &nbsp;
          <strong>BR-4:</strong> Submissions are only allowed during the active week window.
        </v-alert>
      </v-col>
    </v-row>

    <!-- Week selector -->
    <v-row class="mb-4">
      <v-col cols="12" sm="4">
        <v-select
          label="Active Week"
          :items="['Week 5', 'Week 6', 'Week 7', 'Week 8']"
          variant="outlined"
          density="compact"
        />
      </v-col>
    </v-row>

    <!-- Teammates to evaluate -->
    <h3 class="text-h6 font-weight-medium mb-3">Teammates to Evaluate</h3>
    <v-row>
      <v-col v-for="n in 3" :key="n" cols="12" sm="6" md="4">
        <v-card variant="outlined" rounded="lg">
          <v-card-text class="d-flex align-center justify-space-between pa-4">
            <div class="d-flex align-center ga-3">
              <v-avatar color="primary" size="40">
                <v-icon icon="mdi-account" />
              </v-avatar>
              <div>
                <div class="font-weight-medium">Teammate {{ n }}</div>
                <div class="text-caption text-medium-emphasis">Student</div>
              </div>
            </div>
            <v-chip color="grey" size="small">Not submitted</v-chip>
          </v-card-text>
          <v-card-actions class="px-4 pb-4">
            <v-btn color="success" variant="tonal" size="small" block>
              Evaluate
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- TODO banner -->
    <v-alert type="info" variant="tonal" class="mt-6" icon="mdi-code-tags">
      <strong>TODO:</strong> Wire up
      <code>GET /api/v1/teams/{id}/members</code>,
      <code>POST /api/v1/evaluations</code>,
      <code>GET /api/v1/evaluations/my-scores</code>.
      See UC-14 (submit evaluation), UC-15 (view scores — BR-5: students see own only).
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
</script>
