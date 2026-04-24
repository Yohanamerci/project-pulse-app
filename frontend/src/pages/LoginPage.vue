<template>
  <v-container fluid class="fill-height pa-0">

    <!-- Floating theme toggle — visible before login -->
    <v-btn
      :prepend-icon="isDark ? 'mdi-weather-sunny' : 'mdi-weather-night'"
      variant="elevated"
      :color="isDark ? 'warning' : 'secondary'"
      size="default"
      rounded="pill"
      elevation="4"
      style="position: fixed; top: 16px; right: 16px; z-index: 200; font-weight: 600; letter-spacing: 0.3px"
      @click="toggleTheme"
    >
      {{ isDark ? 'Light Mode' : 'Dark Mode' }}
    </v-btn>

    <v-row no-gutters class="fill-height">

      <!-- ── Left branding panel (desktop only) ── -->
      <v-col
        cols="12"
        md="5"
        class="d-none d-md-flex flex-column justify-center align-center pa-12 text-center"
        style="background: linear-gradient(160deg, #1D4ED8 0%, #4F46E5 100%); min-height: 100vh"
      >
        <v-avatar color="white" size="72" rounded="xl" class="mb-6">
          <v-icon icon="mdi-pulse" size="38" color="primary" />
        </v-avatar>
        <h1 class="text-h4 font-weight-bold text-white mb-3">Project Pulse</h1>
        <p class="text-body-1 mb-8" style="color: rgba(255,255,255,0.72); max-width: 320px; line-height: 1.6">
          Track weekly activities, submit peer evaluations, and manage your senior design team — all in one place.
        </p>
        <v-divider style="border-color: rgba(255,255,255,0.2); width: 60px" class="mb-8" />
        <div class="d-flex ga-8">
          <div>
            <div class="text-h6 font-weight-bold text-white">WAR</div>
            <div class="text-caption" style="color: rgba(255,255,255,0.55)">Weekly Reports</div>
          </div>
          <div>
            <div class="text-h6 font-weight-bold text-white">Peer</div>
            <div class="text-caption" style="color: rgba(255,255,255,0.55)">Evaluations</div>
          </div>
          <div>
            <div class="text-h6 font-weight-bold text-white">Grade</div>
            <div class="text-caption" style="color: rgba(255,255,255,0.55)">Tracking</div>
          </div>
        </div>
      </v-col>

      <!-- ── Right form panel ── -->
      <v-col
        cols="12"
        md="7"
        class="d-flex align-center justify-center pa-6 pa-sm-10 position-relative overflow-hidden"
        :style="rightPanelStyle"
      >
        <!-- Decorative background blobs -->
        <div class="pp-blob pp-blob--1" :style="{ background: blobColor1 }" />
        <div class="pp-blob pp-blob--2" :style="{ background: blobColor2 }" />
        <div class="pp-blob pp-blob--3" :style="{ background: blobColor3 }" />

        <!-- Centred column: card + features + footer -->
        <div class="d-flex flex-column align-center w-100 position-relative" style="max-width: 440px; z-index: 1">

          <!-- Form card -->
          <v-card elevation="2" rounded="xl" class="w-100 mb-6">
            <v-card-text class="pa-8">

              <!-- Mobile-only logo -->
              <div class="d-flex d-md-none align-center ga-3 mb-6">
                <v-avatar color="primary" size="40" rounded="lg">
                  <v-icon icon="mdi-pulse" size="22" color="white" />
                </v-avatar>
                <div>
                  <div class="text-h6 font-weight-bold">Project Pulse</div>
                  <div class="text-caption text-medium-emphasis">Team Activity Tracker</div>
                </div>
              </div>

              <h2 class="text-h5 font-weight-bold mb-1">Sign in</h2>
              <p class="text-body-2 text-medium-emphasis mb-6">
                Enter your credentials to continue
              </p>

              <v-alert
                v-if="errorMessage"
                type="error"
                variant="tonal"
                rounded="lg"
                class="mb-5"
                closable
                @click:close="errorMessage = ''"
              >
                {{ errorMessage }}
              </v-alert>

              <v-form @submit.prevent="handleLogin">
                <v-text-field
                  v-model="username"
                  label="Username"
                  prepend-inner-icon="mdi-account-outline"
                  variant="outlined"
                  :rules="[rules.required]"
                  class="mb-3"
                  autofocus
                  rounded="lg"
                />
                <v-text-field
                  v-model="password"
                  label="Password"
                  prepend-inner-icon="mdi-lock-outline"
                  :type="showPassword ? 'text' : 'password'"
                  :append-inner-icon="showPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'"
                  variant="outlined"
                  :rules="[rules.required]"
                  rounded="lg"
                  @click:append-inner="showPassword = !showPassword"
                />

                <v-btn
                  type="submit"
                  color="primary"
                  size="large"
                  block
                  rounded="lg"
                  class="mt-5"
                  :loading="loading"
                >
                  Sign In
                </v-btn>
              </v-form>

              <v-divider class="my-6" />

              <div class="text-center">
                <p class="text-body-2 text-medium-emphasis mb-3">
                  Instructor? Received an invitation email?
                </p>
                <v-btn
                  variant="tonal"
                  color="primary"
                  size="small"
                  rounded="lg"
                  prepend-icon="mdi-account-plus-outline"
                  @click="router.push('/register')"
                >
                  Register with Invitation Token
                </v-btn>
              </div>

            </v-card-text>
          </v-card>

          <!-- Feature highlights -->
          <div class="d-flex flex-wrap justify-center ga-2 mb-6">
            <v-chip
              v-for="f in features"
              :key="f.label"
              :color="f.color"
              :prepend-icon="f.icon"
              variant="tonal"
              size="small"
              label
            >
              {{ f.label }}
            </v-chip>
          </div>

          <!-- Footer -->
          <p class="text-caption text-center text-disabled">
            TCU Computer Science &nbsp;·&nbsp; Senior Design Program
          </p>

        </div>
      </v-col>

    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useTheme } from 'vuetify'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// ── Theme (shared with App.vue via same Vuetify instance + localStorage) ──
const vuetifyTheme = useTheme()
const isDark = computed(() => vuetifyTheme.global.name.value === 'dark')

function toggleTheme() {
  const next = isDark.value ? 'light' : 'dark'
  vuetifyTheme.global.name.value = next
  localStorage.setItem('pp-theme', next)
}

// Theme-aware right panel background
const rightPanelStyle = computed(() => ({
  background: isDark.value
    ? 'linear-gradient(145deg, #0D1829 0%, #0F172A 55%, #0C1A11 100%)'
    : 'linear-gradient(145deg, #EFF6FF 0%, #F8FAFC 55%, #F0FDF4 100%)',
  minHeight: '100vh',
}))

// Theme-aware blob colors (brighter in dark mode to stay visible)
const blobColor1 = computed(() =>
  isDark.value
    ? 'radial-gradient(circle, rgba(96,165,250,0.13) 0%, transparent 70%)'
    : 'radial-gradient(circle, rgba(29,78,216,0.08) 0%, transparent 70%)'
)
const blobColor2 = computed(() =>
  isDark.value
    ? 'radial-gradient(circle, rgba(129,140,248,0.10) 0%, transparent 70%)'
    : 'radial-gradient(circle, rgba(79,70,229,0.07) 0%, transparent 70%)'
)
const blobColor3 = computed(() =>
  isDark.value
    ? 'radial-gradient(circle, rgba(52,211,153,0.08) 0%, transparent 70%)'
    : 'radial-gradient(circle, rgba(5,150,105,0.05) 0%, transparent 70%)'
)

// ── Form logic (unchanged) ──
const username = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const errorMessage = ref('')

const rules = {
  required: (v: string) => !!v || 'This field is required',
}

const features = [
  { label: 'Weekly Activity Reports', icon: 'mdi-calendar-check',  color: 'primary'   },
  { label: 'Peer Evaluations',        icon: 'mdi-star-check',       color: 'success'   },
  { label: 'Grade Tracking',          icon: 'mdi-chart-bar',        color: 'info'      },
  { label: 'Team Management',         icon: 'mdi-account-group',    color: 'warning'   },
  { label: 'Section Scheduling',      icon: 'mdi-school',           color: 'secondary' },
]

async function handleLogin() {
  if (!username.value || !password.value) return
  loading.value = true
  errorMessage.value = ''
  try {
    await authStore.login(username.value, password.value)
    router.push('/dashboard')
  } catch {
    errorMessage.value = 'Invalid username or password.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.pp-blob {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.pp-blob--1 {
  width: 420px; height: 420px;
  top: -140px; right: -140px;
}
.pp-blob--2 {
  width: 320px; height: 320px;
  bottom: -100px; left: -100px;
}
.pp-blob--3 {
  width: 200px; height: 200px;
  top: 45%; left: 15%;
}
</style>
