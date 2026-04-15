import { type ReactNode } from 'react'

interface ModalProps {
  title: string
  onClose: () => void
  children: ReactNode
}

export function Modal({ title, onClose, children }: ModalProps) {
  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 50,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }}>
      <div
        style={{ position: 'absolute', inset: 0, background: 'rgba(24,24,26,0.45)', backdropFilter: 'blur(6px)' }}
        onClick={onClose}
      />
      <div style={{
        position: 'relative',
        background: '#fff',
        borderRadius: '16px',
        width: '100%',
        maxWidth: '500px',
        margin: '0 16px',
        maxHeight: '92vh',
        overflowY: 'auto',
        boxShadow: '0 24px 60px rgba(0,0,0,0.14), 0 6px 20px rgba(0,0,0,0.08)',
        animation: 'slideUp 0.22s ease forwards',
      }}>
        {/* Header */}
        <div style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '20px 24px 16px',
          borderBottom: '1px solid var(--bd)',
        }}>
          <h2 style={{
            fontFamily: 'Syne, sans-serif',
            fontWeight: 700,
            fontSize: '17px',
            color: 'var(--tx)',
            letterSpacing: '-0.01em',
          }}>
            {title}
          </h2>
          <button onClick={onClose} className="btn-ghost" style={{ padding: '6px', borderRadius: '8px' }}>
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <path d="M2 2l12 12M14 2L2 14"/>
            </svg>
          </button>
        </div>
        {/* Body */}
        <div style={{ padding: '20px 24px 24px' }}>
          {children}
        </div>
      </div>
    </div>
  )
}
