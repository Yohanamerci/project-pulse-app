import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { loginApi } from '@/apis/auth'

interface UserInfo {
  userId: number
  username: string
  role: 'ADMIN' | 'INSTRUCTOR' | 'STUDENT'
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(
    localStorage.getItem('userInfo')
      ? JSON.parse(localStorage.getItem('userInfo')!)
      : null
  )

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const isInstructor = computed(() => userInfo.value?.role === 'INSTRUCTOR')
  const isStudent = computed(() => userInfo.value?.role === 'STUDENT')

  async function login(username: string, password: string) {
    const data = await loginApi(username, password)
    token.value = data.token
    userInfo.value = { userId: data.userId, username: data.username, role: data.role }
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  function logout() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, isLoggedIn, isAdmin, isInstructor, isStudent, login, logout }
})
