import { useGameStore } from './gameStore'
import { makeGameState } from '@/test/factories'

describe('gameStore', () => {
  beforeEach(() => {
    useGameStore.setState(useGameStore.getInitialState())
  })

  it('has correct initial state', () => {
    const state = useGameStore.getState()
    expect(state.gameId).toBeNull()
    expect(state.gameState).toBeNull()
    expect(state.connected).toBe(false)
    expect(state.error).toBeNull()
    expect(state.opponentConnected).toBe(true)
    expect(state.forfeitDeadlineUtc).toBeNull()
  })

  it('setGameId updates gameId', () => {
    useGameStore.getState().setGameId('abc-123')
    expect(useGameStore.getState().gameId).toBe('abc-123')
  })

  it('setGameState updates gameState', () => {
    const gs = makeGameState()
    useGameStore.getState().setGameState(gs)
    expect(useGameStore.getState().gameState).toBe(gs)
  })

  it('setConnected updates connected', () => {
    useGameStore.getState().setConnected(true)
    expect(useGameStore.getState().connected).toBe(true)
  })

  it('setError updates error', () => {
    useGameStore.getState().setError('something broke')
    expect(useGameStore.getState().error).toBe('something broke')
  })

  it('setOpponentPresence updates opponentConnected and forfeitDeadlineUtc', () => {
    useGameStore.getState().setOpponentPresence(false, 1234567890)
    const state = useGameStore.getState()
    expect(state.opponentConnected).toBe(false)
    expect(state.forfeitDeadlineUtc).toBe(1234567890)
  })

  it('reset returns to initial state', () => {
    useGameStore.getState().setGameId('abc')
    useGameStore.getState().setConnected(true)
    useGameStore.getState().setError('err')
    useGameStore.getState().setOpponentPresence(false, 999)
    useGameStore.getState().reset()

    const state = useGameStore.getState()
    expect(state.gameId).toBeNull()
    expect(state.gameState).toBeNull()
    expect(state.connected).toBe(false)
    expect(state.error).toBeNull()
    expect(state.opponentConnected).toBe(true)
    expect(state.forfeitDeadlineUtc).toBeNull()
  })
})
