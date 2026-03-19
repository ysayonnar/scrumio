import { useRef, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getTickets, updateTicket } from '../api/tickets'
import { getProject } from '../api/projects'
import { Layout } from '../components/Layout'
import { Badge, ticketPriorityVariant } from '../components/Badge'
import type { TicketStatus, TicketResponse, Page } from '../types'

const STATUSES: TicketStatus[] = ['BACKLOG', 'TODO', 'IN_PROGRESS', 'ON_HOLD', 'ON_REVIEW', 'DONE']

const STATUS_LABELS: Record<TicketStatus, string> = {
  BACKLOG: 'Backlog',
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress',
  ON_HOLD: 'On Hold',
  ON_REVIEW: 'On Review',
  DONE: 'Done',
}

const COLUMN_BORDER: Record<TicketStatus, string> = {
  BACKLOG: 'border-t-gray-400',
  TODO: 'border-t-blue-400',
  IN_PROGRESS: 'border-t-yellow-400',
  ON_HOLD: 'border-t-red-400',
  ON_REVIEW: 'border-t-purple-400',
  DONE: 'border-t-green-400',
}

const COLUMN_DROP_BG: Record<TicketStatus, string> = {
  BACKLOG: 'bg-gray-100 ring-2 ring-gray-300',
  TODO: 'bg-blue-50 ring-2 ring-blue-300',
  IN_PROGRESS: 'bg-yellow-50 ring-2 ring-yellow-300',
  ON_HOLD: 'bg-red-50 ring-2 ring-red-300',
  ON_REVIEW: 'bg-purple-50 ring-2 ring-purple-300',
  DONE: 'bg-green-50 ring-2 ring-green-300',
}

const PRIORITY_DOT: Record<string, string> = {
  LOW: 'bg-green-400',
  MEDIUM: 'bg-yellow-400',
  HIGH: 'bg-red-500',
}

function TicketCard({
  ticket,
  isDragging,
  onDragStart,
  onStatusChange,
}: {
  ticket: TicketResponse
  isDragging: boolean
  onDragStart: (id: string) => void
  onStatusChange: (id: string, status: TicketStatus) => void
}) {
  return (
    <div
      draggable
      onDragStart={(e) => {
        e.dataTransfer.effectAllowed = 'move'
        onDragStart(ticket.id)
      }}
      className={`bg-white border border-gray-200 rounded-lg p-3 shadow-sm cursor-grab active:cursor-grabbing
        hover:shadow-md hover:border-indigo-300 transition-all group select-none
        ${isDragging ? 'opacity-40 scale-95' : ''}`}
    >
      <Link to={`/tickets/${ticket.id}`} className="block" draggable={false}>
        <p className="text-sm font-medium text-gray-900 group-hover:text-indigo-700 leading-snug line-clamp-2">
          {ticket.title}
        </p>
      </Link>

      <div className="mt-2 flex items-center gap-1.5">
        <span className={`w-2 h-2 rounded-full flex-shrink-0 ${PRIORITY_DOT[ticket.priority] ?? 'bg-gray-400'}`} />
        <Badge label={ticket.priority} variant={ticketPriorityVariant(ticket.priority)} />
      </div>

      {ticket.sprintName && (
        <p className="mt-1.5 text-xs text-gray-400 truncate">{ticket.sprintName}</p>
      )}

      {ticket.estimation !== null && (
        <p className="mt-1 text-xs text-gray-400">{ticket.estimation} pts</p>
      )}

      <div className="mt-2 pt-2 border-t border-gray-100">
        <select
          value={ticket.status}
          onClick={(e) => e.stopPropagation()}
          onChange={(e) => onStatusChange(ticket.id, e.target.value as TicketStatus)}
          className="w-full text-xs border border-gray-200 rounded px-1.5 py-1 text-gray-600 focus:outline-none focus:ring-1 focus:ring-indigo-400 bg-gray-50 cursor-pointer"
        >
          {STATUSES.map((s) => (
            <option key={s} value={s}>{STATUS_LABELS[s]}</option>
          ))}
        </select>
      </div>
    </div>
  )
}

function KanbanColumn({
  status,
  tickets,
  draggingId,
  dragOverStatus,
  onDragEnter,
  onDragLeave,
  onDrop,
  onDragStart,
  onStatusChange,
}: {
  status: TicketStatus
  tickets: TicketResponse[]
  draggingId: string | null
  dragOverStatus: TicketStatus | null
  onDragEnter: (status: TicketStatus) => void
  onDragLeave: () => void
  onDrop: (status: TicketStatus) => void
  onDragStart: (id: string) => void
  onStatusChange: (id: string, status: TicketStatus) => void
}) {
  const isOver = dragOverStatus === status

  return (
    <div
      className={`flex-shrink-0 w-64 rounded-lg border border-gray-200 border-t-4 ${COLUMN_BORDER[status]} flex flex-col transition-colors
        ${isOver ? COLUMN_DROP_BG[status] : 'bg-gray-50'}`}
      onDragOver={(e) => { e.preventDefault(); e.dataTransfer.dropEffect = 'move' }}
      onDragEnter={() => onDragEnter(status)}
      onDragLeave={onDragLeave}
      onDrop={(e) => { e.preventDefault(); onDrop(status) }}
    >
      <div className="px-3 py-2.5 flex items-center justify-between border-b border-gray-200">
        <span className="text-sm font-semibold text-gray-700">{STATUS_LABELS[status]}</span>
        <span className="text-xs font-medium text-gray-400 bg-gray-200 rounded-full px-2 py-0.5">
          {tickets.length}
        </span>
      </div>
      <div className="p-2 space-y-2 flex-1 overflow-y-auto max-h-[calc(100vh-220px)]">
        {tickets.length === 0 && !isOver ? (
          <p className="text-xs text-gray-400 text-center py-6">No tickets</p>
        ) : (
          tickets.map((ticket) => (
            <TicketCard
              key={ticket.id}
              ticket={ticket}
              isDragging={draggingId === ticket.id}
              onDragStart={onDragStart}
              onStatusChange={onStatusChange}
            />
          ))
        )}
        {isOver && (
          <div className="h-16 border-2 border-dashed border-current opacity-30 rounded-lg" />
        )}
      </div>
    </div>
  )
}

export function BoardPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const draggingId = useRef<string | null>(null)
  const [dragOverStatus, setDragOverStatus] = useState<TicketStatus | null>(null)
  const [localDraggingId, setLocalDraggingId] = useState<string | null>(null)

  const { data: project, isLoading: projectLoading } = useQuery({
    queryKey: ['project', id],
    queryFn: () => getProject(id!),
    enabled: !!id,
  })

  const { data: ticketsPage, isLoading: ticketsLoading } = useQuery({
    queryKey: ['board-tickets', id],
    queryFn: () => getTickets({ projectId: id!, size: 200 }),
    enabled: !!id,
  })

  const updateMutation = useMutation({
    mutationFn: ({ ticketId, status }: { ticketId: string; status: TicketStatus }) =>
      updateTicket(ticketId, { status }),
    onError: () => queryClient.invalidateQueries({ queryKey: ['board-tickets', id] }),
  })

  const handleDragStart = (ticketId: string) => {
    draggingId.current = ticketId
    setLocalDraggingId(ticketId)
  }

  const handleDragEnter = (status: TicketStatus) => {
    setDragOverStatus(status)
  }

  const handleDragLeave = () => {
    setDragOverStatus(null)
  }

  const handleDrop = (targetStatus: TicketStatus) => {
    setDragOverStatus(null)
    setLocalDraggingId(null)
    const ticketId = draggingId.current
    if (!ticketId) return
    draggingId.current = null

    const currentTickets = ticketsPage?.content ?? []
    const ticket = currentTickets.find((t) => t.id === ticketId)
    if (!ticket || ticket.status === targetStatus) return

    queryClient.setQueryData<Page<TicketResponse>>(['board-tickets', id], (old) => {
      if (!old) return old
      return {
        ...old,
        content: old.content.map((t) =>
          t.id === ticketId ? { ...t, status: targetStatus } : t
        ),
      }
    })

    updateMutation.mutate({ ticketId, status: targetStatus })
  }

  const handleStatusChange = (ticketId: string, status: TicketStatus) => {
    queryClient.setQueryData<Page<TicketResponse>>(['board-tickets', id], (old) => {
      if (!old) return old
      return {
        ...old,
        content: old.content.map((t) =>
          t.id === ticketId ? { ...t, status } : t
        ),
      }
    })
    updateMutation.mutate({ ticketId, status })
  }

  const tickets = ticketsPage?.content ?? []
  const byStatus = Object.fromEntries(
    STATUSES.map((s) => [s, tickets.filter((t) => t.status === s)])
  ) as Record<TicketStatus, TicketResponse[]>

  if (projectLoading) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64 text-gray-400">Loading…</div>
      </Layout>
    )
  }

  if (!project) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64 text-red-500">Project not found</div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="flex flex-col h-full">
        <div className="px-6 py-4 border-b border-gray-200 bg-white flex-shrink-0">
          <div className="mb-1">
            <button
              onClick={() => navigate(`/projects/${id}`)}
              className="text-sm text-indigo-600 hover:text-indigo-700 flex items-center gap-1"
            >
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
              {project.name}
            </button>
          </div>
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-xl font-bold text-gray-900">Board</h1>
              {ticketsLoading ? (
                <p className="text-xs text-gray-400 mt-0.5">Loading tickets…</p>
              ) : (
                <p className="text-xs text-gray-400 mt-0.5">{tickets.length} tickets total</p>
              )}
            </div>
            <Link
              to={`/projects/${id}`}
              className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50"
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 10h16M4 14h16M4 18h16" />
              </svg>
              List View
            </Link>
          </div>
        </div>

        <div
          className="flex-1 overflow-x-auto p-6"
          onDragEnd={() => { setLocalDraggingId(null); setDragOverStatus(null) }}
        >
          <div className="flex gap-4 h-full min-w-max">
            {STATUSES.map((status) => (
              <KanbanColumn
                key={status}
                status={status}
                tickets={byStatus[status]}
                draggingId={localDraggingId}
                dragOverStatus={dragOverStatus}
                onDragStart={handleDragStart}
                onDragEnter={handleDragEnter}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                onStatusChange={handleStatusChange}
              />
            ))}
          </div>
        </div>
      </div>
    </Layout>
  )
}
