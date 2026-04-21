<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getUserById, updateMe, type UserDto } from '@/apis/users'

const authStore = useAuthStore()

const loading = ref(false)
const saving = ref(false)
const error = ref<string | null>(null)
const snackbar = ref(false)
const snackbarMessage = ref('')

const formRef = ref<{ validate: () => Promise<{ valid: boolean }> } | null>(null)

const form = ref({
  firstName: '',
  lastName: '',
  email: '',
})

async function loadProfile() {
  if (!authStore.userInfo?.userId) return
  loading.value = true
  error.value = null
  try {
    const user: UserDto = await getUserById(authStore.userInfo.userId)
    form.value = {
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
    }
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to load profile.'
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!formRef.value) return
  const { valid } = await formRef.value.validate()
  if (!valid) return

  saving.value = true
  error.value = null
  try {
    await updateMe({
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      email: form.value.email,
    })
    snackbarMessage.value = 'Profile updated successfully!'
    snackbar.value = true
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'Failed to update profile.'
  } finally {
    saving.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <v-container fluid class="pa-6">
    <!-- Header -->
    <v-row align="center" class="mb-6">
      <v-col>
        <div class="d-flex align-center ga-3">
          <v-avatar color="primary" variant="tonal" size="48" rounded="lg">
            <v-icon icon="mdi-account-edit" size="28" />
          </v-avatar>
          <div>
            <h1 class="text-h5 font-weight-bold">Edit Profile</h1>
            <p class="text-body-2 text-medium-emphasis mb-0">
              Update your first name, last name, and email address
            </p>
          </div>
        </div>
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

    <div v-if="loading" class="d-flex justify-center py-12">
      <v-progress-circular indeterminate color="primary" />
    </div>

    <v-card v-else rounded="lg" elevation="2" max-width="480">
      <v-card-text class="pa-6">
        <v-form ref="formRef">
          <v-text-field
            v-model="form.firstName"
            label="First Name *"
            :rules="[(v) => !!v || 'First name is required']"
            variant="outlined"
            class="mb-4"
          />

          <v-text-field
            v-model="form.lastName"
            label="Last Name *"
            :rules="[(v) => !!v || 'Last name is required']"
            variant="outlined"
            class="mb-4"
          />

          <v-text-field
            v-model="form.email"
            label="Email *"
            type="email"
            :rules="[
              (v) => !!v || 'Email is required',
              (v) => /.+@.+\..+/.test(v) || 'Enter a valid email',
            ]"
            variant="outlined"
          />
        </v-form>
      </v-card-text>
      <v-divider />
      <v-card-actions class="pa-4">
        <v-spacer />
        <v-btn
          color="primary"
          variant="elevated"
          :loading="saving"
          @click="handleSave"
        >
          Save Changes
        </v-btn>
      </v-card-actions>
    </v-card>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar" color="success" :timeout="3000" location="bottom end">
      {{ snackbarMessage }}
      <template #actions>
        <v-btn variant="text" @click="snackbar = false">Close</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>
