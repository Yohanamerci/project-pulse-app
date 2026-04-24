<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserById, type UserDto } from '@/apis/users'
import { getTeams, type TeamDto } from '@/apis/teams'
import { getStudentActivities, type ActivityDto } from '@/apis/activities'
import { getStudentGrades, type GradeDto } from '@/apis/evaluations'

const route = useRoute()
const router = useRouter()

const studentId = Number(route.params.id)

const student = ref<UserDto | null>(null)
const team = ref<TeamDto | null>(null)
const activities = ref<ActivityDto[]>([])
const grades = ref<GradeDto[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const activityHeaders = [
  { title: 'Week', key: 'weekNumber', sortable: true },
  { title: 'Activity', key: 'activityName', sortable: true },
  { title: 'Category', key: 'category', sortable: true },
  { title: 'Status', key: 'status', sortable: true },
  { title: 'Planned', key: 'plannedHours', sortable: true },
  { title: 'Actual', key: 'actualHours', sortable: true },
]

const gradeHeaders = [
  { title: 'Week', key: 'weekNumber', sortable: true },
  { title: 'Avg Score', key: 'averageScore', sortable: true },
  { title: 'Max Score', key: 'maxPossibleScore', sortable: true },
  { title: 'Evaluations', key: 'evaluationsReceived', sortable: false },
]

const totalAvgScore = () => {
  if (grades.value.length === 0) return 0
  return grades.value.reduce((sum, g) => sum + g.averageScore, 0) / grades.value.length
}

onMounted(async () => {
  loading.value = true
  error.value = null
  try {
    const [s, allTeams, acts, g] = await Promise.all([
      getUserById(studentId),
      getTeams(),
      getStudentActivities(studentId),
      getStudentGrades(studentId),
    ])
    student.value = s
    team.value = allTeams.find((t) => t.students.some((st) => st.id === studentId)) ?? null
    activities.value = acts
    grades.value = g
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load student data.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <v-container fluid class="pa-6">
    <!-- Back button -->
    <v-btn
      prepend-icon="mdi-arrow-left"
      variant="text"
      class="mb-4"
      @click="router.back()"
    >
      Back
    </v-btn>

    <div v-if="loading" class="d-flex justify-center py-12">
      <v-progress-circular indeterminate color="primary" size="48" />
    </div>

    <v-alert v-else-if="error" type="error" variant="tonal" class="mb-4">{{ error }}</v-alert>

    <template v-else-if="student">
      <!-- Header -->
      <div class="pp-page-hero mb-6">
        <div class="pp-page-hero-grid" />
        <div class="pp-page-hero-glow" style="background:radial-gradient(ellipse,rgba(96,165,250,0.22) 0%,transparent 70%)" />
        <div class="d-flex align-center position-relative" style="z-index:1">
          <div class="d-flex align-center ga-4">
            <div class="pp-page-hero-icon" style="background:linear-gradient(135deg,#60A5FA,#3B82F6);width:60px!important;height:60px!important;min-width:60px!important;border-radius:16px!important">
              <v-icon icon="mdi-account" size="32" color="white" />
            </div>
            <div>
              <h1 class="text-h5 font-weight-bold text-white mb-0">
                {{ student.firstName }} {{ student.lastName }}
              </h1>
              <p class="text-body-2 mb-0" style="color:rgba(255,255,255,0.65)">
                @{{ student.username }} ·
                <span v-if="team">{{ team.name }}</span>
                <span v-else>No team assigned</span>
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Summary cards -->
      <v-row class="mb-6">
        <v-col cols="6" sm="3">
          <v-card rounded="lg" elevation="1" class="pa-4 text-center">
            <div class="text-h4 font-weight-bold text-primary">{{ activities.length }}</div>
            <div class="text-caption text-medium-emphasis">WAR Entries</div>
          </v-card>
        </v-col>
        <v-col cols="6" sm="3">
          <v-card rounded="lg" elevation="1" class="pa-4 text-center">
            <div class="text-h4 font-weight-bold text-success">{{ grades.length }}</div>
            <div class="text-caption text-medium-emphasis">Weeks Evaluated</div>
          </v-card>
        </v-col>
        <v-col cols="6" sm="3">
          <v-card rounded="lg" elevation="1" class="pa-4 text-center">
            <div class="text-h4 font-weight-bold text-warning">
              {{ totalAvgScore().toFixed(1) }}
            </div>
            <div class="text-caption text-medium-emphasis">Overall Avg Score</div>
          </v-card>
        </v-col>
        <v-col cols="6" sm="3">
          <v-card rounded="lg" elevation="1" class="pa-4 text-center">
            <div class="text-h4 font-weight-bold text-info">
              {{ activities.reduce((sum, a) => sum + (a.actualHours ?? a.plannedHours), 0).toFixed(1) }}h
            </div>
            <div class="text-caption text-medium-emphasis">Total Hours Logged</div>
          </v-card>
        </v-col>
      </v-row>

      <!-- Grade History -->
      <v-card rounded="lg" elevation="2" class="mb-6">
        <v-card-title class="pa-4 pb-2">
          <v-icon icon="mdi-star-check" class="mr-2" color="warning" />
          Peer Evaluation Grades
          <v-chip size="small" class="ml-2" color="warning" variant="tonal">
            {{ grades.length }} week{{ grades.length !== 1 ? 's' : '' }}
          </v-chip>
        </v-card-title>
        <v-divider />
        <v-data-table
          :headers="gradeHeaders"
          :items="grades"
          :items-per-page="10"
          no-data-text="No evaluation grades yet."
          density="comfortable"
        >
          <template #item.weekNumber="{ item }">
            Week {{ item.weekNumber }}
          </template>
          <template #item.averageScore="{ item }">
            <strong>{{ item.averageScore.toFixed(1) }}</strong>
          </template>
          <template #item.evaluationsReceived="{ item }">
            {{ item.evaluationsReceived.length }} peer evaluation{{ item.evaluationsReceived.length !== 1 ? 's' : '' }}
          </template>
        </v-data-table>
      </v-card>

      <!-- Weekly Activity Reports -->
      <v-card rounded="lg" elevation="2">
        <v-card-title class="pa-4 pb-2">
          <v-icon icon="mdi-calendar-check" class="mr-2" color="primary" />
          Weekly Activity Reports
          <v-chip size="small" class="ml-2" color="primary" variant="tonal">
            {{ activities.length }}
          </v-chip>
        </v-card-title>
        <v-divider />
        <v-data-table
          :headers="activityHeaders"
          :items="activities"
          :items-per-page="15"
          no-data-text="No activities submitted yet."
          density="comfortable"
        >
          <template #item.weekNumber="{ item }">
            Wk {{ item.weekNumber }}
          </template>
          <template #item.category="{ item }">
            <v-chip size="x-small" label>{{ item.category.replace(/_/g, ' ') }}</v-chip>
          </template>
          <template #item.status="{ item }">
            <v-chip
              size="x-small"
              :color="item.status === 'DONE' ? 'success' : item.status === 'UNDER_TESTING' ? 'info' : 'warning'"
              label
            >
              {{ item.status.replace(/_/g, ' ') }}
            </v-chip>
          </template>
          <template #item.plannedHours="{ item }">
            {{ item.plannedHours }}h
          </template>
          <template #item.actualHours="{ item }">
            {{ item.actualHours != null ? `${item.actualHours}h` : '—' }}
          </template>
        </v-data-table>
      </v-card>
    </template>
  </v-container>
</template>
