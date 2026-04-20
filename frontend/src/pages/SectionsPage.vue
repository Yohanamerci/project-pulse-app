<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  getSections,
  createSection,
  updateSection,
  deleteSection,
  getSectionActiveWeeks,
  setActiveWeek,
  type SectionDto,
  type ActiveWeekDto,
} from '@/apis/sections'

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

// Set Active Week dialog
const weekDialog = ref(false)
const weekDialogSection = ref<SectionDto | null>(null)
const existingWeeks = ref<ActiveWeekDto[]>([])
const loadingWeeks = ref(false)
const weekFormRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)
const settingWeek = ref(false)
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

async function handleSetWeek() {
  if (!weekFormRef.value || !weekDialogSection.value) return
  const { valid } = await weekFormRef.value.validate()
  if (!valid) return

  settingWeek.value = true
  try {
    await setActiveWeek(weekDialogSection.value.id, {
      weekNumber: Number(weekForm.value.weekNumber),
      startDate: weekForm.value.startDate,
      endDate: weekForm.value.endDate,
    })
    snackbarMessage.value = `Week ${weekForm.value.weekNumber} set as active!`
    snackbar.value = true
    // Refresh weeks list in dialog
    existingWeeks.value = await getSectionActiveWeeks(weekDialogSection.value.id)
    await loadSections()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to set active week.'
  } finally {
    settingWeek.value = false
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
              Manage course sections and control the active submission week
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
              type="number"
              :rules="[
                (v) => !!v || 'Year is required',
                (v) => Number(v) >= 2000 || 'Enter a valid year',
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

    <!-- Set Active Week Dialog -->
    <v-dialog v-model="weekDialog" max-width="560" persistent>
      <v-card v-if="weekDialogSection" rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-calendar-edit" color="info" />
          Set Active Week — {{ weekDialogSection.name }}
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <!-- BR-2 hint -->
          <v-alert type="info" variant="tonal" density="compact" class="mb-4">
            {{ weekRangeHint(weekDialogSection.semester) }}
          </v-alert>

          <!-- Existing weeks -->
          <p class="text-subtitle-2 font-weight-medium mb-2">Existing weeks:</p>
          <div v-if="loadingWeeks" class="d-flex justify-center py-4">
            <v-progress-circular indeterminate size="24" color="info" />
          </div>
          <div v-else class="d-flex flex-wrap ga-2 mb-6">
            <v-chip
              v-for="week in existingWeeks"
              :key="week.id"
              :color="week.active ? 'success' : 'default'"
              size="small"
              :variant="week.active ? 'elevated' : 'tonal'"
            >
              Week {{ week.weekNumber }}
              <template v-if="week.active"> (active)</template>
            </v-chip>
            <span
              v-if="!existingWeeks.length && !loadingWeeks"
              class="text-body-2 text-medium-emphasis"
            >
              No weeks set yet
            </span>
          </div>

          <!-- Week form -->
          <v-form ref="weekFormRef">
            <v-text-field
              v-model="weekForm.weekNumber"
              label="Week Number *"
              type="number"
              :rules="[
                (v) => !!v || 'Week number is required',
                (v) => Number(v) >= weekMin(weekDialogSection!.semester) || `Minimum week ${weekMin(weekDialogSection!.semester)} for ${weekDialogSection!.semester}`,
                (v) => Number(v) <= 15 || 'Maximum week is 15',
              ]"
              variant="outlined"
              :min="weekMin(weekDialogSection.semester)"
              max="15"
              class="mb-4"
            />
            <v-text-field
              v-model="weekForm.startDate"
              label="Start Date *"
              type="date"
              :rules="[(v) => !!v || 'Start date is required']"
              variant="outlined"
              class="mb-4"
            />
            <v-text-field
              v-model="weekForm.endDate"
              label="End Date *"
              type="date"
              :rules="[
                (v) => !!v || 'End date is required',
                (v) => !weekForm.startDate || v >= weekForm.startDate || 'End date must be after start date',
              ]"
              variant="outlined"
            />
          </v-form>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="settingWeek" @click="weekDialog = false">Cancel</v-btn>
          <v-btn
            color="info"
            variant="elevated"
            :loading="settingWeek"
            @click="handleSetWeek"
          >
            Set Active Week
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
