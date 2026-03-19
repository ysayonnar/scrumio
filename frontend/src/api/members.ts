import client from './client'
import type { ProjectMemberResponse, MemberTicketResponse, ProjectMemberRole } from '../types'

export const getProjectMembers = (projectId: string) =>
  client.get<ProjectMemberResponse[]>(`/api/v1/projects/${projectId}/members`).then((r) => r.data)

export const addProjectMember = (projectId: string, data: { userId: string; role: ProjectMemberRole }) =>
  client.post<ProjectMemberResponse>(`/api/v1/projects/${projectId}/members`, data).then((r) => r.data)

export const updateProjectMemberRole = (projectId: string, memberId: string, role: ProjectMemberRole) =>
  client.patch<ProjectMemberResponse>(`/api/v1/projects/${projectId}/members/${memberId}`, { role }).then((r) => r.data)

export const removeProjectMember = (projectId: string, memberId: string) =>
  client.delete<ProjectMemberResponse>(`/api/v1/projects/${projectId}/members/${memberId}`).then((r) => r.data)

export const getTicketMembers = (ticketId: string) =>
  client.get<MemberTicketResponse[]>(`/api/v1/tickets/${ticketId}/members`).then((r) => r.data)

export const assignTicketMember = (ticketId: string, memberId: string) =>
  client.post<MemberTicketResponse>(`/api/v1/tickets/${ticketId}/members`, { memberId }).then((r) => r.data)

export const unassignTicketMember = (ticketId: string, assignmentId: string) =>
  client.delete<MemberTicketResponse>(`/api/v1/tickets/${ticketId}/members/${assignmentId}`).then((r) => r.data)
