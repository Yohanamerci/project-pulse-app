<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  submitActivity,
  updateActivity,
  deleteActivity,
  getMyActivities,
  getAllActivities,
  getTeamWeekActivities,
  type ActivityDto,
  type ActivityCategory,
  type ActivityStatus,
} from '@/apis/activities'
import { getMyTeam, getTeams, type TeamDto } from '@/apis/teams'
import { getSectionActiveWeek, getSectionActiveWeeks, type ActiveWeekDto } from '@/apis/sections'

const authStore = useAuthStore()

const activities = ref<ActivityDto[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const snackbar = ref(false)
const snackbarMessage = ref('')

// Submit dialog
const submitDialog = ref(false)
const submitting = ref(false)
const formRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)

// Edit dialog
const editDialog = ref(false)
const editingId = ref<number | null>(null)
const editFormRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)
const editing = ref(false)

// Delete confirm dialog
const deleteDialog = ref(false)
const deletingId = ref<number | null>(null)
const deletingName = ref('')
const deleting = ref(false)

const currentTeamId = ref<number | null>(null)
const currentWeekId = ref<number | null>(null)

const emptyForm = () => ({
  activityName: '',
  category: null as ActivityCategory | null,
  description: '',
  plannedHours: 1,
  actualHours: null as number | null,
  status: 'IN_PROGRESS' as ActivityStatus,
})

const form = ref(emptyForm())
const editForm = ref(emptyForm())

const CATEGORIES: ActivityCategory[] = [
  'DEVELOPMENT',
  'TESTING',
  'BUGFIX',
  'COMMUNICATION',
  'DOCUMENTATION',
  'DESIGN',
  'PLANNING',
  'LEARNING',
  'DEPLOYMENT',
  'SUPPORT',
  'MISCELLANEOUS',
]

const STATUSES: ActivityStatus[] = ['IN_PROGRESS', 'UNDER_TESTING', 'DONE']

const statusLabels: Record<ActivityStatus, string> = {
  IN_PROGRESS: 'In Progress',
  UNDER_TESTING: 'Under Testing',
  DONE: 'Done',
}

const statusColors: Record<ActivityStatus, string> = {
  IN_PROGRESS: 'warning',
  UNDER_TESTING: 'info',
  DONE: 'success',
}

const categoryColors: Record<ActivityCategory, string> = {
  DEVELOPMENT: 'primary',
  TESTING: 'success',
  BUGFIX: 'error',
  COMMUNICATION: 'teal',
  DOCUMENTATION: 'info',
  DESIGN: 'purple',
  PLANNING: 'orange',
  LEARNING: 'cyan',
  DEPLOYMENT: 'deep-purple',
  SUPPORT: 'brown',
  MISCELLANEOUS: 'grey',
}

const allHeaders = [
  { title: 'Student', key: 'studentName', sortable: true },
  { title: 'Activity', key: 'activityName', sortable: true },
  { title: 'Category', key: 'category', sortable: true },
  { title: 'Status', key: 'status', sortable: true },
  { title: 'Description', key: 'description', sortable: false },
  { title: 'Planned h', key: 'plannedHours', sortable: true },
  { title: 'Actual h', key: 'actualHours', sortable: true },
  { title: 'Week #', key: 'weekNumber', sortable: true },
  { title: 'Submitted', key: 'submittedAt', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false },
]

const tableHeaders = computed(() => {
  let headers = allHeaders
  if (authStore.isStudent) {
    headers = headers.filter((h) => h.key !== 'studentName')
  } else {
    headers = headers.filter((h) => h.key !== 'actions')
  }
  return headers
})

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
    form.value = emptyForm()
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
      activityName: form.value.activityName,
      category: form.value.category,
      description: form.value.description,
      plannedHours: Number(form.value.plannedHours),
      actualHours: form.value.actualHours != null ? Number(form.value.actualHours) : null,
      status: form.value.status,
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

function openEditDialog(item: ActivityDto) {
  editingId.value = item.id
  editForm.value = {
    // Guard against legacy seed data that stored '' / 0.0 before V5 migration
    activityName: item.activityName || item.description?.slice(0, 100) || '',
    category: item.category,
    description: item.description,
    plannedHours: (item.plannedHours ?? 0) >= 0.5 ? item.plannedHours : 1,
    actualHours: item.actualHours,
    status: item.status,
  }
  editDialog.value = true
}

async function handleEdit() {
  if (!editFormRef.value || editingId.value == null) return
  const { valid } = await editFormRef.value.validate()
  if (!valid || !editForm.value.category) return

  editing.value = true
  try {
    await updateActivity(editingId.value, {
      activityName: editForm.value.activityName,
      category: editForm.value.category,
      description: editForm.value.description,
      plannedHours: Number(editForm.value.plannedHours),
      actualHours: editForm.value.actualHours != null ? Number(editForm.value.actualHours) : null,
      status: editForm.value.status,
    })
    editDialog.value = false
    snackbarMessage.value = 'Activity updated successfully!'
    snackbar.value = true
    await loadActivities()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to update activity.'
  } finally {
    editing.value = false
  }
}

function openDeleteDialog(item: ActivityDto) {
  deletingId.value = item.id
  deletingName.value = item.activityName
  deleteDialog.value = true
}

async function handleDelete() {
  if (deletingId.value == null) return
  deleting.value = true
  try {
    await deleteActivity(deletingId.value)
    deleteDialog.value = false
    snackbarMessage.value = 'Activity deleted.'
    snackbar.value = true
    await loadActivities()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to delete activity.'
  } finally {
    deleting.value = false
  }
}

// WAR Report (Admin/Instructor)
const activeTab = ref('all')
const warTeams = ref<TeamDto[]>([])
const warTeamId = ref<number | null>(null)
const warWeeks = ref<ActiveWeekDto[]>([])
const warWeekId = ref<number | null>(null)
const warActivities = ref<ActivityDto[]>([])
const warLoading = ref(false)
const warError = ref<string | null>(null)
const warGenerated = ref(false)

async function loadWarTeams() {
  try {
    if (authStore.isStudent) {
      const myTeam = await getMyTeam()
      warTeams.value = [myTeam]
      warTeamId.value = myTeam.id
      await onWarTeamChange()
    } else {
      warTeams.value = await getTeams()
    }
  } catch {
    // ignore
  }
}

async function onWarTeamChange() {
  warWeekId.value = null
  warWeeks.value = []
  warActivities.value = []
  warGenerated.value = false
  const team = warTeams.value.find((t) => t.id === warTeamId.value)
  if (!team?.sectionId) return
  try {
    warWeeks.value = await getSectionActiveWeeks(team.sectionId)
  } catch {
    // ignore
  }
}

async function generateWarReport() {
  if (!warTeamId.value || !warWeekId.value) return
  warLoading.value = true
  warError.value = null
  warGenerated.value = false
  try {
    warActivities.value = await getTeamWeekActivities(warTeamId.value, warWeekId.value)
    warGenerated.value = true
  } catch (e: unknown) {
    warError.value = e instanceof Error ? e.message : 'Failed to load report.'
  } finally {
    warLoading.value = false
  }
}

onMounted(() => {
  loadActivities()
  if (!authStore.isAdmin) loadWarTeams()
})
</script>

<template>
  <v-container fluid class="pa-6">
    <!-- Header -->
    <div class="pp-page-hero mb-6">
      <div class="pp-page-hero-grid" />
      <div class="pp-page-hero-glow" style="background:radial-gradient(ellipse,rgba(96,165,250,0.25) 0%,transparent 70%)" />
      <div class="d-flex align-center justify-space-between flex-wrap gap-3 position-relative" style="z-index:1">
        <div class="d-flex align-center ga-4">
          <div class="pp-page-hero-icon" style="background:linear-gradient(135deg,#60A5FA,#3B82F6)">
            <v-icon icon="mdi-calendar-check" size="28" color="white" />
          </div>
          <div>
            <h1 class="text-h5 font-weight-bold text-white mb-0">Weekly Activity Reports</h1>
            <p class="text-body-2 mb-0" style="color:rgba(255,255,255,0.65)">
              <template v-if="authStore.isStudent">Log and track your weekly contributions</template>
              <template v-else>View all submitted activity reports across teams</template>
            </p>
          </div>
        </div>
        <v-btn v-if="authStore.isStudent" color="primary" variant="elevated" prepend-icon="mdi-plus" @click="openSubmitDialog">
          Submit Activity
        </v-btn>
      </div>
    </div>

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

    <!-- Tabs for Student/Instructor -->
    <v-tabs v-if="!authStore.isAdmin" v-model="activeTab" color="primary" class="mb-4">
      <v-tab value="all">{{ authStore.isStudent ? 'My Activities' : 'All Activities' }}</v-tab>
      <v-tab value="war">WAR Report</v-tab>
    </v-tabs>

    <!-- WAR Report Panel -->
    <template v-if="!authStore.isAdmin && activeTab === 'war'">
      <v-card rounded="lg" elevation="2" class="mb-4 pa-4">
        <v-row align="center" class="ga-2">
          <v-col cols="12" sm="4">
            <v-select
              v-model="warTeamId"
              :items="warTeams"
              item-title="name"
              item-value="id"
              label="Select Team"
              variant="outlined"
              density="compact"
              :clearable="!authStore.isStudent"
              :disabled="authStore.isStudent"
              @update:model-value="onWarTeamChange"
            />
          </v-col>
          <v-col cols="12" sm="4">
            <v-select
              v-model="warWeekId"
              :items="warWeeks"
              :item-title="(w: ActiveWeekDto) => `Week ${w.weekNumber} (${w.startDate} → ${w.endDate})`"
              item-value="id"
              label="Select Week"
              variant="outlined"
              density="compact"
              :disabled="!warTeamId || warWeeks.length === 0"
              clearable
            />
          </v-col>
          <v-col cols="12" sm="auto">
            <v-btn
              color="primary"
              :loading="warLoading"
              :disabled="!warTeamId || !warWeekId"
              @click="generateWarReport"
            >
              Generate Report
            </v-btn>
          </v-col>
        </v-row>
      </v-card>

      <v-alert v-if="warError" type="error" variant="tonal" closable class="mb-4" @click:close="warError = null">
        {{ warError }}
      </v-alert>

      <v-card v-if="warGenerated" rounded="lg" elevation="2">
        <v-card-title class="pa-4 pb-2">
          WAR Report — {{ warTeams.find((t) => t.id === warTeamId)?.name }}
          &nbsp;/&nbsp;
          Week {{ warWeeks.find((w) => w.id === warWeekId)?.weekNumber }}
        </v-card-title>
        <v-card-subtitle class="px-4 pb-3 text-medium-emphasis">
          {{ warActivities.length }} activit{{ warActivities.length === 1 ? 'y' : 'ies' }} submitted
        </v-card-subtitle>
        <v-divider />
        <v-data-table
          :headers="allHeaders.filter((h) => h.key !== 'actions')"
          :items="warActivities"
          item-value="id"
          :items-per-page="-1"
          hide-default-footer
          no-data-text="No activities submitted for this week."
        >
          <template #item.category="{ item }">
            <v-chip :color="categoryColors[item.category as ActivityCategory]" size="small" label>
              {{ item.category.replace(/_/g, ' ') }}
            </v-chip>
          </template>
          <template #item.status="{ item }">
            <v-chip :color="statusColors[item.status as ActivityStatus]" size="small" label>
              {{ statusLabels[item.status as ActivityStatus] }}
            </v-chip>
          </template>
          <template #item.submittedAt="{ item }">{{ formatDate(item.submittedAt) }}</template>
          <template #item.plannedHours="{ item }">{{ item.plannedHours }}h</template>
          <template #item.actualHours="{ item }">
            {{ item.actualHours != null ? `${item.actualHours}h` : '—' }}
          </template>
        </v-data-table>
      </v-card>
    </template>

    <!-- All Activities Table (admin always, others on "all" tab) -->
    <v-card v-if="authStore.isAdmin || activeTab === 'all'" rounded="lg" elevation="2">
      <v-data-table
        :headers="tableHeaders"
        :items="activities"
        :loading="loading"
        loading-text="Loading activities..."
        no-data-text="No activities found."
        item-value="id"
        :items-per-page="10"
        :items-per-page-options="[10, 25, 50, { value: -1, title: 'All' }]"
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

        <template #item.status="{ item }">
          <v-chip
            :color="statusColors[item.status as ActivityStatus]"
            size="small"
            label
          >
            {{ statusLabels[item.status as ActivityStatus] }}
          </v-chip>
        </template>

        <template #item.submittedAt="{ item }">
          {{ formatDate(item.submittedAt) }}
        </template>

        <template #item.plannedHours="{ item }">
          {{ item.plannedHours }}h
        </template>

        <template #item.actualHours="{ item }">
          {{ item.actualHours != null ? `${item.actualHours}h` : '—' }}
        </template>

        <template #item.actions="{ item }">
          <v-btn icon size="small" variant="text" @click="openEditDialog(item)">
            <v-icon>mdi-pencil</v-icon>
          </v-btn>
          <v-btn icon size="small" variant="text" color="error" @click="openDeleteDialog(item)">
            <v-icon>mdi-delete</v-icon>
          </v-btn>
        </template>
      </v-data-table>
    </v-card>

    <!-- Submit Activity Dialog -->
    <v-dialog v-model="submitDialog" max-width="560" persistent>
      <v-card rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-calendar-plus" color="primary" />
          Submit Activity
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <v-form ref="formRef">
            <v-text-field
              v-model="form.activityName"
              label="Activity Name *"
              :rules="[(v) => !!v || 'Activity name is required']"
              variant="outlined"
              class="mb-4"
            />

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

            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="form.plannedHours"
                  label="Planned Hours *"
                  type="number"
                  :rules="[
                    (v) => !!v || 'Planned hours is required',
                    (v) => Number(v) >= 0.5 || 'Minimum 0.5 hours',
                  ]"
                  variant="outlined"
                  min="0.5"
                  step="0.5"
                />
              </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model="form.actualHours"
                  label="Actual Hours"
                  type="number"
                  :rules="[
                    (v) => !v || Number(v) >= 0 || 'Must be 0 or more',
                  ]"
                  variant="outlined"
                  min="0"
                  step="0.5"
                  placeholder="Leave blank if not done"
                />
              </v-col>
            </v-row>

            <v-select
              v-model="form.status"
              :items="STATUSES"
              :item-title="(s) => statusLabels[s as ActivityStatus]"
              :item-value="(s) => s"
              label="Status *"
              :rules="[(v) => !!v || 'Status is required']"
              variant="outlined"
              class="mt-4"
            />
          </v-form>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="submitting" @click="submitDialog = false">Cancel</v-btn>
          <v-btn color="primary" variant="elevated" :loading="submitting" @click="handleSubmit">
            Submit
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Edit Activity Dialog -->
    <v-dialog v-model="editDialog" max-width="560" persistent>
      <v-card rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-pencil" color="primary" />
          Edit Activity
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <v-form ref="editFormRef">
            <v-text-field
              v-model="editForm.activityName"
              label="Activity Name *"
              :rules="[(v) => !!v || 'Activity name is required']"
              variant="outlined"
              class="mb-4"
            />

            <v-select
              v-model="editForm.category"
              :items="CATEGORIES"
              label="Category *"
              :rules="[(v) => !!v || 'Category is required']"
              variant="outlined"
              class="mb-4"
            />

            <v-textarea
              v-model="editForm.description"
              label="Description *"
              :rules="[(v) => !!v || 'Description is required']"
              variant="outlined"
              rows="3"
              class="mb-4"
            />

            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="editForm.plannedHours"
                  label="Planned Hours *"
                  type="number"
                  :rules="[
                    (v) => !!v || 'Planned hours is required',
                    (v) => Number(v) >= 0.5 || 'Minimum 0.5 hours',
                  ]"
                  variant="outlined"
                  min="0.5"
                  step="0.5"
                />
              </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model="editForm.actualHours"
                  label="Actual Hours"
                  type="number"
                  :rules="[
                    (v) => !v || Number(v) >= 0 || 'Must be 0 or more',
                  ]"
                  variant="outlined"
                  min="0"
                  step="0.5"
                />
              </v-col>
            </v-row>

            <v-select
              v-model="editForm.status"
              :items="STATUSES"
              :item-title="(s) => statusLabels[s as ActivityStatus]"
              :item-value="(s) => s"
              label="Status *"
              :rules="[(v) => !!v || 'Status is required']"
              variant="outlined"
              class="mt-4"
            />
          </v-form>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="editing" @click="editDialog = false">Cancel</v-btn>
          <v-btn color="primary" variant="elevated" :loading="editing" @click="handleEdit">
            Save
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirm Dialog -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card rounded="lg">
        <v-card-title class="pa-6 pb-4">Delete Activity</v-card-title>
        <v-card-text class="px-6 pb-4">
          Are you sure you want to delete <strong>{{ deletingName }}</strong>? This cannot be undone.
        </v-card-text>
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="deleting" @click="deleteDialog = false">Cancel</v-btn>
          <v-btn color="error" variant="elevated" :loading="deleting" @click="handleDelete">
            Delete
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
