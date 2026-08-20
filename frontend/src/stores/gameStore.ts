import { create } from 'zustand'
import type { GameStateDto } from '@/types/game'

interface GameStore {
  gameId: string | null
  gameState: GameStateDto | null
  connected: boolean
  error: string | null

  setGameId: (id: string) => void
  setGameState: (state: GameStateDto) => void
  setConnected: (connected: boolean) => void
  setError: (error: string | null) => void
  reset: () => void
}

export const useGameStore = create<GameStore>((set) => ({
  gameId: null,
  gameState: null,
  connected: false,
  error: null,

  setGameId: (id) => set({ gameId: id }),
  setGameState: (state) => set({ gameState: state }),
  setConnected: (connected) => set({ connected }),
  setError: (error) => set({ error }),
  reset: () => set({ gameId: null, gameState: null, connected: false, error: null }),
}))
