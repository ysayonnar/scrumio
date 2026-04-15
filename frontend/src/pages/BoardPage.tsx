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

const STATUS_COUNT_COLORS: Record<TicketStatus, string> = {
  BACKLOG: 'var(--tx3)',
  TODO: 'rgba(96,184,255,0.7)',
  IN_PROGRESS: 'rgba(255,200,64,0.7)',
  ON_HOLD: 'rgba(255,107,107,0.7)',
  ON_REVIEW: 'rgba(192,132,252,0.7)',
  DONE: 'rgba(78,240,160,0.7)',
}

function TicketCard({
  ticket, isDragging, onDragStart, onStatusChange,
}: {
  ticket: TicketResponse
  isDragging: boolean
  onDragStart: (id: string) => void
  onStatusChange: (id: string, status: TicketStatus) => void
}) {
  return (
    <div
      draggable
      onDragStart={(e) => { e.dataTransfer.effectAllowed = 'move'; onDragStart(ticket.id) }}
      className={`k-card pri-${ticket.priority}${isDragging ? ' dragging' : ''}`}
    >
      <Link to={`/tickets/${ticket.id}`} draggable={false} style={{ display: 'block', marginBottom: '8px' }}>
        <div style={{
          color: '#e4e4f4',
          fontSize: '12px',
          fontWeight: '500',
          lineHeight: '1.4',
          display: '-webkit-box',
          WebkitLineClamp: 2,
          WebkitBoxOrient: 'vertical',
          overflow: 'hidden',
        }}>
          {ticket.title}
        </div>
      </Link>

      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
        <Badge label={ticket.priority} variant={ticketPriorityVariant(ticket.priority)} />
        {ticket.estimation !== null && (
          <span style={{ color: 'var(--tx3)', fontSize: '10px', letterSpacing: '0.04em' }}>
            {ticket.estimation}pt
          </span>
        )}
      </div>

      {ticket.sprintName && (
        <div style={{ color: 'var(--tx3)', fontSize: '10px', marginTop: '5px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {ticket.sprintName}
        </div>
      )}

      <div style={{ marginTop: '8px', paddingTop: '8px', borderTop: '1px solid var(--bd)' }}>
        <select
          value={ticket.status}
          onClick={(e) => e.stopPropagation()}
          onChange={(e) => onStatusChange(ticket.id, e.target.value as TicketStatus)}
          className="field-sm"
          style={{ width: '100%' }}
        >
          {STATUSES.map((s) => <option key={s} value={s}>{STATUS_LABELS[s]}</option>)}
        </select>
      </div>
    </div>
  )
}

function KanbanColumn({
  status, tickets, draggingId, dragOverStatus,
  onDragEnter, onDragLeave, onDrop, onDragStart, onStatusChange,
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
      className={`col-${status}`}
      style={{
        flexShrink: 0,
        width: '240px',
        background: isOver ? 'rgba(255,255,255,0.02)' : 'var(--bg-2)',
        border: `1px solid ${isOver ? 'var(--bd-3)' : 'var(--bd)'}`,
        borderRadius: '2px',
        display: 'flex',
        flexDirection: 'column',
        transition: 'border-color 0.12s, background 0.12s',
      }}
      onDragOver={(e) => { e.preventDefault(); e.dataTransfer.dropEffect = 'move' }}
      onDragEnter={() => onDragEnter(status)}
      onDragLeave={onDragLeave}
      onDrop={(e) => { e.preventDefault(); onDrop(status) }}
    >
      <div style={{
        padding: '10px 12px',
        borderBottom: '1px solid var(--bd)',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        <span style={{ color: 'var(--tx2)', fontSize: '11px', letterSpacing: '0.06em', textTransform: 'uppercase' }}>
          {STATUS_LABELS[status]}
        </span>
        <span style={{
          color: STATUS_COUNT_COLORS[status],
          fontSize: '10px',
          fontWeight: '600',
          letterSpacing: '0.04em',
          background: 'var(--bg)',
          border: '1px solid var(--bd)',
          borderRadius: '2px',
          padding: '1px 6px',
        }}>
          {tickets.length}
        </span>
      </div>

      <div style={{
        padding: '8px',
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
        flex: 1,
        overflowY: 'auto',
        maxHeight: 'calc(100vh - 180px)',
      }}>
        {tickets.length === 0 && !isOver ? (
          <div style={{ color: 'var(--tx3)', fontSize: '11px', textAlign: 'center', padding: '20px 0', letterSpacing: '0.06em' }}>
            empty
          </div>
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
          <div style={{
            height: '56px',
            border: '1px dashed var(--bd-3)',
            borderRadius: '2px',
            opacity: 0.5,
          }} />
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
    queryKey: ['project', id], queryFn: () => getProject(id!), enabled: !!id,
  })
  const { data: ticketsPage, isLoading: ticketsLoading } = useQuery({
    queryKey: ['board-tickets', id], queryFn: () => getTickets({ projectId: id!, size: 200 }), enabled: !!id,
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
      return { ...old, content: old.content.map((t) => t.id === ticketId ? { ...t, status: targetStatus } : t) }
    })
    updateMutation.mutate({ ticketId, status: targetStatus })
  }

  const handleStatusChange = (ticketId: string, status: TicketStatus) => {
    queryClient.setQueryData<Page<TicketResponse>>(['board-tickets', id], (old) => {
      if (!old) return old
      return { ...old, content: old.content.map((t) => t.id === ticketId ? { ...t, status } : t) }
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
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh', color: 'var(--tx3)', fontSize: '12px', letterSpacing: '0.06em' }}>
          loading<span className="cursor-blink">_</span>
        </div>
      </Layout>
    )
  }

  if (!project) {
    return (
      <Layout>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh' }}>
          <div className="err-box">Project not found</div>
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
        <div style={{
          padding: '16px 24px',
          borderBottom: '1px solid var(--bd)',
          background: 'rgba(20,20,31,0.97)',
          flexShrink: 0,
        }}>
          <button className="back" onClick={() => navigate(`/projects/${id}`)} style={{ marginBottom: '8px' }}>
            ← {project.name}
          </button>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <div className="sh">board</div>
              <div style={{ color: 'var(--tx3)', fontSize: '11px', marginTop: '2px', letterSpacing: '0.04em' }}>
                {ticketsLoading ? 'loading tickets...' : `${tickets.length} tickets`}
              </div>
            </div>
            <Link to={`/projects/${id}`} className="btn btn-outline" style={{ fontSize: '11px' }}>
              <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round">
                <path d="M1 2h10M1 6h10M1 10h10" />
              </svg>
              List View
            </Link>
          </div>
        </div>

        <div
          style={{ flex: 1, overflowX: 'auto', padding: '20px 24px' }}
          onDragEnd={() => { setLocalDraggingId(null); setDragOverStatus(null) }}
        >
          <div style={{ display: 'flex', gap: '10px', height: '100%', minWidth: 'max-content' }}>
            {STATUSES.map((status) => (
              <KanbanColumn
                key={status}
                status={status}
                tickets={byStatus[status]}
                draggingId={localDraggingId}
                dragOverStatus={dragOverStatus}
                onDragStart={handleDragStart}
                onDragEnter={setDragOverStatus}
                onDragLeave={() => setDragOverStatus(null)}
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
