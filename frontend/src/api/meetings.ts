import client from './client'
import type { MeetingResponse, MeetingType } from '../types'

export interface MeetingPayload {
  title: string
  description: string
  type: MeetingType
  startsAt: string
  endsAt: string
  sprintId?: string | null
  projectId: string
}

export interface MeetingWithMembersPayload extends MeetingPayload {
  memberIds: string[]
}

export const getMeetings = (projectId: string) =>
  client.get<MeetingResponse[]>('/api/v1/meetings', { params: { project_id: projectId } }).then((r) => r.data)

export const getMeeting = (id: string) =>
  client.get<MeetingResponse>(`/api/v1/meetings/${id}`).then((r) => r.data)

export const createMeeting = (data: MeetingPayload) =>
  client.post<MeetingResponse>('/api/v1/meetings', data).then((r) => r.data)

export const createMeetingWithMembers = (data: MeetingWithMembersPayload) =>
  client.post<MeetingResponse>('/api/v1/meetings/with-members', data).then((r) => r.data)

export const updateMeeting = (id: string, data: Partial<MeetingPayload>) =>
  client.patch<MeetingResponse>(`/api/v1/meetings/${id}`, data).then((r) => r.data)

export const deleteMeeting = (id: string) =>
  client.delete<MeetingResponse>(`/api/v1/meetings/${id}`).then((r) => r.data)
