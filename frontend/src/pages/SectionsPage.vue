<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
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
import { getTeamsBySection, type TeamDto } from '@/apis/teams'

const authStore = useAuthStore()

const sections = ref<SectionDto[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const snackbar = ref(false)
const snackbarMessage = ref('')

const SEMESTERS = ['FALL', 'SPRING']

// UC-2: search field — filters by section name (case-insensitive)
const searchQuery = ref('')
const filteredSections = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return sections.value
  return sections.value.filter(s => s.name.toLowerCase().includes(q))
})

// UC-2 + UC-3: Name and Team Names are the primary search-result columns.
// Full details (members, active week) appear in the expandable row.
// Default sort: section name descending (per UC-2 sort criteria).
const headers = [
  { title: '', key: 'data-table-expand', width: '48px' },
  { title: 'Name', key: 'name', sortable: true },
  { title: 'Semester', key: 'semester', sortable: true },
  { title: 'Year', key: 'year', sortable: true },
  { title: 'Team Names', key: 'teamNames', sortable: false },
  { title: 'Active Weeks', key: 'activeWeekCount', sortable: true },
  { title: 'Actions', key: 'actions', sortable: false, align: 'end' as const },
]
const defaultSort = [{ key: 'name', order: 'desc' as const }]

// Tracks expanded row IDs and lazily loaded team data
const expandedIds = ref<number[]>([])
const sectionTeams = ref<Record<number, TeamDto[] | null>>({})

async function onExpandedChange(newExpanded: number[]) {
  for (const id of newExpanded) {
    if (sectionTeams.value[id] !== undefined) continue
    sectionTeams.value[id] = null // mark as loading
    try {
      sectionTeams.value[id] = await getTeamsBySection(id)
    } catch {
      sectionTeams.value[id] = []
    }
  }
}

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

/** Validate a YYYY-MM-DD string. */
function isValidDate(v: string): boolean {
  return /^\d{4}-\d{2}-\d{2}$/.test(v) && !isNaN(Date.parse(v))
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

  weekDateError.value = null
  if (!weekForm.value.startDate || !isValidDate(weekForm.value.startDate)) {
    weekDateError.value = 'Start date is required (format: YYYY-MM-DD).'
    return
  }
  if (!weekForm.value.endDate || !isValidDate(weekForm.value.endDate)) {
    weekDateError.value = 'End date is required (format: YYYY-MM-DD).'
    return
  }
  if (weekForm.value.endDate < weekForm.value.startDate) {
    weekDateError.value = 'End date must be on or after the start date.'
    return
  }

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
    <div class="pp-page-hero mb-6">
      <div class="pp-page-hero-grid" />
      <div class="pp-page-hero-glow" style="background:radial-gradient(ellipse,rgba(252,211,77,0.22) 0%,transparent 70%)" />
      <div class="d-flex align-center justify-space-between flex-wrap gap-3 position-relative" style="z-index:1">
        <div class="d-flex align-center ga-4">
          <div class="pp-page-hero-icon" style="background:linear-gradient(135deg,#FCD34D,#F59E0B)">
            <v-icon icon="mdi-school" size="28" color="white" />
          </div>
          <div>
            <h1 class="text-h5 font-weight-bold text-white mb-0">Sections</h1>
            <p class="text-body-2 mb-0" style="color:rgba(255,255,255,0.65)">
              <template v-if="authStore.isAdmin">Manage course sections and control the active submission week</template>
              <template v-else>Create and edit sections, set active submission weeks</template>
            </p>
          </div>
        </div>
        <v-btn color="warning" variant="elevated" prepend-icon="mdi-plus" @click="openCreateDialog">
          Create Section
        </v-btn>
      </div>
    </div>

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

    <!-- UC-2: Search field — filters by section name -->
    <v-card rounded="lg" elevation="1" class="mb-4 pa-4">
      <v-text-field
        v-model="searchQuery"
        label="Search sections by name"
        prepend-inner-icon="mdi-magnify"
        variant="outlined"
        density="compact"
        clearable
        hide-details
        placeholder="e.g. CS 4391"
      />
      <p
        v-if="searchQuery && !filteredSections.length"
        class="text-body-2 text-error mt-3 mb-0"
      >
        No sections found matching "{{ searchQuery }}". Try a different name or
        <v-btn variant="text" size="small" color="warning" class="pa-0" @click="openCreateDialog">
          create a new section
        </v-btn>.
      </p>
    </v-card>

    <!-- Sections Table — UC-2: sorted by name desc; UC-3: expand arrow reveals details -->
    <v-card rounded="lg" elevation="2">
      <v-data-table
        :headers="headers"
        :items="filteredSections"
        :loading="loading"
        :sort-by="defaultSort"
        loading-text="Loading sections..."
        no-data-text="No sections found. Create one to get started."
        item-value="id"
        show-expand
        v-model:expanded="expandedIds"
        @update:expanded="onExpandedChange"
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

        <!-- UC-2: team names sorted ascending -->
        <template #item.teamNames="{ item }">
          <div v-if="item.teamNames.length" class="d-flex flex-wrap ga-1 py-1">
            <v-chip
              v-for="name in [...item.teamNames].sort()"
              :key="name"
              size="x-small"
              color="teal"
              variant="tonal"
            >
              {{ name }}
            </v-chip>
          </div>
          <span v-else class="text-caption text-medium-emphasis">—</span>
        </template>

        <template #item.actions="{ item }">
          <v-btn
            prepend-icon="mdi-pencil"
            variant="tonal"
            size="small"
            color="warning"
            class="mr-1"
            @click.stop="openEditDialog(item)"
          >
            Edit
          </v-btn>
          <v-btn
            prepend-icon="mdi-calendar-edit"
            variant="tonal"
            size="small"
            color="info"
            class="mr-1"
            @click.stop="openWeekDialog(item)"
          >
            Weeks
          </v-btn>
          <v-btn
            v-if="authStore.isAdmin"
            prepend-icon="mdi-delete"
            variant="tonal"
            size="small"
            color="error"
            :loading="deletingSectionId === item.id"
            @click.stop="handleDeleteSection(item)"
          >
            Delete
          </v-btn>
        </template>

        <!-- UC-3: Expanded section detail — teams + members + active week -->
        <template #expanded-row="{ item, columns }">
          <tr>
            <td :colspan="columns.length" class="pa-0">
              <v-sheet color="surface-variant" class="pa-4">
                <div v-if="sectionTeams[item.id] === null" class="d-flex align-center ga-2 py-2">
                  <v-progress-circular indeterminate size="18" color="info" />
                  <span class="text-body-2">Loading section details…</span>
                </div>

                <template v-else>
                  <!-- Active week summary -->
                  <p class="text-subtitle-2 font-weight-medium mb-2">
                    <v-icon icon="mdi-calendar-check" size="16" class="mr-1" />
                    Active Weeks: {{ item.activeWeekCount }}
                    <span v-if="item.activeWeekCount === 0" class="text-caption text-medium-emphasis font-weight-regular ml-1">
                      (none set — use the Weeks button to configure)
                    </span>
                  </p>

                  <v-divider class="mb-3" />

                  <!-- Teams -->
                  <p class="text-subtitle-2 font-weight-medium mb-2">
                    <v-icon icon="mdi-account-group" size="16" class="mr-1" />
                    Teams ({{ sectionTeams[item.id]?.length ?? 0 }})
                  </p>

                  <div v-if="!sectionTeams[item.id]?.length" class="text-body-2 text-medium-emphasis mb-2">
                    No teams in this section yet.
                  </div>

                  <v-row v-else dense class="mb-1">
                    <v-col
                      v-for="team in sectionTeams[item.id]"
                      :key="team.id"
                      cols="12"
                      sm="6"
                      md="4"
                    >
                      <v-card variant="outlined" rounded="lg" class="pa-3">
                        <p class="text-body-2 font-weight-bold mb-2">{{ team.name }}</p>

                        <!-- Instructors -->
                        <div class="mb-2">
                          <span class="text-caption text-medium-emphasis">Instructors: </span>
                          <span v-if="!team.instructors.length" class="text-caption text-error">None assigned</span>
                          <v-chip
                            v-for="ins in team.instructors"
                            :key="ins.id"
                            size="x-small"
                            color="purple"
                            variant="tonal"
                            class="mr-1"
                          >
                            {{ ins.firstName }} {{ ins.lastName }}
                          </v-chip>
                        </div>

                        <!-- Students -->
                        <div>
                          <span class="text-caption text-medium-emphasis">Students: </span>
                          <span v-if="!team.students.length" class="text-caption text-medium-emphasis">None</span>
                          <v-chip
                            v-for="stu in team.students"
                            :key="stu.id"
                            size="x-small"
                            color="teal"
                            variant="tonal"
                            class="mr-1 mb-1"
                          >
                            {{ stu.firstName }} {{ stu.lastName }}
                          </v-chip>
                        </div>

                        <div v-if="team.rubricName" class="mt-2">
                          <span class="text-caption text-medium-emphasis">Rubric: </span>
                          <v-chip size="x-small" color="amber" variant="tonal">{{ team.rubricName }}</v-chip>
                        </div>
                      </v-card>
                    </v-col>
                  </v-row>
                </template>
              </v-sheet>
            </td>
          </tr>
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

          <p class="text-subtitle-2 font-weight-medium mb-3">
            {{ existingWeeks.some(w => w.weekNumber === Number(weekForm.weekNumber)) ? 'Edit Week' : 'New Week' }}
          </p>

          <!-- UC-6 fix: use text fields with YYYY-MM-DD format to avoid
               the native date picker's year-field cycling bug. -->
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

            <v-text-field
              v-model="weekForm.startDate"
              label="Start Date *"
              placeholder="YYYY-MM-DD"
              hint="Format: YYYY-MM-DD — e.g. 2026-04-23"
              persistent-hint
              :rules="[
                (v) => !!v || 'Start date is required',
                (v) => isValidDate(v) || 'Enter a valid date (YYYY-MM-DD)',
              ]"
              variant="outlined"
              class="mb-4"
            />

            <v-text-field
              v-model="weekForm.endDate"
              label="End Date *"
              placeholder="YYYY-MM-DD"
              hint="Format: YYYY-MM-DD — e.g. 2026-04-29"
              persistent-hint
              :rules="[
                (v) => !!v || 'End date is required',
                (v) => isValidDate(v) || 'Enter a valid date (YYYY-MM-DD)',
                (v) => !weekForm.startDate || v >= weekForm.startDate || 'End date must be on or after the start date',
              ]"
              variant="outlined"
              class="mb-2"
            />
          </v-form>

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
