import request from './request'

export interface SectionDto {
  id: number
  name: string
  semester: string
  year: number
  teamCount: number
  activeWeekCount: number
}

export interface ActiveWeekDto {
  id: number
  weekNumber: number
  startDate: string
  endDate: string
  active: boolean
  sectionId: number
}

export async function getSections(): Promise<SectionDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: SectionDto[] }>('/sections')
  return response.data.data
}

export async function createSection(data: {
  name: string
  semester: string
  year: number
}): Promise<SectionDto> {
  const response = await request.post<{ flag: boolean; code: number; message: string; data: SectionDto }>('/sections', data)
  return response.data.data
}

export async function updateSection(
  id: number,
  data: { name: string; semester: string; year: number }
): Promise<SectionDto> {
  const response = await request.put<{ flag: boolean; code: number; message: string; data: SectionDto }>(`/sections/${id}`, data)
  return response.data.data
}

export async function getSectionActiveWeek(id: number): Promise<ActiveWeekDto> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: ActiveWeekDto }>(`/sections/${id}/active-week`)
  return response.data.data
}

export async function getSectionActiveWeeks(id: number): Promise<ActiveWeekDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: ActiveWeekDto[] }>(`/sections/${id}/active-weeks`)
  return response.data.data
}

export async function setActiveWeek(
  id: number,
  data: { weekNumber: number; startDate: string; endDate: string }
): Promise<ActiveWeekDto> {
  const response = await request.post<{ flag: boolean; code: number; message: string; data: ActiveWeekDto }>(`/sections/${id}/active-week`, data)
  return response.data.data
}

export async function deleteSection(id: number): Promise<void> {
  await request.delete(`/sections/${id}`)
}
