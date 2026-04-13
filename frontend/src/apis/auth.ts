import request from './request'

interface LoginResponse {
  userId: number
  username: string
  role: 'ADMIN' | 'INSTRUCTOR' | 'STUDENT'
  token: string
}

export async function loginApi(username: string, password: string): Promise<LoginResponse> {
  const response = await request.post<{ flag: boolean; data: LoginResponse }>('/auth/login', {
    username,
    password,
  })
  return response.data.data
}
