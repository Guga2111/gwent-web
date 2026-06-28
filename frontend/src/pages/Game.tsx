import { useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useGameStore } from '@/stores/gameStore'
import { useAuthStore } from '@/stores/authStore'
import { useWebSocket } from '@/hooks/useWebSocket'
import type { Turn } from '@/types/game'

export default function Game() {
  const { gameId } = useParams<{ gameId: string }>()
  const navigate = useNavigate()

  const gameState = useGameStore((s) => s.gameState)
  const connected = useGameStore((s) => s.connected)
  const setGameId = useGameStore((s) => s.setGameId)
  const reset = useGameStore((s) => s.reset)
  const user = useAuthStore((s) => s.user)

  const { sendCommand } = useWebSocket(gameId ?? null)

  useEffect(() => {
    if (gameId) setGameId(gameId)
    return () => reset()
  }, [gameId, setGameId, reset])

  // Determine which player we are
  const myTurn: Turn | null = gameState
    ? gameState.player1.playerId === user?.email
      ? 'PLAYER_1'
      : gameState.player2.playerId === user?.email
        ? 'PLAYER_2'
        : null
    : null

  const myState = myTurn === 'PLAYER_1' ? gameState?.player1 : gameState?.player2
  const opponentState = myTurn === 'PLAYER_1' ? gameState?.player2 : gameState?.player1
  const isMyTurn = gameState?.currentTurn === myTurn

  if (!connected || !gameState) {
    return (
      <div className="flex h-full flex-col items-center justify-center gap-4">
        <p style={{ color: 'var(--text-secondary)', fontFamily: 'var(--font-heading)' }}>
          {!connected ? 'Connecting...' : 'Waiting for game state...'}
        </p>
        <button
          onClick={() => navigate('/lobby')}
          className="text-sm underline"
          style={{ color: 'var(--text-muted)' }}
        >
          Back to Lobby
        </button>
      </div>
    )
  }

  return (
    <div className="flex h-full flex-col" style={{ backgroundColor: 'var(--bg-darkest)' }}>
      {/* Top bar */}
      <div
        className="flex items-center justify-between px-4 py-2 text-sm"
        style={{ backgroundColor: 'var(--bg-dark)', borderBottom: '1px solid var(--border-subtle)' }}
      >
        <span style={{ color: 'var(--text-secondary)' }}>
          Round {gameState.round} &middot; {gameState.phase}
        </span>
        <span style={{ color: isMyTurn ? 'var(--gold-light)' : 'var(--text-muted)' }}>
          {isMyTurn ? 'Your turn' : "Opponent's turn"}
        </span>
      </div>

      {/* Board area — placeholder */}
      <div className="flex flex-1 items-center justify-center gap-12">
        {/* Opponent side */}
        <div className="text-center">
          <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>
            {opponentState?.playerId ?? 'Opponent'}
          </p>
          <p className="text-2xl font-bold" style={{ color: 'var(--gold)', fontFamily: 'var(--font-heading)' }}>
            {opponentState?.totalScore ?? 0}
          </p>
          <p className="text-xs" style={{ color: 'var(--text-muted)' }}>
            Rounds: {opponentState?.roundsWon ?? 0} &middot; Hand: {opponentState?.hand.length ?? 0}
            {opponentState?.passed ? ' · PASSED' : ''}
          </p>
        </div>

        {/* VS divider */}
        <div
          className="text-3xl font-bold"
          style={{ fontFamily: 'var(--font-display)', color: 'var(--gold-dim)' }}
        >
          VS
        </div>

        {/* Player side */}
        <div className="text-center">
          <p className="text-sm" style={{ color: 'var(--text-secondary)' }}>
            {myState?.playerId ?? 'You'}
          </p>
          <p className="text-2xl font-bold" style={{ color: 'var(--gold)', fontFamily: 'var(--font-heading)' }}>
            {myState?.totalScore ?? 0}
          </p>
          <p className="text-xs" style={{ color: 'var(--text-muted)' }}>
            Rounds: {myState?.roundsWon ?? 0} &middot; Hand: {myState?.hand.length ?? 0}
            {myState?.passed ? ' · PASSED' : ''}
          </p>
        </div>
      </div>

      {/* Hand + Controls */}
      <div
        className="flex flex-col gap-2 px-4 py-3"
        style={{ backgroundColor: 'var(--bg-dark)', borderTop: '1px solid var(--border-subtle)' }}
      >
        {/* Hand */}
        <div className="flex gap-2 overflow-x-auto pb-1">
          {myState?.hand.map((card) => (
            <button
              key={card.id}
              onClick={() =>
                sendCommand({
                  commandType: 'PLAY_CARD',
                  cardId: card.id,
                  row: card.row,
                })
              }
              disabled={!isMyTurn || myState.passed}
              className="flex-shrink-0 rounded border px-3 py-2 text-left text-xs transition-colors disabled:opacity-40"
              style={{
                backgroundColor: 'var(--bg-card)',
                borderColor: 'var(--border-gold)',
                color: 'var(--text-primary)',
                minWidth: '100px',
              }}
            >
              <div className="font-semibold" style={{ color: 'var(--gold-light)' }}>
                {card.name}
              </div>
              <div style={{ color: 'var(--text-muted)' }}>
                {card.strength} &middot; {card.row}
              </div>
            </button>
          ))}
        </div>

        {/* Actions */}
        <div className="flex gap-3">
          <button
            onClick={() => sendCommand({ commandType: 'PASS' })}
            disabled={!isMyTurn || (myState?.passed ?? false)}
            className="rounded border px-4 py-1 text-sm transition-colors disabled:opacity-40"
            style={{
              borderColor: 'var(--border-gold)',
              color: 'var(--gold)',
              backgroundColor: 'var(--bg-medium)',
            }}
          >
            Pass
          </button>
          <button
            onClick={() => sendCommand({ commandType: 'USE_LEADER' })}
            disabled={!isMyTurn || (myState?.leaderUsed ?? true)}
            className="rounded border px-4 py-1 text-sm transition-colors disabled:opacity-40"
            style={{
              borderColor: 'var(--border-gold)',
              color: 'var(--gold)',
              backgroundColor: 'var(--bg-medium)',
            }}
          >
            Leader Ability
          </button>
        </div>
      </div>
    </div>
  )
}
