import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    if (password.length < 8) {
      setError('Password must be at least 8 characters')
      return
    }
    setLoading(true)
    try {
      await register(email, password)
      navigate('/login')
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status
      if (status === 409) {
        setError('Email already registered')
      } else {
        setError('Registration failed. Try again.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      minHeight: '100vh',
      background: '#14141f',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '20px',
      backgroundImage: `
        radial-gradient(ellipse at 85% 50%, rgba(200,255,74,0.04) 0%, transparent 55%),
        linear-gradient(rgba(200,255,74,0.018) 1px, transparent 1px),
        linear-gradient(90deg, rgba(200,255,74,0.018) 1px, transparent 1px)
      `,
      backgroundSize: 'auto, 40px 40px, 40px 40px',
    }}>
      <div style={{ width: '100%', maxWidth: '340px', animation: 'slideUp 0.4s ease forwards' }}>

        <div style={{ marginBottom: '28px', textAlign: 'center' }}>
          <div style={{
            display: 'inline-flex',
            width: '44px', height: '44px',
            background: 'var(--ac)',
            alignItems: 'center', justifyContent: 'center',
            borderRadius: '2px',
            marginBottom: '14px',
          }}>
            <svg width="24" height="24" viewBox="0 0 16 16" fill="none">
              <rect x="2" y="2" width="5" height="5" fill="#060608" />
              <rect x="9" y="2" width="5" height="5" fill="#060608" />
              <rect x="2" y="9" width="5" height="5" fill="#060608" />
              <rect x="9" y="9" width="5" height="2" fill="#060608" />
              <rect x="12" y="12" width="2" height="2" fill="#060608" />
            </svg>
          </div>
          <div style={{ color: '#eaeaf8', fontSize: '20px', fontWeight: '700', letterSpacing: '0.08em' }}>
            SCRUMIO
            <span className="cursor-blink" style={{ color: 'var(--ac)', marginLeft: '2px' }}>_</span>
          </div>
          <div style={{ color: 'var(--tx3)', fontSize: '10px', letterSpacing: '0.12em', textTransform: 'uppercase', marginTop: '3px' }}>
            create account
          </div>
        </div>

        <div style={{
          background: '#1c1c2c',
          border: '1px solid #2e2e48',
          borderRadius: '2px',
          padding: '24px',
          boxShadow: '0 24px 64px rgba(0,0,0,0.6)',
        }}>
          <div style={{
            display: 'flex', alignItems: 'center', gap: '8px',
            marginBottom: '20px',
            paddingBottom: '14px',
            borderBottom: '1px solid #2e2e48',
          }}>
            <span style={{ color: 'var(--ac)', fontSize: '11px', fontWeight: '600' }}>//</span>
            <span style={{ color: 'var(--tx2)', fontSize: '11px', letterSpacing: '0.1em', textTransform: 'uppercase' }}>
              register
            </span>
          </div>

          <div className="info-box" style={{ marginBottom: '16px' }}>
            Display name auto-generated. Update it after login.
          </div>

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            {error && <div className="err-box">{error}</div>}

            <div>
              <label className="lbl">Email</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="field"
                placeholder="user@domain.com"
                autoComplete="email"
              />
            </div>

            <div>
              <label className="lbl">Password</label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="field"
                placeholder="min 8 characters"
                autoComplete="new-password"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary"
              style={{ width: '100%', marginTop: '4px', padding: '10px' }}
            >
              {loading ? 'creating...' : '→  create account'}
            </button>
          </form>

          <div style={{ marginTop: '18px', paddingTop: '14px', borderTop: '1px solid #2e2e48', textAlign: 'center' }}>
            <span style={{ color: 'var(--tx3)', fontSize: '11px' }}>Have an account? </span>
            <Link to="/login" style={{ color: 'var(--ac)', fontSize: '11px' }}>
              Sign in →
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
