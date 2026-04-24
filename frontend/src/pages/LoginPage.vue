<template>
  <v-container class="fill-height" fluid>
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="5" lg="4">
        <v-card elevation="4" rounded="lg">
          <v-card-title class="text-h5 font-weight-bold pa-6 pb-2">
            Project Pulse
          </v-card-title>
          <v-card-subtitle class="px-6 pb-4">
            Sign in to your account
          </v-card-subtitle>

          <v-card-text>
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

            <v-form @submit.prevent="handleLogin">
              <v-text-field
                v-model="username"
                label="Username"
                prepend-inner-icon="mdi-account"
                variant="outlined"
                :rules="[rules.required]"
                class="mb-3"
                autofocus
              />
              <v-text-field
                v-model="password"
                label="Password"
                prepend-inner-icon="mdi-lock"
                :type="showPassword ? 'text' : 'password'"
                :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                variant="outlined"
                :rules="[rules.required]"
                @click:append-inner="showPassword = !showPassword"
              />

              <v-btn
                type="submit"
                color="primary"
                size="large"
                block
                class="mt-4"
                :loading="loading"
              >
                Sign In
              </v-btn>
            </v-form>

            <v-divider class="my-4" />

            <div class="text-center">
              <p class="text-body-2 text-medium-emphasis mb-2">
                Instructor? Received an invitation email?
              </p>
              <v-btn
                variant="tonal"
                color="primary"
                size="small"
                prepend-icon="mdi-account-plus"
                @click="router.push('/register')"
              >
                Register with Invitation Token
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const errorMessage = ref('')

const rules = {
  required: (v: string) => !!v || 'This field is required',
}

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
