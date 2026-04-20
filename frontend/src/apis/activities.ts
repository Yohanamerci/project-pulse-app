import request from './request'

export type ActivityCategory =
  | 'DEVELOPMENT'
  | 'TESTING'
  | 'BUG_FIX'
  | 'DOCUMENTATION'
  | 'DESIGN'
  | 'MEETING'
  | 'OTHER'

export interface ActivityDto {
  id: number
  category: ActivityCategory
  description: string
  hours: number
  submittedAt: string
  studentId: number
  studentName: string
  teamId: number
  weekId: number
  weekNumber: number
}

export async function submitActivity(data: {
  teamId: number
  weekId: number
  category: ActivityCategory
  description: string
  hours: number
}): Promise<ActivityDto> {
  const response = await request.post<{ flag: boolean; code: number; message: string; data: ActivityDto }>('/activities', data)
  return response.data.data
}

export async function getMyActivities(): Promise<ActivityDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: ActivityDto[] }>('/activities/my')
  return response.data.data
}

export async function getTeamActivities(teamId: number): Promise<ActivityDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: ActivityDto[] }>(`/activities/team/${teamId}`)
  return response.data.data
}

export async function getTeamWeekActivities(teamId: number, weekId: number): Promise<ActivityDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: ActivityDto[] }>(`/activities/team/${teamId}/week/${weekId}`)
  return response.data.data
}

export async function getAllActivities(): Promise<ActivityDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: ActivityDto[] }>('/activities')
  return response.data.data
}
