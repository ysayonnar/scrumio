import { type ReactNode } from 'react'

interface ModalProps {
  title: string
  onClose: () => void
  children: ReactNode
}

export function Modal({ title, onClose, children }: ModalProps) {
  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 50, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div
        style={{ position: 'absolute', inset: 0, background: 'rgba(4,4,10,0.88)', backdropFilter: 'blur(4px)' }}
        onClick={onClose}
      />
      <div style={{
        position: 'relative',
        background: '#1c1c2c',
        border: '1px solid #3c3c5c',
        borderRadius: '2px',
        width: '100%',
        maxWidth: '480px',
        margin: '0 16px',
        maxHeight: '90vh',
        overflowY: 'auto',
        boxShadow: '0 32px 80px rgba(0,0,0,0.8), 0 0 0 1px rgba(200,255,74,0.04)',
        animation: 'slideUp 0.2s ease forwards',
      }}>
        <div style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '13px 20px',
          borderBottom: '1px solid #2e2e48',
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ color: 'var(--ac)', fontSize: '11px', fontWeight: '600' }}>//</span>
            <span style={{ color: '#e4e4f4', fontSize: '13px', fontWeight: '600', letterSpacing: '0.01em' }}>{title}</span>
          </div>
          <button onClick={onClose} className="btn-ghost" style={{ padding: '4px' }}>
            <svg width="13" height="13" viewBox="0 0 13 13" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round">
              <path d="M1 1l11 11M12 1L1 12" />
            </svg>
          </button>
        </div>
        <div style={{ padding: '20px' }}>
          {children}
        </div>
      </div>
    </div>
  )
}
