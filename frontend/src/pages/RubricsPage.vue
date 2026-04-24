<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import {
  getRubrics,
  createRubric,
  addCriterion,
  updateCriterion,
  deleteRubric,
  deleteCriterion,
  type RubricDto,
  type CriterionDto,
} from '@/apis/rubrics'
import { getSections, type SectionDto } from '@/apis/sections'

const authStore = useAuthStore()

// ── Predefined criteria templates ──────────────────────────────────────────
const DEFAULT_CRITERIA = [
  { name: 'Technical Contribution', description: 'Quality and quantity of technical work completed this week', maxScore: 10 },
  { name: 'Collaboration', description: 'Willingness to help teammates and communicate effectively', maxScore: 10 },
  { name: 'Meeting Attendance', description: 'Attendance and participation in scheduled team meetings', maxScore: 5 },
  { name: 'Task Completion', description: 'Delivered on assigned tasks and met agreed deadlines', maxScore: 10 },
  { name: 'Code Quality / Artifacts', description: 'Code reviews, documentation, and deliverable quality', maxScore: 10 },
]

const STORAGE_KEY = 'projectpulse_criteria_templates'

function loadTemplates(): typeof DEFAULT_CRITERIA {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const custom = JSON.parse(stored) as typeof DEFAULT_CRITERIA
      const defaultNames = new Set(DEFAULT_CRITERIA.map((c) => c.name))
      const extras = custom.filter((c) => !defaultNames.has(c.name))
      return [...DEFAULT_CRITERIA, ...extras]
    }
  } catch { /* ignore */ }
  return [...DEFAULT_CRITERIA]
}

function saveTemplates(templates: typeof DEFAULT_CRITERIA) {
  try { localStorage.setItem(STORAGE_KEY, JSON.stringify(templates)) } catch { /* ignore */ }
}

const criteriaTemplates = ref(loadTemplates())

const rubrics = ref<RubricDto[]>([])
const sections = ref<SectionDto[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const snackbar = ref(false)
const snackbarMessage = ref('')

async function loadAll() {
  loading.value = true
  error.value = null
  try {
    ;[rubrics.value, sections.value] = await Promise.all([getRubrics(), getSections()])
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load rubrics.'
  } finally {
    loading.value = false
  }
}

function sectionName(id: number | null): string {
  if (!id) return '—'
  return sections.value.find((s) => s.id === id)?.name ?? `Section ${id}`
}

// ── Create Rubric Dialog ────────────────────────────────────────────────────
const createDialog = ref(false)
const createFormRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)
const creating = ref(false)
const createForm = ref({ name: '', description: '', sectionId: null as number | null })

function openCreateDialog() {
  createForm.value = { name: '', description: '', sectionId: null }
  createDialog.value = true
}

async function handleCreate() {
  if (!createFormRef.value) return
  const { valid } = await createFormRef.value.validate()
  if (!valid) return

  creating.value = true
  try {
    await createRubric({
      name: createForm.value.name,
      description: createForm.value.description || undefined,
      sectionId: createForm.value.sectionId ?? undefined,
      criteria: [],
    })
    createDialog.value = false
    snackbarMessage.value = `Rubric "${createForm.value.name}" created!`
    snackbar.value = true
    await loadAll()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to create rubric.'
  } finally {
    creating.value = false
  }
}

// ── Delete Rubric ───────────────────────────────────────────────────────────
const deletingRubricId = ref<number | null>(null)

async function handleDeleteRubric(rubric: RubricDto) {
  if (!confirm(`Delete rubric "${rubric.name}"? This cannot be undone.`)) return
  deletingRubricId.value = rubric.id
  try {
    await deleteRubric(rubric.id)
    snackbarMessage.value = 'Rubric deleted.'
    snackbar.value = true
    await loadAll()
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to delete rubric.'
  } finally {
    deletingRubricId.value = null
  }
}

// ── Criteria Management Dialog ──────────────────────────────────────────────
const criteriaDialog = ref(false)
const activeCriteriaRubric = ref<RubricDto | null>(null)
const criterionFormRef = ref<{ validate: () => Promise<{ valid: boolean }>; reset: () => void } | null>(null)
const addingCriterion = ref(false)
const criterionForm = ref({ name: '', description: '', maxScore: 10 })
const editingCriterion = ref<CriterionDto | null>(null)

function openCriteriaDialog(rubric: RubricDto) {
  activeCriteriaRubric.value = rubric
  resetCriterionForm()
  criteriaDialog.value = true
}

function resetCriterionForm() {
  criterionForm.value = { name: '', description: '', maxScore: 10 }
  editingCriterion.value = null
}

function startEditCriterion(c: CriterionDto) {
  editingCriterion.value = c
  criterionForm.value = { name: c.name, description: c.description ?? '', maxScore: c.maxScore }
}

function applyTemplate(t: { name: string; description: string; maxScore: number }) {
  criterionForm.value = { name: t.name, description: t.description, maxScore: t.maxScore }
  editingCriterion.value = null
}

async function handleSaveCriterion() {
  if (!criterionFormRef.value || !activeCriteriaRubric.value) return
  const { valid } = await criterionFormRef.value.validate()
  if (!valid) return

  addingCriterion.value = true
  try {
    const payload = {
      name: criterionForm.value.name,
      description: criterionForm.value.description || undefined,
      maxScore: Number(criterionForm.value.maxScore),
    }
    if (editingCriterion.value) {
      await updateCriterion(editingCriterion.value.id, payload)
      snackbarMessage.value = 'Criterion updated.'
    } else {
      await addCriterion(activeCriteriaRubric.value.id, payload)
      snackbarMessage.value = 'Criterion added.'
      // Persist as a reusable template if it's new
      const exists = criteriaTemplates.value.some((t) => t.name === payload.name)
      if (!exists) {
        criteriaTemplates.value = [...criteriaTemplates.value, {
          name: payload.name,
          description: payload.description ?? '',
          maxScore: payload.maxScore,
        }]
        saveTemplates(criteriaTemplates.value)
      }
    }
    snackbar.value = true
    resetCriterionForm()
    criterionFormRef.value?.reset()
    await loadAll()
    // Refresh the active rubric in the dialog
    const fresh = rubrics.value.find((r) => r.id === activeCriteriaRubric.value!.id)
    if (fresh) activeCriteriaRubric.value = fresh
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to save criterion.'
  } finally {
    addingCriterion.value = false
  }
}

async function handleDeleteCriterion(c: CriterionDto) {
  if (!confirm(`Remove criterion "${c.name}"?`)) return
  try {
    await deleteCriterion(c.id)
    snackbarMessage.value = 'Criterion removed.'
    snackbar.value = true
    await loadAll()
    const fresh = rubrics.value.find((r) => r.id === activeCriteriaRubric.value?.id)
    if (fresh) activeCriteriaRubric.value = fresh
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to delete criterion.'
  }
}

onMounted(loadAll)
</script>

<template>
  <v-container fluid class="pa-6">
    <!-- Header -->
    <div class="pp-page-hero mb-6">
      <div class="pp-page-hero-grid" />
      <div class="pp-page-hero-glow" style="background:radial-gradient(ellipse,rgba(167,139,250,0.26) 0%,transparent 70%)" />
      <div class="d-flex align-center justify-space-between flex-wrap gap-3 position-relative" style="z-index:1">
        <div class="d-flex align-center ga-4">
          <div class="pp-page-hero-icon" style="background:linear-gradient(135deg,#A78BFA,#7C3AED)">
            <v-icon icon="mdi-clipboard-list" size="28" color="white" />
          </div>
          <div>
            <h1 class="text-h5 font-weight-bold text-white mb-0">Rubrics</h1>
            <p class="text-body-2 mb-0" style="color:rgba(255,255,255,0.65)">
              <template v-if="authStore.isAdmin">Create and manage peer evaluation rubrics and scoring criteria</template>
              <template v-else>Manage scoring criteria within existing rubrics</template>
            </p>
          </div>
        </div>
        <v-btn v-if="authStore.isAdmin" color="purple" variant="elevated" prepend-icon="mdi-plus" @click="openCreateDialog">
          Create Rubric
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

    <!-- Loading -->
    <v-progress-linear v-if="loading" indeterminate color="purple" class="mb-4" />

    <!-- Empty state -->
    <v-alert
      v-if="!loading && rubrics.length === 0"
      type="info"
      variant="tonal"
      class="mb-4"
    >
      No rubrics yet. Create one to get started.
    </v-alert>

    <!-- Rubric Cards -->
    <v-row>
      <v-col
        v-for="rubric in rubrics"
        :key="rubric.id"
        cols="12"
        md="6"
        xl="4"
      >
        <v-card rounded="lg" elevation="2" height="100%">
          <v-card-title class="d-flex align-center justify-space-between pa-4 pb-2">
            <span class="text-subtitle-1 font-weight-bold">{{ rubric.name }}</span>
            <v-chip color="purple" size="small" variant="tonal">
              {{ rubric.criteria.length }} criteria
            </v-chip>
          </v-card-title>

          <v-card-subtitle class="px-4 pb-1">
            <v-icon icon="mdi-school" size="14" class="mr-1" />
            {{ sectionName(rubric.sectionId) }}
          </v-card-subtitle>

          <v-card-text class="px-4">
            <p v-if="rubric.description" class="text-body-2 text-medium-emphasis mb-3">
              {{ rubric.description }}
            </p>

            <!-- Criteria chips -->
            <div v-if="rubric.criteria.length" class="d-flex flex-wrap ga-1 mb-2">
              <v-chip
                v-for="c in rubric.criteria.slice().sort((a, b) => a.displayOrder - b.displayOrder)"
                :key="c.id"
                size="small"
                variant="tonal"
                color="grey"
              >
                {{ c.name }} ({{ c.maxScore }})
              </v-chip>
            </div>
            <p v-else class="text-body-2 text-disabled">No criteria added yet.</p>

            <!-- Max score -->
            <p class="text-caption text-medium-emphasis mt-2">
              Max total:
              <strong>
                {{ rubric.criteria.reduce((sum, c) => sum + c.maxScore, 0) }} pts
              </strong>
            </p>
          </v-card-text>

          <v-divider />
          <v-card-actions class="pa-3">
            <v-btn
              prepend-icon="mdi-pencil-outline"
              size="small"
              variant="tonal"
              color="purple"
              @click="openCriteriaDialog(rubric)"
            >
              Manage Criteria
            </v-btn>
            <v-spacer />
            <v-btn
              v-if="authStore.isAdmin"
              icon="mdi-delete-outline"
              variant="text"
              size="small"
              color="error"
              :loading="deletingRubricId === rubric.id"
              @click="handleDeleteRubric(rubric)"
            />
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <!-- Create Rubric Dialog -->
    <v-dialog v-model="createDialog" max-width="480" persistent>
      <v-card rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-clipboard-plus" color="purple" />
          Create Rubric
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <v-form ref="createFormRef">
            <v-text-field
              v-model="createForm.name"
              label="Rubric Name *"
              :rules="[(v) => !!v || 'Name is required']"
              variant="outlined"
              class="mb-4"
            />
            <v-text-field
              v-model="createForm.description"
              label="Description"
              variant="outlined"
              class="mb-4"
            />
            <v-select
              v-model="createForm.sectionId"
              :items="sections.map((s) => ({ title: s.name, value: s.id }))"
              label="Linked Section (optional)"
              variant="outlined"
              clearable
            />
          </v-form>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn variant="text" :disabled="creating" @click="createDialog = false">Cancel</v-btn>
          <v-btn color="purple" variant="elevated" :loading="creating" @click="handleCreate">
            Create
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Criteria Management Dialog -->
    <v-dialog v-model="criteriaDialog" max-width="620" persistent>
      <v-card v-if="activeCriteriaRubric" rounded="lg">
        <v-card-title class="pa-6 pb-4 d-flex align-center ga-2">
          <v-icon icon="mdi-format-list-checks" color="purple" />
          Criteria — {{ activeCriteriaRubric.name }}
        </v-card-title>
        <v-divider />
        <v-card-text class="pa-6">
          <!-- Existing criteria list -->
          <p class="text-subtitle-2 font-weight-medium mb-3">
            Current Criteria
            <v-chip size="x-small" color="purple" variant="tonal" class="ml-1">
              Max:
              {{
                activeCriteriaRubric.criteria.reduce((s, c) => s + c.maxScore, 0)
              }}
              pts
            </v-chip>
          </p>

          <v-list
            v-if="activeCriteriaRubric.criteria.length"
            density="compact"
            class="mb-4 border rounded-lg"
          >
            <v-list-item
              v-for="c in activeCriteriaRubric.criteria
                .slice()
                .sort((a, b) => a.displayOrder - b.displayOrder)"
              :key="c.id"
              :title="c.name"
              :subtitle="c.description ?? ''"
            >
              <template #append>
                <v-chip size="x-small" variant="tonal" color="success" class="mr-2">
                  {{ c.maxScore }} pts
                </v-chip>
                <v-btn
                  icon="mdi-pencil"
                  variant="text"
                  size="x-small"
                  color="primary"
                  @click="startEditCriterion(c)"
                />
                <v-btn
                  icon="mdi-delete"
                  variant="text"
                  size="x-small"
                  color="error"
                  @click="handleDeleteCriterion(c)"
                />
              </template>
            </v-list-item>
          </v-list>
          <p v-else class="text-body-2 text-disabled mb-4">No criteria added yet.</p>

          <v-divider class="mb-4" />

          <!-- Quick-add from templates -->
          <p class="text-subtitle-2 font-weight-medium mb-2">Quick Add from Templates</p>
          <div class="d-flex flex-wrap ga-2 mb-4">
            <v-btn
              v-for="t in criteriaTemplates"
              :key="t.name"
              size="x-small"
              variant="tonal"
              color="purple"
              :disabled="activeCriteriaRubric.criteria.some((c) => c.name === t.name)"
              @click="applyTemplate(t)"
            >
              {{ t.name }} ({{ t.maxScore }}pts)
            </v-btn>
          </div>

          <v-divider class="mb-4" />

          <!-- Add / Edit criterion form -->
          <p class="text-subtitle-2 font-weight-medium mb-3">
            {{ editingCriterion ? 'Edit Criterion' : 'Add Criterion' }}
          </p>
          <v-form ref="criterionFormRef">
            <v-text-field
              v-model="criterionForm.name"
              label="Criterion Name *"
              :rules="[(v) => !!v || 'Name is required']"
              variant="outlined"
              density="compact"
              class="mb-3"
            />
            <v-text-field
              v-model="criterionForm.description"
              label="Description"
              variant="outlined"
              density="compact"
              class="mb-3"
            />
            <v-text-field
              v-model="criterionForm.maxScore"
              label="Max Score *"
              type="number"
              :rules="[
                (v) => !!v || 'Max score is required',
                (v) => Number(v) > 0 || 'Must be greater than 0',
              ]"
              variant="outlined"
              density="compact"
              min="1"
              max="100"
            />
          </v-form>
        </v-card-text>
        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn v-if="editingCriterion" variant="text" @click="resetCriterionForm">
            Cancel Edit
          </v-btn>
          <v-spacer />
          <v-btn variant="text" @click="criteriaDialog = false">Close</v-btn>
          <v-btn
            color="purple"
            variant="elevated"
            :loading="addingCriterion"
            @click="handleSaveCriterion"
          >
            {{ editingCriterion ? 'Save' : 'Add Criterion' }}
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
