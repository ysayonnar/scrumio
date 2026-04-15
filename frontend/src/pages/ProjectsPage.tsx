import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProjects, createProject, deleteProject } from '../api/projects'
import { Layout } from '../components/Layout'
import { Modal } from '../components/Modal'
import type { ProjectResponse } from '../types'

function ProjectCard({ project, onDelete }: { project: ProjectResponse; onDelete: (id: string) => void }) {
  const navigate = useNavigate()

  return (
    <div
      className="card"
      style={{
        padding: '16px 18px',
        cursor: 'pointer',
        borderLeft: '2px solid var(--bd)',
        transition: 'border-color 0.15s, border-left-color 0.15s',
        position: 'relative',
      }}
      onClick={() => navigate(`/projects/${project.id}`)}
      onMouseEnter={(e) => { (e.currentTarget as HTMLDivElement).style.borderLeftColor = 'var(--ac)' }}
      onMouseLeave={(e) => { (e.currentTarget as HTMLDivElement).style.borderLeftColor = 'var(--bd)' }}
    >
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '10px' }}>
        <div style={{ minWidth: 0, flex: 1 }}>
          <div style={{ color: '#eaeaf8', fontSize: '13px', fontWeight: '600', letterSpacing: '0.01em', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {project.name}
          </div>
          {project.description && (
            <div style={{ color: 'var(--tx2)', fontSize: '12px', marginTop: '4px', overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
              {project.description}
            </div>
          )}
        </div>
        <button
          onClick={(e) => { e.stopPropagation(); onDelete(project.id) }}
          className="btn-ghost"
          title="Delete project"
          style={{ flexShrink: 0 }}
        >
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round">
            <path d="M1 3h10M4.5 1.5h3M2 3l.6 7.5A1 1 0 003.6 11.5h4.8a1 1 0 001-.9L10 3" />
          </svg>
        </button>
      </div>
      <div style={{ marginTop: '12px', paddingTop: '10px', borderTop: '1px solid var(--bd)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ color: 'var(--tx3)', fontSize: '11px', letterSpacing: '0.04em' }}>
          {new Date(project.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })}
        </span>
        <span style={{ color: 'var(--ac)', fontSize: '10px', letterSpacing: '0.1em' }}>
          VIEW →
        </span>
      </div>
    </div>
  )
}

function CreateProjectModal({ onClose }: { onClose: () => void }) {
  const queryClient = useQueryClient()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [error, setError] = useState('')

  const mutation = useMutation({
    mutationFn: createProject,
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['projects'] }); onClose() },
    onError: () => setError('Failed to create project'),
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    setError('')
    mutation.mutate({ name, description })
  }

  return (
    <Modal title="New Project" onClose={onClose}>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {error && <div className="err-box">{error}</div>}
        <div>
          <label className="lbl">Name *</label>
          <input required value={name} onChange={(e) => setName(e.target.value)} className="field" placeholder="Project name" />
        </div>
        <div>
          <label className="lbl">Description</label>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} className="field" placeholder="Optional description" />
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

export function ProjectsPage() {
  const queryClient = useQueryClient()
  const [showCreate, setShowCreate] = useState(false)
  const { data: projects, isLoading, error } = useQuery({ queryKey: ['projects'], queryFn: getProjects })

  const deleteMutation = useMutation({
    mutationFn: deleteProject,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['projects'] }),
  })

  const empty = !isLoading && !error && projects?.length === 0

  return (
    <Layout>
      <div className="grid-bg" style={{ minHeight: '100%' }}>
        <div style={{ padding: '28px 32px', borderBottom: '1px solid var(--bd)', background: 'rgba(20,20,31,0.95)' }}>
          <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between' }}>
            <div>
              <div className="sh" style={{ marginBottom: '6px' }}>projects</div>
              <h1 style={{ fontSize: '20px', fontWeight: '700', color: '#eaeaf8', letterSpacing: '-0.01em' }}>
                Workspace
              </h1>
              <div style={{ color: 'var(--tx3)', fontSize: '11px', marginTop: '2px', letterSpacing: '0.04em' }}>
                {projects?.length ?? 0} project{(projects?.length ?? 0) !== 1 ? 's' : ''} found
              </div>
            </div>
            <button onClick={() => setShowCreate(true)} className="btn btn-primary">
              <svg width="11" height="11" viewBox="0 0 11 11" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                <path d="M5.5 1v9M1 5.5h9" />
              </svg>
              New Project
            </button>
          </div>
        </div>

        <div style={{ padding: '28px 32px' }}>
          {isLoading && (
            <div style={{ color: 'var(--tx3)', fontSize: '12px', letterSpacing: '0.06em' }}>
              loading<span className="cursor-blink">_</span>
            </div>
          )}

          {error && (
            <div className="err-box">Failed to load projects</div>
          )}

          {empty && (
            <div style={{ textAlign: 'center', padding: '64px 20px' }}>
              <div style={{ color: 'var(--tx3)', fontSize: '32px', marginBottom: '12px', letterSpacing: '-0.02em', fontWeight: '700' }}>
                []
              </div>
              <div style={{ color: 'var(--tx2)', fontSize: '13px' }}>No projects yet</div>
              <div style={{ color: 'var(--tx3)', fontSize: '12px', marginTop: '4px' }}>
                Create your first project to get started
              </div>
              <button onClick={() => setShowCreate(true)} className="btn btn-outline" style={{ marginTop: '20px' }}>
                + New Project
              </button>
            </div>
          )}

          {projects && projects.length > 0 && (
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
              gap: '12px',
            }}>
              {projects.map((project) => (
                <ProjectCard
                  key={project.id}
                  project={project}
                  onDelete={(id) => deleteMutation.mutate(id)}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {showCreate && <CreateProjectModal onClose={() => setShowCreate(false)} />}
    </Layout>
  )
}
