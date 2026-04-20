import request from './request'

export interface ScoreDto {
  criterionId: number
  criterionName: string
  score: number
  maxScore: number
}

export interface EvaluationDto {
  id: number
  evaluatorId: number
  evaluatorName: string
  evaluateeId: number
  evaluateeName: string
  teamId: number
  weekId: number
  weekNumber: number
  submittedAt: string
  scores: ScoreDto[]
  totalScore: number
}

export interface GradeDto {
  studentId: number
  studentName: string
  weekId: number
  weekNumber: number
  averageScore: number
  maxPossibleScore: number
  evaluationsReceived: EvaluationDto[]
}

export async function submitEvaluation(data: {
  evaluateeId: number
  teamId: number
  weekId: number
  scores: { criterionId: number; score: number }[]
}): Promise<EvaluationDto> {
  const response = await request.post<{ flag: boolean; code: number; message: string; data: EvaluationDto }>('/evaluations', data)
  return response.data.data
}

export async function getMyEvaluations(): Promise<EvaluationDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: EvaluationDto[] }>('/evaluations/my')
  return response.data.data
}

export async function getMyScores(): Promise<EvaluationDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: EvaluationDto[] }>('/evaluations/my-scores')
  return response.data.data
}

export async function getTeamWeekEvaluations(teamId: number, weekId: number): Promise<EvaluationDto[]> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: EvaluationDto[] }>(`/evaluations/team/${teamId}/week/${weekId}`)
  return response.data.data
}

export async function getGrade(teamId: number, weekId: number, studentId: number): Promise<GradeDto> {
  const response = await request.get<{ flag: boolean; code: number; message: string; data: GradeDto }>(`/evaluations/grade/team/${teamId}/week/${weekId}/student/${studentId}`)
  return response.data.data
}
