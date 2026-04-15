import client from './client'
import type { Page, TicketResponse, TicketStatus, TicketPriority, SprintStatus } from '../types'

export interface TicketFilters {
  projectId: string
  status?: TicketStatus
  priority?: TicketPriority
  sprintStatus?: SprintStatus
  sprintId?: string
  page?: number
  size?: number
}

export const getTickets = (filters: TicketFilters) =>
  client
    .get<Page<TicketResponse>>('/api/v1/tickets', {
      params: {
        project_id: filters.projectId,
        status: filters.status || undefined,
        priority: filters.priority || undefined,
        sprint_status: filters.sprintStatus || undefined,
        sprint_id: filters.sprintId || undefined,
        page: filters.page ?? 0,
        size: filters.size ?? 20,
      },
    })
    .then((r) => r.data)

export const getTicket = (id: string) =>
  client.get<TicketResponse>(`/api/v1/tickets/${id}`).then((r) => r.data)

export interface TicketPayload {
  title: string
  description: string
  priority: TicketPriority
  status: TicketStatus
  estimation?: number | null
  sprintId?: string | null
  projectId: string
}

export const createTicket = (data: TicketPayload) =>
  client.post<TicketResponse>('/api/v1/tickets', data).then((r) => r.data)

export const updateTicket = (id: string, data: Partial<TicketPayload>) =>
  client.patch<TicketResponse>(`/api/v1/tickets/${id}`, data).then((r) => r.data)

export const deleteTicket = (id: string) =>
  client.delete<TicketResponse>(`/api/v1/tickets/${id}`).then((r) => r.data)
