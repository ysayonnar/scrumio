import client from './client'
import type { SprintResponse, SprintStatus, SprintEstimationType } from '../types'

export const getSprints = (projectId: string) =>
  client.get<SprintResponse[]>('/api/v1/sprints', { params: { project_id: projectId } }).then((r) => r.data)

export const getSprint = (id: string) =>
  client.get<SprintResponse>(`/api/v1/sprints/${id}`).then((r) => r.data)

export interface SprintPayload {
  name: string
  businessGoal: string
  devPlan: string
  startDate: string
  endDate: string
  status: SprintStatus
  estimationType: SprintEstimationType
  projectId: string
}

export const createSprint = (data: SprintPayload) =>
  client.post<SprintResponse>('/api/v1/sprints', data).then((r) => r.data)

export const updateSprint = (id: string, data: Partial<SprintPayload>) =>
  client.patch<SprintResponse>(`/api/v1/sprints/${id}`, data).then((r) => r.data)

export const deleteSprint = (id: string) =>
  client.delete<SprintResponse>(`/api/v1/sprints/${id}`).then((r) => r.data)
