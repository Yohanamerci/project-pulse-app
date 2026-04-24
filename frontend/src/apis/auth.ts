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

/** UC-30: Admin invites an instructor by email. */
export async function inviteInstructor(email: string): Promise<void> {
  await request.post('/auth/invite', { email })
}

interface RegisterResponse {
  userId: number
  username: string
  role: 'ADMIN' | 'INSTRUCTOR' | 'STUDENT'
  token: string
}

/** UC-30: Instructor completes self-registration via invitation token. */
export async function registerInstructor(data: {
  token: string
  firstName: string
  middleInitial?: string
  lastName: string
  password: string
  confirmPassword: string
}): Promise<RegisterResponse> {
  const response = await request.post<{ flag: boolean; data: RegisterResponse }>('/auth/register', data)
  return response.data.data
}
