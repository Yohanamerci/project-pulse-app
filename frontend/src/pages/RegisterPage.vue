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
          <v-icon icon="mdi-account-plus" size="36" color="primary" />
        </v-avatar>
        <h1 class="text-h4 font-weight-bold text-white mb-3">Instructor Setup</h1>
        <p class="text-body-1 mb-8" style="color: rgba(255,255,255,0.72); max-width: 320px; line-height: 1.6">
          You've been invited to join Project Pulse as an instructor. Complete your profile to get started.
        </p>
        <v-divider style="border-color: rgba(255,255,255,0.2); width: 60px" class="mb-8" />
        <div class="d-flex ga-8">
          <div>
            <div class="text-h6 font-weight-bold text-white">1</div>
            <div class="text-caption" style="color: rgba(255,255,255,0.55)">Enter token</div>
          </div>
          <div>
            <div class="text-h6 font-weight-bold text-white">2</div>
            <div class="text-caption" style="color: rgba(255,255,255,0.55)">Fill details</div>
          </div>
          <div>
            <div class="text-h6 font-weight-bold text-white">3</div>
            <div class="text-caption" style="color: rgba(255,255,255,0.55)">Access dashboard</div>
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
        <!-- Decorative blobs -->
        <div class="pp-blob pp-blob--1" :style="{ background: blobColor1 }" />
        <div class="pp-blob pp-blob--2" :style="{ background: blobColor2 }" />
        <div class="pp-blob pp-blob--3" :style="{ background: blobColor3 }" />

        <div class="d-flex flex-column align-center w-100 position-relative" style="max-width: 480px; z-index: 1">
        <v-card elevation="2" rounded="xl" class="w-100 mb-5">
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

            <h2 class="text-h5 font-weight-bold mb-1">Create your account</h2>
            <p class="text-body-2 text-medium-emphasis mb-6">
              Complete your instructor profile to get started
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

              <!-- Invitation token -->
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

              <!-- Name row -->
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

        </div><!-- end centred column -->
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

const route = useRoute()
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

// Theme-aware blob colors
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

const tokenFromUrl = String(route.query.token ?? '')
const form = ref({
  token: tokenFromUrl,
  firstName: '',
  middleInitial: '',
  lastName: '',
  password: '',
  confirmPassword: '',
})
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const loading = ref(false)
const errorMessage = ref('')
const formRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)

const rules = {
  required: (v: string) => !!v || 'This field is required',
  minLength: (v: string) => v.length >= 8 || 'Password must be at least 8 characters',
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
      token: form.value.token,
      firstName: form.value.firstName,
      middleInitial: form.value.middleInitial || undefined,
      lastName: form.value.lastName,
      password: form.value.password,
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
