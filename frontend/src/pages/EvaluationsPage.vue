<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  submitEvaluation,
  resubmitEvaluation,
  getMyEvaluations,
  getMyScores,
  getTeamWeekEvaluations,
  getGrade,
  type EvaluationDto,
  type MyScoreDto,
  type AnonymousScoreDto,
  type GradeDto,
} from '@/apis/evaluations'
import { getMyTeam, getTeams, type TeamDto, type TeamMemberDto } from '@/apis/teams'
import { getRubric, type RubricDto } from '@/apis/rubrics'
import { getSectionActiveWeek, getSectionActiveWeeks, type ActiveWeekDto } from '@/apis/sections'

const authStore = useAuthStore()

const tab = ref(authStore.isStudent ? 'evaluate' : 'overview')

const error = ref<string | null>(null)
const snackbar = ref(false)
const snackbarMessage = ref('')

// ---- STUDENT: Evaluate Teammates ----
const myTeam = ref<TeamDto | null>(null)
const activeWeek = ref<ActiveWeekDto | null>(null)
const rubric = ref<RubricDto | null>(null)
const myEvaluations = ref<EvaluationDto[]>([])
const loadingTeam = ref(false)
const notOnTeam = ref(false)

const teammates = computed<TeamMemberDto[]>(() => {
  if (!myTeam.value) return []
  return myTeam.value.students.filter(
    (s) => s.id !== authStore.userInfo?.userId
  )
})

function hasEvaluated(teammateId: number): boolean {
  return myEvaluations.value.some(
    (e) => e.evaluateeId === teammateId && e.weekId === activeWeek.value?.id
  )
}

// Evaluate dialog
const evalDialog = ref(false)
const evalTarget = ref<TeamMemberDto | null>(null)
const criterionScores = ref<Record<number, number>>({})
const criterionPublicComments = ref<Record<number, string>>({})
const criterionPrivateComments = ref<Record<number, string>>({})
const submittingEval = ref(false)
const editingEvaluationId = ref<number | null>(null)

function getExistingEvaluation(teammateId: number): EvaluationDto | undefined {
  return myEvaluations.value.find(
    (e) => e.evaluateeId === teammateId && e.weekId === activeWeek.value?.id
  )
}

function openEvalDialog(teammate: TeamMemberDto, editMode = false) {
  evalTarget.value = teammate
  editingEvaluationId.value = null
  criterionScores.value = {}
  criterionPublicComments.value = {}
  criterionPrivateComments.value = {}

  if (rubric.value) {
    rubric.value.criteria.forEach((c) => {
      criterionScores.value[c.id] = 0
      criterionPublicComments.value[c.id] = ''
      criterionPrivateComments.value[c.id] = ''
    })
  }

  if (editMode) {
    const existing = getExistingEvaluation(teammate.id)
    if (existing) {
      editingEvaluationId.value = existing.id
      existing.scores.forEach((s) => {
        criterionScores.value[s.criterionId] = s.score
        criterionPublicComments.value[s.criterionId] = s.publicComment ?? ''
        criterionPrivateComments.value[s.criterionId] = s.privateComment ?? ''
      })
    }
  }

  evalDialog.value = true
}

async function handleSubmitEval() {
  if (!evalTarget.value || !myTeam.value || !activeWeek.value || !rubric.value) return

  const scores = rubric.value.criteria.map((c) => ({
    criterionId: c.id,
    score: criterionScores.value[c.id] ?? 0,
    publicComment: criterionPublicComments.value[c.id] || undefined,
    privateComment: criterionPrivateComments.value[c.id] || undefined,
  }))

  const payload = {
    evaluateeId: evalTarget.value.id,
    teamId: myTeam.value.id,
    weekId: activeWeek.value.id,
    scores,
  }

  submittingEval.value = true
  try {
    if (editingEvaluationId.value !== null) {
      await resubmitEvaluation(editingEvaluationId.value, payload)
      snackbarMessage.value = `Evaluation for ${evalTarget.value.firstName} updated!`
    } else {
      await submitEvaluation(payload)
      snackbarMessage.value = `Evaluation for ${evalTarget.value.firstName} submitted!`
    }
    evalDialog.value = false
    snackbar.value = true
    myEvaluations.value = await getMyEvaluations()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to submit evaluation.'
  } finally {
    submittingEval.value = false
  }
}

async function loadStudentData() {
  loadingTeam.value = true
  error.value = null
  notOnTeam.value = false
  try {
    myTeam.value = await getMyTeam()
    if (myTeam.value.sectionId) {
      activeWeek.value = await getSectionActiveWeek(myTeam.value.sectionId)
    }
    if (myTeam.value.rubricId) {
      rubric.value = await getRubric(myTeam.value.rubricId)
    }
    myEvaluations.value = await getMyEvaluations()
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : String(e)
    if (msg.includes('404') || msg.toLowerCase().includes('not found') || msg.toLowerCase().includes('not assigned')) {
      notOnTeam.value = true
    } else {
      error.value = msg || 'Could not load team data.'
    }
  } finally {
    loadingTeam.value = false
  }
}

// ---- STUDENT: My Scores ----
const myScores = ref<MyScoreDto[]>([])
const loadingScores = ref(false)

async function loadMyScores() {
  loadingScores.value = true
  error.value = null
  try {
    myScores.value = await getMyScores()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load scores.'
  } finally {
    loadingScores.value = false
  }
}

function criterionAverage(criterionId: number): string {
  const values = myScores.value.flatMap((e) =>
    e.scores.filter((s) => s.criterionId === criterionId).map((s) => s.score)
  )
  if (!values.length) return '—'
  const avg = values.reduce((a, b) => a + b, 0) / values.length
  return avg.toFixed(1)
}

function overallAverageScore(): string {
  if (!myScores.value.length) return '—'
  const avg = myScores.value.reduce((a, e) => a + e.totalScore, 0) / myScores.value.length
  return avg.toFixed(1)
}

function uniqueCriteria(): AnonymousScoreDto[] {
  const seen = new Set<number>()
  const result: AnonymousScoreDto[] = []
  for (const e of myScores.value) {
    for (const s of e.scores) {
      if (!seen.has(s.criterionId)) {
        seen.add(s.criterionId)
        result.push(s)
      }
    }
  }
  return result
}

function publicCommentsFor(criterionId: number): string[] {
  return myScores.value
    .flatMap((e) => e.scores.filter((s) => s.criterionId === criterionId && s.publicComment))
    .map((s) => s.publicComment as string)
}

// ---- ADMIN/INSTRUCTOR: Team Overview ----
const allTeams = ref<TeamDto[]>([])
const selectedTeamId = ref<number | null>(null)
const selectedWeekId = ref<number | null>(null)
const overviewWeeks = ref<ActiveWeekDto[]>([])
const overviewEvaluations = ref<EvaluationDto[]>([])
const loadingOverview = ref(false)
const loadingWeeks = ref(false)

const teamItems = computed(() =>
  allTeams.value.map((t) => ({ title: t.name, value: t.id }))
)

const weekItems = computed(() =>
  overviewWeeks.value.map((w) => ({
    title: `Week ${w.weekNumber}${w.active ? ' (active)' : ''}`,
    value: w.id,
  }))
)

async function loadTeamsForOverview() {
  try {
    allTeams.value = await getTeams()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load teams.'
  }
}

watch(selectedTeamId, async (teamId) => {
  selectedWeekId.value = null
  overviewWeeks.value = []
  overviewEvaluations.value = []
  if (!teamId) return
  const team = allTeams.value.find((t) => t.id === teamId)
  if (!team?.sectionId) return
  loadingWeeks.value = true
  try {
    overviewWeeks.value = await getSectionActiveWeeks(team.sectionId)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load weeks.'
  } finally {
    loadingWeeks.value = false
  }
})

watch(selectedWeekId, async (weekId) => {
  overviewEvaluations.value = []
  if (!selectedTeamId.value || !weekId) return
  loadingOverview.value = true
  try {
    overviewEvaluations.value = await getTeamWeekEvaluations(selectedTeamId.value, weekId)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load evaluations.'
  } finally {
    loadingOverview.value = false
  }
})

const overviewHeaders = [
  { title: 'Evaluator', key: 'evaluatorName', sortable: true },
  { title: 'Evaluatee', key: 'evaluateeName', sortable: true },
  { title: 'Week #', key: 'weekNumber', sortable: true },
  { title: 'Total Score', key: 'totalScore', sortable: true },
  { title: 'Submitted', key: 'submittedAt', sortable: true },
]

// ---- ADMIN/INSTRUCTOR: Grade Report ----
const grades = ref<GradeDto[]>([])
const loadingGrades = ref(false)

async function loadGradeReport() {
  if (!selectedTeamId.value || !selectedWeekId.value) return
  const team = allTeams.value.find((t) => t.id === selectedTeamId.value)
  if (!team) return
  loadingGrades.value = true
  grades.value = []
  error.value = null
  try {
    grades.value = await Promise.all(
      team.students.map((s) =>
        getGrade(selectedTeamId.value!, selectedWeekId.value!, s.id)
      )
    )
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load grade report.'
  } finally {
    loadingGrades.value = false
  }
}

function gradePercent(grade: GradeDto): number {
  if (!grade.maxPossibleScore) return 0
  return Math.round((grade.averageScore / grade.maxPossibleScore) * 100)
}

function gradeColor(pct: number): string {
  if (pct >= 80) return 'success'
  if (pct >= 60) return 'warning'
  return 'error'
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

onMounted(() => {
  if (authStore.isStudent) {
    loadStudentData()
  } else {
    loadTeamsForOverview()
  }
})

watch(tab, (newTab) => {
  if (newTab === 'scores' && myScores.value.length === 0 && !loadingScores.value) {
    loadMyScores()
  }
  if (newTab === 'grades' && selectedTeamId.value && selectedWeekId.value) {
    loadGradeReport()
  }
})

watch(selectedWeekId, () => {
  grades.value = []
})
</script>

<template>
  <v-container fluid class="pa-6">
    <!-- Header -->
    <div class="pp-page-hero mb-4">
      <div class="pp-page-hero-grid" />
      <div class="pp-page-hero-glow" style="background:radial-gradient(ellipse,rgba(52,211,153,0.24) 0%,transparent 70%)" />
      <div class="d-flex align-center position-relative" style="z-index:1">
        <div class="d-flex align-center ga-4">
          <div class="pp-page-hero-icon" style="background:linear-gradient(135deg,#34D399,#10B981)">
            <v-icon icon="mdi-star-check" size="28" color="white" />
          </div>
          <div>
            <h1 class="text-h5 font-weight-bold text-white mb-0">Peer Evaluations</h1>
            <p class="text-body-2 mb-0" style="color:rgba(255,255,255,0.65)">
              <template v-if="authStore.isStudent">Submit weekly peer evaluations and view your received scores</template>
              <template v-else>View peer evaluation submissions and grade reports for your teams</template>
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Business rules alert -->
    <v-alert type="warning" variant="tonal" icon="mdi-shield-alert-outline" class="mb-4" density="compact">
      <strong>BR-3:</strong> Evaluations cannot be edited after submission. &nbsp;
      <strong>BR-4:</strong> Submissions only allowed during the active week window. &nbsp;
      <strong>BR-5:</strong> Students may only view their own received scores.
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

    <!-- Tabs -->
    <v-tabs v-model="tab" color="success" class="mb-4">
      <v-tab v-if="authStore.isStudent" value="evaluate">
        <v-icon start icon="mdi-pencil" />
        Evaluate Teammates
      </v-tab>
      <v-tab v-if="authStore.isStudent" value="scores">
        <v-icon start icon="mdi-chart-bar" />
        My Scores
      </v-tab>
      <v-tab v-if="!authStore.isStudent" value="overview">
        <v-icon start icon="mdi-table-eye" />
        Team Overview
      </v-tab>
      <v-tab v-if="!authStore.isStudent" value="grades">
        <v-icon start icon="mdi-chart-bar" />
        Grade Report
      </v-tab>
    </v-tabs>

    <v-window v-model="tab">
      <!-- TAB: Evaluate Teammates -->
      <v-window-item value="evaluate">
        <div v-if="loadingTeam" class="d-flex justify-center py-12">
          <v-progress-circular indeterminate color="success" />
        </div>

        <template v-else-if="myTeam">
          <!-- Active week info -->
          <v-alert
            v-if="!activeWeek"
            type="warning"
            variant="tonal"
            class="mb-4"
            density="compact"
          >
            No active week set for your section. Evaluations cannot be submitted yet.
          </v-alert>
          <v-alert
            v-else
            type="success"
            variant="tonal"
            class="mb-4"
            density="compact"
          >
            Active Week: <strong>Week {{ activeWeek.weekNumber }}</strong>
            ({{ activeWeek.startDate }} – {{ activeWeek.endDate }})
          </v-alert>

          <v-alert
            v-if="!rubric"
            type="warning"
            variant="tonal"
            class="mb-4"
            density="compact"
          >
            No rubric assigned to your team. Contact your instructor.
          </v-alert>

          <v-row v-if="teammates.length">
            <v-col
              v-for="teammate in teammates"
              :key="teammate.id"
              cols="12"
              sm="6"
              md="4"
            >
              <v-card rounded="lg" elevation="2">
                <v-card-text class="d-flex align-center ga-4 pb-2">
                  <v-avatar color="success" size="48">
                    <span class="text-h6">{{ teammate.firstName[0] }}</span>
                  </v-avatar>
                  <div class="flex-grow-1">
                    <div class="font-weight-bold">
                      {{ teammate.firstName }} {{ teammate.lastName }}
                    </div>
                    <div class="text-body-2 text-medium-emphasis">@{{ teammate.username }}</div>
                  </div>
                  <v-chip
                    :color="hasEvaluated(teammate.id) ? 'success' : 'grey'"
                    size="small"
                    variant="tonal"
                  >
                    {{ hasEvaluated(teammate.id) ? 'Submitted' : 'Pending' }}
                  </v-chip>
                </v-card-text>
                <v-card-actions class="pt-0">
                  <v-spacer />
                  <v-btn
                    v-if="hasEvaluated(teammate.id) && activeWeek"
                    color="warning"
                    variant="tonal"
                    size="small"
                    prepend-icon="mdi-pencil"
                    @click="openEvalDialog(teammate, true)"
                  >
                    Edit
                  </v-btn>
                  <v-btn
                    v-else
                    color="success"
                    variant="tonal"
                    size="small"
                    :disabled="!activeWeek || !rubric"
                    @click="openEvalDialog(teammate)"
                  >
                    Evaluate
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-col>
          </v-row>

          <v-alert
            v-else
            type="info"
            variant="tonal"
            density="compact"
          >
            No teammates found on your team.
          </v-alert>
        </template>

        <v-alert
          v-else-if="notOnTeam"
          type="warning"
          variant="tonal"
          density="compact"
        >
          You are not assigned to a team yet. Contact your instructor.
        </v-alert>
      </v-window-item>

      <!-- TAB: My Scores -->
      <v-window-item value="scores">
        <div v-if="loadingScores" class="d-flex justify-center py-12">
          <v-progress-circular indeterminate color="success" />
        </div>

        <template v-else-if="myScores.length === 0">
          <v-alert type="info" variant="tonal" density="compact">
            No scores received yet.
          </v-alert>
        </template>

        <template v-else>
          <!-- Overall summary card -->
          <v-card rounded="lg" elevation="2" class="mb-6">
            <v-card-title class="pa-4 pb-2 d-flex align-center justify-space-between">
              <span>Your Overall Score</span>
              <v-chip color="success" size="small" variant="tonal">
                {{ overallAverageScore() }} avg pts · {{ myScores.length }} evaluation(s)
              </v-chip>
            </v-card-title>
            <v-card-text class="pt-0">
              <v-list density="compact">
                <v-list-item
                  v-for="criterion in uniqueCriteria()"
                  :key="criterion.criterionId"
                >
                  <template #title>
                    <span class="text-body-2 font-weight-medium">{{ criterion.criterionName }}</span>
                  </template>
                  <template #append>
                    <v-chip size="x-small" variant="outlined">
                      avg {{ criterionAverage(criterion.criterionId) }} / {{ criterion.maxScore }}
                    </v-chip>
                  </template>
                  <!-- Public comments for this criterion -->
                  <template #subtitle>
                    <div
                      v-for="(comment, i) in publicCommentsFor(criterion.criterionId)"
                      :key="i"
                      class="text-caption text-medium-emphasis"
                    >
                      "{{ comment }}"
                    </div>
                  </template>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </template>
      </v-window-item>

      <!-- TAB: Team Overview (admin/instructor) -->
      <v-window-item value="overview">
        <v-row class="mb-4">
          <v-col cols="12" sm="4">
            <v-select
              v-model="selectedTeamId"
              :items="teamItems"
              label="Select Team"
              variant="outlined"
              density="compact"
              clearable
            />
          </v-col>
          <v-col cols="12" sm="4">
            <v-select
              v-model="selectedWeekId"
              :items="weekItems"
              label="Select Week"
              variant="outlined"
              density="compact"
              clearable
              :loading="loadingWeeks"
              :disabled="!selectedTeamId"
            />
          </v-col>
        </v-row>

        <v-card rounded="lg" elevation="2">
          <v-data-table
            :headers="overviewHeaders"
            :items="overviewEvaluations"
            :loading="loadingOverview"
            loading-text="Loading evaluations..."
            :no-data-text="!selectedTeamId || !selectedWeekId ? 'Select a team and week to view evaluations.' : 'No evaluations submitted for this week yet.'"
            item-value="id"
            :items-per-page="10"
            :items-per-page-options="[10, 25, 50, { value: -1, title: 'All' }]"
          >
            <template #item.submittedAt="{ item }">
              {{ formatDate(item.submittedAt) }}
            </template>
          </v-data-table>
        </v-card>
      </v-window-item>

      <!-- TAB: Grade Report (admin/instructor) -->
      <v-window-item value="grades">
        <!-- Team + week selectors reused from overview tab state -->
        <v-row class="mb-4">
          <v-col cols="12" sm="4">
            <v-select
              v-model="selectedTeamId"
              :items="teamItems"
              label="Select Team"
              variant="outlined"
              density="compact"
              clearable
            />
          </v-col>
          <v-col cols="12" sm="4">
            <v-select
              v-model="selectedWeekId"
              :items="weekItems"
              label="Select Week"
              variant="outlined"
              density="compact"
              clearable
              :loading="loadingWeeks"
              :disabled="!selectedTeamId"
            />
          </v-col>
          <v-col cols="12" sm="auto" class="d-flex align-center">
            <v-btn
              color="success"
              variant="elevated"
              :disabled="!selectedTeamId || !selectedWeekId"
              :loading="loadingGrades"
              @click="loadGradeReport"
            >
              Load Grades
            </v-btn>
          </v-col>
        </v-row>

        <div v-if="loadingGrades" class="d-flex justify-center py-12">
          <v-progress-circular indeterminate color="success" />
        </div>

        <v-alert
          v-else-if="!selectedTeamId || !selectedWeekId"
          type="info"
          variant="tonal"
          density="compact"
        >
          Select a team and week, then click "Load Grades" to view the grade report.
        </v-alert>

        <v-alert
          v-else-if="grades.length === 0 && !loadingGrades"
          type="warning"
          variant="tonal"
          density="compact"
        >
          No grades calculated yet. Ensure evaluations have been submitted for this week.
        </v-alert>

        <v-row v-else>
          <v-col
            v-for="grade in grades"
            :key="grade.studentId"
            cols="12"
            sm="6"
            lg="4"
          >
            <v-card rounded="lg" elevation="2">
              <v-card-title class="pa-4 pb-2 d-flex align-center ga-3">
                <v-avatar :color="gradeColor(gradePercent(grade))" size="40">
                  <span class="text-body-2 font-weight-bold">{{ grade.studentName.split(' ').map(n => n[0]).join('') }}</span>
                </v-avatar>
                <div>
                  <div class="text-subtitle-1 font-weight-bold">{{ grade.studentName }}</div>
                  <div class="text-caption text-medium-emphasis">Week {{ grade.weekNumber }}</div>
                </div>
              </v-card-title>
              <v-card-text class="pt-0">
                <div class="d-flex align-center justify-space-between mb-2">
                  <span class="text-body-2">Average Score</span>
                  <v-chip :color="gradeColor(gradePercent(grade))" size="small" variant="tonal">
                    {{ grade.averageScore.toFixed(1) }} / {{ grade.maxPossibleScore }}
                  </v-chip>
                </div>
                <v-progress-linear
                  :model-value="gradePercent(grade)"
                  :color="gradeColor(gradePercent(grade))"
                  rounded
                  height="8"
                  class="mb-3"
                />
                <div class="text-caption text-medium-emphasis">
                  {{ grade.evaluationsReceived.length }} evaluation(s) received
                  · {{ gradePercent(grade) }}%
                </div>
              </v-card-text>
              <v-divider />
              <!-- Expandable individual evaluations with comments -->
              <v-expansion-panels variant="accordion" flat>
                <v-expansion-panel>
                  <v-expansion-panel-title class="text-caption font-weight-medium px-4 py-2">
                    View Individual Evaluations
                  </v-expansion-panel-title>
                  <v-expansion-panel-text class="pa-0">
                    <div
                      v-for="(ev, idx) in grade.evaluationsReceived"
                      :key="ev.id"
                      class="px-4 py-3"
                      :class="{ 'border-t-thin': idx > 0 }"
                    >
                      <div class="d-flex justify-space-between mb-1">
                        <span class="text-caption font-weight-bold">Peer {{ idx + 1 }}</span>
                        <v-chip size="x-small" variant="tonal" color="success">
                          {{ ev.totalScore }} pts
                        </v-chip>
                      </div>
                      <div
                        v-for="score in ev.scores"
                        :key="score.criterionId"
                        class="mb-2"
                      >
                        <div class="d-flex justify-space-between text-caption">
                          <span>{{ score.criterionName }}</span>
                          <span>{{ score.score }}/{{ score.maxScore }}</span>
                        </div>
                        <div v-if="score.publicComment" class="text-caption text-medium-emphasis mt-1">
                          <v-icon icon="mdi-comment-outline" size="10" class="mr-1" />
                          Public: {{ score.publicComment }}
                        </div>
                        <div v-if="score.privateComment" class="text-caption text-warning mt-1">
                          <v-icon icon="mdi-eye-off-outline" size="10" class="mr-1" />
                          Private: {{ score.privateComment }}
                        </div>
                      </div>
                    </div>
                    <div v-if="grade.evaluationsReceived.length === 0" class="px-4 py-3 text-caption text-medium-emphasis">
                      No evaluations yet.
                    </div>
                  </v-expansion-panel-text>
                </v-expansion-panel>
              </v-expansion-panels>
            </v-card>
          </v-col>
        </v-row>
      </v-window-item>
    </v-window>

    <!-- Evaluate Dialog -->
    <v-dialog v-model="evalDialog" max-width="600" persistent>
      <v-card v-if="evalTarget" rounded="lg">
        <v-card-title class="pa-6 pb-4">
          {{ editingEvaluationId !== null ? 'Edit Evaluation —' : 'Evaluate' }}
          {{ evalTarget.firstName }} {{ evalTarget.lastName }}
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <template v-if="rubric">
            <div
              v-for="criterion in rubric.criteria"
              :key="criterion.id"
              class="mb-6"
            >
              <div class="d-flex justify-space-between align-center mb-1">
                <div>
                  <span class="font-weight-medium">{{ criterion.name }}</span>
                  <span
                    v-if="criterion.description"
                    class="text-body-2 text-medium-emphasis d-block"
                  >
                    {{ criterion.description }}
                  </span>
                </div>
                <v-chip size="small" variant="outlined">
                  {{ criterionScores[criterion.id] ?? 0 }} / {{ criterion.maxScore }}
                </v-chip>
              </div>
              <v-slider
                v-model="criterionScores[criterion.id]"
                :min="0"
                :max="criterion.maxScore"
                :step="1"
                thumb-label
                color="success"
                hide-details
                class="mb-3"
              />
              <v-textarea
                v-model="criterionPublicComments[criterion.id]"
                label="Public comment (visible to evaluatee)"
                variant="outlined"
                density="compact"
                rows="2"
                hide-details
                class="mb-2"
              />
              <v-textarea
                v-model="criterionPrivateComments[criterion.id]"
                label="Private comment (instructor only)"
                variant="outlined"
                density="compact"
                rows="2"
                hide-details
              />
            </div>
          </template>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="submittingEval" @click="evalDialog = false">
            Cancel
          </v-btn>
          <v-btn
            color="success"
            variant="elevated"
            :loading="submittingEval"
            @click="handleSubmitEval"
          >
            {{ editingEvaluationId !== null ? 'Save Changes' : 'Submit Evaluation' }}
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
