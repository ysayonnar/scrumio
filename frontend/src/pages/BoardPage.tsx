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
  BACKLOG: 'Backlog', TODO: 'To Do', IN_PROGRESS: 'In Progress',
  ON_HOLD: 'On Hold', ON_REVIEW: 'On Review', DONE: 'Done',
}

const COL_BG: Record<TicketStatus, string> = {
  BACKLOG:     '#f8f7f5',
  TODO:        '#f0f4ff',
  IN_PROGRESS: '#fffbeb',
  ON_HOLD:     '#fff4f4',
  ON_REVIEW:   '#f5f0ff',
  DONE:        '#f0fdf6',
}

const COL_COUNT_COLOR: Record<TicketStatus, string> = {
  BACKLOG:     '#94a3b8',
  TODO:        '#2563eb',
  IN_PROGRESS: '#b45309',
  ON_HOLD:     '#dc2626',
  ON_REVIEW:   '#7c3aed',
  DONE:        '#16a34a',
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
      <Link to={`/tickets/${ticket.id}`} draggable={false} style={{ display: 'block', marginBottom: '10px' }}>
        <p style={{
          color: 'var(--tx)', fontSize: '13px', fontWeight: 500, lineHeight: 1.45,
          display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden',
        }}>
          {ticket.title}
        </p>
      </Link>

      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
        <Badge label={ticket.priority} variant={ticketPriorityVariant(ticket.priority)} />
        {ticket.estimation !== null && (
          <span style={{
            fontSize: '11px', color: 'var(--tx3)',
            background: 'var(--surf2)', border: '1px solid var(--bd)',
            borderRadius: '99px', padding: '1px 7px',
          }}>
            {ticket.estimation} pts
          </span>
        )}
      </div>

      {ticket.sprintName && (
        <p style={{ fontSize: '11px', color: 'var(--tx3)', marginTop: '6px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {ticket.sprintName}
        </p>
      )}

      <div style={{ marginTop: '10px', paddingTop: '8px', borderTop: '1px solid var(--bd)' }}>
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
  onDragEnter: (s: TicketStatus) => void
  onDragLeave: () => void
  onDrop: (s: TicketStatus) => void
  onDragStart: (id: string) => void
  onStatusChange: (id: string, s: TicketStatus) => void
}) {
  const isOver = dragOverStatus === status

  return (
    <div
      className={`col-${status}`}
      style={{
        flexShrink: 0, width: '248px',
        background: isOver ? '#fff' : COL_BG[status],
        border: `1px solid ${isOver ? 'var(--ac)' : 'var(--bd)'}`,
        borderRadius: '12px',
        display: 'flex', flexDirection: 'column',
        boxShadow: isOver ? `0 0 0 3px var(--ac-l)` : 'none',
        transition: 'border-color 0.15s, box-shadow 0.15s',
      }}
      onDragOver={(e) => { e.preventDefault(); e.dataTransfer.dropEffect = 'move' }}
      onDragEnter={() => onDragEnter(status)}
      onDragLeave={onDragLeave}
      onDrop={(e) => { e.preventDefault(); onDrop(status) }}
    >
      <div style={{
        padding: '12px 14px 10px',
        borderBottom: '1px solid var(--bd)',
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        <span style={{ fontFamily: 'Syne, sans-serif', fontWeight: 700, fontSize: '12px', color: 'var(--tx2)', letterSpacing: '0.04em', textTransform: 'uppercase' }}>
          {STATUS_LABELS[status]}
        </span>
        <span style={{
          background: '#fff',
          border: `1px solid var(--bd)`,
          borderRadius: '99px',
          padding: '2px 9px',
          fontSize: '12px',
          fontWeight: 600,
          color: COL_COUNT_COLOR[status],
        }}>
          {tickets.length}
        </span>
      </div>

      <div style={{
        padding: '10px',
        display: 'flex', flexDirection: 'column', gap: '8px',
        flex: 1, overflowY: 'auto',
        maxHeight: 'calc(100vh - 190px)',
      }}>
        {tickets.length === 0 && !isOver ? (
          <p style={{ color: 'var(--tx3)', fontSize: '12px', textAlign: 'center', padding: '20px 0' }}>
            Drop tickets here
          </p>
        ) : (
          tickets.map((t) => (
            <TicketCard
              key={t.id} ticket={t}
              isDragging={draggingId === t.id}
              onDragStart={onDragStart}
              onStatusChange={onStatusChange}
            />
          ))
        )}
        {isOver && (
          <div style={{
            height: '60px',
            border: '2px dashed var(--ac)',
            borderRadius: '8px',
            background: 'var(--ac-l)',
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
    mutationFn: ({ ticketId, status }: { ticketId: string; status: TicketStatus }) => updateTicket(ticketId, { status }),
    onError: () => queryClient.invalidateQueries({ queryKey: ['board-tickets', id] }),
  })

  const handleDrop = (targetStatus: TicketStatus) => {
    setDragOverStatus(null); setLocalDraggingId(null)
    const ticketId = draggingId.current
    if (!ticketId) return
    draggingId.current = null
    const ticket = (ticketsPage?.content ?? []).find((t) => t.id === ticketId)
    if (!ticket || ticket.status === targetStatus) return
    queryClient.setQueryData<Page<TicketResponse>>(['board-tickets', id], (old) =>
      old ? { ...old, content: old.content.map((t) => t.id === ticketId ? { ...t, status: targetStatus } : t) } : old
    )
    updateMutation.mutate({ ticketId, status: targetStatus })
  }

  const handleStatusChange = (ticketId: string, status: TicketStatus) => {
    queryClient.setQueryData<Page<TicketResponse>>(['board-tickets', id], (old) =>
      old ? { ...old, content: old.content.map((t) => t.id === ticketId ? { ...t, status } : t) } : old
    )
    updateMutation.mutate({ ticketId, status })
  }

  const tickets = ticketsPage?.content ?? []
  const byStatus = Object.fromEntries(STATUSES.map((s) => [s, tickets.filter((t) => t.status === s)])) as Record<TicketStatus, TicketResponse[]>

  if (projectLoading) return (
    <Layout>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh', color: 'var(--tx3)' }}>
        Loading…
      </div>
    </Layout>
  )

  if (!project) return (
    <Layout>
      <div style={{ padding: '32px' }}><div className="err-box">Project not found</div></div>
    </Layout>
  )

  return (
    <Layout>
      <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
        {/* Header */}
        <div style={{ background: '#fff', borderBottom: '1px solid var(--bd)', padding: '18px 24px', flexShrink: 0 }}>
          <button className="back" onClick={() => navigate(`/projects/${id}`)} style={{ marginBottom: '8px' }}>
            ← {project.name}
          </button>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <div>
              <h1 style={{ fontFamily: 'Syne, sans-serif', fontWeight: 800, fontSize: '20px', color: 'var(--tx)', letterSpacing: '-0.02em' }}>
                Board
              </h1>
              <p style={{ fontSize: '13px', color: 'var(--tx3)', marginTop: '1px' }}>
                {ticketsLoading ? 'Loading…' : `${tickets.length} tickets`}
              </p>
            </div>
            <Link to={`/projects/${id}`} className="btn btn-outline" style={{ fontSize: '13px' }}>
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round">
                <path d="M1 2.5h12M1 7h12M1 11.5h12"/>
              </svg>
              List view
            </Link>
          </div>
        </div>

        {/* Board */}
        <div
          style={{ flex: 1, overflowX: 'auto', padding: '20px 24px' }}
          onDragEnd={() => { setLocalDraggingId(null); setDragOverStatus(null) }}
        >
          <div style={{ display: 'flex', gap: '12px', height: '100%', minWidth: 'max-content' }}>
            {STATUSES.map((status) => (
              <KanbanColumn
                key={status} status={status} tickets={byStatus[status]}
                draggingId={localDraggingId} dragOverStatus={dragOverStatus}
                onDragStart={(id) => { draggingId.current = id; setLocalDraggingId(id) }}
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
