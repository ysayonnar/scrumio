import { useState } from 'react'
import { useParams, useNavigate, useSearchParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getTicket, updateTicket } from '../api/tickets'
import { getTicketMembers, assignTicketMember, unassignTicketMember, getProjectMembers } from '../api/members'
import { Layout } from '../components/Layout'
import { Modal } from '../components/Modal'
import { Badge, ticketStatusVariant, ticketPriorityVariant, memberRoleVariant } from '../components/Badge'
import type { TicketStatus, TicketPriority } from '../types'

const TICKET_STATUSES: TicketStatus[] = ['BACKLOG', 'TODO', 'IN_PROGRESS', 'ON_HOLD', 'ON_REVIEW', 'DONE']
const TICKET_PRIORITIES: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH']

function AssignMemberModal({ ticketId, projectId, assignedMemberIds, onClose }: {
  ticketId: string; projectId: string; assignedMemberIds: string[]; onClose: () => void
}) {
  const queryClient = useQueryClient()
  const [error, setError] = useState('')

  const { data: projectMembers } = useQuery({
    queryKey: ['members', projectId], queryFn: () => getProjectMembers(projectId),
  })

  const assignMutation = useMutation({
    mutationFn: (memberId: string) => assignTicketMember(ticketId, memberId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['ticketMembers', ticketId] }),
    onError: () => setError('Failed to assign member'),
  })

  const unassigned = projectMembers?.filter((m) => !assignedMemberIds.includes(m.id)) ?? []

  return (
    <Modal title="Assign Member" onClose={onClose}>
      {error && <div className="err-box" style={{ marginBottom: '12px' }}>{error}</div>}
      {unassigned.length === 0 ? (
        <div style={{ color: 'var(--tx2)', fontSize: '12px', padding: '8px 0' }}>
          All project members are already assigned.
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          {unassigned.map((member) => (
            <div key={member.id} style={{
              display: 'flex', alignItems: 'center', justifyContent: 'space-between',
              padding: '10px 12px',
              background: 'var(--bg)',
              border: '1px solid var(--bd)',
              borderRadius: '2px',
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div style={{
                  width: '28px', height: '28px',
                  background: 'rgba(200,255,74,0.1)',
                  border: '1px solid rgba(200,255,74,0.2)',
                  borderRadius: '2px',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  color: 'var(--ac)', fontSize: '12px', fontWeight: '600',
                }}>
                  {member.userName.charAt(0).toUpperCase()}
                </div>
                <div>
                  <div style={{ color: '#e4e4f4', fontSize: '12px', fontWeight: '500' }}>{member.userName}</div>
                  <Badge label={member.role} variant={memberRoleVariant(member.role)} />
                </div>
              </div>
              <button
                onClick={() => assignMutation.mutate(member.id)}
                disabled={assignMutation.isPending}
                className="btn btn-primary"
                style={{ padding: '5px 12px', fontSize: '11px' }}
              >
                Assign
              </button>
            </div>
          ))}
        </div>
      )}
      <div style={{ marginTop: '16px', display: 'flex', justifyContent: 'flex-end' }}>
        <button onClick={onClose} className="btn btn-outline">Close</button>
      </div>
    </Modal>
  )
}

function EditTicketModal({ ticketId, onClose }: { ticketId: string; onClose: () => void }) {
  const queryClient = useQueryClient()
  const { data: ticket } = useQuery({ queryKey: ['ticket', ticketId], queryFn: () => getTicket(ticketId) })

  const [title, setTitle] = useState(ticket?.title ?? '')
  const [description, setDescription] = useState(ticket?.description ?? '')
  const [status, setStatus] = useState<TicketStatus>(ticket?.status ?? 'BACKLOG')
  const [priority, setPriority] = useState<TicketPriority>(ticket?.priority ?? 'MEDIUM')
  const [estimation, setEstimation] = useState<string>(ticket?.estimation?.toString() ?? '')

  const mutation = useMutation({
    mutationFn: () => updateTicket(ticketId, {
      title, description, status, priority,
      estimation: estimation ? Number(estimation) : null,
    }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] }); onClose() },
  })

  if (!ticket) return null

  return (
    <Modal title="Edit Ticket" onClose={onClose}>
      <form onSubmit={(e) => { e.preventDefault(); mutation.mutate() }} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        <div>
          <label className="lbl">Title</label>
          <input value={title} onChange={(e) => setTitle(e.target.value)} className="field" />
        </div>
        <div>
          <label className="lbl">Description</label>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} className="field" />
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <div>
            <label className="lbl">Status</label>
            <select value={status} onChange={(e) => setStatus(e.target.value as TicketStatus)} className="field">
              {TICKET_STATUSES.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div>
            <label className="lbl">Priority</label>
            <select value={priority} onChange={(e) => setPriority(e.target.value as TicketPriority)} className="field">
              {TICKET_PRIORITIES.map((p) => <option key={p}>{p}</option>)}
            </select>
          </div>
        </div>
        <div>
          <label className="lbl">Estimation</label>
          <input type="number" min={0} value={estimation} onChange={(e) => setEstimation(e.target.value)} className="field" placeholder="pts / hours" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', paddingTop: '6px' }}>
          <button type="button" onClick={onClose} className="btn btn-outline">Cancel</button>
          <button type="submit" disabled={mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'saving...' : 'Save'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

export function TicketDetailPage() {
  const { id: ticketId } = useParams<{ id: string }>()
  const [searchParams] = useSearchParams()
  const projectId = searchParams.get('projectId') ?? ''
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [showAssign, setShowAssign] = useState(false)
  const [showEdit, setShowEdit] = useState(false)

  const { data: ticket, isLoading } = useQuery({
    queryKey: ['ticket', ticketId], queryFn: () => getTicket(ticketId!), enabled: !!ticketId,
  })
  const { data: assignments } = useQuery({
    queryKey: ['ticketMembers', ticketId], queryFn: () => getTicketMembers(ticketId!), enabled: !!ticketId,
  })

  const unassignMutation = useMutation({
    mutationFn: (assignmentId: string) => unassignTicketMember(ticketId!, assignmentId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['ticketMembers', ticketId] }),
  })

  if (isLoading) {
    return (
      <Layout>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh', color: 'var(--tx3)', fontSize: '12px', letterSpacing: '0.06em' }}>
          loading<span className="cursor-blink">_</span>
        </div>
      </Layout>
    )
  }

  if (!ticket) {
    return (
      <Layout>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh' }}>
          <div className="err-box">Ticket not found</div>
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div style={{ padding: '24px 32px', borderBottom: '1px solid var(--bd)', background: 'rgba(20,20,31,0.97)' }}>
        <button className="back" onClick={() => navigate(-1)} style={{ marginBottom: '10px' }}>
          ← Back
        </button>
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '16px' }}>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap', marginBottom: '8px' }}>
              <Badge label={ticket.status} variant={ticketStatusVariant(ticket.status)} />
              <Badge label={ticket.priority} variant={ticketPriorityVariant(ticket.priority)} />
              {ticket.estimation != null && (
                <span style={{
                  color: 'var(--tx2)', fontSize: '11px',
                  background: 'var(--bg-2)', border: '1px solid var(--bd)',
                  borderRadius: '2px', padding: '2px 7px',
                }}>
                  {ticket.estimation} pts
                </span>
              )}
            </div>
            <h1 style={{ fontSize: '18px', fontWeight: '700', color: '#eaeaf8', letterSpacing: '-0.01em' }}>
              {ticket.title}
            </h1>
          </div>
          <button onClick={() => setShowEdit(true)} className="btn btn-outline" style={{ flexShrink: 0 }}>
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round" strokeLinejoin="round">
              <path d="M8.5 1.5l2 2L4 10 1.5 10.5 2 8 8.5 1.5z" />
            </svg>
            Edit
          </button>
        </div>
      </div>

      <div style={{ padding: '28px 32px', maxWidth: '800px', display: 'flex', flexDirection: 'column', gap: '28px' }}>

        <div className="card" style={{ padding: '18px 20px' }}>
          {ticket.description && (
            <div style={{ color: 'var(--tx)', fontSize: '13px', lineHeight: '1.7', marginBottom: '16px', paddingBottom: '16px', borderBottom: '1px solid var(--bd)' }}>
              {ticket.description}
            </div>
          )}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div>
              <div style={{ color: 'var(--tx3)', fontSize: '10px', letterSpacing: '0.1em', textTransform: 'uppercase', marginBottom: '4px' }}>Sprint</div>
              <div style={{ color: 'var(--tx)', fontSize: '12px' }}>{ticket.sprintName ?? '—'}</div>
            </div>
            <div>
              <div style={{ color: 'var(--tx3)', fontSize: '10px', letterSpacing: '0.1em', textTransform: 'uppercase', marginBottom: '4px' }}>Created</div>
              <div style={{ color: 'var(--tx)', fontSize: '12px' }}>
                {new Date(ticket.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })}
              </div>
            </div>
          </div>
        </div>

        <section>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <div className="sh">
              assignees
              <span style={{ color: 'var(--tx3)', fontSize: '10px' }}>[{assignments?.length ?? 0}]</span>
            </div>
            {projectId && (
              <button onClick={() => setShowAssign(true)} className="btn btn-link" style={{ fontSize: '11px' }}>
                + assign
              </button>
            )}
          </div>

          {assignments && assignments.length === 0 ? (
            <div style={{ color: 'var(--tx3)', fontSize: '12px', padding: '8px 0' }}>No assignees yet.</div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              {assignments?.map((assignment) => (
                <div key={assignment.id} className="card" style={{ padding: '12px 14px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div style={{
                      width: '30px', height: '30px',
                      background: 'rgba(200,255,74,0.08)',
                      border: '1px solid rgba(200,255,74,0.18)',
                      borderRadius: '2px',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      color: 'var(--ac)', fontSize: '12px', fontWeight: '600', flexShrink: 0,
                    }}>
                      {assignment.userName.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <div style={{ color: '#e4e4f4', fontSize: '13px', fontWeight: '500' }}>{assignment.userName}</div>
                      <div style={{ color: 'var(--tx3)', fontSize: '10px', marginTop: '1px' }}>{assignment.userId.slice(0, 12)}...</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <Badge label={assignment.role} variant={memberRoleVariant(assignment.role)} />
                    <button
                      onClick={() => unassignMutation.mutate(assignment.id)}
                      className="btn-ghost"
                      style={{ padding: '4px' }}
                      title="Unassign"
                    >
                      <svg width="10" height="10" viewBox="0 0 10 10" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round">
                        <path d="M1 1l8 8M9 1L1 9" />
                      </svg>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>

      {showAssign && ticketId && projectId && (
        <AssignMemberModal
          ticketId={ticketId}
          projectId={projectId}
          assignedMemberIds={assignments?.map((a) => a.memberId) ?? []}
          onClose={() => setShowAssign(false)}
        />
      )}
      {showEdit && ticketId && (
        <EditTicketModal ticketId={ticketId} onClose={() => setShowEdit(false)} />
      )}
    </Layout>
  )
}
