<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  getSections,
  createSection,
  updateSection,
  deleteSection,
  getSectionActiveWeeks,
  saveWeek,
  activateWeek,
  type SectionDto,
  type ActiveWeekDto,
} from '@/apis/sections'

const authStore = useAuthStore()

const sections = ref<SectionDto[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const snackbar = ref(false)
const snackbarMessage = ref('')

const SEMESTERS = ['FALL', 'SPRING']

const headers = [
  { title: 'Name', key: 'name', sortable: true },
  { title: 'Semester', key: 'semester', sortable: true },
  { title: 'Year', key: 'year', sortable: true },
  { title: 'Teams', key: 'teamCount', sortable: true },
  { title: 'Active Weeks', key: 'activeWeekCount', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false, align: 'end' as const },
]

async function loadSections() {
  loading.value = true
  error.value = null
  try {
    sections.value = await getSections()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load sections.'
  } finally {
    loading.value = false
  }
}

// Create / Edit dialog
const sectionDialog = ref(false)
const editingSection = ref<SectionDto | null>(null)
const sectionFormRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)
const savingSection = ref(false)
const sectionForm = ref({
  name: '',
  semester: 'FALL' as string,
  year: new Date().getFullYear(),
})

function openCreateDialog() {
  editingSection.value = null
  sectionForm.value = { name: '', semester: 'FALL', year: new Date().getFullYear() }
  sectionDialog.value = true
}

function openEditDialog(section: SectionDto) {
  editingSection.value = section
  sectionForm.value = { name: section.name, semester: section.semester, year: section.year }
  sectionDialog.value = true
}

async function handleSaveSection() {
  if (!sectionFormRef.value) return
  const { valid } = await sectionFormRef.value.validate()
  if (!valid) return

  savingSection.value = true
  try {
    if (editingSection.value) {
      await updateSection(editingSection.value.id, {
        name: sectionForm.value.name,
        semester: sectionForm.value.semester,
        year: Number(sectionForm.value.year),
      })
      snackbarMessage.value = 'Section updated!'
    } else {
      await createSection({
        name: sectionForm.value.name,
        semester: sectionForm.value.semester,
        year: Number(sectionForm.value.year),
      })
      snackbarMessage.value = 'Section created!'
    }
    sectionDialog.value = false
    snackbar.value = true
    await loadSections()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to save section.'
  } finally {
    savingSection.value = false
  }
}

// Manage Weeks dialog
const weekDialog = ref(false)
const weekDialogSection = ref<SectionDto | null>(null)
const existingWeeks = ref<ActiveWeekDto[]>([])
const loadingWeeks = ref(false)
const weekFormRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)
const savingWeek = ref(false)
const activatingWeekNumber = ref<number | null>(null)
const weekDateError = ref<string | null>(null)
const weekForm = ref({
  weekNumber: 1,
  startDate: '',
  endDate: '',
})

function weekRangeHint(semester: string): string {
  return semester === 'FALL'
    ? 'FALL semester: valid week range is 5–15'
    : 'SPRING semester: valid week range is 1–15'
}

function weekMin(semester: string): number {
  return semester === 'FALL' ? 5 : 1
}

async function openWeekDialog(section: SectionDto) {
  weekDialogSection.value = section
  weekForm.value = { weekNumber: weekMin(section.semester), startDate: '', endDate: '' }
  weekDateError.value = null
  weekDialog.value = true
  loadingWeeks.value = true
  try {
    existingWeeks.value = await getSectionActiveWeeks(section.id)
  } catch {
    existingWeeks.value = []
  } finally {
    loadingWeeks.value = false
  }
}

/** Click an existing week chip to pre-fill the form for editing */
function selectWeekForEdit(week: ActiveWeekDto) {
  weekForm.value = {
    weekNumber: week.weekNumber,
    startDate: String(week.startDate),
    endDate: String(week.endDate),
  }
  weekDateError.value = null
}

async function handleSaveWeek() {
  if (!weekDialogSection.value) return

  // Manual date validation — bypasses Vuetify's unreliable type="date" rule system
  weekDateError.value = null
  if (!weekForm.value.startDate) {
    weekDateError.value = 'Start date is required.'
    return
  }
  if (!weekForm.value.endDate) {
    weekDateError.value = 'End date is required.'
    return
  }
  if (weekForm.value.endDate < weekForm.value.startDate) {
    weekDateError.value = 'End date must be on or after the start date.'
    return
  }

  // Validate week number field (still uses Vuetify rules for the number input)
  if (weekFormRef.value) {
    const { valid } = await weekFormRef.value.validate()
    if (!valid) return
  }

  savingWeek.value = true
  try {
    await saveWeek(weekDialogSection.value.id, {
      weekNumber: Number(weekForm.value.weekNumber),
      startDate: weekForm.value.startDate,
      endDate: weekForm.value.endDate,
    })
    snackbarMessage.value = `Week ${weekForm.value.weekNumber} saved.`
    snackbar.value = true
    weekDateError.value = null
    existingWeeks.value = await getSectionActiveWeeks(weekDialogSection.value.id)
    await loadSections()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to save week.'
  } finally {
    savingWeek.value = false
  }
}

async function handleActivateWeek(weekNumber: number) {
  if (!weekDialogSection.value) return
  activatingWeekNumber.value = weekNumber
  try {
    await activateWeek(weekDialogSection.value.id, weekNumber)
    snackbarMessage.value = `Week ${weekNumber} is now active!`
    snackbar.value = true
    existingWeeks.value = await getSectionActiveWeeks(weekDialogSection.value.id)
    await loadSections()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to activate week.'
  } finally {
    activatingWeekNumber.value = null
  }
}

const deletingSectionId = ref<number | null>(null)

async function handleDeleteSection(section: SectionDto) {
  if (section.teamCount > 0) {
    error.value = `Cannot delete "${section.name}" — it still has ${section.teamCount} team(s). Remove all teams first.`
    return
  }
  if (!confirm(`Delete section "${section.name}"? This cannot be undone.`)) return
  deletingSectionId.value = section.id
  try {
    await deleteSection(section.id)
    snackbarMessage.value = `Section "${section.name}" deleted.`
    snackbar.value = true
    await loadSections()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to delete section.'
  } finally {
    deletingSectionId.value = null
  }
}

onMounted(loadSections)
</script>

<template>
  <v-container fluid class="pa-6">
    <!-- Header -->
    <v-row align="center" class="mb-6">
      <v-col>
        <div class="d-flex align-center ga-3">
          <v-avatar color="warning" variant="tonal" size="48" rounded="lg">
            <v-icon icon="mdi-school" size="28" />
          </v-avatar>
          <div>
            <h1 class="text-h5 font-weight-bold">Sections</h1>
            <p class="text-body-2 text-medium-emphasis mb-0">
              <template v-if="authStore.isAdmin">Manage course sections and control the active submission week</template>
              <template v-else>Create and edit sections, set active submission weeks</template>
            </p>
          </div>
        </div>
      </v-col>
      <v-col cols="auto">
        <v-btn color="warning" prepend-icon="mdi-plus" @click="openCreateDialog">
          Create Section
        </v-btn>
      </v-col>
    </v-row>

    <!-- BR-2 Alert -->
    <v-alert
      type="warning"
      variant="tonal"
      icon="mdi-calendar-alert"
      class="mb-4"
      density="compact"
    >
      <strong>BR-2:</strong> Active week range is <strong>Weeks 5–15 for Fall</strong> semesters
      and <strong>Weeks 1–15 for Spring</strong> semesters. Only one week can be active at a time per section.
    </v-alert>

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

    <!-- Sections Table -->
    <v-card rounded="lg" elevation="2">
      <v-data-table
        :headers="headers"
        :items="sections"
        :loading="loading"
        loading-text="Loading sections..."
        no-data-text="No sections found. Create one to get started."
        item-value="id"
      >
        <template #item.semester="{ item }">
          <v-chip
            :color="item.semester === 'FALL' ? 'orange' : 'blue'"
            size="small"
            label
          >
            {{ item.semester }}
          </v-chip>
        </template>

        <template #item.actions="{ item }">
          <v-btn
            icon="mdi-pencil"
            variant="text"
            size="small"
            color="warning"
            @click="openEditDialog(item)"
          />
          <v-btn
            icon="mdi-calendar-edit"
            variant="text"
            size="small"
            color="info"
            @click="openWeekDialog(item)"
          />
          <v-btn
            v-if="authStore.isAdmin"
            icon="mdi-delete-outline"
            variant="text"
            size="small"
            color="error"
            :loading="deletingSectionId === item.id"
            @click="handleDeleteSection(item)"
          />
        </template>
      </v-data-table>
    </v-card>

    <!-- Create / Edit Section Dialog -->
    <v-dialog v-model="sectionDialog" max-width="480" persistent>
      <v-card rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-school" color="warning" />
          {{ editingSection ? 'Edit Section' : 'Create Section' }}
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <v-form ref="sectionFormRef">
            <v-text-field
              v-model="sectionForm.name"
              label="Section Name *"
              :rules="[(v) => !!v || 'Name is required']"
              variant="outlined"
              class="mb-4"
            />
            <v-select
              v-model="sectionForm.semester"
              :items="SEMESTERS"
              label="Semester *"
              :rules="[(v) => !!v || 'Semester is required']"
              variant="outlined"
              class="mb-4"
            />
            <v-text-field
              v-model="sectionForm.year"
              label="Year *"
              type="text"
              inputmode="numeric"
              :rules="[
                (v) => !!v || 'Year is required',
                (v) => /^\d{4}$/.test(String(v)) || 'Enter a 4-digit year (e.g. 2026)',
                (v) => Number(v) >= 2000 || 'Year must be 2000 or later',
              ]"
              variant="outlined"
            />
          </v-form>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="savingSection" @click="sectionDialog = false">
            Cancel
          </v-btn>
          <v-btn
            color="warning"
            variant="elevated"
            :loading="savingSection"
            @click="handleSaveSection"
          >
            {{ editingSection ? 'Save' : 'Create' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Manage Weeks Dialog -->
    <v-dialog v-model="weekDialog" max-width="580" persistent>
      <v-card v-if="weekDialogSection" rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-calendar-edit" color="info" />
          Manage Weeks — {{ weekDialogSection.name }}
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <!-- BR-2 hint -->
          <v-alert type="info" variant="tonal" density="compact" class="mb-4">
            {{ weekRangeHint(weekDialogSection.semester) }}
          </v-alert>

          <!-- Existing weeks -->
          <p class="text-subtitle-2 font-weight-medium mb-1">
            Saved weeks
            <span class="text-caption text-medium-emphasis font-weight-regular ml-1">
              (click to load into form · use
              <v-icon size="12" icon="mdi-lightning-bolt" />
              to activate)
            </span>
          </p>
          <div v-if="loadingWeeks" class="d-flex justify-center py-4">
            <v-progress-circular indeterminate size="24" color="info" />
          </div>
          <div v-else class="d-flex flex-wrap ga-2 mb-5">
            <div
              v-for="week in existingWeeks"
              :key="week.id"
              class="d-flex align-center ga-1"
            >
              <v-chip
                :color="weekForm.weekNumber === week.weekNumber ? 'info' : week.active ? 'success' : 'default'"
                size="small"
                :variant="weekForm.weekNumber === week.weekNumber ? 'elevated' : week.active ? 'elevated' : 'tonal'"
                class="cursor-pointer"
                @click="selectWeekForEdit(week)"
              >
                Week {{ week.weekNumber }}
                <template v-if="week.active"> · active</template>
                <template v-else-if="week.startDate"> · {{ week.startDate }}</template>
              </v-chip>
              <!-- Activate button — only shown for inactive weeks -->
              <v-btn
                v-if="!week.active"
                icon="mdi-lightning-bolt"
                size="x-small"
                variant="tonal"
                color="warning"
                :loading="activatingWeekNumber === week.weekNumber"
                :disabled="activatingWeekNumber !== null"
                @click.stop="handleActivateWeek(week.weekNumber)"
              />
            </div>
            <span
              v-if="!existingWeeks.length && !loadingWeeks"
              class="text-body-2 text-medium-emphasis"
            >
              No weeks saved yet
            </span>
          </div>

          <v-divider class="mb-4" />

          <!-- Week form -->
          <p class="text-subtitle-2 font-weight-medium mb-3">
            {{ existingWeeks.some(w => w.weekNumber === Number(weekForm.weekNumber)) ? 'Edit Week' : 'New Week' }}
          </p>

          <v-form ref="weekFormRef">
            <v-text-field
              v-model="weekForm.weekNumber"
              label="Week Number *"
              type="text"
              inputmode="numeric"
              :rules="[
                (v) => !!v || 'Week number is required',
                (v) => /^\d+$/.test(String(v)) || 'Must be a number',
                (v) => Number(v) >= weekMin(weekDialogSection!.semester) || `Minimum week ${weekMin(weekDialogSection!.semester)} for ${weekDialogSection!.semester}`,
                (v) => Number(v) <= 15 || 'Maximum week is 15',
              ]"
              variant="outlined"
              class="mb-4"
            />
          </v-form>

          <!-- Plain date inputs — Vuetify's rules on type=date are unreliable;
               we validate manually in handleSaveWeek() instead -->
          <div class="mb-4">
            <label class="text-body-2 font-weight-medium d-block mb-1">Start Date *</label>
            <input
              v-model="weekForm.startDate"
              type="date"
              class="pp-date-input"
            />
          </div>
          <div class="mb-2">
            <label class="text-body-2 font-weight-medium d-block mb-1">End Date *</label>
            <input
              v-model="weekForm.endDate"
              type="date"
              :min="weekForm.startDate || undefined"
              class="pp-date-input"
            />
          </div>

          <!-- Manual date validation error -->
          <v-alert
            v-if="weekDateError"
            type="error"
            variant="tonal"
            density="compact"
            class="mt-3"
          >
            {{ weekDateError }}
          </v-alert>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="savingWeek" @click="weekDialog = false">Close</v-btn>
          <v-btn
            color="info"
            variant="elevated"
            :loading="savingWeek"
            @click="handleSaveWeek"
          >
            Save Week
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

<style scoped>
.pp-date-input {
  display: block;
  width: 100%;
  padding: 14px 16px;
  font-size: 1rem;
  line-height: 1.5;
  color: rgba(var(--v-theme-on-surface), var(--v-high-emphasis-opacity));
  background-color: transparent;
  border: 1px solid rgba(var(--v-theme-on-surface), 0.38);
  border-radius: 4px;
  outline: none;
  transition: border-color 0.15s ease;
  font-family: inherit;
  color-scheme: light dark;
}

.pp-date-input:hover {
  border-color: rgba(var(--v-theme-on-surface), 0.87);
}

.pp-date-input:focus {
  border-color: rgb(var(--v-theme-info));
  border-width: 2px;
  padding: 13px 15px;
}
</style>
