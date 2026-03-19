import { useState, type FormEvent } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProject, updateProject } from '../api/projects'
import { getSprints, createSprint, deleteSprint, type SprintPayload } from '../api/sprints'
import { getProjectMembers, addProjectMember, removeProjectMember, updateProjectMemberRole } from '../api/members'
import { getApiError } from '../api/utils'
import { Layout } from '../components/Layout'
import { Modal } from '../components/Modal'
import { Badge, sprintStatusVariant, memberRoleVariant } from '../components/Badge'
import type { ProjectMemberRole, SprintStatus, SprintEstimationType } from '../types'

const SPRINT_STATUSES: SprintStatus[] = ['PLANNED', 'ACTIVE', 'COMPLETED']
const ESTIMATION_TYPES: SprintEstimationType[] = ['STORY_POINTS', 'HOURS']
const MEMBER_ROLES: ProjectMemberRole[] = ['OWNER', 'MANAGER', 'DEVELOPER', 'STAKEHOLDER']

function CreateSprintModal({ projectId, onClose }: { projectId: string; onClose: () => void }) {
  const queryClient = useQueryClient()
  const [form, setForm] = useState<SprintPayload>({
    name: '',
    businessGoal: '',
    devPlan: '',
    startDate: '',
    endDate: '',
    status: 'PLANNED',
    estimationType: 'STORY_POINTS',
    projectId,
  })
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: createSprint,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sprints', projectId] })
      onClose()
    },
    onError: (err) => setError(getApiError(err, 'Failed to create sprint')),
  })

  const set = (field: keyof SprintPayload, value: string) =>
    setForm((f) => ({ ...f, [field]: value }))

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    setError('')
    if (!form.name.trim()) { setError('Name is required'); return }
    if (!form.startDate) { setError('Start date is required'); return }
    if (!form.endDate) { setError('End date is required'); return }
    if (form.startDate >= form.endDate) { setError('Start date must be before end date'); return }
    mutation.mutate(form)
  }

  return (
    <Modal title="New Sprint" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <div className="bg-red-50 border border-red-200 rounded-lg px-3 py-2 text-sm text-red-700">{error}</div>}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Name *</label>
          <input required value={form.name} onChange={(e) => set('name', e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Business Goal</label>
          <textarea value={form.businessGoal} onChange={(e) => set('businessGoal', e.target.value)} rows={2}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none" />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Dev Plan</label>
          <textarea value={form.devPlan} onChange={(e) => set('devPlan', e.target.value)} rows={2}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none" />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Start Date *</label>
            <input required type="date" value={form.startDate} onChange={(e) => set('startDate', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">End Date *</label>
            <input required type="date" value={form.endDate} onChange={(e) => set('endDate', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select value={form.status} onChange={(e) => set('status', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
              {SPRINT_STATUSES.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Estimation</label>
            <select value={form.estimationType} onChange={(e) => set('estimationType', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
              {ESTIMATION_TYPES.map((t) => <option key={t}>{t}</option>)}
            </select>
          </div>
        </div>
        <div className="flex justify-end gap-3 pt-2">
          <button type="button" onClick={onClose} className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900">Cancel</button>
          <button type="submit" disabled={mutation.isPending}
            className="px-4 py-2 text-sm font-medium bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-60">
            {mutation.isPending ? 'Creating…' : 'Create'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

function AddMemberModal({ projectId, onClose }: { projectId: string; onClose: () => void }) {
  const queryClient = useQueryClient()
  const [userId, setUserId] = useState('')
  const [role, setRole] = useState<ProjectMemberRole>('DEVELOPER')
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: () => addProjectMember(projectId, { userId, role }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['members', projectId] })
      onClose()
    },
    onError: (err) => setError(getApiError(err, 'Failed to add member. Check the user ID.')),
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    setError('')
    mutation.mutate()
  }

  return (
    <Modal title="Add Member" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <div className="bg-red-50 border border-red-200 rounded-lg px-3 py-2 text-sm text-red-700">{error}</div>}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">User ID *</label>
          <input required value={userId} onChange={(e) => setUserId(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            placeholder="UUID of the user" />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Role</label>
          <select value={role} onChange={(e) => setRole(e.target.value as ProjectMemberRole)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
            {MEMBER_ROLES.map((r) => <option key={r}>{r}</option>)}
          </select>
        </div>
        <div className="flex justify-end gap-3 pt-2">
          <button type="button" onClick={onClose} className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900">Cancel</button>
          <button type="submit" disabled={mutation.isPending}
            className="px-4 py-2 text-sm font-medium bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-60">
            {mutation.isPending ? 'Adding…' : 'Add Member'}
          </button>
        </div>
      </form>
    </Modal>
  )
}

function EditProjectModal({ projectId, initialName, initialDesc, onClose }: {
  projectId: string; initialName: string; initialDesc: string; onClose: () => void
}) {
  const queryClient = useQueryClient()
  const [name, setName] = useState(initialName)
  const [description, setDescription] = useState(initialDesc)

  const mutation = useMutation({
    mutationFn: () => updateProject(projectId, { name, description }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['project', projectId] })
      onClose()
    },
  })

  return (
    <Modal title="Edit Project" onClose={onClose}>
      <form onSubmit={(e) => { e.preventDefault(); mutation.mutate() }} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
          <input value={name} onChange={(e) => setName(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none" />
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

export function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [showCreateSprint, setShowCreateSprint] = useState(false)
  const [showAddMember, setShowAddMember] = useState(false)
  const [showEdit, setShowEdit] = useState(false)

  const { data: project, isLoading: projectLoading } = useQuery({
    queryKey: ['project', id],
    queryFn: () => getProject(id!),
    enabled: !!id,
  })

  const { data: sprints } = useQuery({
    queryKey: ['sprints', id],
    queryFn: () => getSprints(id!),
    enabled: !!id,
  })

  const { data: members } = useQuery({
    queryKey: ['members', id],
    queryFn: () => getProjectMembers(id!),
    enabled: !!id,
  })

  const deleteSprintMutation = useMutation({
    mutationFn: deleteSprint,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sprints', id] }),
  })

  const removeMemberMutation = useMutation({
    mutationFn: (memberId: string) => removeProjectMember(id!, memberId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['members', id] }),
  })

  const updateRoleMutation = useMutation({
    mutationFn: ({ memberId, role }: { memberId: string; role: ProjectMemberRole }) =>
      updateProjectMemberRole(id!, memberId, role),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['members', id] }),
  })

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
      <div className="p-8 max-w-5xl">
        <div className="mb-2">
          <button onClick={() => navigate('/projects')} className="text-sm text-indigo-600 hover:text-indigo-700 flex items-center gap-1">
            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
            Projects
          </button>
        </div>

        <div className="flex items-start justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{project.name}</h1>
            {project.description && <p className="text-gray-500 mt-1">{project.description}</p>}
            <p className="text-xs text-gray-400 mt-2">ID: {project.id}</p>
          </div>
          <div className="flex items-center gap-2">
            <Link to={`/projects/${id}/board`}
              className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-indigo-600 border border-indigo-300 rounded-lg hover:bg-indigo-50">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 17V7m0 10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h2a2 2 0 012 2m0 10a2 2 0 002 2h2a2 2 0 002-2M9 7a2 2 0 012-2h2a2 2 0 012 2m0 10V7m0 10a2 2 0 002 2h2a2 2 0 002-2V7a2 2 0 00-2-2h-2a2 2 0 00-2 2" />
              </svg>
              Board
            </Link>
            <button onClick={() => setShowEdit(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 text-sm text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              Edit
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-6">
            <section>
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                  <svg className="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                  Sprints
                  <span className="text-sm font-normal text-gray-400">({sprints?.length ?? 0})</span>
                </h2>
                <button onClick={() => setShowCreateSprint(true)}
                  className="flex items-center gap-1 text-sm text-indigo-600 hover:text-indigo-700 font-medium">
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                  </svg>
                  New Sprint
                </button>
              </div>

              {sprints && sprints.length === 0 ? (
                <p className="text-sm text-gray-400 py-4">No sprints yet.</p>
              ) : (
                <div className="space-y-3">
                  {sprints?.map((sprint) => (
                    <div key={sprint.id}
                      className="bg-white border border-gray-200 rounded-lg p-4 hover:border-indigo-300 hover:shadow-sm transition-all">
                      <div className="flex items-start justify-between">
                        <Link to={`/projects/${id}/sprints/${sprint.id}`}
                          className="font-medium text-gray-900 hover:text-indigo-700 transition-colors flex-1 min-w-0">
                          {sprint.name}
                        </Link>
                        <div className="flex items-center gap-2 ml-3">
                          <Badge label={sprint.status} variant={sprintStatusVariant(sprint.status)} />
                          <button onClick={() => deleteSprintMutation.mutate(sprint.id)}
                            className="text-gray-300 hover:text-red-500 transition-colors">
                            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                          </button>
                        </div>
                      </div>
                      {sprint.businessGoal && (
                        <p className="text-sm text-gray-500 mt-1 truncate">{sprint.businessGoal}</p>
                      )}
                      <div className="flex items-center gap-4 mt-2 text-xs text-gray-400">
                        <span>{sprint.startDate} → {sprint.endDate}</span>
                        <span>{sprint.estimationType.replace('_', ' ')}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>
          </div>

          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                <svg className="w-5 h-5 text-indigo-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                Members
                <span className="text-sm font-normal text-gray-400">({members?.length ?? 0})</span>
              </h2>
              <button onClick={() => setShowAddMember(true)}
                className="text-sm text-indigo-600 hover:text-indigo-700 font-medium">
                + Add
              </button>
            </div>

            <div className="space-y-2">
              {members?.map((member) => (
                <div key={member.id} className="bg-white border border-gray-200 rounded-lg p-3">
                  <div className="flex items-center justify-between gap-2">
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-gray-900 truncate">{member.userName}</p>
                      <p className="text-xs text-gray-400 truncate">{member.userId}</p>
                    </div>
                    <div className="flex items-center gap-1.5 shrink-0">
                      <select
                        value={member.role}
                        onChange={(e) => updateRoleMutation.mutate({ memberId: member.id, role: e.target.value as ProjectMemberRole })}
                        className="text-xs border border-gray-200 rounded px-1.5 py-1 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                      >
                        {MEMBER_ROLES.map((r) => <option key={r}>{r}</option>)}
                      </select>
                      <button onClick={() => removeMemberMutation.mutate(member.id)}
                        className="text-gray-300 hover:text-red-500 transition-colors">
                        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </div>
                  </div>
                  <div className="mt-1.5">
                    <Badge label={member.role} variant={memberRoleVariant(member.role)} />
                  </div>
                </div>
              ))}
              {members && members.length === 0 && (
                <p className="text-sm text-gray-400 py-2">No members yet.</p>
              )}
            </div>
          </div>
        </div>
      </div>

      {showCreateSprint && <CreateSprintModal projectId={id!} onClose={() => setShowCreateSprint(false)} />}
      {showAddMember && <AddMemberModal projectId={id!} onClose={() => setShowAddMember(false)} />}
      {showEdit && project && (
        <EditProjectModal
          projectId={id!}
          initialName={project.name}
          initialDesc={project.description}
          onClose={() => setShowEdit(false)}
        />
      )}
    </Layout>
  )
}
