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
      {/* Sidebar */}
      <aside style={{
        width: '240px',
        flexShrink: 0,
        background: '#fff',
        borderRight: '1px solid var(--bd)',
        display: 'flex',
        flexDirection: 'column',
        overflowY: 'auto',
      }}>
        {/* Logo */}
        <div style={{ padding: '20px 20px 16px', borderBottom: '1px solid var(--bd)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{
              width: '34px', height: '34px',
              background: 'var(--ac)',
              borderRadius: '8px',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              flexShrink: 0,
              boxShadow: '0 2px 8px rgba(232,69,10,0.3)',
            }}>
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <rect x="2" y="2" width="6" height="6" rx="1" fill="white"/>
                <rect x="10" y="2" width="6" height="6" rx="1" fill="white" opacity="0.7"/>
                <rect x="2" y="10" width="6" height="6" rx="1" fill="white" opacity="0.7"/>
                <rect x="10" y="10" width="3" height="6" rx="1" fill="white" opacity="0.4"/>
                <rect x="13" y="13" width="3" height="3" rx="0.5" fill="white" opacity="0.4"/>
              </svg>
            </div>
            <div>
              <div style={{
                fontFamily: 'Syne, sans-serif',
                fontWeight: 800,
                fontSize: '16px',
                color: 'var(--tx)',
                letterSpacing: '-0.01em',
              }}>
                Scrumio
              </div>
              <div style={{ fontSize: '11px', color: 'var(--tx3)', marginTop: '-1px' }}>
                workspace
              </div>
            </div>
          </div>
        </div>

        {/* Nav */}
        <nav style={{ flex: 1, padding: '12px 10px' }}>
          <div style={{
            fontSize: '11px', fontWeight: 600, color: 'var(--tx3)',
            letterSpacing: '0.1em', textTransform: 'uppercase',
            padding: '4px 12px 8px',
          }}>
            Main
          </div>

          <Link to="/projects" className={`nav-item ${isActive ? 'active' : ''}`}>
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M1.5 5a1 1 0 011-1h3l1.5-1.5H14a1 1 0 011 1v8a1 1 0 01-1 1H2.5a1 1 0 01-1-1V5z"/>
            </svg>
            Projects
          </Link>
        </nav>

        {/* Bottom */}
        <div style={{ padding: '10px', borderTop: '1px solid var(--bd)' }}>
          <button onClick={handleLogout} className="nav-item" style={{ width: '100%' }}>
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M11 10.5l3-3-3-3M14 7.5H6.5M6.5 13H3a.5.5 0 01-.5-.5v-10A.5.5 0 013 2h3.5"/>
            </svg>
            Sign out
          </button>
        </div>
      </aside>

      {/* Content */}
      <main style={{ flex: 1, overflowY: 'auto', background: 'var(--bg)' }}>
        {children}
      </main>
    </div>
  )
}
