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

const MEMBER_COLORS = ['#e8450a', '#2563eb', '#16a34a', '#7c3aed', '#b45309', '#0891b2', '#be185d', '#059669']
function memberColor(name: string) {
  let h = 0
  for (let i = 0; i < name.length; i++) h = (h * 31 + name.charCodeAt(i)) & 0xffffffff
  return MEMBER_COLORS[Math.abs(h) % MEMBER_COLORS.length]
}

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
        <div style={{ color: 'var(--tx2)', fontSize: '13px', padding: '12px 0', textAlign: 'center' }}>
          All project members are already assigned.
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          {unassigned.map((member) => (
            <div key={member.id} style={{
              display: 'flex', alignItems: 'center', justifyContent: 'space-between',
              padding: '10px 12px',
              background: 'var(--surf2)',
              border: '1px solid var(--bd)',
              borderRadius: '10px',
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div style={{
                  width: '32px', height: '32px', borderRadius: '8px', flexShrink: 0,
                  background: memberColor(member.userName),
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  color: '#fff', fontSize: '13px', fontWeight: 700,
                }}>
                  {member.userName.charAt(0).toUpperCase()}
                </div>
                <div>
                  <div style={{ color: 'var(--tx)', fontSize: '13px', fontWeight: 500 }}>{member.userName}</div>
                  <Badge label={member.role} variant={memberRoleVariant(member.role)} />
                </div>
              </div>
              <button
                onClick={() => assignMutation.mutate(member.id)}
                disabled={assignMutation.isPending}
                className="btn btn-primary"
                style={{ padding: '5px 14px', fontSize: '12px' }}
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
            {mutation.isPending ? 'Saving…' : 'Save changes'}
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
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh', color: 'var(--tx3)' }}>
          Loading…
        </div>
      </Layout>
    )
  }

  if (!ticket) {
    return (
      <Layout>
        <div style={{ padding: '32px' }}><div className="err-box">Ticket not found</div></div>
      </Layout>
    )
  }

  return (
    <Layout>
      {/* Header */}
      <div style={{ background: '#fff', borderBottom: '1px solid var(--bd)', padding: '18px 24px', flexShrink: 0 }}>
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
                  color: 'var(--tx2)', fontSize: '12px',
                  background: 'var(--surf2)', border: '1px solid var(--bd)',
                  borderRadius: '99px', padding: '1px 8px',
                }}>
                  {ticket.estimation} pts
                </span>
              )}
            </div>
            <h1 style={{ fontFamily: 'Syne, sans-serif', fontWeight: 800, fontSize: '20px', color: 'var(--tx)', letterSpacing: '-0.02em' }}>
              {ticket.title}
            </h1>
          </div>
          <button onClick={() => setShowEdit(true)} className="btn btn-outline" style={{ flexShrink: 0, fontSize: '13px' }}>
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M8.5 1.5l2 2L4 10 1.5 10.5 2 8 8.5 1.5z" />
            </svg>
            Edit
          </button>
        </div>
      </div>

      {/* Content */}
      <div style={{ padding: '28px 24px', maxWidth: '800px', display: 'flex', flexDirection: 'column', gap: '24px' }}>

        {/* Details card */}
        <div className="card-flat" style={{ padding: '20px' }}>
          {ticket.description ? (
            <p style={{ color: 'var(--tx)', fontSize: '14px', lineHeight: 1.7, marginBottom: '18px', paddingBottom: '18px', borderBottom: '1px solid var(--bd)' }}>
              {ticket.description}
            </p>
          ) : (
            <p style={{ color: 'var(--tx3)', fontSize: '13px', fontStyle: 'italic', marginBottom: '18px', paddingBottom: '18px', borderBottom: '1px solid var(--bd)' }}>
              No description provided.
            </p>
          )}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))', gap: '16px' }}>
            <div>
              <div style={{ color: 'var(--tx3)', fontSize: '11px', fontWeight: 600, letterSpacing: '0.06em', textTransform: 'uppercase', marginBottom: '4px' }}>Sprint</div>
              <div style={{ color: 'var(--tx)', fontSize: '13px' }}>{ticket.sprintName ?? '—'}</div>
            </div>
            <div>
              <div style={{ color: 'var(--tx3)', fontSize: '11px', fontWeight: 600, letterSpacing: '0.06em', textTransform: 'uppercase', marginBottom: '4px' }}>Created</div>
              <div style={{ color: 'var(--tx)', fontSize: '13px' }}>
                {new Date(ticket.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })}
              </div>
            </div>
            {ticket.estimation != null && (
              <div>
                <div style={{ color: 'var(--tx3)', fontSize: '11px', fontWeight: 600, letterSpacing: '0.06em', textTransform: 'uppercase', marginBottom: '4px' }}>Estimation</div>
                <div style={{ color: 'var(--tx)', fontSize: '13px' }}>{ticket.estimation} pts</div>
              </div>
            )}
          </div>
        </div>

        {/* Assignees section */}
        <section>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span className="sh">Assignees</span>
              <span style={{
                background: 'var(--surf2)', border: '1px solid var(--bd)', borderRadius: '99px',
                padding: '1px 8px', fontSize: '12px', fontWeight: 600, color: 'var(--tx3)',
              }}>
                {assignments?.length ?? 0}
              </span>
            </div>
            {projectId && (
              <button onClick={() => setShowAssign(true)} className="btn btn-outline" style={{ fontSize: '13px' }}>
                <svg width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
                  <path d="M7 1v12M1 7h12"/>
                </svg>
                Assign
              </button>
            )}
          </div>

          {assignments && assignments.length === 0 ? (
            <div style={{ color: 'var(--tx3)', fontSize: '13px', padding: '16px 0', textAlign: 'center' }}>
              No assignees yet.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {assignments?.map((assignment) => (
                <div key={assignment.id} className="card-flat" style={{ padding: '12px 16px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                      width: '36px', height: '36px', borderRadius: '10px', flexShrink: 0,
                      background: memberColor(assignment.userName),
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      color: '#fff', fontSize: '14px', fontWeight: 700,
                      boxShadow: `0 2px 8px ${memberColor(assignment.userName)}40`,
                    }}>
                      {assignment.userName.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <div style={{ color: 'var(--tx)', fontSize: '14px', fontWeight: 600 }}>{assignment.userName}</div>
                      <div style={{ marginTop: '2px' }}>
                        <Badge label={assignment.role} variant={memberRoleVariant(assignment.role)} />
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={() => unassignMutation.mutate(assignment.id)}
                    className="btn-ghost"
                    style={{ padding: '6px' }}
                    title="Unassign"
                  >
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round">
                      <path d="M1 1l10 10M11 1L1 11" />
                    </svg>
                  </button>
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
