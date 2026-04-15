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
          <input required value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} className="field" />
        </div>
        <div>
          <label className="lbl">Description</label>
          <textarea value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} rows={3} className="field" />
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
            {mutation.isPending ? 'creating...' : '+ Create'}
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
          <input required value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} className="field" />
        </div>
        <div>
          <label className="lbl">Type</label>
          <select value={form.type} onChange={(e) => setForm((f) => ({ ...f, type: e.target.value as MeetingType }))} className="field">
            {MEETING_TYPES.map((t) => <option key={t}>{t}</option>)}
          </select>
        </div>
        <div>
          <label className="lbl">Description</label>
          <textarea value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} rows={2} className="field" />
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
              <span style={{ color: 'var(--tx3)', marginLeft: '6px', textTransform: 'none', letterSpacing: 0 }}>
                ({selectedMemberIds.length} selected)
              </span>
            </label>
            <div style={{ border: '1px solid var(--bd-2)', borderRadius: '2px', maxHeight: '160px', overflowY: 'auto' }}>
              {members.map((member, i) => (
                <label
                  key={member.id}
                  style={{
                    display: 'flex', alignItems: 'center', gap: '10px',
                    padding: '8px 12px',
                    cursor: 'pointer',
                    borderBottom: i < members.length - 1 ? '1px solid var(--bd)' : 'none',
                    transition: 'background 0.1s',
                  }}
                  onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg-3)' }}
                  onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent' }}
                >
                  <input
                    type="checkbox"
                    checked={selectedMemberIds.includes(member.id)}
                    onChange={() => toggleMember(member.id)}
                  />
                  <span style={{ color: 'var(--tx)', fontSize: '12px', flex: 1 }}>{member.userName}</span>
                  <Badge label={member.role} variant={memberRoleVariant(member.role)} />
                </label>
              ))}
            </div>
          </div>
        )}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', paddingTop: '6px' }}>
          <button type="button" onClick={onClose} className="btn btn-outline">Cancel</button>
          <button type="submit" disabled={mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'creating...' : '+ Create'}
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
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh', color: 'var(--tx3)', fontSize: '12px', letterSpacing: '0.06em' }}>
          loading<span className="cursor-blink">_</span>
        </div>
      </Layout>
    )
  }

  if (!sprint) {
    return (
      <Layout>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh' }}>
          <div className="err-box">Sprint not found</div>
        </div>
      </Layout>
    )
  }

  const hasFilters = filterStatus || filterPriority || filterSprintStatus

  return (
    <Layout>
      <div style={{ padding: '22px 32px', borderBottom: '1px solid var(--bd)', background: 'rgba(20,20,31,0.97)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '10px', fontSize: '11px', color: 'var(--tx3)' }}>
          <button className="back" onClick={() => navigate('/projects')}>Projects</button>
          <span>/</span>
          <button className="back" onClick={() => navigate(`/projects/${projectId}`)}>Project</button>
          <span>/</span>
          <span style={{ color: 'var(--tx2)' }}>{sprint.name}</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}>
          <h1 style={{ fontSize: '18px', fontWeight: '700', color: '#eaeaf8', letterSpacing: '-0.01em', flex: 1 }}>
            {sprint.name}
          </h1>
          <Badge
            label={sprint.status}
            variant={sprint.status === 'ACTIVE' ? 'green' : sprint.status === 'COMPLETED' ? 'gray' : 'blue'}
          />
        </div>
        <div style={{ display: 'flex', gap: '20px', marginTop: '6px', color: 'var(--tx3)', fontSize: '11px', letterSpacing: '0.03em' }}>
          <span>{sprint.startDate} → {sprint.endDate}</span>
          <span>{sprint.estimationType.replace('_', ' ')}</span>
        </div>
        {sprint.businessGoal && (
          <div style={{ marginTop: '6px', color: 'var(--tx2)', fontSize: '12px' }}>
            <span style={{ color: 'var(--tx3)' }}>goal: </span>{sprint.businessGoal}
          </div>
        )}
      </div>

      <div style={{ padding: '28px 32px', display: 'flex', flexDirection: 'column', gap: '40px' }}>

        <section>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <div className="sh">
              tickets
              <span style={{ color: 'var(--tx3)', fontSize: '10px' }}>[{ticketsPage?.totalElements ?? 0}]</span>
            </div>
            <button onClick={() => setShowCreateTicket(true)} className="btn btn-link" style={{ fontSize: '11px' }}>
              + new ticket
            </button>
          </div>

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
                className="btn-link"
                style={{ fontSize: '11px', padding: '3px 0' }}
              >
                clear ×
              </button>
            )}
          </div>

          {ticketsPage && ticketsPage.content.length === 0 ? (
            <div style={{ color: 'var(--tx3)', fontSize: '12px', padding: '16px 0' }}>No tickets found.</div>
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
                          style={{ color: '#e4e4f4', fontWeight: '500', fontSize: '12px' }}
                          onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--ac)' }}
                          onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.color = '#e4e4f4' }}
                        >
                          {ticket.title}
                        </Link>
                        {ticket.description && (
                          <div style={{ color: 'var(--tx3)', fontSize: '11px', marginTop: '2px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '320px' }}>
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
                      <td style={{ color: 'var(--tx3)', fontSize: '11px' }}>{ticket.estimation ?? '—'}</td>
                      <td>
                        <button onClick={() => deleteTicketMutation.mutate(ticket.id)} className="btn-ghost" style={{ padding: '4px' }}>
                          <svg width="11" height="11" viewBox="0 0 11 11" fill="none" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round">
                            <path d="M1 2.5h9M4 1h3M2.5 2.5l.5 7h5l.5-7" />
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
                  padding: '10px 16px',
                  borderTop: '1px solid var(--bd)',
                  background: 'var(--bg-3)',
                }}>
                  <span style={{ color: 'var(--tx3)', fontSize: '11px', letterSpacing: '0.03em' }}>
                    page {page + 1} / {ticketsPage.totalPages} · {ticketsPage.totalElements} total
                  </span>
                  <div style={{ display: 'flex', gap: '6px' }}>
                    <button disabled={page === 0} onClick={() => setPage((p) => p - 1)} className="btn btn-outline" style={{ padding: '4px 10px', fontSize: '11px' }}>
                      ←
                    </button>
                    <button disabled={page >= ticketsPage.totalPages - 1} onClick={() => setPage((p) => p + 1)} className="btn btn-outline" style={{ padding: '4px 10px', fontSize: '11px' }}>
                      →
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}
        </section>

        <section>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <div className="sh">
              meetings
              <span style={{ color: 'var(--tx3)', fontSize: '10px' }}>[{sprintMeetings?.length ?? 0}]</span>
            </div>
            <button onClick={() => setShowCreateMeeting(true)} className="btn btn-link" style={{ fontSize: '11px' }}>
              + new meeting
            </button>
          </div>

          {sprintMeetings && sprintMeetings.length === 0 ? (
            <div style={{ color: 'var(--tx3)', fontSize: '12px', padding: '16px 0' }}>No meetings for this sprint.</div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '10px' }}>
              {sprintMeetings?.map((meeting) => (
                <div key={meeting.id} className="card" style={{ padding: '14px 16px' }}>
                  <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '8px' }}>
                    <div style={{ minWidth: 0, flex: 1 }}>
                      <div style={{ color: '#e4e4f4', fontSize: '13px', fontWeight: '500', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {meeting.title}
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '5px', flexWrap: 'wrap' }}>
                        <Badge label={meeting.type} variant="blue" />
                        <span style={{ color: 'var(--tx3)', fontSize: '11px' }}>
                          {new Date(meeting.startsAt).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                        </span>
                      </div>
                    </div>
                    <button onClick={() => deleteMeetingMutation.mutate(meeting.id)} className="btn-ghost" style={{ padding: '3px', flexShrink: 0 }}>
                      <svg width="11" height="11" viewBox="0 0 11 11" fill="none" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round">
                        <path d="M1 2.5h9M4 1h3M2.5 2.5l.5 7h5l.5-7" />
                      </svg>
                    </button>
                  </div>
                  {meeting.members.length > 0 && (
                    <div style={{ marginTop: '10px', paddingTop: '8px', borderTop: '1px solid var(--bd)' }}>
                      <div style={{ color: 'var(--tx3)', fontSize: '10px', letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: '5px' }}>
                        attendees ({meeting.members.length})
                      </div>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                        {meeting.members.map((m) => (
                          <span key={m.id} style={{
                            display: 'inline-flex', alignItems: 'center', gap: '5px',
                            background: 'var(--bg)', border: '1px solid var(--bd)',
                            borderRadius: '2px', padding: '2px 7px', fontSize: '11px', color: 'var(--tx2)',
                          }}>
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
