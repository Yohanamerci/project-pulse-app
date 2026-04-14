<template>
  <v-container class="pa-6">
    <v-row align="center" class="mb-6">
      <v-col>
        <div class="d-flex align-center ga-3">
          <v-icon icon="mdi-account-cog" size="32" color="error" />
          <div>
            <h1 class="text-h4 font-weight-bold">Users</h1>
            <p class="text-subtitle-1 text-medium-emphasis mb-0">
              Manage instructors and students
            </p>
          </div>
        </div>
      </v-col>
      <v-col cols="auto">
        <v-btn color="error" prepend-icon="mdi-account-plus">
          Add User
        </v-btn>
      </v-col>
    </v-row>

    <!-- Filter tabs -->
    <v-tabs v-model="roleTab" color="primary" class="mb-4">
      <v-tab value="ALL">All</v-tab>
      <v-tab value="INSTRUCTOR">Instructors</v-tab>
      <v-tab value="STUDENT">Students</v-tab>
      <v-tab value="ADMIN">Admins</v-tab>
    </v-tabs>

    <!-- Search -->
    <v-text-field
      v-model="search"
      label="Search users..."
      prepend-inner-icon="mdi-magnify"
      variant="outlined"
      density="compact"
      clearable
      class="mb-4"
      style="max-width: 400px"
    />

    <!-- Users table -->
    <v-card rounded="lg" elevation="2">
      <v-data-table
        :headers="headers"
        :items="filteredUsers"
        :search="search"
        class="rounded-lg"
      >
        <template #item.role="{ item }">
          <v-chip
            :color="roleColor(item.role)"
            size="small"
          >
            {{ item.role }}
          </v-chip>
        </template>
        <template #item.enabled="{ item }">
          <v-icon
            :icon="item.enabled ? 'mdi-check-circle' : 'mdi-close-circle'"
            :color="item.enabled ? 'success' : 'error'"
          />
        </template>
        <template #item.actions>
          <v-btn icon="mdi-pencil" variant="text" size="small" color="primary" />
          <v-btn icon="mdi-lock-reset" variant="text" size="small" color="warning" />
        </template>
      </v-data-table>
    </v-card>

    <!-- TODO banner -->
    <v-alert type="info" variant="tonal" class="mt-6" icon="mdi-code-tags">
      <strong>TODO:</strong> Replace placeholder data with live calls to
      <code>GET /api/v1/users</code> and
      <code>POST /api/v1/users</code>.
      See UC-8 (add instructor), UC-9 (add student).
    </v-alert>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const roleTab = ref('ALL')
const search = ref('')

const headers = [
  { title: 'Username', key: 'username', sortable: true },
  { title: 'Name', key: 'name', sortable: true },
  { title: 'Email', key: 'email', sortable: true },
  { title: 'Role', key: 'role', sortable: true },
  { title: 'Enabled', key: 'enabled', sortable: false },
  { title: 'Actions', key: 'actions', sortable: false, align: 'end' as const },
]

const allUsers = [
  { username: 'admin', name: 'System Admin', email: 'admin@projectpulse.local', role: 'ADMIN', enabled: true },
  { username: 'drsmith', name: 'Dr. Smith', email: 'smith@tcu.edu', role: 'INSTRUCTOR', enabled: true },
  { username: 'jdoe', name: 'John Doe', email: 'j.doe@tcu.edu', role: 'STUDENT', enabled: true },
  { username: 'jsmith', name: 'Jane Smith', email: 'j.smith@tcu.edu', role: 'STUDENT', enabled: true },
]

const filteredUsers = computed(() =>
  roleTab.value === 'ALL' ? allUsers : allUsers.filter(u => u.role === roleTab.value)
)

function roleColor(role: string): string {
  return { ADMIN: 'error', INSTRUCTOR: 'warning', STUDENT: 'primary' }[role] ?? 'grey'
}
</script>
