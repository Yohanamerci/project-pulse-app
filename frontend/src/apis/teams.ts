import request from './request'

export interface TeamMemberDto {
  id: number
  username: string
  firstName: string
  lastName: string
  role: string
}

export interface TeamDto {
  id: number
  name: string
  sectionId: number | null
  sectionName: string | null
  rubricId: number | null
  rubricName: string | null
  students: TeamMemberDto[]
  instructors: TeamMemberDto[]
}

export async function getTeams(): Promise<TeamDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: TeamDto[] }>('/teams')
  return response.data.data
}

export async function getTeam(id: number): Promise<TeamDto> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: TeamDto }>(`/teams/${id}`)
  return response.data.data
}

export async function getTeamsBySection(sectionId: number): Promise<TeamDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: TeamDto[] }>(`/teams/section/${sectionId}`)
  return response.data.data
}

export async function getMyTeam(): Promise<TeamDto> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: TeamDto }>('/teams/my-team')
  return response.data.data
}

export async function createTeam(data: {
  name: string
  sectionId: number
  rubricId?: number
}): Promise<TeamDto> {
  const response = await request.post<{ flag: boolean; code: number; message: string; data: TeamDto }>('/teams', data)
  return response.data.data
}

export async function addStudent(teamId: number, studentId: number): Promise<TeamDto> {
  const response = await request.post<{ flag: boolean; code: number; message: string; data: TeamDto }>(`/teams/${teamId}/students/${studentId}`)
  return response.data.data
}

export async function removeStudent(teamId: number, studentId: number): Promise<TeamDto> {
  const response = await request.delete<{ flag: boolean; code: number; message: string; data: TeamDto }>(`/teams/${teamId}/students/${studentId}`)
  return response.data.data
}

export async function assignInstructor(teamId: number, instructorId: number): Promise<TeamDto> {
  const response = await request.put<{ flag: boolean; code: number; message: string; data: TeamDto }>(`/teams/${teamId}/instructor/${instructorId}`)
  return response.data.data
}

export async function assignRubric(teamId: number, rubricId: number): Promise<TeamDto> {
  const response = await request.put<{ flag: boolean; code: number; message: string; data: TeamDto }>(`/teams/${teamId}/rubric/${rubricId}`)
  return response.data.data
}

export async function deleteTeam(teamId: number): Promise<void> {
  await request.delete(`/teams/${teamId}`)
}
