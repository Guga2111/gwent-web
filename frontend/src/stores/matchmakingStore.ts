import { create } from 'zustand'

export type MatchmakingPhase = 'idle' | 'searching' | 'found'

interface MatchmakingState {
  phase: MatchmakingPhase
  matchedGameId: string | null
  error: string | null
  setSearching: () => void
  setFound: (gameId: string) => void
  setError: (msg: string) => void
  reset: () => void
}

export const useMatchmakingStore = create<MatchmakingState>((set) => ({
  phase: 'idle',
  matchedGameId: null,
  error: null,
  setSearching: () => set({ phase: 'searching', matchedGameId: null, error: null }),
  setFound: (gameId) => set({ phase: 'found', matchedGameId: gameId }),
  setError: (msg) => set({ phase: 'idle', error: msg }),
  reset: () => set({ phase: 'idle', matchedGameId: null, error: null }),
}))
