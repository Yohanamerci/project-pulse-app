<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  submitActivity,
  getMyActivities,
  getAllActivities,
  type ActivityDto,
  type ActivityCategory,
} from '@/apis/activities'
import { getMyTeam } from '@/apis/teams'
import { getSectionActiveWeek } from '@/apis/sections'

const authStore = useAuthStore()

const activities = ref<ActivityDto[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const snackbar = ref(false)
const snackbarMessage = ref('')

// Submit dialog state
const submitDialog = ref(false)
const submitting = ref(false)
const formRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)

const currentTeamId = ref<number | null>(null)
const currentWeekId = ref<number | null>(null)

const form = ref({
  category: null as ActivityCategory | null,
  description: '',
  hours: 1,
})

const CATEGORIES: ActivityCategory[] = [
  'DEVELOPMENT',
  'TESTING',
  'BUG_FIX',
  'DOCUMENTATION',
  'DESIGN',
  'MEETING',
  'OTHER',
]

const categoryColors: Record<ActivityCategory, string> = {
  DEVELOPMENT: 'primary',
  TESTING: 'success',
  BUG_FIX: 'error',
  DOCUMENTATION: 'info',
  DESIGN: 'purple',
  MEETING: 'warning',
  OTHER: 'grey',
}

const allHeaders = [
  { title: 'Student', key: 'studentName', sortable: true },
  { title: 'Category', key: 'category', sortable: true },
  { title: 'Description', key: 'description', sortable: false },
  { title: 'Hours', key: 'hours', sortable: true },
  { title: 'Week #', key: 'weekNumber', sortable: true },
  { title: 'Submitted', key: 'submittedAt', sortable: true },
]

const tableHeaders = computed(() =>
  authStore.isStudent
    ? allHeaders.filter((h) => h.key !== 'studentName')
    : allHeaders
)

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

async function loadActivities() {
  loading.value = true
  error.value = null
  try {
    if (authStore.isStudent) {
      activities.value = await getMyActivities()
    } else {
      activities.value = await getAllActivities()
    }
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load activities.'
  } finally {
    loading.value = false
  }
}

async function openSubmitDialog() {
  error.value = null
  try {
    const team = await getMyTeam()
    currentTeamId.value = team.id
    if (!team.sectionId) {
      error.value = 'Your team is not assigned to a section.'
      return
    }
    const activeWeek = await getSectionActiveWeek(team.sectionId)
    if (!activeWeek) {
      error.value = 'No active week is set for your section. Ask your instructor to set one.'
      return
    }
    currentWeekId.value = activeWeek.id
    form.value = { category: null, description: '', hours: 1 }
    submitDialog.value = true
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : String(e)
    if (msg.includes('404') || msg.toLowerCase().includes('no active')) {
      error.value = 'No active week found for your section. Ask your instructor to set one.'
    } else {
      error.value = 'Could not load team or active week info. Make sure you are assigned to a team.'
    }
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  const { valid } = await formRef.value.validate()
  if (!valid) return
  if (!currentTeamId.value || !currentWeekId.value || !form.value.category) return

  submitting.value = true
  try {
    await submitActivity({
      teamId: currentTeamId.value,
      weekId: currentWeekId.value,
      category: form.value.category,
      description: form.value.description,
      hours: Number(form.value.hours),
    })
    submitDialog.value = false
    snackbarMessage.value = 'Activity submitted successfully!'
    snackbar.value = true
    await loadActivities()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to submit activity.'
  } finally {
    submitting.value = false
  }
}

onMounted(loadActivities)
</script>

<template>
  <v-container fluid class="pa-6">
    <!-- Header -->
    <v-row align="center" class="mb-6">
      <v-col>
        <div class="d-flex align-center ga-3">
          <v-avatar color="primary" variant="tonal" size="48" rounded="lg">
            <v-icon icon="mdi-calendar-check" size="28" />
          </v-avatar>
          <div>
            <h1 class="text-h5 font-weight-bold">Weekly Activity Reports</h1>
            <p class="text-body-2 text-medium-emphasis mb-0">
              <template v-if="authStore.isStudent">Log and track your weekly contributions</template>
              <template v-else>View all submitted activity reports across teams</template>
            </p>
          </div>
        </div>
      </v-col>
      <v-col v-if="authStore.isStudent" cols="auto">
        <v-btn color="primary" prepend-icon="mdi-plus" @click="openSubmitDialog">
          Submit Activity
        </v-btn>
      </v-col>
    </v-row>

    <!-- Error Alert -->
    <v-alert
      v-if="error"
      type="error"
      variant="tonal"
      closable
      class="mb-4"
      @click:close="error = null"
    >
      {{ error }}
    </v-alert>

    <!-- Data Table -->
    <v-card rounded="lg" elevation="2">
      <v-data-table
        :headers="tableHeaders"
        :items="activities"
        :loading="loading"
        loading-text="Loading activities..."
        no-data-text="No activities found."
        item-value="id"
      >
        <template #item.category="{ item }">
          <v-chip
            :color="categoryColors[item.category as ActivityCategory]"
            size="small"
            label
          >
            {{ item.category.replace(/_/g, ' ') }}
          </v-chip>
        </template>

        <template #item.submittedAt="{ item }">
          {{ formatDate(item.submittedAt) }}
        </template>

        <template #item.hours="{ item }">
          {{ item.hours }}h
        </template>
      </v-data-table>
    </v-card>

    <!-- Submit Activity Dialog (student only) -->
    <v-dialog v-model="submitDialog" max-width="560" persistent>
      <v-card rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-calendar-plus" color="primary" />
          Submit Activity
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <v-form ref="formRef">
            <v-select
              v-model="form.category"
              :items="CATEGORIES"
              label="Category *"
              :rules="[(v) => !!v || 'Category is required']"
              variant="outlined"
              class="mb-4"
            />

            <v-textarea
              v-model="form.description"
              label="Description *"
              :rules="[(v) => !!v || 'Description is required']"
              variant="outlined"
              rows="3"
              class="mb-4"
              placeholder="Briefly describe what you worked on..."
            />

            <v-text-field
              v-model="form.hours"
              label="Hours *"
              type="number"
              :rules="[
                (v) => !!v || 'Hours is required',
                (v) => Number(v) >= 0.5 || 'Minimum 0.5 hours',
              ]"
              variant="outlined"
              min="0.5"
              step="0.5"
            />
          </v-form>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="submitting" @click="submitDialog = false">Cancel</v-btn>
          <v-btn
            color="primary"
            variant="elevated"
            :loading="submitting"
            @click="handleSubmit"
          >
            Submit
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar" color="success" :timeout="3000" location="bottom end">
      {{ snackbarMessage }}
      <template #actions>
        <v-btn variant="text" @click="snackbar = false">Close</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>
