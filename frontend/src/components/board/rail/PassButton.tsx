import { useEffect } from 'react'
import { Button } from '@/components/ui/button'

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
      <Button
        onClick={onClick}
        disabled={disabled}
        variant="pass"
      >
        PASSAR
      </Button>
      <div
        className="text-[10px] text-text-muted mt-1 font-ui"
      >
        [ESPAÇO] segure
      </div>
    </div>
  )
}
