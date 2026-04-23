import axios from 'axios'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
})

// Attach JWT token from localStorage to every request
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Redirect to login on 401; surface backend message for all other errors
request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    // Extract the backend's { message } field so catch blocks see the real error
    const backendMessage: string | undefined = error.response?.data?.message
    return Promise.reject(new Error(backendMessage ?? error.message))
  }
)

export default request
