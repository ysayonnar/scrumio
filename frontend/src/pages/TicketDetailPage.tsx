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
    queryKey: ['members', projectId],
    queryFn: () => getProjectMembers(projectId),
  })

  const assignMutation = useMutation({
    mutationFn: (memberId: string) => assignTicketMember(ticketId, memberId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['ticketMembers', ticketId] }),
    onError: () => setError('Failed to assign member'),
  })

  const unassigned = projectMembers?.filter((m) => !assignedMemberIds.includes(m.id)) ?? []

  return (
    <Modal title="Assign Member" onClose={onClose}>
      {error && <div className="bg-red-50 border border-red-200 rounded-lg px-3 py-2 text-sm text-red-700 mb-3">{error}</div>}
      {unassigned.length === 0 ? (
        <p className="text-sm text-gray-500 py-2">All project members are already assigned.</p>
      ) : (
        <div className="space-y-2">
          {unassigned.map((member) => (
            <div key={member.id} className="flex items-center justify-between px-3 py-2.5 border border-gray-200 rounded-lg hover:bg-gray-50">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-xs font-semibold text-indigo-700">
                  {member.userName.charAt(0).toUpperCase()}
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">{member.userName}</p>
                  <Badge label={member.role} variant={memberRoleVariant(member.role)} />
                </div>
              </div>
              <button
                onClick={() => assignMutation.mutate(member.id)}
                disabled={assignMutation.isPending}
                className="px-3 py-1.5 text-xs font-medium bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-60"
              >
                Assign
              </button>
            </div>
          ))}
        </div>
      )}
      <div className="mt-4 flex justify-end">
        <button onClick={onClose} className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900">Close</button>
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
      title,
      description,
      status,
      priority,
      estimation: estimation ? Number(estimation) : null,
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ticket', ticketId] })
      onClose()
    },
  })

  if (!ticket) return null

  return (
    <Modal title="Edit Ticket" onClose={onClose}>
      <form onSubmit={(e) => { e.preventDefault(); mutation.mutate() }} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Title</label>
          <input value={title} onChange={(e) => setTitle(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none" />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select value={status} onChange={(e) => setStatus(e.target.value as TicketStatus)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
              {TICKET_STATUSES.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Priority</label>
            <select value={priority} onChange={(e) => setPriority(e.target.value as TicketPriority)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
              {TICKET_PRIORITIES.map((p) => <option key={p}>{p}</option>)}
            </select>
          </div>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Estimation</label>
          <input type="number" min={0} value={estimation} onChange={(e) => setEstimation(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            placeholder="Story points / hours" />
        </div>
        <div className="flex justify-end gap-3 pt-2">
          <button type="button" onClick={onClose} className="px-4 py-2 text-sm font-medium text-gray-700">Cancel</button>
          <button type="submit" disabled={mutation.isPending}
            className="px-4 py-2 text-sm font-medium bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-60">
            {mutation.isPending ? 'Saving…' : 'Save'}
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
    queryKey: ['ticket', ticketId],
    queryFn: () => getTicket(ticketId!),
    enabled: !!ticketId,
  })

  const { data: assignments } = useQuery({
    queryKey: ['ticketMembers', ticketId],
    queryFn: () => getTicketMembers(ticketId!),
    enabled: !!ticketId,
  })

  const unassignMutation = useMutation({
    mutationFn: (assignmentId: string) => unassignTicketMember(ticketId!, assignmentId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['ticketMembers', ticketId] }),
  })

  if (isLoading) {
    return <Layout><div className="flex items-center justify-center h-64 text-gray-400">Loading…</div></Layout>
  }

  if (!ticket) {
    return <Layout><div className="flex items-center justify-center h-64 text-red-500">Ticket not found</div></Layout>
  }

  return (
    <Layout>
      <div className="p-8 max-w-3xl">
        <button onClick={() => navigate(-1)} className="text-sm text-indigo-600 hover:text-indigo-700 flex items-center gap-1 mb-4">
          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Back
        </button>

        <div className="bg-white border border-gray-200 rounded-xl p-6 mb-6">
          <div className="flex items-start justify-between mb-4">
            <div className="flex items-center gap-3 flex-wrap">
              <Badge label={ticket.status} variant={ticketStatusVariant(ticket.status)} />
              <Badge label={ticket.priority} variant={ticketPriorityVariant(ticket.priority)} />
              {ticket.estimation != null && (
                <span className="text-xs text-gray-500 bg-gray-100 px-2.5 py-0.5 rounded-full">
                  {ticket.estimation} pts
                </span>
              )}
            </div>
            <button onClick={() => setShowEdit(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Edit
            </button>
          </div>

          <h1 className="text-xl font-bold text-gray-900 mb-2">{ticket.title}</h1>
          {ticket.description && (
            <p className="text-gray-600 text-sm leading-relaxed">{ticket.description}</p>
          )}

          <div className="mt-4 pt-4 border-t border-gray-100 grid grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-gray-400 text-xs mb-1">Sprint</p>
              <p className="text-gray-700">{ticket.sprintName ?? '—'}</p>
            </div>
            <div>
              <p className="text-gray-400 text-xs mb-1">Created</p>
              <p className="text-gray-700">{new Date(ticket.createdAt).toLocaleDateString()}</p>
            </div>
          </div>
        </div>

        <section>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
              <svg className="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              Assignees
              <span className="text-sm font-normal text-gray-400">({assignments?.length ?? 0})</span>
            </h2>
            {projectId && (
              <button onClick={() => setShowAssign(true)}
                className="flex items-center gap-1 text-sm text-indigo-600 hover:text-indigo-700 font-medium">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
                Assign
              </button>
            )}
          </div>

          {assignments && assignments.length === 0 ? (
            <p className="text-sm text-gray-400">No assignees yet.</p>
          ) : (
            <div className="space-y-2">
              {assignments?.map((assignment) => (
                <div key={assignment.id}
                  className="flex items-center justify-between px-4 py-3 bg-white border border-gray-200 rounded-lg hover:shadow-sm transition-all">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-full bg-indigo-100 flex items-center justify-center text-sm font-semibold text-indigo-700">
                      {assignment.userName.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-900">{assignment.userName}</p>
                      <p className="text-xs text-gray-400">{assignment.userId}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <Badge label={assignment.role} variant={memberRoleVariant(assignment.role)} />
                    <button
                      onClick={() => unassignMutation.mutate(assignment.id)}
                      className="text-gray-300 hover:text-red-500 transition-colors"
                      title="Unassign"
                    >
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
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
