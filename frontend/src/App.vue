<template>
  <v-app>
    <AppNavBar v-if="authStore.isLoggedIn" />
    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import AppNavBar from '@/components/AppNavBar.vue'

const authStore = useAuthStore()
</script>
