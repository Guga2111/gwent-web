import { create } from 'zustand'
import type { GameStateDto } from '@/types/game'

interface GameStore {
  gameId: string | null
  gameState: GameStateDto | null
  connected: boolean
  error: string | null
  opponentConnected: boolean
  forfeitDeadlineUtc: number | null

  setGameId: (id: string) => void
  setGameState: (state: GameStateDto) => void
  setConnected: (connected: boolean) => void
  setError: (error: string | null) => void
  setOpponentPresence: (connected: boolean, deadline: number | null) => void
  reset: () => void
}

export const useGameStore = create<GameStore>((set) => ({
  gameId: null,
  gameState: null,
  connected: false,
  error: null,
  opponentConnected: true,
  forfeitDeadlineUtc: null,

  setGameId: (id) => set({ gameId: id }),
  setGameState: (state) => set({ gameState: state }),
  setConnected: (connected) => set({ connected }),
  setError: (error) => set({ error }),
  setOpponentPresence: (connected, deadline) => set({ opponentConnected: connected, forfeitDeadlineUtc: deadline }),
  reset: () => set({ gameId: null, gameState: null, connected: false, error: null, opponentConnected: true, forfeitDeadlineUtc: null }),
}))
