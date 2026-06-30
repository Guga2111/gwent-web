import { useEffect } from 'react'

interface PassButtonProps {
  onClick: () => void
  disabled: boolean
}

export default function PassButton({ onClick, disabled }: PassButtonProps) {
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if (e.code === 'Space' && !disabled) {
        e.preventDefault()
        onClick()
      }
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [onClick, disabled])

  return (
    <div style={{ padding: '8px 12px', textAlign: 'center' }}>
      <button
        onClick={onClick}
        disabled={disabled}
        style={{
          width: '100%',
          padding: '8px 0',
          backgroundColor: 'var(--bg-medium)',
          border: '1px solid var(--border-gold)',
          borderRadius: 4,
          fontFamily: 'var(--font-heading)',
          fontSize: 14,
          color: disabled ? 'var(--text-muted)' : 'var(--gold)',
          cursor: disabled ? 'not-allowed' : 'pointer',
          opacity: disabled ? 0.5 : 1,
        }}
      >
        PASSAR
      </button>
      <div
        style={{
          fontSize: 10,
          fontFamily: 'var(--font-ui)',
          color: 'var(--text-muted)',
          marginTop: 4,
        }}
      >
        [ESPAÇO] segure
      </div>
    </div>
  )
}
