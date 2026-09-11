import { useMatchmakingStore } from './matchmakingStore'

describe('matchmakingStore', () => {
  beforeEach(() => {
    useMatchmakingStore.setState(useMatchmakingStore.getInitialState())
  })

  it('has correct initial state', () => {
    const state = useMatchmakingStore.getState()
    expect(state.phase).toBe('idle')
    expect(state.matchedGameId).toBeNull()
    expect(state.error).toBeNull()
  })

  it('setSearching transitions to searching phase', () => {
    useMatchmakingStore.getState().setSearching()
    const state = useMatchmakingStore.getState()
    expect(state.phase).toBe('searching')
    expect(state.matchedGameId).toBeNull()
    expect(state.error).toBeNull()
  })

  it('setFound transitions to found phase with gameId', () => {
    useMatchmakingStore.getState().setFound('game-42')
    const state = useMatchmakingStore.getState()
    expect(state.phase).toBe('found')
    expect(state.matchedGameId).toBe('game-42')
  })

  it('setError transitions back to idle with error message', () => {
    useMatchmakingStore.getState().setSearching()
    useMatchmakingStore.getState().setError('timeout')
    const state = useMatchmakingStore.getState()
    expect(state.phase).toBe('idle')
    expect(state.error).toBe('timeout')
  })

  it('reset returns to initial state', () => {
    useMatchmakingStore.getState().setFound('game-42')
    useMatchmakingStore.getState().reset()
    const state = useMatchmakingStore.getState()
    expect(state.phase).toBe('idle')
    expect(state.matchedGameId).toBeNull()
    expect(state.error).toBeNull()
  })
})
