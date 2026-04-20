import request from './request'

export interface UserDto {
  id: number
  username: string
  email: string
  firstName: string
  lastName: string
  role: 'ADMIN' | 'INSTRUCTOR' | 'STUDENT'
  enabled: boolean
}

export async function getUsers(): Promise<UserDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: UserDto[] }>('/users')
  return response.data.data
}

export async function getUserById(id: number): Promise<UserDto> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: UserDto }>(`/users/${id}`)
  return response.data.data
}

export async function createUser(data: {
  username: string
  email: string
  password: string
  firstName: string
  lastName: string
  role: 'ADMIN' | 'INSTRUCTOR' | 'STUDENT'
}): Promise<UserDto> {
  const response = await request.post<{ flag: boolean; code: number; message: string; data: UserDto }>('/users', data)
  return response.data.data
}

export async function updateUser(
  id: number,
  data: {
    firstName?: string
    lastName?: string
    email?: string
    enabled?: boolean
  }
): Promise<UserDto> {
  const response = await request.put<{ flag: boolean; code: number; message: string; data: UserDto }>(`/users/${id}`, data)
  return response.data.data
}
