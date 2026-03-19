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
    title: '',
    description: '',
    priority: 'MEDIUM',
    status: 'BACKLOG',
    estimation: null,
    sprintId,
    projectId,
  })
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: createTicket,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tickets'] })
      onClose()
    },
    onError: (err) => setError(getApiError(err, 'Failed to create ticket')),
  })

  return (
    <Modal title="New Ticket" onClose={onClose}>
      <form onSubmit={(e) => { e.preventDefault(); mutation.mutate(form) }} className="space-y-4">
        {error && <div className="bg-red-50 border border-red-200 rounded-lg px-3 py-2 text-sm text-red-700">{error}</div>}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Title *</label>
          <input required value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none" />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Priority</label>
            <select value={form.priority} onChange={(e) => setForm((f) => ({ ...f, priority: e.target.value as TicketPriority }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
              {TICKET_PRIORITIES.map((p) => <option key={p}>{p}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select value={form.status} onChange={(e) => setForm((f) => ({ ...f, status: e.target.value as TicketStatus }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
              {TICKET_STATUSES.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Estimation</label>
          <input type="number" min={0}
            value={form.estimation ?? ''}
            onChange={(e) => setForm((f) => ({ ...f, estimation: e.target.value ? Number(e.target.value) : null }))}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            placeholder="Story points / hours" />
        </div>
        <div className="flex justify-end gap-3 pt-2">
          <button type="button" onClick={onClose} className="px-4 py-2 text-sm font-medium text-gray-700">Cancel</button>
          <button type="submit" disabled={mutation.isPending}
            className="px-4 py-2 text-sm font-medium bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-60">
            {mutation.isPending ? 'Creating…' : 'Create'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

function CreateMeetingModal({ sprintId, projectId, onClose }: { sprintId: string; projectId: string; onClose: () => void }) {
  const queryClient = useQueryClient()
  const [form, setForm] = useState({
    title: '',
    description: '',
    type: 'PLANNING' as MeetingType,
    startsAt: '',
    endsAt: '',
  })
  const [selectedMemberIds, setSelectedMemberIds] = useState<string[]>([])
  const [error, setError] = useState('')

  const { data: members } = useQuery({
    queryKey: ['members', projectId],
    queryFn: () => getProjectMembers(projectId),
  })

  const mutation = useMutation({
    mutationFn: (data: MeetingWithMembersPayload) => createMeetingWithMembers(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['meetings', projectId] })
      onClose()
    },
    onError: (err) => setError(getApiError(err, 'Failed to create meeting')),
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    setError('')
    mutation.mutate({
      ...form,
      sprintId,
      projectId,
      startsAt: new Date(form.startsAt).toISOString(),
      endsAt: new Date(form.endsAt).toISOString(),
      memberIds: selectedMemberIds,
    })
  }

  const toggleMember = (memberId: string) => {
    setSelectedMemberIds((prev) =>
      prev.includes(memberId) ? prev.filter((id) => id !== memberId) : [...prev, memberId]
    )
  }

  return (
    <Modal title="New Meeting" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <div className="bg-red-50 border border-red-200 rounded-lg px-3 py-2 text-sm text-red-700">{error}</div>}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Title *</label>
          <input required value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Type</label>
          <select value={form.type} onChange={(e) => setForm((f) => ({ ...f, type: e.target.value as MeetingType }))}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
            {MEETING_TYPES.map((t) => <option key={t}>{t}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} rows={2}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none" />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Starts At *</label>
            <input required type="datetime-local" value={form.startsAt} onChange={(e) => setForm((f) => ({ ...f, startsAt: e.target.value }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Ends At *</label>
            <input required type="datetime-local" value={form.endsAt} onChange={(e) => setForm((f) => ({ ...f, endsAt: e.target.value }))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
        </div>

        {members && members.length > 0 && (
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Attendees <span className="text-gray-400 font-normal">({selectedMemberIds.length} selected)</span>
            </label>
            <div className="border border-gray-200 rounded-lg divide-y divide-gray-100 max-h-40 overflow-y-auto">
              {members.map((member) => (
                <label key={member.id} className="flex items-center gap-3 px-3 py-2 cursor-pointer hover:bg-gray-50">
                  <input
                    type="checkbox"
                    checked={selectedMemberIds.includes(member.id)}
                    onChange={() => toggleMember(member.id)}
                    className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
                  />
                  <span className="text-sm text-gray-700">{member.userName}</span>
                  <Badge label={member.role} variant={memberRoleVariant(member.role)} />
                </label>
              ))}
            </div>
          </div>
        )}

        <div className="flex justify-end gap-3 pt-2">
          <button type="button" onClick={onClose} className="px-4 py-2 text-sm font-medium text-gray-700">Cancel</button>
          <button type="submit" disabled={mutation.isPending}
            className="px-4 py-2 text-sm font-medium bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-60">
            {mutation.isPending ? 'Creating…' : 'Create'}
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
    queryKey: ['sprint', sprintId],
    queryFn: () => getSprint(sprintId!),
    enabled: !!sprintId,
  })

  const ticketFilters: TicketFilters = {
    projectId: projectId!,
    status: filterStatus || undefined,
    priority: filterPriority || undefined,
    sprintStatus: filterSprintStatus || undefined,
    page,
    size: 20,
  }

  const { data: ticketsPage } = useQuery({
    queryKey: ['tickets', ticketFilters],
    queryFn: () => getTickets(ticketFilters),
    enabled: !!projectId,
  })

  const { data: meetings } = useQuery({
    queryKey: ['meetings', projectId],
    queryFn: () => getMeetings(projectId!),
    enabled: !!projectId,
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
    return <Layout><div className="flex items-center justify-center h-64 text-gray-400">Loading…</div></Layout>
  }

  if (!sprint) {
    return <Layout><div className="flex items-center justify-center h-64 text-red-500">Sprint not found</div></Layout>
  }

  return (
    <Layout>
      <div className="p-8 max-w-6xl">
        <div className="mb-2 flex items-center gap-2 text-sm text-gray-500">
          <button onClick={() => navigate('/projects')} className="text-indigo-600 hover:text-indigo-700">Projects</button>
          <span>/</span>
          <button onClick={() => navigate(`/projects/${projectId}`)} className="text-indigo-600 hover:text-indigo-700">Project</button>
          <span>/</span>
          <span className="text-gray-700">{sprint.name}</span>
        </div>

        <div className="mb-6">
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-900">{sprint.name}</h1>
            <Badge label={sprint.status} variant={sprint.status === 'ACTIVE' ? 'green' : sprint.status === 'COMPLETED' ? 'gray' : 'blue'} />
          </div>
          <div className="flex items-center gap-6 mt-2 text-sm text-gray-500">
            <span>{sprint.startDate} → {sprint.endDate}</span>
            <span>{sprint.estimationType.replace('_', ' ')}</span>
          </div>
          {sprint.businessGoal && (
            <p className="mt-2 text-sm text-gray-600"><span className="font-medium">Goal:</span> {sprint.businessGoal}</p>
          )}
        </div>

        <div className="space-y-8">
          <section>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                <svg className="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
                Tickets
                <span className="text-sm font-normal text-gray-400">({ticketsPage?.totalElements ?? 0})</span>
              </h2>
              <button onClick={() => setShowCreateTicket(true)}
                className="flex items-center gap-1 text-sm text-indigo-600 hover:text-indigo-700 font-medium">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                New Ticket
              </button>
            </div>

            <div className="flex items-center gap-3 mb-4 flex-wrap">
              <select value={filterStatus} onChange={(e) => { setFilterStatus(e.target.value as TicketStatus | ''); setPage(0) }}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
                <option value="">All statuses</option>
                {TICKET_STATUSES.map((s) => <option key={s}>{s}</option>)}
              </select>
              <select value={filterPriority} onChange={(e) => { setFilterPriority(e.target.value as TicketPriority | ''); setPage(0) }}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
                <option value="">All priorities</option>
                {TICKET_PRIORITIES.map((p) => <option key={p}>{p}</option>)}
              </select>
              <select value={filterSprintStatus} onChange={(e) => { setFilterSprintStatus(e.target.value as SprintStatus | ''); setPage(0) }}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
                <option value="">All sprint statuses</option>
                {SPRINT_STATUSES.map((s) => <option key={s}>{s}</option>)}
              </select>
              {(filterStatus || filterPriority || filterSprintStatus) && (
                <button onClick={() => { setFilterStatus(''); setFilterPriority(''); setFilterSprintStatus(''); setPage(0) }}
                  className="text-sm text-gray-500 hover:text-gray-700 underline">
                  Clear filters
                </button>
              )}
            </div>

            {ticketsPage && ticketsPage.content.length === 0 ? (
              <p className="text-sm text-gray-400 py-4">No tickets found.</p>
            ) : (
              <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="bg-gray-50 border-b border-gray-200">
                      <th className="text-left px-4 py-3 font-medium text-gray-600">Title</th>
                      <th className="text-left px-4 py-3 font-medium text-gray-600">Status</th>
                      <th className="text-left px-4 py-3 font-medium text-gray-600">Priority</th>
                      <th className="text-left px-4 py-3 font-medium text-gray-600">Est.</th>
                      <th className="px-4 py-3"></th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {ticketsPage?.content.map((ticket) => (
                      <tr key={ticket.id} className="hover:bg-gray-50 transition-colors">
                        <td className="px-4 py-3">
                          <Link to={`/tickets/${ticket.id}?projectId=${projectId}`}
                            className="font-medium text-gray-900 hover:text-indigo-700 transition-colors">
                            {ticket.title}
                          </Link>
                          {ticket.description && (
                            <p className="text-xs text-gray-400 mt-0.5 truncate max-w-xs">{ticket.description}</p>
                          )}
                        </td>
                        <td className="px-4 py-3">
                          <select
                            value={ticket.status}
                            onChange={(e) => updateStatusMutation.mutate({ ticketId: ticket.id, status: e.target.value as TicketStatus })}
                            className="text-xs border border-gray-200 rounded px-1.5 py-1 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                          >
                            {TICKET_STATUSES.map((s) => <option key={s}>{s}</option>)}
                          </select>
                        </td>
                        <td className="px-4 py-3">
                          <Badge label={ticket.priority} variant={ticketPriorityVariant(ticket.priority)} />
                        </td>
                        <td className="px-4 py-3 text-gray-500">{ticket.estimation ?? '—'}</td>
                        <td className="px-4 py-3">
                          <button onClick={() => deleteTicketMutation.mutate(ticket.id)}
                            className="text-gray-300 hover:text-red-500 transition-colors">
                            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {ticketsPage && ticketsPage.totalPages > 1 && (
                  <div className="flex items-center justify-between px-4 py-3 border-t border-gray-200 bg-gray-50">
                    <p className="text-sm text-gray-500">
                      Page {page + 1} of {ticketsPage.totalPages} · {ticketsPage.totalElements} total
                    </p>
                    <div className="flex gap-2">
                      <button disabled={page === 0} onClick={() => setPage((p) => p - 1)}
                        className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-100">
                        Prev
                      </button>
                      <button disabled={page >= ticketsPage.totalPages - 1} onClick={() => setPage((p) => p + 1)}
                        className="px-3 py-1 text-sm border border-gray-300 rounded-lg disabled:opacity-40 hover:bg-gray-100">
                        Next
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}
          </section>

          <section>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                <svg className="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                Meetings
                <span className="text-sm font-normal text-gray-400">({sprintMeetings?.length ?? 0})</span>
              </h2>
              <button onClick={() => setShowCreateMeeting(true)}
                className="flex items-center gap-1 text-sm text-indigo-600 hover:text-indigo-700 font-medium">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                New Meeting
              </button>
            </div>

            {sprintMeetings && sprintMeetings.length === 0 ? (
              <p className="text-sm text-gray-400 py-4">No meetings for this sprint.</p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {sprintMeetings?.map((meeting) => (
                  <div key={meeting.id} className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-sm transition-all">
                    <div className="flex items-start justify-between">
                      <div className="min-w-0">
                        <p className="font-medium text-gray-900 truncate">{meeting.title}</p>
                        <div className="flex items-center gap-2 mt-1">
                          <Badge label={meeting.type} variant="blue" />
                          <span className="text-xs text-gray-400">
                            {new Date(meeting.startsAt).toLocaleString()} – {new Date(meeting.endsAt).toLocaleTimeString()}
                          </span>
                        </div>
                      </div>
                      <button onClick={() => deleteMeetingMutation.mutate(meeting.id)}
                        className="text-gray-300 hover:text-red-500 transition-colors ml-2">
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </div>

                    {meeting.members.length > 0 && (
                      <div className="mt-3">
                        <p className="text-xs text-gray-500 mb-1.5">Attendees ({meeting.members.length}):</p>
                        <div className="flex flex-wrap gap-1">
                          {meeting.members.map((m) => (
                            <span key={m.id} className="inline-flex items-center gap-1 px-2 py-0.5 bg-gray-100 rounded-full text-xs text-gray-700">
                              {m.userName}
                              <Badge label={m.role} variant={memberRoleVariant(m.role)} />
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
