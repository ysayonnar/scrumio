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
    name: '', businessGoal: '', devPlan: '',
    startDate: '', endDate: '', status: 'PLANNED',
    estimationType: 'STORY_POINTS', projectId,
  })
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: createSprint,
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['sprints', projectId] }); onClose() },
    onError: (err) => setError(getApiError(err, 'Failed to create sprint')),
  })

  const set = (field: keyof SprintPayload, value: string) => setForm((f) => ({ ...f, [field]: value }))

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
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {error && <div className="err-box">{error}</div>}
        <div>
          <label className="lbl">Name *</label>
          <input required value={form.name} onChange={(e) => set('name', e.target.value)} className="field" />
        </div>
        <div>
          <label className="lbl">Business Goal</label>
          <textarea value={form.businessGoal} onChange={(e) => set('businessGoal', e.target.value)} rows={2} className="field" />
        </div>
        <div>
          <label className="lbl">Dev Plan</label>
          <textarea value={form.devPlan} onChange={(e) => set('devPlan', e.target.value)} rows={2} className="field" />
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <div>
            <label className="lbl">Start Date *</label>
            <input required type="date" value={form.startDate} onChange={(e) => set('startDate', e.target.value)} className="field" />
          </div>
          <div>
            <label className="lbl">End Date *</label>
            <input required type="date" value={form.endDate} onChange={(e) => set('endDate', e.target.value)} className="field" />
          </div>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <div>
            <label className="lbl">Status</label>
            <select value={form.status} onChange={(e) => set('status', e.target.value)} className="field">
              {SPRINT_STATUSES.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div>
            <label className="lbl">Estimation</label>
            <select value={form.estimationType} onChange={(e) => set('estimationType', e.target.value)} className="field">
              {ESTIMATION_TYPES.map((t) => <option key={t}>{t}</option>)}
            </select>
          </div>
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

function AddMemberModal({ projectId, onClose }: { projectId: string; onClose: () => void }) {
  const queryClient = useQueryClient()
  const [userId, setUserId] = useState('')
  const [role, setRole] = useState<ProjectMemberRole>('DEVELOPER')
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: () => addProjectMember(projectId, { userId, role }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['members', projectId] }); onClose() },
    onError: (err) => setError(getApiError(err, 'Failed to add member. Check the user ID.')),
  })

  return (
    <Modal title="Add Member" onClose={onClose}>
      <form onSubmit={(e) => { e.preventDefault(); setError(''); mutation.mutate() }} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {error && <div className="err-box">{error}</div>}
        <div>
          <label className="lbl">User ID *</label>
          <input required value={userId} onChange={(e) => setUserId(e.target.value)} className="field" placeholder="UUID of the user" />
        </div>
        <div>
          <label className="lbl">Role</label>
          <select value={role} onChange={(e) => setRole(e.target.value as ProjectMemberRole)} className="field">
            {MEMBER_ROLES.map((r) => <option key={r}>{r}</option>)}
          </select>
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', paddingTop: '6px' }}>
          <button type="button" onClick={onClose} className="btn btn-outline">Cancel</button>
          <button type="submit" disabled={mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'adding...' : '+ Add Member'}
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
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['project', projectId] }); onClose() },
  })

  return (
    <Modal title="Edit Project" onClose={onClose}>
      <form onSubmit={(e) => { e.preventDefault(); mutation.mutate() }} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        <div>
          <label className="lbl">Name</label>
          <input value={name} onChange={(e) => setName(e.target.value)} className="field" />
        </div>
        <div>
          <label className="lbl">Description</label>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} className="field" />
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

export function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [showCreateSprint, setShowCreateSprint] = useState(false)
  const [showAddMember, setShowAddMember] = useState(false)
  const [showEdit, setShowEdit] = useState(false)

  const { data: project, isLoading: projectLoading } = useQuery({
    queryKey: ['project', id], queryFn: () => getProject(id!), enabled: !!id,
  })
  const { data: sprints } = useQuery({
    queryKey: ['sprints', id], queryFn: () => getSprints(id!), enabled: !!id,
  })
  const { data: members } = useQuery({
    queryKey: ['members', id], queryFn: () => getProjectMembers(id!), enabled: !!id,
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
      <div style={{ padding: '24px 32px', borderBottom: '1px solid var(--bd)', background: 'rgba(20,20,31,0.97)' }}>
        <button className="back" onClick={() => navigate('/projects')} style={{ marginBottom: '10px' }}>
          ← Projects
        </button>
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '16px' }}>
          <div>
            <h1 style={{ fontSize: '18px', fontWeight: '700', color: '#eaeaf8', letterSpacing: '-0.01em' }}>
              {project.name}
            </h1>
            {project.description && (
              <div style={{ color: 'var(--tx2)', fontSize: '12px', marginTop: '4px' }}>
                {project.description}
              </div>
            )}
            <div style={{ color: 'var(--tx3)', fontSize: '10px', marginTop: '6px', letterSpacing: '0.04em' }}>
              ID: {project.id}
            </div>
          </div>
          <div style={{ display: 'flex', gap: '8px', flexShrink: 0 }}>
            <Link to={`/projects/${id}/board`} className="btn btn-outline">
              <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round">
                <rect x="1" y="2" width="2" height="8" rx="0.5" />
                <rect x="5" y="2" width="2" height="8" rx="0.5" />
                <rect x="9" y="2" width="2" height="8" rx="0.5" />
              </svg>
              Board
            </Link>
            <button onClick={() => setShowEdit(true)} className="btn btn-outline">
              <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round" strokeLinejoin="round">
                <path d="M8.5 1.5l2 2L4 10 1.5 10.5 2 8 8.5 1.5z" />
              </svg>
              Edit
            </button>
          </div>
        </div>
      </div>

      <div style={{ padding: '28px 32px', display: 'grid', gridTemplateColumns: '1fr 280px', gap: '32px', alignItems: 'start' }}>

        <section>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <div className="sh">
              sprints
              <span style={{ color: 'var(--tx3)', fontSize: '10px' }}>
                [{sprints?.length ?? 0}]
              </span>
            </div>
            <button onClick={() => setShowCreateSprint(true)} className="btn btn-link" style={{ fontSize: '11px' }}>
              + new sprint
            </button>
          </div>

          {sprints && sprints.length === 0 ? (
            <div style={{ color: 'var(--tx3)', fontSize: '12px', padding: '16px 0' }}>
              No sprints yet.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {sprints?.map((sprint) => (
                <div key={sprint.id} className="card" style={{ padding: '12px 14px' }}>
                  <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '10px' }}>
                    <Link
                      to={`/projects/${id}/sprints/${sprint.id}`}
                      style={{ color: '#e4e4f4', fontSize: '13px', fontWeight: '500', flex: 1, minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                      onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--ac)' }}
                      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.color = '#e4e4f4' }}
                    >
                      {sprint.name}
                    </Link>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexShrink: 0 }}>
                      <Badge label={sprint.status} variant={sprintStatusVariant(sprint.status)} />
                      <button onClick={() => deleteSprintMutation.mutate(sprint.id)} className="btn-ghost" style={{ padding: '3px' }}>
                        <svg width="11" height="11" viewBox="0 0 11 11" fill="none" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round">
                          <path d="M1 2.5h9M4 1h3M2.5 2.5l.5 7h5l.5-7" />
                        </svg>
                      </button>
                    </div>
                  </div>
                  {sprint.businessGoal && (
                    <div style={{ color: 'var(--tx2)', fontSize: '11px', marginTop: '5px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {sprint.businessGoal}
                    </div>
                  )}
                  <div style={{ display: 'flex', gap: '16px', marginTop: '8px', color: 'var(--tx3)', fontSize: '11px', letterSpacing: '0.03em' }}>
                    <span>{sprint.startDate} → {sprint.endDate}</span>
                    <span>{sprint.estimationType.replace('_', ' ')}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        <aside>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
            <div className="sh">
              members
              <span style={{ color: 'var(--tx3)', fontSize: '10px' }}>
                [{members?.length ?? 0}]
              </span>
            </div>
            <button onClick={() => setShowAddMember(true)} className="btn btn-link" style={{ fontSize: '11px' }}>
              + add
            </button>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            {members?.map((member) => (
              <div key={member.id} className="card" style={{ padding: '10px 12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '8px' }}>
                  <div style={{ minWidth: 0, flex: 1 }}>
                    <div style={{ color: '#e4e4f4', fontSize: '12px', fontWeight: '500', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {member.userName}
                    </div>
                    <div style={{ color: 'var(--tx3)', fontSize: '10px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', marginTop: '1px' }}>
                      {member.userId.slice(0, 8)}...
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexShrink: 0 }}>
                    <select
                      value={member.role}
                      onChange={(e) => updateRoleMutation.mutate({ memberId: member.id, role: e.target.value as ProjectMemberRole })}
                      className="field-sm"
                    >
                      {MEMBER_ROLES.map((r) => <option key={r}>{r}</option>)}
                    </select>
                    <button onClick={() => removeMemberMutation.mutate(member.id)} className="btn-ghost" style={{ padding: '3px' }}>
                      <svg width="10" height="10" viewBox="0 0 10 10" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round">
                        <path d="M1 1l8 8M9 1L1 9" />
                      </svg>
                    </button>
                  </div>
                </div>
                <div style={{ marginTop: '7px' }}>
                  <Badge label={member.role} variant={memberRoleVariant(member.role)} />
                </div>
              </div>
            ))}
            {members && members.length === 0 && (
              <div style={{ color: 'var(--tx3)', fontSize: '12px', padding: '8px 0' }}>
                No members yet.
              </div>
            )}
          </div>
        </aside>
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
