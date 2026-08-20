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
    <div className="px-3 py-2 text-center">
      <button
        onClick={onClick}
        disabled={disabled}
        className="w-full py-2 bg-[var(--bg-medium)] border border-[var(--border-gold)] rounded text-sm"
        style={{
          fontFamily: 'var(--font-heading)',
          color: disabled ? 'var(--text-muted)' : 'var(--gold)',
          cursor: disabled ? 'not-allowed' : 'pointer',
          opacity: disabled ? 0.5 : 1,
        }}
      >
        PASSAR
      </button>
      <div
        className="text-[10px] text-[var(--text-muted)] mt-1"
        style={{ fontFamily: 'var(--font-ui)' }}
      >
        [ESPAÇO] segure
      </div>
    </div>
  )
}
