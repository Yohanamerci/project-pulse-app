import request from './request'

export interface CriterionDto {
  id: number
  name: string
  description: string | null
  maxScore: number
  displayOrder: number
}

export interface RubricDto {
  id: number
  name: string
  description: string | null
  sectionId: number | null
  criteria: CriterionDto[]
}

export async function getRubrics(): Promise<RubricDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: RubricDto[] }>('/rubrics')
  return response.data.data
}

export async function getRubric(id: number): Promise<RubricDto> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: RubricDto }>(`/rubrics/${id}`)
  return response.data.data
}

export async function createRubric(data: {
  name: string
  description?: string
  sectionId?: number
  criteria: {
    name: string
    description?: string
    maxScore: number
    displayOrder?: number
  }[]
}): Promise<RubricDto> {
  const response = await request.post<{ flag: boolean; code: number; message: string; data: RubricDto }>('/rubrics', data)
  return response.data.data
}

export async function addCriterion(
  rubricId: number,
  data: {
    name: string
    description?: string
    maxScore: number
    displayOrder?: number
  }
): Promise<CriterionDto> {
  const response = await request.post<{ flag: boolean; code: number; message: string; data: CriterionDto }>(`/rubrics/${rubricId}/criteria`, data)
  return response.data.data
}

export async function updateCriterion(
  criterionId: number,
  data: {
    name: string
    description?: string
    maxScore: number
    displayOrder?: number
  }
): Promise<CriterionDto> {
  const response = await request.put<{ flag: boolean; code: number; message: string; data: CriterionDto }>(`/rubrics/criteria/${criterionId}`, data)
  return response.data.data
}

export async function deleteRubric(id: number): Promise<void> {
  await request.delete(`/rubrics/${id}`)
}

export async function deleteCriterion(criterionId: number): Promise<void> {
  await request.delete(`/rubrics/criteria/${criterionId}`)
}
