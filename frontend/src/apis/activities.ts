import request from './request'

export type ActivityCategory =
  | 'DEVELOPMENT'
  | 'TESTING'
  | 'BUGFIX'
  | 'COMMUNICATION'
  | 'DOCUMENTATION'
  | 'DESIGN'
  | 'PLANNING'
  | 'LEARNING'
  | 'DEPLOYMENT'
  | 'SUPPORT'
  | 'MISCELLANEOUS'

export type ActivityStatus = 'IN_PROGRESS' | 'UNDER_TESTING' | 'DONE'

export interface ActivityDto {
  id: number
  activityName: string
  category: ActivityCategory
  description: string
  plannedHours: number
  actualHours: number | null
  status: ActivityStatus
  submittedAt: string
  studentId: number
  studentName: string
  teamId: number
  weekId: number
  weekNumber: number
}

export interface ActivityCreatePayload {
  teamId: number
  weekId: number
  activityName: string
  category: ActivityCategory
  description: string
  plannedHours: number
  actualHours: number | null
  status: ActivityStatus
}

export interface ActivityUpdatePayload {
  activityName: string
  category: ActivityCategory
  description: string
  plannedHours: number
  actualHours: number | null
  status: ActivityStatus
}

type ApiResponse<T> = { flag: boolean; code: number; message: string; data: T }

export async function submitActivity(data: ActivityCreatePayload): Promise<ActivityDto> {
  const response = await request.post<ApiResponse<ActivityDto>>('/activities', data)
  return response.data.data
}

export async function updateActivity(id: number, data: ActivityUpdatePayload): Promise<ActivityDto> {
  const response = await request.put<ApiResponse<ActivityDto>>(`/activities/${id}`, data)
  return response.data.data
}

export async function deleteActivity(id: number): Promise<void> {
  await request.delete(`/activities/${id}`)
}

export async function getMyActivities(): Promise<ActivityDto[]> {
  const response = await request.get<ApiResponse<ActivityDto[]>>('/activities/my')
  return response.data.data
}

export async function getTeamActivities(teamId: number): Promise<ActivityDto[]> {
  const response = await request.get<ApiResponse<ActivityDto[]>>(`/activities/team/${teamId}`)
  return response.data.data
}

export async function getTeamWeekActivities(teamId: number, weekId: number): Promise<ActivityDto[]> {
  const response = await request.get<ApiResponse<ActivityDto[]>>(`/activities/team/${teamId}/week/${weekId}`)
  return response.data.data
}

export async function getAllActivities(): Promise<ActivityDto[]> {
  const response = await request.get<ApiResponse<ActivityDto[]>>('/activities')
  return response.data.data
}

export async function getStudentActivities(studentId: number): Promise<ActivityDto[]> {
  const response = await request.get<ApiResponse<ActivityDto[]>>(`/activities/student/${studentId}`)
  return response.data.data
}
