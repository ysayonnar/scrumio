import { Link, useLocation, useNavigate } from 'react-router-dom'
import { type ReactNode } from 'react'
import { useAuth } from '../context/AuthContext'

interface LayoutProps {
  children: ReactNode
}

export function Layout({ children }: LayoutProps) {
  const { logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const isActive = location.pathname.startsWith('/projects')

  return (
    <div style={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      <aside style={{
        width: '216px',
        flexShrink: 0,
        background: '#181828',
        borderRight: '1px solid var(--bd)',
        display: 'flex',
        flexDirection: 'column',
        overflowY: 'auto',
      }}>
        <div style={{ padding: '18px 14px 14px', borderBottom: '1px solid var(--bd)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{
              width: '30px', height: '30px',
              background: 'var(--ac)',
              borderRadius: '2px',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              flexShrink: 0,
            }}>
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <rect x="2" y="2" width="5" height="5" fill="#060608" />
                <rect x="9" y="2" width="5" height="5" fill="#060608" />
                <rect x="2" y="9" width="5" height="5" fill="#060608" />
                <rect x="9" y="9" width="5" height="2" fill="#060608" />
                <rect x="12" y="12" width="2" height="2" fill="#060608" />
              </svg>
            </div>
            <div>
              <div style={{ color: '#eaeaf8', fontSize: '13px', fontWeight: '700', letterSpacing: '0.06em' }}>
                SCRUMIO
              </div>
              <div style={{ color: 'var(--tx3)', fontSize: '10px', letterSpacing: '0.1em' }}>
                workspace
              </div>
            </div>
          </div>
        </div>

        <nav style={{ flex: 1, padding: '10px 8px' }}>
          <div style={{
            color: 'var(--tx3)', fontSize: '10px',
            letterSpacing: '0.12em', textTransform: 'uppercase',
            padding: '4px 10px 8px',
          }}>
            navigate
          </div>
          <Link to="/projects" className={`nav-item ${isActive ? 'active' : ''}`}>
            <svg width="13" height="13" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round">
              <path d="M1 4.5a1 1 0 011-1h2.5L6 2h7a1 1 0 011 1v8a1 1 0 01-1 1H2a1 1 0 01-1-1V4.5z" />
            </svg>
            Projects
          </Link>
        </nav>

        <div style={{ padding: '8px', borderTop: '1px solid var(--bd)' }}>
          <button onClick={handleLogout} className="nav-item">
            <svg width="13" height="13" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9.5 10l3-3-3-3M12.5 7H5.5M5.5 12.5H2a.5.5 0 01-.5-.5V2A.5.5 0 012 1.5h3.5" />
            </svg>
            Sign out
          </button>
        </div>
      </aside>

      <main style={{ flex: 1, overflowY: 'auto', background: 'var(--bg)' }}>
        {children}
      </main>
    </div>
  )
}
