import { useState, useEffect, useCallback } from 'react'
import { useMatchmakingStore } from '@/stores/matchmakingStore'
import { joinMatchmakingQueue, leaveMatchmakingQueue } from '@/api/matchmaking'

export function useMatchmaking(activeDeckId: string | null) {
  const [matchmakingOpen, setMatchmakingOpen] = useState(false)
  const setSearching = useMatchmakingStore((s) => s.setSearching)
  const setFound = useMatchmakingStore((s) => s.setFound)
  const reset = useMatchmakingStore((s) => s.reset)

  useEffect(() => {
    return () => { reset() }
  }, [reset])

  const searchOpponent = useCallback(async () => {
    if (!activeDeckId) return
    try {
      const matchedGameId = await joinMatchmakingQueue(activeDeckId)
      if (matchedGameId) {
        setFound(matchedGameId)
      } else {
        setSearching()
      }
      setMatchmakingOpen(true)
    } catch (err: any) {
      if (err?.response?.status === 409) {
        setSearching()
        setMatchmakingOpen(true)
      }
    }
  }, [activeDeckId, setFound, setSearching])

  const cancelMatchmaking = useCallback(async () => {
    await leaveMatchmakingQueue().catch(() => {})
    reset()
    setMatchmakingOpen(false)
  }, [reset])

  return { matchmakingOpen, searchOpponent, cancelMatchmaking }
}
