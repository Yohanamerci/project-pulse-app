<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  getTeams,
  getMyTeam,
  createTeam,
  addStudent,
  removeStudent,
  assignInstructor,
  deleteTeam,
  type TeamDto,
} from '@/apis/teams'
import { getSections, type SectionDto } from '@/apis/sections'
import { getRubrics, type RubricDto } from '@/apis/rubrics'
import { getUsers, type UserDto } from '@/apis/users'

const authStore = useAuthStore()

const teams = ref<TeamDto[]>([])
const myTeam = ref<TeamDto | null>(null)
const loading = ref(false)
const myTeamLoading = ref(false)
const myTeamNotFound = ref(false)
const error = ref<string | null>(null)
const snackbar = ref(false)
const snackbarMessage = ref('')

const search = ref('')

const filteredTeams = computed(() => {
  if (!search.value) return teams.value
  const q = search.value.toLowerCase()
  return teams.value.filter(
    (t) =>
      t.name.toLowerCase().includes(q) ||
      (t.sectionName ?? '').toLowerCase().includes(q)
  )
})

// Sections, Rubrics, Users for dropdowns
const sections = ref<SectionDto[]>([])
const rubrics = ref<RubricDto[]>([])
const allUsers = ref<UserDto[]>([])

const instructors = computed(() =>
  allUsers.value.filter((u) => u.role === 'INSTRUCTOR')
)
const students = computed(() =>
  allUsers.value.filter((u) => u.role === 'STUDENT')
)

const sectionItems = computed(() =>
  sections.value.map((s) => ({ title: `${s.name} (${s.semester} ${s.year})`, value: s.id }))
)
const rubricItems = computed(() => [
  { title: 'No rubric', value: null },
  ...rubrics.value.map((r) => ({ title: r.name, value: r.id })),
])
const instructorItems = computed(() =>
  instructors.value.map((u) => ({
    title: `${u.firstName} ${u.lastName} (@${u.username})`,
    value: u.id,
  }))
)
const studentItems = computed(() =>
  students.value.map((u) => ({
    title: `${u.firstName} ${u.lastName} (@${u.username})`,
    value: u.id,
  }))
)

// Create Team dialog
const createDialog = ref(false)
const createFormRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)
const creating = ref(false)
const createForm = ref({
  name: '',
  sectionId: null as number | null,
  rubricId: null as number | null,
})

async function handleCreateTeam() {
  if (!createFormRef.value) return
  const { valid } = await createFormRef.value.validate()
  if (!valid || !createForm.value.sectionId) return
  creating.value = true
  try {
    await createTeam({
      name: createForm.value.name,
      sectionId: createForm.value.sectionId,
      rubricId: createForm.value.rubricId ?? undefined,
    })
    createDialog.value = false
    snackbarMessage.value = `Team "${createForm.value.name}" created!`
    snackbar.value = true
    await loadTeams()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to create team.'
  } finally {
    creating.value = false
  }
}

function openCreateDialog() {
  createForm.value = { name: '', sectionId: null, rubricId: null }
  createDialog.value = true
}

// Manage Team dialog
const manageDialog = ref(false)
const managedTeam = ref<TeamDto | null>(null)
const selectedInstructorId = ref<number | null>(null)
const selectedStudentId = ref<number | null>(null)
const assigningInstructor = ref(false)
const addingStudent = ref(false)
const removingStudentId = ref<number | null>(null)

function openManageDialog(team: TeamDto) {
  managedTeam.value = { ...team, students: [...team.students], instructors: [...team.instructors] }
  selectedInstructorId.value = null
  selectedStudentId.value = null
  manageDialog.value = true
}

async function handleAssignInstructor() {
  if (!managedTeam.value || !selectedInstructorId.value) return
  assigningInstructor.value = true
  try {
    const updated = await assignInstructor(managedTeam.value.id, selectedInstructorId.value)
    managedTeam.value = updated
    selectedInstructorId.value = null
    snackbarMessage.value = 'Instructor assigned!'
    snackbar.value = true
    await loadTeams()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to assign instructor.'
  } finally {
    assigningInstructor.value = false
  }
}

async function handleAddStudent() {
  if (!managedTeam.value || !selectedStudentId.value) return
  addingStudent.value = true
  try {
    const updated = await addStudent(managedTeam.value.id, selectedStudentId.value)
    managedTeam.value = updated
    selectedStudentId.value = null
    snackbarMessage.value = 'Student added!'
    snackbar.value = true
    await loadTeams()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to add student.'
  } finally {
    addingStudent.value = false
  }
}

async function handleRemoveStudent(studentId: number) {
  if (!managedTeam.value) return
  removingStudentId.value = studentId
  try {
    const updated = await removeStudent(managedTeam.value.id, studentId)
    managedTeam.value = updated
    snackbarMessage.value = 'Student removed.'
    snackbar.value = true
    await loadTeams()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to remove student.'
  } finally {
    removingStudentId.value = null
  }
}

const deletingTeamId = ref<number | null>(null)

async function handleDeleteTeam(team: TeamDto) {
  if (team.students.length > 0) {
    error.value = `Cannot delete "${team.name}" — remove all ${team.students.length} student(s) first.`
    return
  }
  if (!confirm(`Delete team "${team.name}"? This cannot be undone.`)) return
  deletingTeamId.value = team.id
  try {
    await deleteTeam(team.id)
    snackbarMessage.value = `Team "${team.name}" deleted.`
    snackbar.value = true
    await loadTeams()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to delete team.'
  } finally {
    deletingTeamId.value = null
  }
}

async function loadTeams() {
  loading.value = true
  error.value = null
  try {
    teams.value = await getTeams()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load teams.'
  } finally {
    loading.value = false
  }
}

async function loadMyTeam() {
  myTeamLoading.value = true
  myTeamNotFound.value = false
  error.value = null
  try {
    myTeam.value = await getMyTeam()
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : String(e)
    if (msg.includes('404') || msg.toLowerCase().includes('not found')) {
      myTeamNotFound.value = true
    } else {
      error.value = msg
    }
  } finally {
    myTeamLoading.value = false
  }
}

async function loadSupportData() {
  try {
    const [s, r, u] = await Promise.all([getSections(), getRubrics(), getUsers()])
    sections.value = s
    rubrics.value = r
    allUsers.value = u
  } catch {
    // non-critical, silently ignore
  }
}

onMounted(async () => {
  if (authStore.isStudent) {
    await loadMyTeam()
  } else {
    await loadTeams()
    if (authStore.isAdmin) {
      await loadSupportData()
    }
  }
})
</script>

<template>
  <v-container fluid class="pa-6">
    <!-- Header -->
    <v-row align="center" class="mb-6">
      <v-col>
        <div class="d-flex align-center ga-3">
          <v-avatar color="info" variant="tonal" size="48" rounded="lg">
            <v-icon icon="mdi-account-multiple" size="28" />
          </v-avatar>
          <div>
            <h1 class="text-h5 font-weight-bold">Teams</h1>
            <p class="text-body-2 text-medium-emphasis mb-0">
              <span v-if="authStore.isAdmin">Create teams, assign instructors and students</span>
              <span v-else-if="authStore.isInstructor">View all teams</span>
              <span v-else>View your team and teammates</span>
            </p>
          </div>
        </div>
      </v-col>
      <v-col v-if="authStore.isAdmin" cols="auto">
        <v-btn color="info" prepend-icon="mdi-plus" @click="openCreateDialog">
          Create Team
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

    <!-- STUDENT: My Team -->
    <template v-if="authStore.isStudent">
      <div v-if="myTeamLoading" class="d-flex justify-center py-12">
        <v-progress-circular indeterminate color="info" />
      </div>

      <v-alert
        v-else-if="myTeamNotFound"
        type="warning"
        variant="tonal"
        density="compact"
        class="mb-4"
      >
        You are not currently assigned to a team. Contact your instructor.
      </v-alert>

      <v-card v-else-if="myTeam" rounded="lg" elevation="2">
        <v-card-title class="pa-6 pb-2">
          <div class="d-flex align-center ga-3">
            <span class="text-h6">{{ myTeam.name }}</span>
            <v-chip v-if="myTeam.sectionName" color="info" size="small" variant="tonal">
              {{ myTeam.sectionName }}
            </v-chip>
            <v-chip v-if="myTeam.rubricName" color="success" size="small" variant="tonal">
              {{ myTeam.rubricName }}
            </v-chip>
          </div>
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <p class="text-subtitle-2 font-weight-bold mb-2">Instructors</p>
          <div class="d-flex flex-wrap ga-2 mb-6">
            <v-chip
              v-for="instructor in myTeam.instructors"
              :key="instructor.id"
              color="warning"
              prepend-icon="mdi-school"
              size="small"
            >
              {{ instructor.firstName }} {{ instructor.lastName }}
            </v-chip>
            <span
              v-if="!myTeam.instructors.length"
              class="text-body-2 text-medium-emphasis"
            >
              No instructors assigned
            </span>
          </div>

          <p class="text-subtitle-2 font-weight-bold mb-2">Students</p>
          <div class="d-flex flex-wrap ga-2">
            <v-chip
              v-for="student in myTeam.students"
              :key="student.id"
              :color="student.id === authStore.userInfo?.userId ? 'primary' : 'default'"
              prepend-icon="mdi-account"
              size="small"
              :variant="student.id === authStore.userInfo?.userId ? 'elevated' : 'tonal'"
            >
              {{ student.firstName }} {{ student.lastName }}
              <span v-if="student.id === authStore.userInfo?.userId"> (you)</span>
            </v-chip>
            <span
              v-if="!myTeam.students.length"
              class="text-body-2 text-medium-emphasis"
            >
              No students assigned
            </span>
          </div>
        </v-card-text>
      </v-card>
    </template>

    <!-- ADMIN: All Teams Grid with search -->
    <template v-else-if="authStore.isAdmin">
      <v-text-field
        v-model="search"
        label="Search teams..."
        prepend-inner-icon="mdi-magnify"
        variant="outlined"
        density="compact"
        clearable
        class="mb-4"
        style="max-width: 400px"
      />

      <div v-if="loading" class="d-flex justify-center py-12">
        <v-progress-circular indeterminate color="info" />
      </div>

      <v-row v-else>
        <v-col
          v-for="team in filteredTeams"
          :key="team.id"
          cols="12"
          sm="6"
          md="4"
        >
          <v-card rounded="lg" elevation="2" height="100%">
            <v-card-title class="pa-4 pb-2 d-flex align-center justify-space-between">
              <span class="text-body-1 font-weight-bold">{{ team.name }}</span>
              <v-chip
                v-if="team.sectionName"
                color="info"
                size="x-small"
                variant="tonal"
              >
                {{ team.sectionName }}
              </v-chip>
            </v-card-title>
            <v-card-text class="pt-2">
              <div class="d-flex align-center ga-1 mb-1">
                <v-icon icon="mdi-school" size="14" color="warning" />
                <span class="text-body-2 text-medium-emphasis">
                  <template v-if="team.instructors.length">
                    {{ team.instructors.map((i) => `${i.firstName} ${i.lastName}`).join(', ') }}
                  </template>
                  <template v-else>No instructor assigned</template>
                </span>
              </div>
              <div class="d-flex align-center ga-1">
                <v-icon icon="mdi-account-group" size="14" color="primary" />
                <span class="text-body-2 text-medium-emphasis">
                  {{ team.students.length }} student{{ team.students.length !== 1 ? 's' : '' }}
                </span>
              </div>
            </v-card-text>
            <v-card-actions>
              <v-btn
                icon="mdi-delete-outline"
                variant="text"
                size="small"
                color="error"
                :loading="deletingTeamId === team.id"
                @click="handleDeleteTeam(team)"
              />
              <v-spacer />
              <v-btn
                color="info"
                variant="tonal"
                size="small"
                @click="openManageDialog(team)"
              >
                Manage
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col v-if="filteredTeams.length === 0" cols="12">
          <v-alert type="info" variant="tonal" density="compact">
            No teams found.
          </v-alert>
        </v-col>
      </v-row>
    </template>

    <!-- INSTRUCTOR: Read-only grid -->
    <template v-else-if="authStore.isInstructor">
      <div v-if="loading" class="d-flex justify-center py-12">
        <v-progress-circular indeterminate color="info" />
      </div>

      <v-row v-else>
        <v-col
          v-for="team in teams"
          :key="team.id"
          cols="12"
          sm="6"
          md="4"
        >
          <v-card rounded="lg" elevation="2">
            <v-card-title class="pa-4 pb-2 d-flex align-center justify-space-between">
              <span class="text-body-1 font-weight-bold">{{ team.name }}</span>
              <v-chip
                v-if="team.sectionName"
                color="info"
                size="x-small"
                variant="tonal"
              >
                {{ team.sectionName }}
              </v-chip>
            </v-card-title>
            <v-card-text class="pt-2">
              <div class="d-flex align-center ga-1 mb-1">
                <v-icon icon="mdi-school" size="14" color="warning" />
                <span class="text-body-2 text-medium-emphasis">
                  <template v-if="team.instructors.length">
                    {{ team.instructors.map((i) => `${i.firstName} ${i.lastName}`).join(', ') }}
                  </template>
                  <template v-else>No instructor assigned</template>
                </span>
              </div>
              <div class="d-flex align-center ga-1">
                <v-icon icon="mdi-account-group" size="14" color="primary" />
                <span class="text-body-2 text-medium-emphasis">
                  {{ team.students.length }} student{{ team.students.length !== 1 ? 's' : '' }}
                </span>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col v-if="teams.length === 0" cols="12">
          <v-alert type="info" variant="tonal" density="compact">
            No teams found.
          </v-alert>
        </v-col>
      </v-row>
    </template>

    <!-- Create Team Dialog -->
    <v-dialog v-model="createDialog" max-width="500" persistent>
      <v-card rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-account-multiple-plus" color="info" />
          Create Team
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <v-form ref="createFormRef">
            <v-text-field
              v-model="createForm.name"
              label="Team Name *"
              :rules="[(v) => !!v || 'Team name is required']"
              variant="outlined"
              class="mb-4"
            />
            <v-select
              v-model="createForm.sectionId"
              :items="sectionItems"
              label="Section *"
              :rules="[(v) => !!v || 'Section is required']"
              variant="outlined"
              class="mb-4"
            />
            <v-select
              v-model="createForm.rubricId"
              :items="rubricItems"
              label="Rubric (optional)"
              variant="outlined"
            />
          </v-form>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="creating" @click="createDialog = false">Cancel</v-btn>
          <v-btn
            color="info"
            variant="elevated"
            :loading="creating"
            @click="handleCreateTeam"
          >
            Create
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Manage Team Dialog -->
    <v-dialog v-model="manageDialog" max-width="600" persistent>
      <v-card v-if="managedTeam" rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-cog" color="info" />
          Manage: {{ managedTeam.name }}
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <!-- Assign Instructor (BR-1) -->
          <p class="text-subtitle-2 font-weight-bold mb-3">
            <v-icon icon="mdi-school" size="16" class="mr-1" />
            Assign Instructor (BR-1)
          </p>
          <v-row align="center" class="mb-4">
            <v-col>
              <v-select
                v-model="selectedInstructorId"
                :items="instructorItems"
                label="Select Instructor"
                variant="outlined"
                density="compact"
                clearable
                hide-details
              />
            </v-col>
            <v-col cols="auto">
              <v-btn
                color="warning"
                variant="tonal"
                :loading="assigningInstructor"
                :disabled="!selectedInstructorId"
                @click="handleAssignInstructor"
              >
                Assign
              </v-btn>
            </v-col>
          </v-row>
          <div class="d-flex flex-wrap ga-2 mb-6">
            <v-chip
              v-for="inst in managedTeam.instructors"
              :key="inst.id"
              color="warning"
              size="small"
              prepend-icon="mdi-school"
            >
              {{ inst.firstName }} {{ inst.lastName }}
            </v-chip>
            <span
              v-if="!managedTeam.instructors.length"
              class="text-body-2 text-medium-emphasis"
            >
              No instructors yet
            </span>
          </div>

          <v-divider class="mb-4" />

          <!-- Add Student -->
          <p class="text-subtitle-2 font-weight-bold mb-3">
            <v-icon icon="mdi-account-plus" size="16" class="mr-1" />
            Add Student
          </p>
          <v-row align="center" class="mb-4">
            <v-col>
              <v-select
                v-model="selectedStudentId"
                :items="studentItems"
                label="Select Student"
                variant="outlined"
                density="compact"
                clearable
                hide-details
              />
            </v-col>
            <v-col cols="auto">
              <v-btn
                color="primary"
                variant="tonal"
                :loading="addingStudent"
                :disabled="!selectedStudentId"
                @click="handleAddStudent"
              >
                Add
              </v-btn>
            </v-col>
          </v-row>

          <!-- Current Students -->
          <p class="text-subtitle-2 font-weight-bold mb-2">Current Students</p>
          <div class="d-flex flex-wrap ga-2">
            <v-chip
              v-for="student in managedTeam.students"
              :key="student.id"
              color="primary"
              size="small"
              closable
              :disabled="removingStudentId === student.id"
              @click:close="handleRemoveStudent(student.id)"
            >
              {{ student.firstName }} {{ student.lastName }}
            </v-chip>
            <span
              v-if="!managedTeam.students.length"
              class="text-body-2 text-medium-emphasis"
            >
              No students yet
            </span>
          </div>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="elevated" color="info" @click="manageDialog = false">Done</v-btn>
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
