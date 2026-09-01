import { useState, useEffect, useCallback } from 'react'
import { useMatchmakingStore } from '@/stores/matchmakingStore'
import { joinMatchmakingQueue, leaveMatchmakingQueue } from '@/api/matchmaking'

export function useMatchmaking(activeDeckId: string | null) {
  const [matchmakingOpen, setMatchmakingOpen] = useState(false)
  const setSearching = useMatchmakingStore((s) => s.setSearching)
  const setFound = useMatchmakingStore((s) => s.setFound)
  const setError = useMatchmakingStore((s) => s.setError)
  const reset = useMatchmakingStore((s) => s.reset)

  useEffect(() => {
    return () => { reset() }
  }, [reset])

  const searchOpponent = useCallback(async () => {
    if (!activeDeckId) return
    setSearching()
    setMatchmakingOpen(true)
    try {
      const matchedGameId = await joinMatchmakingQueue(activeDeckId)
      if (matchedGameId) {
        setFound(matchedGameId)
      }
    } catch {
      reset()
      setMatchmakingOpen(false)
      setError('Erro ao entrar na fila. Tente novamente.')
    }
  }, [activeDeckId, setFound, setSearching, reset, setError])

  const cancelMatchmaking = useCallback(async () => {
    await leaveMatchmakingQueue().catch(() => {})
    reset()
    setMatchmakingOpen(false)
  }, [reset])

  return { matchmakingOpen, searchOpponent, cancelMatchmaking }
}
