import { create } from 'zustand'
import type { DeckDto } from '@/types/deck'

interface HubState {
  activeDeck: DeckDto | null
  setActiveDeck: (deck: DeckDto | null) => void
}

export const useHubStore = create<HubState>((set) => ({
  activeDeck: null,
  setActiveDeck: (deck) => set({ activeDeck: deck }),
}))
