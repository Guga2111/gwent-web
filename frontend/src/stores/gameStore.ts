import { create } from 'zustand'
import type { GameStateDto } from '@/types/game'

interface GameStore {
  gameId: string | null
  gameState: GameStateDto | null
  connected: boolean

  setGameId: (id: string) => void
  setGameState: (state: GameStateDto) => void
  setConnected: (connected: boolean) => void
  reset: () => void
}

export const useGameStore = create<GameStore>((set) => ({
  gameId: null,
  gameState: null,
  connected: false,

  setGameId: (id) => set({ gameId: id }),
  setGameState: (state) => set({ gameState: state }),
  setConnected: (connected) => set({ connected }),
  reset: () => set({ gameId: null, gameState: null, connected: false }),
}))
