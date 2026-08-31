import { create } from 'zustand'
import type { DeckDto } from '@/types/deck'

export type TabId = 'home' | 'deck' | 'shop' | 'rank' | 'profile'

interface HubState {
  activeDeck: DeckDto | null
  setActiveDeck: (deck: DeckDto | null) => void
  activeTab: TabId
  setActiveTab: (tab: TabId) => void
}

export const useHubStore = create<HubState>((set) => ({
  activeDeck: null,
  setActiveDeck: (deck) => set({ activeDeck: deck }),
  activeTab: 'home',
  setActiveTab: (tab) => set({ activeTab: tab }),
}))
