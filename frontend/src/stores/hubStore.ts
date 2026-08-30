import { create } from 'zustand'
import type { DeckDto } from '@/types/deck'

interface HubState {
  activeDeck: DeckDto | null
  setActiveDeck: (deck: DeckDto | null) => void
  setTab: ((tab: string) => void) | null
  registerSetTab: (fn: (tab: string) => void) => void
}

export const useHubStore = create<HubState>((set) => ({
  activeDeck: null,
  setActiveDeck: (deck) => set({ activeDeck: deck }),
  setTab: null,
  registerSetTab: (fn) => set({ setTab: fn }),
}))
