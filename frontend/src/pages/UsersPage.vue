<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getUsers, createUser, updateUser, deleteUser, type UserDto } from '@/apis/users'
import { getTeams, type TeamDto } from '@/apis/teams'
import { getStudentActivities, type ActivityDto } from '@/apis/activities'
import { getStudentGrades, type GradeDto } from '@/apis/evaluations'

const authStore = useAuthStore()

const users = ref<UserDto[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const snackbar = ref(false)
const snackbarMessage = ref('')

const search = ref('')
// Instructors default to the Students tab (UC-15: find students)
const tab = ref(authStore.isInstructor ? 'STUDENT' : 'ALL')

const ROLES: Array<'ADMIN' | 'INSTRUCTOR' | 'STUDENT'> = ['ADMIN', 'INSTRUCTOR', 'STUDENT']

const roleColors: Record<string, string> = {
  ADMIN: 'error',
  INSTRUCTOR: 'warning',
  STUDENT: 'primary',
}

const headers = [
  { title: 'Username', key: 'username', sortable: true },
  { title: 'Name', key: 'fullName', sortable: true },
  { title: 'Email', key: 'email', sortable: true },
  { title: 'Role', key: 'role', sortable: true },
  { title: 'Active', key: 'enabled', sortable: false },
  { title: 'Actions', key: 'actions', sortable: false, align: 'end' as const },
]

const filteredUsers = computed(() => {
  let list = users.value

  if (tab.value !== 'ALL') {
    list = list.filter((u) => u.role === tab.value)
  }

  if (search.value) {
    const q = search.value.toLowerCase()
    list = list.filter(
      (u) =>
        u.username.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q) ||
        u.firstName.toLowerCase().includes(q) ||
        u.lastName.toLowerCase().includes(q)
    )
  }

  return list
})

// Add User dialog
const addDialog = ref(false)
const addFormRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)
const creating = ref(false)
const addForm = ref({
  username: '',
  email: '',
  password: '',
  firstName: '',
  lastName: '',
  role: 'STUDENT' as 'ADMIN' | 'INSTRUCTOR' | 'STUDENT',
})

function openAddDialog() {
  addForm.value = { username: '', email: '', password: '', firstName: '', lastName: '', role: 'STUDENT' }
  addDialog.value = true
}

async function handleCreateUser() {
  if (!addFormRef.value) return
  const { valid } = await addFormRef.value.validate()
  if (!valid) return

  creating.value = true
  try {
    await createUser({ ...addForm.value })
    addDialog.value = false
    snackbarMessage.value = `User "${addForm.value.username}" created!`
    snackbar.value = true
    await loadUsers()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to create user.'
  } finally {
    creating.value = false
  }
}

// Edit User dialog
const editDialog = ref(false)
const editingUser = ref<UserDto | null>(null)
const editFormRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)
const saving = ref(false)
const editForm = ref({
  firstName: '',
  lastName: '',
  email: '',
  enabled: true,
})

function openEditDialog(user: UserDto) {
  editingUser.value = user
  editForm.value = {
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email,
    enabled: user.enabled,
  }
  editDialog.value = true
}

async function handleUpdateUser() {
  if (!editFormRef.value || !editingUser.value) return
  const { valid } = await editFormRef.value.validate()
  if (!valid) return

  saving.value = true
  try {
    await updateUser(editingUser.value.id, { ...editForm.value })
    editDialog.value = false
    snackbarMessage.value = 'User updated!'
    snackbar.value = true
    await loadUsers()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to update user.'
  } finally {
    saving.value = false
  }
}

// View User dialog
const viewDialog = ref(false)
const viewingUser = ref<UserDto | null>(null)
const viewLoading = ref(false)
const viewTeam = ref<TeamDto | null>(null)
const viewActivities = ref<ActivityDto[]>([])
const viewGrades = ref<GradeDto[]>([])

async function openViewDialog(user: UserDto) {
  viewingUser.value = user
  viewTeam.value = null
  viewActivities.value = []
  viewGrades.value = []
  viewDialog.value = true
  viewLoading.value = true
  try {
    const allTeams = await getTeams()
    if (user.role === 'STUDENT') {
      viewTeam.value = allTeams.find((t) => t.students.some((s) => s.id === user.id)) ?? null
      const [acts, grades] = await Promise.all([
        getStudentActivities(user.id),
        getStudentGrades(user.id),
      ])
      viewActivities.value = acts
      viewGrades.value = grades
    } else if (user.role === 'INSTRUCTOR') {
      viewTeam.value = allTeams.find((t) => t.instructors.some((i) => i.id === user.id)) ?? null
    }
  } catch {
    // silently fail — dialog still shows basic info
  } finally {
    viewLoading.value = false
  }
}

// Delete User
const deletingUserId = ref<number | null>(null)

async function handleDeleteUser(user: UserDto) {
  const confirmMessage = user.role === 'STUDENT'
    ? `Permanently delete student "@${user.username}"?\n\nThis will also permanently delete ALL their WAR activities and peer evaluations. This cannot be undone (UC-17).`
    : `Deactivate instructor "@${user.username}"?\n\nTheir account will be disabled but not removed. You can reactivate it later (UC-23).`
  if (!confirm(confirmMessage)) return
  deletingUserId.value = user.id
  try {
    await deleteUser(user.id)
    snackbarMessage.value = user.role === 'STUDENT'
      ? `Student "@${user.username}" permanently deleted.`
      : `Instructor "@${user.username}" deactivated.`
    snackbar.value = true
    await loadUsers()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to delete user.'
  } finally {
    deletingUserId.value = null
  }
}

async function loadUsers() {
  loading.value = true
  error.value = null
  try {
    users.value = await getUsers()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load users.'
  } finally {
    loading.value = false
  }
}

onMounted(loadUsers)
</script>

<template>
  <v-container fluid class="pa-6">
    <!-- Header -->
    <v-row align="center" class="mb-6">
      <v-col>
        <div class="d-flex align-center ga-3">
          <v-avatar color="error" variant="tonal" size="48" rounded="lg">
            <v-icon icon="mdi-account-cog" size="28" />
          </v-avatar>
          <div>
            <h1 class="text-h5 font-weight-bold">Users</h1>
            <p class="text-body-2 text-medium-emphasis mb-0">
              <template v-if="authStore.isAdmin">Create and manage instructor and student accounts</template>
              <template v-else>Find and view students and instructors (UC-15, UC-16)</template>
            </p>
          </div>
        </div>
      </v-col>
      <v-col v-if="authStore.isAdmin" cols="auto">
        <v-btn color="error" prepend-icon="mdi-account-plus" @click="openAddDialog">
          Add User
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

    <!-- Role Tabs -->
    <v-tabs v-model="tab" color="error" class="mb-4">
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

    <!-- Users Table -->
    <v-card rounded="lg" elevation="2">
      <v-data-table
        :headers="headers"
        :items="filteredUsers"
        :loading="loading"
        :items-per-page="15"
        :items-per-page-options="[15, 30, 50, { value: -1, title: 'All' }]"
        loading-text="Loading users..."
        no-data-text="No users found."
        item-value="id"
      >
        <template #item.fullName="{ item }">
          {{ item.firstName }} {{ item.lastName }}
        </template>

        <template #item.role="{ item }">
          <v-chip :color="roleColors[item.role]" size="small" label>
            {{ item.role }}
          </v-chip>
        </template>

        <template #item.enabled="{ item }">
          <v-icon
            :icon="item.enabled ? 'mdi-check-circle' : 'mdi-close-circle'"
            :color="item.enabled ? 'success' : 'error'"
          />
        </template>

        <template #item.actions="{ item }">
          <v-btn
            v-if="item.role === 'STUDENT' || item.role === 'INSTRUCTOR'"
            prepend-icon="mdi-eye"
            variant="tonal"
            size="small"
            color="secondary"
            class="mr-1"
            @click="openViewDialog(item)"
          >
            View
          </v-btn>
          <!-- Instructor: edit students only -->
          <v-btn
            v-if="authStore.isInstructor && item.role === 'STUDENT'"
            prepend-icon="mdi-pencil"
            variant="tonal"
            size="small"
            color="primary"
            @click="openEditDialog(item)"
          >
            Edit
          </v-btn>
          <template v-if="authStore.isAdmin">
            <v-btn
              prepend-icon="mdi-pencil"
              variant="tonal"
              size="small"
              color="primary"
              class="mr-1"
              @click="openEditDialog(item)"
            >
              Edit
            </v-btn>
            <v-btn
              v-if="item.role !== 'ADMIN'"
              prepend-icon="mdi-delete-outline"
              variant="tonal"
              size="small"
              :color="item.role === 'STUDENT' ? 'error' : 'warning'"
              :loading="deletingUserId === item.id"
              @click="handleDeleteUser(item)"
            >
              {{ item.role === 'STUDENT' ? 'Delete' : 'Disable' }}
            </v-btn>
          </template>
        </template>
      </v-data-table>
    </v-card>

    <!-- Add User Dialog -->
    <v-dialog v-model="addDialog" max-width="520" persistent>
      <v-card rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-account-plus" color="error" />
          Add User
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <v-form ref="addFormRef">
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="addForm.firstName"
                  label="First Name *"
                  :rules="[(v) => !!v || 'First name is required']"
                  variant="outlined"
                />
              </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model="addForm.lastName"
                  label="Last Name *"
                  :rules="[(v) => !!v || 'Last name is required']"
                  variant="outlined"
                />
              </v-col>
            </v-row>
            <v-text-field
              v-model="addForm.username"
              label="Username *"
              :rules="[(v) => !!v || 'Username is required']"
              variant="outlined"
              class="mt-2"
            />
            <v-text-field
              v-model="addForm.email"
              label="Email *"
              type="email"
              :rules="[
                (v) => !!v || 'Email is required',
                (v) => /.+@.+\..+/.test(v) || 'Invalid email',
              ]"
              variant="outlined"
              class="mt-2"
            />
            <v-text-field
              v-model="addForm.password"
              label="Password *"
              type="password"
              :rules="[
                (v) => !!v || 'Password is required',
                (v) => v.length >= 6 || 'Minimum 6 characters',
              ]"
              variant="outlined"
              class="mt-2"
            />
            <v-select
              v-model="addForm.role"
              :items="ROLES"
              label="Role *"
              :rules="[(v) => !!v || 'Role is required']"
              variant="outlined"
              class="mt-2"
            />
          </v-form>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="creating" @click="addDialog = false">Cancel</v-btn>
          <v-btn
            color="error"
            variant="elevated"
            :loading="creating"
            @click="handleCreateUser"
          >
            Create User
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Edit User Dialog -->
    <v-dialog v-model="editDialog" max-width="480" persistent>
      <v-card v-if="editingUser" rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-account-edit" color="primary" />
          Edit: @{{ editingUser.username }}
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <v-form ref="editFormRef">
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="editForm.firstName"
                  label="First Name *"
                  :rules="[(v) => !!v || 'First name is required']"
                  variant="outlined"
                />
              </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model="editForm.lastName"
                  label="Last Name *"
                  :rules="[(v) => !!v || 'Last name is required']"
                  variant="outlined"
                />
              </v-col>
            </v-row>
            <v-text-field
              v-model="editForm.email"
              label="Email *"
              type="email"
              :rules="[
                (v) => !!v || 'Email is required',
                (v) => /.+@.+\..+/.test(v) || 'Invalid email',
              ]"
              variant="outlined"
              class="mt-2"
            />
            <v-switch
              v-if="authStore.isAdmin"
              v-model="editForm.enabled"
              label="Account Active"
              color="success"
              hide-details
              class="mt-4"
            />
          </v-form>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="saving" @click="editDialog = false">Cancel</v-btn>
          <v-btn
            color="primary"
            variant="elevated"
            :loading="saving"
            @click="handleUpdateUser"
          >
            Save Changes
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- View User Dialog -->
    <v-dialog v-model="viewDialog" max-width="680" scrollable>
      <v-card v-if="viewingUser" rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-account-details" :color="roleColors[viewingUser.role]" />
          {{ viewingUser.firstName }} {{ viewingUser.lastName }}
          <v-chip :color="roleColors[viewingUser.role]" size="small" label class="ml-2">
            {{ viewingUser.role }}
          </v-chip>
        </v-card-title>
        <v-divider />

        <v-card-text class="pa-6">
          <v-progress-linear v-if="viewLoading" indeterminate color="primary" class="mb-4" />

          <!-- Basic info -->
          <v-row dense class="mb-4">
            <v-col cols="6">
              <div class="text-caption text-medium-emphasis">Username</div>
              <div>@{{ viewingUser.username }}</div>
            </v-col>
            <v-col cols="6">
              <div class="text-caption text-medium-emphasis">Email</div>
              <div>{{ viewingUser.email }}</div>
            </v-col>
            <v-col cols="6" class="mt-2">
              <div class="text-caption text-medium-emphasis">Status</div>
              <v-chip :color="viewingUser.enabled ? 'success' : 'error'" size="small" label>
                {{ viewingUser.enabled ? 'Active' : 'Disabled' }}
              </v-chip>
            </v-col>
            <v-col cols="6" class="mt-2">
              <div class="text-caption text-medium-emphasis">Team</div>
              <div>{{ viewTeam ? viewTeam.name : '—' }}</div>
            </v-col>
          </v-row>

          <!-- Student-specific: WAR history -->
          <template v-if="viewingUser.role === 'STUDENT' && !viewLoading">
            <v-divider class="mb-4" />
            <div class="text-subtitle-2 font-weight-bold mb-3">
              Weekly Activity Reports ({{ viewActivities.length }})
            </div>
            <v-table v-if="viewActivities.length > 0" density="compact">
              <thead>
                <tr>
                  <th>Week</th>
                  <th>Activity</th>
                  <th>Category</th>
                  <th>Status</th>
                  <th>Hours</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="act in viewActivities" :key="act.id">
                  <td>Wk {{ act.weekNumber }}</td>
                  <td>{{ act.activityName }}</td>
                  <td>
                    <v-chip size="x-small" label>{{ act.category.replace(/_/g, ' ') }}</v-chip>
                  </td>
                  <td>{{ act.status.replace(/_/g, ' ') }}</td>
                  <td>{{ act.plannedHours }}h / {{ act.actualHours != null ? `${act.actualHours}h` : '—' }}</td>
                </tr>
              </tbody>
            </v-table>
            <p v-else class="text-medium-emphasis text-body-2">No activities submitted yet.</p>

            <v-divider class="mt-4 mb-4" />
            <div class="text-subtitle-2 font-weight-bold mb-3">
              Peer Evaluation Grades ({{ viewGrades.length }} week<span v-if="viewGrades.length !== 1">s</span>)
            </div>
            <v-table v-if="viewGrades.length > 0" density="compact">
              <thead>
                <tr>
                  <th>Week</th>
                  <th>Avg Score</th>
                  <th>Max Score</th>
                  <th>Evaluations</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="grade in viewGrades" :key="grade.weekId">
                  <td>Week {{ grade.weekNumber }}</td>
                  <td>{{ grade.averageScore.toFixed(1) }}</td>
                  <td>{{ grade.maxPossibleScore }}</td>
                  <td>{{ grade.evaluationsReceived.length }}</td>
                </tr>
              </tbody>
            </v-table>
            <p v-else class="text-medium-emphasis text-body-2">No evaluation grades yet.</p>
          </template>

          <!-- Instructor-specific: supervised teams -->
          <template v-if="viewingUser.role === 'INSTRUCTOR' && !viewLoading">
            <v-divider class="mb-4" />
            <div class="text-subtitle-2 font-weight-bold mb-3">Supervised Team</div>
            <p v-if="viewTeam">
              <strong>{{ viewTeam.name }}</strong>
              <span v-if="viewTeam.sectionName" class="text-medium-emphasis ml-2">
                ({{ viewTeam.sectionName }})
              </span>
              — {{ viewTeam.students.length }} student<span v-if="viewTeam.students.length !== 1">s</span>
            </p>
            <p v-else class="text-medium-emphasis text-body-2">Not assigned to any team.</p>
          </template>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn
            v-if="viewingUser.role === 'STUDENT'"
            color="primary"
            variant="tonal"
            prepend-icon="mdi-chart-line"
            :to="`/students/${viewingUser.id}`"
          >
            Performance Dashboard
          </v-btn>
          <v-spacer />
          <v-btn variant="text" @click="viewDialog = false">Close</v-btn>
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
