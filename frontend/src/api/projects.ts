import client from './client'
import type { ProjectResponse } from '../types'

export const getProjects = () =>
  client.get<ProjectResponse[]>('/api/v1/projects').then((r) => r.data)

export const getProject = (id: string) =>
  client.get<ProjectResponse>(`/api/v1/projects/${id}`).then((r) => r.data)

export const createProject = (data: { name: string; description: string }) =>
  client.post<ProjectResponse>('/api/v1/projects', data).then((r) => r.data)

export const updateProject = (id: string, data: { name?: string; description?: string }) =>
  client.patch<ProjectResponse>(`/api/v1/projects/${id}`, data).then((r) => r.data)

export const deleteProject = (id: string) =>
  client.delete<ProjectResponse>(`/api/v1/projects/${id}`).then((r) => r.data)
