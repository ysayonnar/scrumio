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

function FormField({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="lbl">{label}</label>
      {children}
    </div>
  )
}

function CreateSprintModal({ projectId, onClose }: { projectId: string; onClose: () => void }) {
  const queryClient = useQueryClient()
  const [form, setForm] = useState<SprintPayload>({
    name: '', businessGoal: '', devPlan: '',
    startDate: '', endDate: '', status: 'PLANNED',
    estimationType: 'STORY_POINTS', projectId,
  })
  const [error, setError] = useState('')
  const set = (k: keyof SprintPayload, v: string) => setForm((f) => ({ ...f, [k]: v }))

  const mutation = useMutation({
    mutationFn: createSprint,
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['sprints', projectId] }); onClose() },
    onError: (err) => setError(getApiError(err, 'Failed to create sprint')),
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault(); setError('')
    if (!form.name.trim()) { setError('Name is required'); return }
    if (!form.startDate || !form.endDate) { setError('Dates are required'); return }
    if (form.startDate >= form.endDate) { setError('Start must be before end'); return }
    mutation.mutate(form)
  }

  return (
    <Modal title="New Sprint" onClose={onClose}>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {error && <div className="err-box">{error}</div>}
        <FormField label="Name *">
          <input required value={form.name} onChange={(e) => set('name', e.target.value)} className="field" placeholder="Sprint 1" />
        </FormField>
        <FormField label="Business goal">
          <textarea value={form.businessGoal} onChange={(e) => set('businessGoal', e.target.value)} rows={2} className="field" placeholder="What do we want to achieve?" />
        </FormField>
        <FormField label="Dev plan">
          <textarea value={form.devPlan} onChange={(e) => set('devPlan', e.target.value)} rows={2} className="field" placeholder="How will we achieve it?" />
        </FormField>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <FormField label="Start date *">
            <input required type="date" value={form.startDate} onChange={(e) => set('startDate', e.target.value)} className="field" />
          </FormField>
          <FormField label="End date *">
            <input required type="date" value={form.endDate} onChange={(e) => set('endDate', e.target.value)} className="field" />
          </FormField>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <FormField label="Status">
            <select value={form.status} onChange={(e) => set('status', e.target.value)} className="field">
              {SPRINT_STATUSES.map((s) => <option key={s}>{s}</option>)}
            </select>
          </FormField>
          <FormField label="Estimation">
            <select value={form.estimationType} onChange={(e) => set('estimationType', e.target.value)} className="field">
              {ESTIMATION_TYPES.map((t) => <option key={t}>{t}</option>)}
            </select>
          </FormField>
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', paddingTop: '4px' }}>
          <button type="button" onClick={onClose} className="btn btn-outline">Cancel</button>
          <button type="submit" disabled={mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'Creating…' : 'Create sprint'}
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
    onError: (err) => setError(getApiError(err, 'Failed to add member')),
  })

  return (
    <Modal title="Add Member" onClose={onClose}>
      <form onSubmit={(e) => { e.preventDefault(); setError(''); mutation.mutate() }} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {error && <div className="err-box">{error}</div>}
        <FormField label="User ID *">
          <input required value={userId} onChange={(e) => setUserId(e.target.value)} className="field" placeholder="Paste user UUID" />
        </FormField>
        <FormField label="Role">
          <select value={role} onChange={(e) => setRole(e.target.value as ProjectMemberRole)} className="field">
            {MEMBER_ROLES.map((r) => <option key={r}>{r}</option>)}
          </select>
        </FormField>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', paddingTop: '4px' }}>
          <button type="button" onClick={onClose} className="btn btn-outline">Cancel</button>
          <button type="submit" disabled={mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'Adding…' : 'Add member'}
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
      <form onSubmit={(e) => { e.preventDefault(); mutation.mutate() }} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <FormField label="Name">
          <input value={name} onChange={(e) => setName(e.target.value)} className="field" />
        </FormField>
        <FormField label="Description">
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} className="field" />
        </FormField>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', paddingTop: '4px' }}>
          <button type="button" onClick={onClose} className="btn btn-outline">Cancel</button>
          <button type="submit" disabled={mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'Saving…' : 'Save changes'}
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

  const { data: project, isLoading } = useQuery({ queryKey: ['project', id], queryFn: () => getProject(id!), enabled: !!id })
  const { data: sprints } = useQuery({ queryKey: ['sprints', id], queryFn: () => getSprints(id!), enabled: !!id })
  const { data: members } = useQuery({ queryKey: ['members', id], queryFn: () => getProjectMembers(id!), enabled: !!id })

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

  if (isLoading) return (
    <Layout>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '60vh', color: 'var(--tx3)', fontSize: '14px' }}>
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
      {/* Header */}
      <div style={{ background: '#fff', borderBottom: '1px solid var(--bd)', padding: '24px 32px' }}>
        <button className="back" onClick={() => navigate('/projects')} style={{ marginBottom: '12px' }}>
          ← All projects
        </button>
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '20px' }}>
          <div>
            <h1 style={{ fontFamily: 'Syne, sans-serif', fontWeight: 800, fontSize: '24px', color: 'var(--tx)', letterSpacing: '-0.02em', marginBottom: '4px' }}>
              {project.name}
            </h1>
            {project.description && (
              <p style={{ color: 'var(--tx2)', fontSize: '14px' }}>{project.description}</p>
            )}
          </div>
          <div style={{ display: 'flex', gap: '8px', flexShrink: 0 }}>
            <Link to={`/projects/${id}/board`} className="btn btn-outline">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round">
                <rect x="1" y="2" width="2.5" height="10" rx="1"/>
                <rect x="5.75" y="2" width="2.5" height="10" rx="1"/>
                <rect x="10.5" y="2" width="2.5" height="10" rx="1"/>
              </svg>
              Board
            </Link>
            <button onClick={() => setShowEdit(true)} className="btn btn-outline">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M9.5 2l2.5 2.5L5 11.5H2.5V9L9.5 2z"/>
              </svg>
              Edit
            </button>
          </div>
        </div>
      </div>

      {/* Body */}
      <div style={{ padding: '28px 32px', display: 'grid', gridTemplateColumns: '1fr 300px', gap: '28px', alignItems: 'start' }}>

        {/* Sprints */}
        <section>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <h2 style={{ fontFamily: 'Syne, sans-serif', fontWeight: 700, fontSize: '16px', color: 'var(--tx)' }}>Sprints</h2>
              <span style={{ background: 'var(--surf2)', border: '1px solid var(--bd)', borderRadius: '99px', padding: '1px 9px', fontSize: '12px', color: 'var(--tx2)' }}>
                {sprints?.length ?? 0}
              </span>
            </div>
            <button onClick={() => setShowCreateSprint(true)} className="btn btn-primary" style={{ padding: '7px 14px', fontSize: '13px' }}>
              + New sprint
            </button>
          </div>

          {sprints && sprints.length === 0 ? (
            <div style={{ background: '#fff', border: '1px solid var(--bd)', borderRadius: '12px', padding: '40px', textAlign: 'center' }}>
              <p style={{ color: 'var(--tx3)', fontSize: '14px' }}>No sprints yet — create your first one.</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {sprints?.map((sprint) => (
                <div key={sprint.id} className="card-flat" style={{ padding: '16px 18px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '12px' }}>
                    <Link
                      to={`/projects/${id}/sprints/${sprint.id}`}
                      style={{ fontFamily: 'Syne, sans-serif', fontWeight: 600, fontSize: '15px', color: 'var(--tx)', flex: 1, minWidth: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', transition: 'color 0.15s' }}
                      onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--ac)' }}
                      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--tx)' }}
                    >
                      {sprint.name}
                    </Link>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexShrink: 0 }}>
                      <Badge label={sprint.status} variant={sprintStatusVariant(sprint.status)} />
                      <button onClick={() => deleteSprintMutation.mutate(sprint.id)} className="btn-ghost">
                        <svg width="13" height="13" viewBox="0 0 13 13" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round">
                          <path d="M1 3h11M4.5 1.5h4M2.5 3l.7 8h6.6l.7-8"/>
                        </svg>
                      </button>
                    </div>
                  </div>
                  {sprint.businessGoal && (
                    <p style={{ color: 'var(--tx2)', fontSize: '13px', marginTop: '4px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {sprint.businessGoal}
                    </p>
                  )}
                  <div style={{ display: 'flex', gap: '16px', marginTop: '10px', color: 'var(--tx3)', fontSize: '12px' }}>
                    <span>{sprint.startDate} → {sprint.endDate}</span>
                    <span>{sprint.estimationType.replace('_', ' ')}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Members */}
        <aside>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <h2 style={{ fontFamily: 'Syne, sans-serif', fontWeight: 700, fontSize: '16px', color: 'var(--tx)' }}>Members</h2>
              <span style={{ background: 'var(--surf2)', border: '1px solid var(--bd)', borderRadius: '99px', padding: '1px 9px', fontSize: '12px', color: 'var(--tx2)' }}>
                {members?.length ?? 0}
              </span>
            </div>
            <button onClick={() => setShowAddMember(true)} className="btn-link">+ Add</button>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {members?.map((m) => (
              <div key={m.id} className="card-flat" style={{ padding: '12px 14px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{
                    width: '32px', height: '32px', borderRadius: '8px',
                    background: 'var(--ac-l)', flexShrink: 0,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}>
                    <span style={{ fontFamily: 'Syne, sans-serif', fontWeight: 700, fontSize: '13px', color: 'var(--ac)' }}>
                      {m.userName.charAt(0).toUpperCase()}
                    </span>
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontWeight: 500, fontSize: '13px', color: 'var(--tx)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {m.userName}
                    </div>
                    <Badge label={m.role} variant={memberRoleVariant(m.role)} />
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexShrink: 0 }}>
                    <select
                      value={m.role}
                      onChange={(e) => updateRoleMutation.mutate({ memberId: m.id, role: e.target.value as ProjectMemberRole })}
                      className="field-sm"
                    >
                      {MEMBER_ROLES.map((r) => <option key={r}>{r}</option>)}
                    </select>
                    <button onClick={() => removeMemberMutation.mutate(m.id)} className="btn-ghost" style={{ padding: '4px' }}>
                      <svg width="11" height="11" viewBox="0 0 11 11" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round">
                        <path d="M1 1l9 9M10 1L1 10"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            ))}
            {members && members.length === 0 && (
              <p style={{ color: 'var(--tx3)', fontSize: '13px', padding: '4px 0' }}>No members yet.</p>
            )}
          </div>
        </aside>
      </div>

      {showCreateSprint && <CreateSprintModal projectId={id!} onClose={() => setShowCreateSprint(false)} />}
      {showAddMember   && <AddMemberModal    projectId={id!} onClose={() => setShowAddMember(false)} />}
      {showEdit && project && (
        <EditProjectModal projectId={id!} initialName={project.name} initialDesc={project.description} onClose={() => setShowEdit(false)} />
      )}
    </Layout>
  )
}
