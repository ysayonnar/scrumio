import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProjects, createProject, deleteProject } from '../api/projects'
import { Layout } from '../components/Layout'
import { Modal } from '../components/Modal'
import type { ProjectResponse } from '../types'

const PROJECT_COLORS = [
  '#e8450a', '#2563eb', '#16a34a', '#7c3aed',
  '#b45309', '#0891b2', '#be185d', '#059669',
]

function projectColor(id: string) {
  let h = 0
  for (let i = 0; i < id.length; i++) h = (h * 31 + id.charCodeAt(i)) & 0xffffffff
  return PROJECT_COLORS[Math.abs(h) % PROJECT_COLORS.length]
}

function ProjectCard({ project, onDelete }: { project: ProjectResponse; onDelete: (id: string) => void }) {
  const navigate = useNavigate()
  const color = projectColor(project.id)
  const initial = project.name.charAt(0).toUpperCase()

  return (
    <div
      className="card"
      style={{ padding: '20px', cursor: 'pointer' }}
      onClick={() => navigate(`/projects/${project.id}`)}
    >
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '12px', marginBottom: '14px' }}>
        <div style={{
          width: '44px', height: '44px', borderRadius: '10px',
          background: color, display: 'flex', alignItems: 'center', justifyContent: 'center',
          flexShrink: 0,
          boxShadow: `0 2px 8px ${color}40`,
        }}>
          <span style={{ fontFamily: 'Syne, sans-serif', fontWeight: 800, fontSize: '18px', color: '#fff' }}>
            {initial}
          </span>
        </div>
        <button
          onClick={(e) => { e.stopPropagation(); onDelete(project.id) }}
          className="btn-ghost"
          title="Delete"
        >
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round">
            <path d="M1.5 3.5h11M5 2h4M3 3.5l.7 8.5A1 1 0 004.7 13h4.6a1 1 0 001-.9L11 3.5"/>
          </svg>
        </button>
      </div>

      <h3 style={{ fontFamily: 'Syne, sans-serif', fontWeight: 700, fontSize: '15px', color: 'var(--tx)', letterSpacing: '-0.01em', marginBottom: '4px' }}>
        {project.name}
      </h3>
      {project.description && (
        <p style={{ fontSize: '13px', color: 'var(--tx2)', lineHeight: 1.5, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {project.description}
        </p>
      )}

      <div style={{ marginTop: '16px', paddingTop: '12px', borderTop: '1px solid var(--bd)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: '12px', color: 'var(--tx3)' }}>
          {new Date(project.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
        </span>
        <span style={{ fontSize: '12px', color: 'var(--tx3)', display: 'flex', alignItems: 'center', gap: '4px' }}>
          Open
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M2.5 6h7M6.5 3l3 3-3 3"/>
          </svg>
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

  return (
    <Modal title="New Project" onClose={onClose}>
      <form onSubmit={(e) => { e.preventDefault(); setError(''); mutation.mutate({ name, description }) }} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {error && <div className="err-box">{error}</div>}
        <div>
          <label className="lbl">Project name *</label>
          <input required value={name} onChange={(e) => setName(e.target.value)} className="field" placeholder="e.g. Mobile App v2" />
        </div>
        <div>
          <label className="lbl">Description</label>
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} className="field" placeholder="What is this project about?" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', paddingTop: '4px' }}>
          <button type="button" onClick={onClose} className="btn btn-outline">Cancel</button>
          <button type="submit" disabled={mutation.isPending} className="btn btn-primary">
            {mutation.isPending ? 'Creating…' : 'Create project'}
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

  return (
    <Layout>
      {/* Page header */}
      <div style={{ background: '#fff', borderBottom: '1px solid var(--bd)', padding: '28px 32px' }}>
        <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between' }}>
          <div>
            <div className="sh" style={{ marginBottom: '6px' }}>Workspace</div>
            <h1 style={{ fontFamily: 'Syne, sans-serif', fontWeight: 800, fontSize: '26px', color: 'var(--tx)', letterSpacing: '-0.02em' }}>
              Projects
            </h1>
            <p style={{ fontSize: '14px', color: 'var(--tx2)', marginTop: '2px' }}>
              {projects?.length ?? 0} project{(projects?.length ?? 0) !== 1 ? 's' : ''}
            </p>
          </div>
          <button onClick={() => setShowCreate(true)} className="btn btn-primary">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
              <path d="M7 1v12M1 7h12"/>
            </svg>
            New project
          </button>
        </div>
      </div>

      {/* Content */}
      <div style={{ padding: '28px 32px' }}>
        {isLoading && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '16px' }}>
            {[1,2,3].map((i) => (
              <div key={i} style={{ height: '168px', borderRadius: '12px' }} className="skeleton"/>
            ))}
          </div>
        )}

        {error && <div className="err-box">Failed to load projects</div>}

        {!isLoading && !error && projects?.length === 0 && (
          <div style={{ textAlign: 'center', padding: '80px 20px' }}>
            <div style={{
              width: '64px', height: '64px', borderRadius: '16px',
              background: 'var(--ac-l)', margin: '0 auto 16px',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--ac)" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z"/>
              </svg>
            </div>
            <h3 style={{ fontFamily: 'Syne, sans-serif', fontWeight: 700, fontSize: '18px', color: 'var(--tx)', marginBottom: '6px' }}>
              No projects yet
            </h3>
            <p style={{ color: 'var(--tx2)', fontSize: '14px', marginBottom: '24px' }}>
              Create your first project to get started
            </p>
            <button onClick={() => setShowCreate(true)} className="btn btn-primary">
              Create a project
            </button>
          </div>
        )}

        {projects && projects.length > 0 && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '16px' }}>
            {projects.map((p) => (
              <ProjectCard key={p.id} project={p} onDelete={(id) => deleteMutation.mutate(id)} />
            ))}
          </div>
        )}
      </div>

      {showCreate && <CreateProjectModal onClose={() => setShowCreate(false)} />}
    </Layout>
  )
}
