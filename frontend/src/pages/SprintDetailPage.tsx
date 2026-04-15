import { useState, type FormEvent } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getSprint } from '../api/sprints'
import { getTickets, createTicket, deleteTicket, updateTicket, type TicketFilters, type TicketPayload } from '../api/tickets'
import { getMeetings, createMeetingWithMembers, deleteMeeting, type MeetingWithMembersPayload } from '../api/meetings'
import { getProjectMembers } from '../api/members'
import { getApiError } from '../api/utils'
import { Layout } from '../components/Layout'
import { Modal } from '../components/Modal'
import { Badge, ticketPriorityVariant, memberRoleVariant } from '../components/Badge'
import type { TicketStatus, TicketPriority, SprintStatus, MeetingType } from '../types'

const TICKET_STATUSES: TicketStatus[] = ['BACKLOG', 'TODO', 'IN_PROGRESS', 'ON_HOLD', 'ON_REVIEW', 'DONE']
const TICKET_PRIORITIES: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH']
const SPRINT_STATUSES: SprintStatus[] = ['PLANNED', 'ACTIVE', 'COMPLETED']
const MEETING_TYPES: MeetingType[] = ['PLANNING', 'DAILY', 'REVIEW', 'RETROSPECTIVE', 'SCRUM_POKER', 'REGULAR']

const MEMBER_COLORS = ['#e8450a', '#2563eb', '#16a34a', '#7c3aed', '#b45309', '#0891b2', '#be185d', '#059669']
function memberColor(name: string) {
  let h = 0
  for (let i = 0; i < name.length; i++) h = (h * 31 + name.charCodeAt(i)) & 0xffffffff
  return MEMBER_COLORS[Math.abs(h) % MEMBER_COLORS.length]
}

function CreateTicketModal({ sprintId, projectId, onClose }: { sprintId: string; projectId: string; onClose: () => void }) {
  const queryClient = useQueryClient()
  const [form, setForm] = useState<TicketPayload>({
    title: '', description: '', priority: 'MEDIUM', status: 'BACKLOG', estimation: null, sprintId, projectId,
  })
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: createTicket,
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['tickets'] }); onClose() },
    onError: (err) => setError(getApiError(err, 'Failed to create ticket')),
  })

  return (
    <Modal title="New Ticket" onClose={onClose}>
      <form onSubmit={(e) => { e.preventDefault(); mutation.mutate(form) }} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {error && <div className="err-box">{error}</div>}
        <div>
          <label className="lbl">Title *</label>
          <input required value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} className="field" placeholder="e.g. Implement user auth" />
        </div>
        <div>
          <label className="lbl">Description</label>
          <textarea value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} rows={3} className="field" placeholder="Optional details…" />
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <div>
            <label className="lbl">Priority</label>
            <select value={form.priority} onChange={(e) => setForm((f) => ({ ...f, priority: e.target.value as TicketPriority }))} className="field">
              {TICKET_PRIORITIES.map((p) => <option key={p}>{p}</option>)}
            </select>
          </div>
          <div>
            <label className="lbl">Status</label>
            <select value={form.status} onChange={(e) => setForm((f) => ({ ...f, status: e.target.value as TicketStatus }))} className="field">
              {TICKET_STATUSES.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
        </div>
        <div>
          <label className="lbl">Estimation</label>
          <input type="number" min={0}
            value={form.estimation ?? ''}
            onChange={(e) => setForm((f) => ({ ...f, estimation: e.target.value ? Number(e.target.value) : null }))}
            className="field" placeholder="pts / hours" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', paddingTop: '6px' }}>
          <button type="button" onClick={onClose} className="btn btn-outline">Cancel</button>
          <button type="submit" disabled={mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'Creating…' : 'Create ticket'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

function CreateMeetingModal({ sprintId, projectId, onClose }: { sprintId: string; projectId: string; onClose: () => void }) {
  const queryClient = useQueryClient()
  const [form, setForm] = useState({ title: '', description: '', type: 'PLANNING' as MeetingType, startsAt: '', endsAt: '' })
  const [selectedMemberIds, setSelectedMemberIds] = useState<string[]>([])
  const [error, setError] = useState('')

  const { data: members } = useQuery({
    queryKey: ['members', projectId], queryFn: () => getProjectMembers(projectId),
  })

  const mutation = useMutation({
    mutationFn: (data: MeetingWithMembersPayload) => createMeetingWithMembers(data),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['meetings', projectId] }); onClose() },
    onError: (err) => setError(getApiError(err, 'Failed to create meeting')),
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    setError('')
    mutation.mutate({
      ...form, sprintId, projectId,
      startsAt: new Date(form.startsAt).toISOString(),
      endsAt: new Date(form.endsAt).toISOString(),
      memberIds: selectedMemberIds,
    })
  }

  const toggleMember = (memberId: string) =>
    setSelectedMemberIds((prev) => prev.includes(memberId) ? prev.filter((id) => id !== memberId) : [...prev, memberId])

  return (
    <Modal title="New Meeting" onClose={onClose}>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {error && <div className="err-box">{error}</div>}
        <div>
          <label className="lbl">Title *</label>
          <input required value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} className="field" placeholder="e.g. Sprint Planning" />
        </div>
        <div>
          <label className="lbl">Type</label>
          <select value={form.type} onChange={(e) => setForm((f) => ({ ...f, type: e.target.value as MeetingType }))} className="field">
            {MEETING_TYPES.map((t) => <option key={t}>{t}</option>)}
          </select>
        </div>
        <div>
          <label className="lbl">Description</label>
          <textarea value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} rows={2} className="field" placeholder="Optional agenda…" />
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <div>
            <label className="lbl">Starts At *</label>
            <input required type="datetime-local" value={form.startsAt} onChange={(e) => setForm((f) => ({ ...f, startsAt: e.target.value }))} className="field" />
          </div>
          <div>
            <label className="lbl">Ends At *</label>
            <input required type="datetime-local" value={form.endsAt} onChange={(e) => setForm((f) => ({ ...f, endsAt: e.target.value }))} className="field" />
          </div>
        </div>
        {members && members.length > 0 && (
          <div>
            <label className="lbl">
              Attendees
              <span style={{ color: 'var(--tx3)', marginLeft: '6px', textTransform: 'none', letterSpacing: 0, fontWeight: 400 }}>
                ({selectedMemberIds.length} selected)
              </span>
            </label>
            <div style={{ border: '1px solid var(--bd)', borderRadius: '8px', maxHeight: '160px', overflowY: 'auto' }}>
              {members.map((member, i) => (
                <label
                  key={member.id}
                  style={{
                    display: 'flex', alignItems: 'center', gap: '10px',
                    padding: '9px 12px',
                    cursor: 'pointer',
                    borderBottom: i < members.length - 1 ? '1px solid var(--bd)' : 'none',
                    background: selectedMemberIds.includes(member.id) ? 'var(--ac-l)' : 'transparent',
                    transition: 'background 0.1s',
                  }}
                >
                  <input
                    type="checkbox"
                    checked={selectedMemberIds.includes(member.id)}
                    onChange={() => toggleMember(member.id)}
                    style={{ accentColor: 'var(--ac)' }}
                  />
                  <div style={{
                    width: '24px', height: '24px', borderRadius: '6px', flexShrink: 0,
                    background: memberColor(member.userName),
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    color: '#fff', fontSize: '10px', fontWeight: 700,
                  }}>
                    {member.userName.charAt(0).toUpperCase()}
                  </div>
                  <span style={{ color: 'var(--tx)', fontSize: '13px', flex: 1 }}>{member.userName}</span>
                  <Badge label={member.role} variant={memberRoleVariant(member.role)} />
                </label>
              ))}
            </div>
          </div>
        )}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', paddingTop: '6px' }}>
          <button type="button" onClick={onClose} className="btn btn-outline">Cancel</button>
          <button type="submit" disabled={mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'Creating…' : 'Create meeting'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

export function SprintDetailPage() {
  const { id: projectId, sprintId } = useParams<{ id: string; sprintId: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [showCreateTicket, setShowCreateTicket] = useState(false)
  const [showCreateMeeting, setShowCreateMeeting] = useState(false)
  const [filterStatus, setFilterStatus] = useState<TicketStatus | ''>('')
  const [filterPriority, setFilterPriority] = useState<TicketPriority | ''>('')
  const [filterSprintStatus, setFilterSprintStatus] = useState<SprintStatus | ''>('')
  const [page, setPage] = useState(0)

  const { data: sprint, isLoading } = useQuery({
    queryKey: ['sprint', sprintId], queryFn: () => getSprint(sprintId!), enabled: !!sprintId,
  })
  const ticketFilters: TicketFilters = {
    projectId: projectId!, status: filterStatus || undefined,
    priority: filterPriority || undefined, sprintStatus: filterSprintStatus || undefined,
    page, size: 20,
  }
  const { data: ticketsPage } = useQuery({
    queryKey: ['tickets', ticketFilters], queryFn: () => getTickets(ticketFilters), enabled: !!projectId,
  })
  const { data: meetings } = useQuery({
    queryKey: ['meetings', projectId], queryFn: () => getMeetings(projectId!), enabled: !!projectId,
  })

  const sprintMeetings = meetings?.filter((m) => m.sprintId === sprintId)

  const deleteTicketMutation = useMutation({
    mutationFn: deleteTicket,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tickets'] }),
  })
  const deleteMeetingMutation = useMutation({
    mutationFn: deleteMeeting,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['meetings', projectId] }),
  })
  const updateStatusMutation = useMutation({
    mutationFn: ({ ticketId, status }: { ticketId: string; status: TicketStatus }) =>
      updateTicket(ticketId, { status }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['tickets'] }),
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

  if (!sprint) {
    return (
      <Layout>
        <div style={{ padding: '32px' }}><div className="err-box">Sprint not found</div></div>
      </Layout>
    )
  }

  const hasFilters = filterStatus || filterPriority || filterSprintStatus

  const sprintStatusVariant = sprint.status === 'ACTIVE' ? 'green' : sprint.status === 'COMPLETED' ? 'gray' : 'blue'

  return (
    <Layout>
      {/* Header */}
      <div style={{ background: '#fff', borderBottom: '1px solid var(--bd)', padding: '18px 24px', flexShrink: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '10px', fontSize: '12px', color: 'var(--tx3)' }}>
          <button className="back" onClick={() => navigate(`/projects/${projectId}`)}>← Project</button>
        </div>
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '16px' }}>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap', marginBottom: '4px' }}>
              <h1 style={{ fontFamily: 'Syne, sans-serif', fontWeight: 800, fontSize: '20px', color: 'var(--tx)', letterSpacing: '-0.02em' }}>
                {sprint.name}
              </h1>
              <Badge label={sprint.status} variant={sprintStatusVariant} />
            </div>
            <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
              <span style={{ fontSize: '13px', color: 'var(--tx3)' }}>
                {sprint.startDate} → {sprint.endDate}
              </span>
              <span style={{ fontSize: '13px', color: 'var(--tx3)' }}>
                {sprint.estimationType.replace('_', ' ')}
              </span>
            </div>
            {sprint.businessGoal && (
              <p style={{ fontSize: '13px', color: 'var(--tx2)', marginTop: '6px', fontStyle: 'italic' }}>
                "{sprint.businessGoal}"
              </p>
            )}
          </div>
        </div>
      </div>

      {/* Content */}
      <div style={{ padding: '28px 24px', display: 'flex', flexDirection: 'column', gap: '36px' }}>

        {/* Tickets section */}
        <section>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span className="sh">Tickets</span>
              <span style={{
                background: 'var(--surf2)', border: '1px solid var(--bd)', borderRadius: '99px',
                padding: '1px 8px', fontSize: '12px', fontWeight: 600, color: 'var(--tx3)',
              }}>
                {ticketsPage?.totalElements ?? 0}
              </span>
            </div>
            <button onClick={() => setShowCreateTicket(true)} className="btn btn-primary" style={{ fontSize: '13px' }}>
              <svg width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
                <path d="M7 1v12M1 7h12"/>
              </svg>
              New ticket
            </button>
          </div>

          {/* Filters */}
          <div style={{ display: 'flex', gap: '8px', marginBottom: '14px', flexWrap: 'wrap' }}>
            <select value={filterStatus} onChange={(e) => { setFilterStatus(e.target.value as TicketStatus | ''); setPage(0) }} className="field-sm">
              <option value="">All statuses</option>
              {TICKET_STATUSES.map((s) => <option key={s}>{s}</option>)}
            </select>
            <select value={filterPriority} onChange={(e) => { setFilterPriority(e.target.value as TicketPriority | ''); setPage(0) }} className="field-sm">
              <option value="">All priorities</option>
              {TICKET_PRIORITIES.map((p) => <option key={p}>{p}</option>)}
            </select>
            <select value={filterSprintStatus} onChange={(e) => { setFilterSprintStatus(e.target.value as SprintStatus | ''); setPage(0) }} className="field-sm">
              <option value="">All sprint statuses</option>
              {SPRINT_STATUSES.map((s) => <option key={s}>{s}</option>)}
            </select>
            {hasFilters && (
              <button
                onClick={() => { setFilterStatus(''); setFilterPriority(''); setFilterSprintStatus(''); setPage(0) }}
                className="btn btn-outline"
                style={{ fontSize: '12px', padding: '4px 10px' }}
              >
                Clear filters
              </button>
            )}
          </div>

          {ticketsPage && ticketsPage.content.length === 0 ? (
            <div style={{ color: 'var(--tx3)', fontSize: '13px', padding: '20px 0', textAlign: 'center' }}>
              No tickets found.
            </div>
          ) : (
            <div className="card" style={{ overflow: 'hidden' }}>
              <table className="dt">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Status</th>
                    <th>Priority</th>
                    <th>Est.</th>
                    <th style={{ width: '40px' }}></th>
                  </tr>
                </thead>
                <tbody>
                  {ticketsPage?.content.map((ticket) => (
                    <tr key={ticket.id}>
                      <td>
                        <Link
                          to={`/tickets/${ticket.id}?projectId=${projectId}`}
                          style={{ color: 'var(--tx)', fontWeight: 500, fontSize: '13px', textDecoration: 'none' }}
                          onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--ac)' }}
                          onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--tx)' }}
                        >
                          {ticket.title}
                        </Link>
                        {ticket.description && (
                          <div style={{ color: 'var(--tx3)', fontSize: '12px', marginTop: '2px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '320px' }}>
                            {ticket.description}
                          </div>
                        )}
                      </td>
                      <td>
                        <select
                          value={ticket.status}
                          onChange={(e) => updateStatusMutation.mutate({ ticketId: ticket.id, status: e.target.value as TicketStatus })}
                          className="field-sm"
                        >
                          {TICKET_STATUSES.map((s) => <option key={s}>{s}</option>)}
                        </select>
                      </td>
                      <td>
                        <Badge label={ticket.priority} variant={ticketPriorityVariant(ticket.priority)} />
                      </td>
                      <td style={{ color: 'var(--tx3)', fontSize: '13px' }}>{ticket.estimation ?? '—'}</td>
                      <td>
                        <button onClick={() => deleteTicketMutation.mutate(ticket.id)} className="btn-ghost" style={{ padding: '4px' }} title="Delete">
                          <svg width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round">
                            <path d="M1.5 3.5h11M5 2h4M3 3.5l.7 8.5A1 1 0 004.7 13h4.6a1 1 0 001-.9L11 3.5"/>
                          </svg>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {ticketsPage && ticketsPage.totalPages > 1 && (
                <div style={{
                  display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                  padding: '12px 16px', borderTop: '1px solid var(--bd)',
                }}>
                  <span style={{ color: 'var(--tx3)', fontSize: '12px' }}>
                    Page {page + 1} of {ticketsPage.totalPages} · {ticketsPage.totalElements} tickets
                  </span>
                  <div style={{ display: 'flex', gap: '6px' }}>
                    <button disabled={page === 0} onClick={() => setPage((p) => p - 1)} className="btn btn-outline" style={{ padding: '4px 12px', fontSize: '12px' }}>
                      ←
                    </button>
                    <button disabled={page >= ticketsPage.totalPages - 1} onClick={() => setPage((p) => p + 1)} className="btn btn-outline" style={{ padding: '4px 12px', fontSize: '12px' }}>
                      →
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </section>

        {/* Meetings section */}
        <section>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span className="sh">Meetings</span>
              <span style={{
                background: 'var(--surf2)', border: '1px solid var(--bd)', borderRadius: '99px',
                padding: '1px 8px', fontSize: '12px', fontWeight: 600, color: 'var(--tx3)',
              }}>
                {sprintMeetings?.length ?? 0}
              </span>
            </div>
            <button onClick={() => setShowCreateMeeting(true)} className="btn btn-outline" style={{ fontSize: '13px' }}>
              <svg width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
                <path d="M7 1v12M1 7h12"/>
              </svg>
              New meeting
            </button>
          </div>

          {sprintMeetings && sprintMeetings.length === 0 ? (
            <div style={{ color: 'var(--tx3)', fontSize: '13px', padding: '20px 0', textAlign: 'center' }}>
              No meetings scheduled for this sprint.
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '12px' }}>
              {sprintMeetings?.map((meeting) => (
                <div key={meeting.id} className="card-flat" style={{ padding: '16px' }}>
                  <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '10px', marginBottom: '10px' }}>
                    <div style={{ minWidth: 0, flex: 1 }}>
                      <div style={{ fontWeight: 600, fontSize: '14px', color: 'var(--tx)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', marginBottom: '5px' }}>
                        {meeting.title}
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                        <Badge label={meeting.type} variant="blue" />
                        <span style={{ color: 'var(--tx3)', fontSize: '12px' }}>
                          {new Date(meeting.startsAt).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                        </span>
                      </div>
                    </div>
                    <button onClick={() => deleteMeetingMutation.mutate(meeting.id)} className="btn-ghost" style={{ padding: '4px', flexShrink: 0 }} title="Delete">
                      <svg width="12" height="12" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round">
                        <path d="M1.5 3.5h11M5 2h4M3 3.5l.7 8.5A1 1 0 004.7 13h4.6a1 1 0 001-.9L11 3.5"/>
                      </svg>
                    </button>
                  </div>
                  {meeting.members.length > 0 && (
                    <div style={{ paddingTop: '10px', borderTop: '1px solid var(--bd)' }}>
                      <div style={{ fontSize: '11px', color: 'var(--tx3)', marginBottom: '6px', fontWeight: 600, letterSpacing: '0.04em', textTransform: 'uppercase' }}>
                        Attendees ({meeting.members.length})
                      </div>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '5px' }}>
                        {meeting.members.map((m) => (
                          <span key={m.id} style={{
                            display: 'inline-flex', alignItems: 'center', gap: '5px',
                            background: 'var(--surf2)', border: '1px solid var(--bd)',
                            borderRadius: '99px', padding: '2px 8px 2px 4px', fontSize: '12px', color: 'var(--tx2)',
                          }}>
                            <span style={{
                              width: '18px', height: '18px', borderRadius: '50%', flexShrink: 0,
                              background: memberColor(m.userName),
                              display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                              color: '#fff', fontSize: '9px', fontWeight: 700,
                            }}>
                              {m.userName.charAt(0).toUpperCase()}
                            </span>
                            {m.userName}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </section>
      </div>

      {showCreateTicket && sprintId && projectId && (
        <CreateTicketModal sprintId={sprintId} projectId={projectId} onClose={() => setShowCreateTicket(false)} />
      )}
      {showCreateMeeting && sprintId && projectId && (
        <CreateMeetingModal sprintId={sprintId} projectId={projectId} onClose={() => setShowCreateMeeting(false)} />
      )}
    </Layout>
  )
}
