<template>
  <v-container fluid class="fill-height pa-0 pp-root" :class="isDark ? 'pp-dark' : 'pp-light'">

    <!-- Floating theme toggle -->
    <v-btn
      :prepend-icon="isDark ? 'mdi-weather-sunny' : 'mdi-weather-night'"
      variant="elevated"
      :color="isDark ? 'warning' : 'secondary'"
      size="default"
      rounded="pill"
      elevation="4"
      class="pp-toggle"
      @click="toggleTheme"
    >
      {{ isDark ? 'Light Mode' : 'Dark Mode' }}
    </v-btn>

    <v-row no-gutters class="fill-height">

      <!-- ── Left branding panel (desktop only) ── -->
      <v-col
        cols="12"
        md="5"
        class="d-none d-md-flex flex-column justify-center align-center pa-12 text-center pp-left-panel"
      >
        <!-- Dot grid overlay -->
        <div class="pp-dot-grid" />
        <!-- Glow orb -->
        <div class="pp-left-orb" />

        <div class="position-relative" style="z-index: 2">
          <v-avatar size="80" rounded="xl" class="mb-6 pp-logo-avatar">
            <v-icon icon="mdi-account-plus" size="42" color="white" />
          </v-avatar>
          <h1 class="text-h4 font-weight-bold text-white mb-3" style="letter-spacing: -0.5px">
            Instructor Setup
          </h1>
          <p class="text-body-1 mb-8" style="color: rgba(255,255,255,0.65); max-width: 300px; line-height: 1.7">
            You've been invited to join Project Pulse as an instructor. Complete your profile to get started.
          </p>

          <v-divider style="border-color: rgba(255,255,255,0.15); max-width: 60px; margin: 0 auto" class="mb-8" />

          <!-- Steps -->
          <div class="d-flex ga-6 justify-center">
            <div v-for="step in steps" :key="step.label" class="text-center">
              <div
                class="text-h6 font-weight-bold mb-1 pp-stat-value"
                :style="{ color: step.color }"
              >{{ step.num }}</div>
              <div class="text-caption" style="color: rgba(255,255,255,0.45); font-size: 11px">{{ step.label }}</div>
            </div>
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
        <!-- Animated blobs -->
        <div class="pp-blob pp-blob--1" :style="{ background: blobColor1 }" />
        <div class="pp-blob pp-blob--2" :style="{ background: blobColor2 }" />
        <div class="pp-blob pp-blob--3" :style="{ background: blobColor3 }" />
        <div class="pp-blob pp-blob--4" :style="{ background: blobColor4 }" />

        <div class="d-flex flex-column align-center w-100 position-relative" style="max-width: 480px; z-index: 1">

          <v-card
            rounded="xl"
            class="w-100 mb-5 pp-form-card"
            :class="isDark ? 'pp-form-card--dark' : 'pp-form-card--light'"
          >
            <div class="pp-card-accent-bar" />
            <v-card-text class="pa-8">

              <!-- Mobile-only logo -->
              <div class="d-flex d-md-none align-center ga-3 mb-6">
                <v-avatar color="primary" size="40" rounded="lg">
                  <v-icon icon="mdi-pulse" size="22" color="white" />
                </v-avatar>
                <div>
                  <div class="text-h6 font-weight-bold">Project Pulse</div>
                  <div class="text-caption text-medium-emphasis">Instructor Registration</div>
                </div>
              </div>

              <div class="mb-6">
                <h2 class="text-h5 font-weight-bold mb-1">Create your account</h2>
                <p class="text-body-2 text-medium-emphasis">Complete your instructor profile to get started</p>
              </div>

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

              <v-alert
                v-if="!tokenFromUrl"
                type="info"
                variant="tonal"
                rounded="lg"
                density="compact"
                class="mb-5"
              >
                Paste the invitation token from your email below.
              </v-alert>

              <v-form ref="formRef" @submit.prevent="handleRegister">

                <v-text-field
                  v-model="form.token"
                  label="Invitation Token *"
                  prepend-inner-icon="mdi-key-outline"
                  variant="outlined"
                  rounded="lg"
                  :rules="[rules.required]"
                  :readonly="!!tokenFromUrl"
                  class="mb-4"
                />

                <v-row dense class="mb-1">
                  <v-col cols="12" sm="5">
                    <v-text-field
                      v-model="form.firstName"
                      label="First Name *"
                      variant="outlined"
                      rounded="lg"
                      :rules="[rules.required]"
                    />
                  </v-col>
                  <v-col cols="12" sm="2">
                    <v-text-field
                      v-model="form.middleInitial"
                      label="M.I."
                      variant="outlined"
                      rounded="lg"
                      maxlength="1"
                      hint="Optional"
                      persistent-hint
                    />
                  </v-col>
                  <v-col cols="12" sm="5">
                    <v-text-field
                      v-model="form.lastName"
                      label="Last Name *"
                      variant="outlined"
                      rounded="lg"
                      :rules="[rules.required]"
                    />
                  </v-col>
                </v-row>

                <v-text-field
                  v-model="form.password"
                  label="Password *"
                  prepend-inner-icon="mdi-lock-outline"
                  :type="showPassword ? 'text' : 'password'"
                  :append-inner-icon="showPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'"
                  variant="outlined"
                  rounded="lg"
                  :rules="[rules.required, rules.minLength]"
                  class="mb-3"
                  @click:append-inner="showPassword = !showPassword"
                />

                <v-text-field
                  v-model="form.confirmPassword"
                  label="Confirm Password *"
                  prepend-inner-icon="mdi-lock-check-outline"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  :append-inner-icon="showConfirmPassword ? 'mdi-eye-off-outline' : 'mdi-eye-outline'"
                  variant="outlined"
                  rounded="lg"
                  :rules="[rules.required, rules.passwordsMatch]"
                  class="mb-5"
                  @click:append-inner="showConfirmPassword = !showConfirmPassword"
                />

                <v-btn
                  type="submit"
                  color="primary"
                  size="large"
                  block
                  rounded="lg"
                  class="pp-sign-in-btn"
                  :loading="loading"
                >
                  Create Account
                </v-btn>
              </v-form>

              <div class="text-center mt-5">
                <v-btn
                  variant="text"
                  size="small"
                  color="medium-emphasis"
                  prepend-icon="mdi-arrow-left"
                  @click="router.push('/login')"
                >
                  Back to Sign In
                </v-btn>
              </div>

            </v-card-text>
          </v-card>

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
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { registerInstructor } from '@/apis/auth'

const route     = useRoute()
const router    = useRouter()
const authStore = useAuthStore()

// ── Theme ──
const vuetifyTheme = useTheme()
const isDark = computed(() => vuetifyTheme.global.name.value === 'dark')

function toggleTheme() {
  const next = isDark.value ? 'light' : 'dark'
  vuetifyTheme.global.name.value = next
  localStorage.setItem('pp-theme', next)
}

// Right panel background
const rightPanelStyle = computed(() => ({
  background: isDark.value
    ? '#050B18'
    : 'linear-gradient(145deg, #F0F4FF 0%, #F8FAFC 60%, #F5F0FF 100%)',
  minHeight: '100vh',
}))

// Animated blob colors
const blobColor1 = computed(() =>
  isDark.value
    ? 'radial-gradient(circle, rgba(0,212,255,0.18) 0%, transparent 65%)'
    : 'radial-gradient(circle, rgba(29,78,216,0.10) 0%, transparent 65%)'
)
const blobColor2 = computed(() =>
  isDark.value
    ? 'radial-gradient(circle, rgba(124,58,237,0.20) 0%, transparent 65%)'
    : 'radial-gradient(circle, rgba(79,70,229,0.09) 0%, transparent 65%)'
)
const blobColor3 = computed(() =>
  isDark.value
    ? 'radial-gradient(circle, rgba(236,72,153,0.14) 0%, transparent 65%)'
    : 'radial-gradient(circle, rgba(236,72,153,0.07) 0%, transparent 65%)'
)
const blobColor4 = computed(() =>
  isDark.value
    ? 'radial-gradient(circle, rgba(16,185,129,0.12) 0%, transparent 65%)'
    : 'radial-gradient(circle, rgba(5,150,105,0.07) 0%, transparent 65%)'
)

// ── Steps data ──
const steps = [
  { num: '1', label: 'Enter token',      color: '#00D4FF' },
  { num: '2', label: 'Fill details',     color: '#A78BFA' },
  { num: '3', label: 'Access dashboard', color: '#34D399' },
]

// ── Form ──
const tokenFromUrl = String(route.query.token ?? '')
const form = ref({
  token: tokenFromUrl,
  firstName: '',
  middleInitial: '',
  lastName: '',
  password: '',
  confirmPassword: '',
})
const showPassword        = ref(false)
const showConfirmPassword = ref(false)
const loading             = ref(false)
const errorMessage        = ref('')
const formRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)

const rules = {
  required:       (v: string) => !!v || 'This field is required',
  minLength:      (v: string) => v.length >= 8 || 'Password must be at least 8 characters',
  passwordsMatch: (v: string) => v === form.value.password || 'Passwords do not match',
}

async function handleRegister() {
  if (!formRef.value) return
  const { valid } = await formRef.value.validate()
  if (!valid) return

  loading.value = true
  errorMessage.value = ''
  try {
    const data = await registerInstructor({
      token:           form.value.token,
      firstName:       form.value.firstName,
      middleInitial:   form.value.middleInitial || undefined,
      lastName:        form.value.lastName,
      password:        form.value.password,
      confirmPassword: form.value.confirmPassword,
    })
    authStore.setSession(data.token, { userId: data.userId, username: data.username, role: data.role })
    router.push('/dashboard')
  } catch (e: unknown) {
    errorMessage.value = e instanceof Error ? e.message : 'Registration failed. Please check your token and try again.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.pp-root { position: relative; }

.pp-toggle {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 200;
  font-weight: 600;
  letter-spacing: 0.3px;
}

/* ── Left panel ── */
.pp-left-panel {
  background: linear-gradient(150deg, #0F0C29 0%, #302B63 50%, #24243E 100%);
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.pp-dot-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(255,255,255,0.12) 1px, transparent 1px);
  background-size: 28px 28px;
  pointer-events: none;
  z-index: 1;
}

.pp-left-orb {
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0,212,255,0.12) 0%, rgba(124,58,237,0.10) 50%, transparent 70%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 1;
  animation: pp-orb-pulse 6s ease-in-out infinite;
}

@keyframes pp-orb-pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1);   opacity: 1;   }
  50%       { transform: translate(-50%, -50%) scale(1.1); opacity: 0.7; }
}

.pp-logo-avatar {
  background: linear-gradient(135deg, #00D4FF, #7C3AED) !important;
  box-shadow: 0 0 28px rgba(0, 212, 255, 0.45);
}

.pp-stat-value {
  text-shadow: 0 0 12px currentColor;
}

/* ── Blobs ── */
.pp-blob {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.pp-blob--1 {
  width: 480px; height: 480px;
  top: -160px; right: -160px;
  animation: pp-drift-1 10s ease-in-out infinite;
}
.pp-blob--2 {
  width: 360px; height: 360px;
  bottom: -120px; left: -120px;
  animation: pp-drift-2 12s ease-in-out infinite;
}
.pp-blob--3 {
  width: 240px; height: 240px;
  top: 40%; left: 10%;
  animation: pp-drift-3 8s ease-in-out infinite;
}
.pp-blob--4 {
  width: 200px; height: 200px;
  top: 20%; left: 55%;
  animation: pp-drift-4 14s ease-in-out infinite;
}

@keyframes pp-drift-1 {
  0%, 100% { transform: translate(0, 0);     }
  50%       { transform: translate(-24px, 18px); }
}
@keyframes pp-drift-2 {
  0%, 100% { transform: translate(0, 0);     }
  50%       { transform: translate(20px, -16px); }
}
@keyframes pp-drift-3 {
  0%, 100% { transform: translate(0, 0);     }
  50%       { transform: translate(14px, 20px); }
}
@keyframes pp-drift-4 {
  0%, 100% { transform: translate(0, 0);     }
  50%       { transform: translate(-18px, 12px); }
}

/* ── Form card ── */
.pp-form-card {
  transition: box-shadow 0.3s ease;
}

.pp-form-card--dark {
  background: rgba(255, 255, 255, 0.04) !important;
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(124, 58, 237, 0.25) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.08),
              0 8px 32px rgba(0, 0, 0, 0.5),
              0 0 60px rgba(124, 58, 237, 0.08) !important;
}

.pp-form-card--light {
  background: #ffffff !important;
  border: 1px solid rgba(99, 102, 241, 0.12) !important;
  box-shadow: 0 4px 24px rgba(29, 78, 216, 0.08) !important;
}

.pp-card-accent-bar {
  height: 3px;
  border-radius: 12px 12px 0 0;
  background: linear-gradient(90deg, #00D4FF, #7C3AED, #EC4899);
}

.pp-sign-in-btn:hover {
  box-shadow: 0 0 20px rgba(29, 78, 216, 0.45) !important;
}
</style>
