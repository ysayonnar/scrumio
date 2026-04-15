import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password)
      navigate('/projects')
    } catch {
      setError('Invalid email or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', fontFamily: "'DM Sans', sans-serif" }}>

      {/* ── Left: Brand panel ── */}
      <div style={{
        width: '44%',
        background: 'var(--ac)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        padding: '48px',
        position: 'relative',
        overflow: 'hidden',
        flexShrink: 0,
      }}>
        {/* Decorative grid */}
        <div style={{
          position: 'absolute', inset: 0, pointerEvents: 'none',
          backgroundImage: `
            linear-gradient(rgba(255,255,255,0.08) 1px, transparent 1px),
            linear-gradient(90deg, rgba(255,255,255,0.08) 1px, transparent 1px)
          `,
          backgroundSize: '48px 48px',
        }}/>
        {/* Large decorative circle */}
        <div style={{
          position: 'absolute',
          bottom: '-80px', right: '-80px',
          width: '360px', height: '360px',
          borderRadius: '50%',
          background: 'rgba(255,255,255,0.07)',
          pointerEvents: 'none',
        }}/>
        <div style={{
          position: 'absolute',
          top: '-40px', left: '-40px',
          width: '200px', height: '200px',
          borderRadius: '50%',
          background: 'rgba(255,255,255,0.06)',
          pointerEvents: 'none',
        }}/>

        <div style={{ position: 'relative' }}>
          <div style={{
            display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '60px',
          }}>
            <div style={{
              width: '36px', height: '36px',
              background: 'rgba(255,255,255,0.2)',
              borderRadius: '8px',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <svg width="20" height="20" viewBox="0 0 18 18" fill="none">
                <rect x="2" y="2" width="6" height="6" rx="1" fill="white"/>
                <rect x="10" y="2" width="6" height="6" rx="1" fill="white" opacity="0.7"/>
                <rect x="2" y="10" width="6" height="6" rx="1" fill="white" opacity="0.7"/>
                <rect x="10" y="10" width="3" height="6" rx="1" fill="white" opacity="0.4"/>
              </svg>
            </div>
            <span style={{ fontFamily: 'Syne, sans-serif', fontWeight: 800, fontSize: '18px', color: '#fff', letterSpacing: '-0.01em' }}>
              Scrumio
            </span>
          </div>

          <h1 style={{
            fontFamily: 'Syne, sans-serif',
            fontWeight: 800,
            fontSize: '48px',
            color: '#fff',
            lineHeight: 1.05,
            letterSpacing: '-0.03em',
            marginBottom: '20px',
          }}>
            Ship faster.<br/>Sprint smarter.
          </h1>
          <p style={{ color: 'rgba(255,255,255,0.75)', fontSize: '16px', lineHeight: 1.6, maxWidth: '280px' }}>
            Your team's sprint command center — track work, run standups, and ship with confidence.
          </p>
        </div>

        <div style={{ position: 'relative', display: 'flex', gap: '24px' }}>
          {[['Sprints', 'Plan & track'], ['Tickets', 'Manage work'], ['Board', 'Visualize']].map(([title, sub]) => (
            <div key={title}>
              <div style={{ color: '#fff', fontWeight: 600, fontSize: '14px' }}>{title}</div>
              <div style={{ color: 'rgba(255,255,255,0.65)', fontSize: '12px', marginTop: '2px' }}>{sub}</div>
            </div>
          ))}
        </div>
      </div>

      {/* ── Right: Form panel ── */}
      <div style={{
        flex: 1,
        background: '#fff',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '48px',
      }}>
        <div style={{ width: '100%', maxWidth: '360px', animation: 'slideUp 0.35s ease forwards' }}>

          <div style={{ marginBottom: '36px' }}>
            <h2 style={{
              fontFamily: 'Syne, sans-serif',
              fontWeight: 800,
              fontSize: '28px',
              color: 'var(--tx)',
              letterSpacing: '-0.02em',
              marginBottom: '8px',
            }}>
              Welcome back
            </h2>
            <p style={{ color: 'var(--tx2)', fontSize: '14px' }}>
              Sign in to your workspace
            </p>
          </div>

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {error && <div className="err-box">{error}</div>}

            <div>
              <label className="lbl">Email address</label>
              <input
                type="email" required value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="field"
                placeholder="you@example.com"
                autoComplete="email"
              />
            </div>

            <div>
              <label className="lbl">Password</label>
              <input
                type="password" required value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="field"
                placeholder="••••••••••"
                autoComplete="current-password"
              />
            </div>

            <button
              type="submit" disabled={loading}
              className="btn btn-primary"
              style={{ width: '100%', padding: '11px', fontSize: '15px', marginTop: '4px', borderRadius: '10px' }}
            >
              {loading ? 'Signing in…' : 'Sign in →'}
            </button>
          </form>

          <p style={{ marginTop: '24px', textAlign: 'center', fontSize: '14px', color: 'var(--tx2)' }}>
            Don't have an account?{' '}
            <Link to="/register" style={{ color: 'var(--ac)', fontWeight: 500 }}>
              Create one
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
