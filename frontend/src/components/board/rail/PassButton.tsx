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
    <div className="px-4 py-2 text-center">
      <button
        onClick={onClick}
        disabled={disabled}
        className="pass-button"
        style={{
          opacity: disabled ? 0.4 : 1,
          cursor: disabled ? 'not-allowed' : 'pointer',
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
