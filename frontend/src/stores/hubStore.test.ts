import { useHubStore } from './hubStore'
import type { DeckDto } from '@/types/deck'

describe('hubStore', () => {
  beforeEach(() => {
    useHubStore.setState(useHubStore.getInitialState())
  })

  it('has correct initial state', () => {
    const state = useHubStore.getState()
    expect(state.activeDeck).toBeNull()
    expect(state.activeTab).toBe('home')
  })

  it('setActiveDeck updates the active deck', () => {
    const deck: DeckDto = { id: 'd-1', name: 'My Deck', faction: 'MONSTER', leaderId: 'l-1', cards: [], createdAt: '2024-01-01' }
    useHubStore.getState().setActiveDeck(deck)
    expect(useHubStore.getState().activeDeck).toEqual(deck)
  })

  it('setActiveTab updates the active tab', () => {
    useHubStore.getState().setActiveTab('deck')
    expect(useHubStore.getState().activeTab).toBe('deck')
  })
})
