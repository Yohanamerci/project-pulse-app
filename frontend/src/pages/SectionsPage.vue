<template>
  <v-container class="pa-6">
    <v-row align="center" class="mb-6">
      <v-col>
        <div class="d-flex align-center ga-3">
          <v-icon icon="mdi-school" size="32" color="warning" />
          <div>
            <h1 class="text-h4 font-weight-bold">Sections</h1>
            <p class="text-subtitle-1 text-medium-emphasis mb-0">
              Manage course sections and active weeks
            </p>
          </div>
        </div>
      </v-col>
      <v-col cols="auto">
        <v-btn color="warning" prepend-icon="mdi-plus">
          Create Section
        </v-btn>
      </v-col>
    </v-row>

    <!-- Sections table -->
    <v-card rounded="lg" elevation="2">
      <v-data-table
        :headers="headers"
        :items="sectionPlaceholders"
        class="rounded-lg"
      >
        <template #item.semester="{ item }">
          <v-chip :color="item.semester === 'SPRING' ? 'success' : 'warning'" size="small">
            {{ item.semester }} {{ item.year }}
          </v-chip>
        </template>
        <template #item.activeWeeks="{ item }">
          <v-chip size="small" variant="outlined">
            {{ item.semester === 'SPRING' ? 'Weeks 1–15' : 'Weeks 5–15' }}
          </v-chip>
        </template>
        <template #item.actions>
          <v-btn icon="mdi-pencil" variant="text" size="small" color="warning" />
          <v-btn icon="mdi-calendar-edit" variant="text" size="small" color="info" />
        </template>
      </v-data-table>
    </v-card>

    <!-- TODO banner -->
    <v-alert type="info" variant="tonal" class="mt-6" icon="mdi-code-tags">
      <strong>TODO:</strong> Wire up
      <code>GET /api/v1/sections</code>,
      <code>POST /api/v1/sections</code>,
      <code>POST /api/v1/sections/{id}/active-weeks</code>.
      See UC-1 (create section), UC-3 (set active week — BR-2).
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
const headers = [
  { title: 'Name', key: 'name', sortable: true },
  { title: 'Semester', key: 'semester', sortable: true },
  { title: 'Teams', key: 'teams', sortable: false },
  { title: 'Active Week Range', key: 'activeWeeks', sortable: false },
  { title: 'Actions', key: 'actions', sortable: false, align: 'end' as const },
]

const sectionPlaceholders = [
  { name: 'CS 4391 - 001', semester: 'SPRING', year: 2026, teams: 6 },
  { name: 'CS 4391 - 002', semester: 'SPRING', year: 2026, teams: 5 },
  { name: 'CS 4391 - 001', semester: 'FALL',   year: 2025, teams: 7 },
]
</script>
