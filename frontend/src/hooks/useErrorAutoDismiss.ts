import { useEffect } from 'react'
import { useGameStore } from '@/stores/gameStore'

export function useErrorAutoDismiss() {
  const error = useGameStore((s) => s.error)
  const setError = useGameStore((s) => s.setError)

  useEffect(() => {
    if (!error) return
    const t = setTimeout(() => setError(null), 3000)
    return () => clearTimeout(t)
  }, [error, setError])
}
