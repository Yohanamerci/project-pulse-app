<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="5">
        <v-card elevation="4" rounded="lg">
          <v-card-title class="text-h5 font-weight-bold pa-6 pb-2">
            Project Pulse
          </v-card-title>
          <v-card-subtitle class="px-6 pb-4">
            Complete your instructor registration
          </v-card-subtitle>

          <v-card-text class="pa-6 pt-0">
            <v-alert
              v-if="errorMessage"
              type="error"
              variant="tonal"
              class="mb-4"
              closable
              @click:close="errorMessage = ''"
            >
              {{ errorMessage }}
            </v-alert>

            <v-alert
              v-if="!tokenFromUrl"
              type="info"
              variant="tonal"
              class="mb-4"
              density="compact"
            >
              Paste the invitation token from your email below, then fill in your details.
            </v-alert>

            <v-form ref="formRef" @submit.prevent="handleRegister">
              <!-- Invitation token -->
              <v-text-field
                v-model="form.token"
                label="Invitation Token *"
                prepend-inner-icon="mdi-key"
                variant="outlined"
                :rules="[rules.required]"
                :readonly="!!tokenFromUrl"
                class="mb-3"
              />

              <v-row dense class="mb-1">
                <v-col cols="12" sm="5">
                  <v-text-field
                    v-model="form.firstName"
                    label="First Name *"
                    variant="outlined"
                    :rules="[rules.required]"
                  />
                </v-col>
                <v-col cols="12" sm="2">
                  <v-text-field
                    v-model="form.middleInitial"
                    label="M.I."
                    variant="outlined"
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
                    :rules="[rules.required]"
                  />
                </v-col>
              </v-row>

              <v-text-field
                v-model="form.password"
                label="Password *"
                prepend-inner-icon="mdi-lock"
                :type="showPassword ? 'text' : 'password'"
                :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                variant="outlined"
                :rules="[rules.required, rules.minLength]"
                class="mb-3"
                @click:append-inner="showPassword = !showPassword"
              />

              <v-text-field
                v-model="form.confirmPassword"
                label="Confirm Password *"
                prepend-inner-icon="mdi-lock-check"
                :type="showConfirmPassword ? 'text' : 'password'"
                :append-inner-icon="showConfirmPassword ? 'mdi-eye-off' : 'mdi-eye'"
                variant="outlined"
                :rules="[rules.required, rules.passwordsMatch]"
                class="mb-4"
                @click:append-inner="showConfirmPassword = !showConfirmPassword"
              />

              <v-btn
                type="submit"
                color="primary"
                size="large"
                block
                :loading="loading"
              >
                Create Account
              </v-btn>
            </v-form>

            <div class="text-center mt-4">
              <v-btn variant="text" size="small" @click="router.push('/login')">
                Back to Sign In
              </v-btn>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { registerInstructor } from '@/apis/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

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
    // Sync auth store and redirect
    authStore.setSession(data.token, { userId: data.userId, username: data.username, role: data.role })
    router.push('/dashboard')
  } catch (e: unknown) {
    errorMessage.value = e instanceof Error ? e.message : 'Registration failed. Please check your token and try again.'
  } finally {
    loading.value = false
  }
}
</script>
