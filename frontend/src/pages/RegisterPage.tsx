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
    if (password.length < 8) { setError('Password must be at least 8 characters'); return }
    setLoading(true)
    try {
      await register(email, password)
      navigate('/login')
    } catch (err: unknown) {
      const status = (err as { response?: { status?: number } })?.response?.status
      setError(status === 409 ? 'Email already registered' : 'Registration failed. Try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', fontFamily: "'DM Sans', sans-serif" }}>

      {/* ── Left: Brand panel ── */}
      <div style={{
        width: '44%',
        background: 'var(--tx)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        padding: '48px',
        position: 'relative',
        overflow: 'hidden',
        flexShrink: 0,
      }}>
        <div style={{
          position: 'absolute', inset: 0, pointerEvents: 'none',
          backgroundImage: `radial-gradient(rgba(255,255,255,0.06) 1px, transparent 1px)`,
          backgroundSize: '28px 28px',
        }}/>
        <div style={{ position: 'absolute', bottom: '-60px', right: '-60px', width: '300px', height: '300px', borderRadius: '50%', background: 'rgba(232,69,10,0.12)', pointerEvents: 'none' }}/>

        <div style={{ position: 'relative' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '60px' }}>
            <div style={{ width: '36px', height: '36px', background: 'var(--ac)', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <svg width="20" height="20" viewBox="0 0 18 18" fill="none">
                <rect x="2" y="2" width="6" height="6" rx="1" fill="white"/>
                <rect x="10" y="2" width="6" height="6" rx="1" fill="white" opacity="0.7"/>
                <rect x="2" y="10" width="6" height="6" rx="1" fill="white" opacity="0.7"/>
              </svg>
            </div>
            <span style={{ fontFamily: 'Syne, sans-serif', fontWeight: 800, fontSize: '18px', color: '#fff', letterSpacing: '-0.01em' }}>
              Scrumio
            </span>
          </div>

          <h1 style={{ fontFamily: 'Syne, sans-serif', fontWeight: 800, fontSize: '44px', color: '#fff', lineHeight: 1.08, letterSpacing: '-0.03em', marginBottom: '20px' }}>
            Join your<br/>team today.
          </h1>
          <p style={{ color: 'rgba(255,255,255,0.6)', fontSize: '15px', lineHeight: 1.7, maxWidth: '270px' }}>
            Create an account and start collaborating on sprints, tickets, and releases.
          </p>
        </div>

        <div style={{ position: 'relative' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'rgba(255,255,255,0.5)', fontSize: '13px' }}>
            <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--ac)' }}/>
            Your display name is auto-generated and can be changed after sign-up.
          </div>
        </div>
      </div>

      {/* ── Right: Form panel ── */}
      <div style={{ flex: 1, background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '48px' }}>
        <div style={{ width: '100%', maxWidth: '360px', animation: 'slideUp 0.35s ease forwards' }}>

          <div style={{ marginBottom: '36px' }}>
            <h2 style={{ fontFamily: 'Syne, sans-serif', fontWeight: 800, fontSize: '28px', color: 'var(--tx)', letterSpacing: '-0.02em', marginBottom: '8px' }}>
              Create account
            </h2>
            <p style={{ color: 'var(--tx2)', fontSize: '14px' }}>Free forever. No credit card needed.</p>
          </div>

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {error && <div className="err-box">{error}</div>}
            <div>
              <label className="lbl">Email address</label>
              <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} className="field" placeholder="you@example.com" autoComplete="email"/>
            </div>
            <div>
              <label className="lbl">Password</label>
              <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} className="field" placeholder="Minimum 8 characters" autoComplete="new-password"/>
            </div>
            <button type="submit" disabled={loading} className="btn btn-primary" style={{ width: '100%', padding: '11px', fontSize: '15px', marginTop: '4px', borderRadius: '10px' }}>
              {loading ? 'Creating account…' : 'Create account →'}
            </button>
          </form>

          <p style={{ marginTop: '24px', textAlign: 'center', fontSize: '14px', color: 'var(--tx2)' }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: 'var(--ac)', fontWeight: 500 }}>Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
