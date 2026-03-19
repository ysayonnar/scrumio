export type UserRole = 'ADMIN' | 'MEMBER'
export type ProjectMemberRole = 'OWNER' | 'STAKEHOLDER' | 'MANAGER' | 'DEVELOPER'
export type TicketStatus = 'BACKLOG' | 'TODO' | 'IN_PROGRESS' | 'ON_HOLD' | 'ON_REVIEW' | 'DONE'
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH'
export type SprintStatus = 'PLANNED' | 'ACTIVE' | 'COMPLETED'
export type SprintEstimationType = 'STORY_POINTS' | 'HOURS'
export type MeetingType = 'PLANNING' | 'DAILY' | 'REVIEW' | 'RETROSPECTIVE' | 'SCRUM_POKER' | 'REGULAR'

export interface UserResponse {
  id: string
  name: string
  email: string
  role: UserRole
  createdAt: string
}

export interface ProjectResponse {
  id: string
  name: string
  description: string
  ownerId: string
  createdAt: string
  updatedAt: string
  deletedAt: string | null
}

export interface SprintResponse {
  id: string
  name: string
  businessGoal: string
  devPlan: string
  startDate: string
  endDate: string
  status: SprintStatus
  estimationType: SprintEstimationType
  projectId: string
  createdAt: string
}

export interface TicketResponse {
  id: string
  title: string
  description: string
  priority: TicketPriority
  status: TicketStatus
  estimation: number | null
  sprintId: string | null
  sprintName: string | null
  projectId: string
  createdAt: string
}

export interface MeetingMemberResponse {
  id: string
  memberId: string
  userId: string
  userName: string
  role: ProjectMemberRole
  meetingId: string
  createdAt: string
}

export interface MeetingResponse {
  id: string
  title: string
  description: string
  type: MeetingType
  startsAt: string
  endsAt: string
  sprintId: string | null
  projectId: string
  createdAt: string
  members: MeetingMemberResponse[]
}

export interface ProjectMemberResponse {
  id: string
  userId: string
  userName: string
  projectId: string
  role: ProjectMemberRole
  createdAt: string
}

export interface MemberTicketResponse {
  id: string
  memberId: string
  userId: string
  userName: string
  role: ProjectMemberRole
  ticketId: string
  createdAt: string
}

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface ErrorResponse {
  code: string
  message: string
}
