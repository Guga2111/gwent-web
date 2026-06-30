import { useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useGameStore } from '@/stores/gameStore'
import { useAuthStore } from '@/stores/authStore'
import { useWebSocket } from '@/hooks/useWebSocket'
import type { Turn } from '@/types/game'

import PlayerPanel from '@/components/board/PlayerPanel'
import LeaderCard from '@/components/board/LeaderCard'
import WeatherZone from '@/components/board/WeatherZone'
import PassButton from '@/components/board/PassButton'
import BoardRow from '@/components/board/BoardRow'
import Hand from '@/components/board/Hand'
import DeckStack from '@/components/board/DeckStack'
import GraveyardStack from '@/components/board/GraveyardStack'
import CentralDivider from '@/components/board/CentralDivider'
import ControlBar from '@/components/board/ControlBar'
import MulliganOverlay from '@/components/board/MulliganOverlay'
import RoundEndOverlay from '@/components/board/RoundEndOverlay'
import GameOverOverlay from '@/components/board/GameOverOverlay'

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

  if (!connected || !gameState || !myState || !opponentState) {
    return (
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          height: '100vh',
          gap: 16,
          backgroundColor: 'var(--bg-darkest)',
        }}
      >
        <p style={{ color: 'var(--text-secondary)', fontFamily: 'var(--font-heading)' }}>
          {!connected ? 'Conectando...' : 'Aguardando estado do jogo...'}
        </p>
        <button
          onClick={() => navigate('/hub')}
          style={{
            fontSize: 14,
            textDecoration: 'underline',
            color: 'var(--text-muted)',
            background: 'none',
            border: 'none',
            cursor: 'pointer',
          }}
        >
          Voltar à Taverna
        </button>
      </div>
    )
  }

  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: '220px 1fr 96px',
        height: '100vh',
        backgroundColor: 'var(--bg-darkest)',
      }}
    >
      {/* Left Rail */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: 'var(--bg-dark)',
          borderRight: '1px solid var(--border-subtle)',
          overflow: 'hidden',
        }}
      >
        <LeaderCard
          leaderUsed={opponentState.leaderUsed}
          disabled
          onClick={() => {}}
          side="top"
        />
        <PlayerPanel player={opponentState} isActive={!isMyTurn} side="top" />
        <WeatherZone weatherEffects={[]} />
        <PassButton
          onClick={() => sendCommand({ commandType: 'PASS' })}
          disabled={!isMyTurn || myState.passed}
        />
        <div style={{ flex: 1 }} />
        <PlayerPanel player={myState} isActive={isMyTurn} side="bottom" />
        <LeaderCard
          leaderUsed={myState.leaderUsed}
          disabled={!isMyTurn || myState.leaderUsed}
          onClick={() => sendCommand({ commandType: 'USE_LEADER' })}
          side="bottom"
        />
      </div>

      {/* Center Board */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        {/* Opponent hand */}
        <Hand cardIds={opponentState.handCardIds} isPlayer={false} interactive={false} />

        {/* Opponent rows: siege, ranged, melee (top to bottom) */}
        <BoardRow cardIds={opponentState.siegeRowCardIds} rowLabel="Cerco" side="opponent" interactive={false} />
        <BoardRow cardIds={opponentState.rangedRowCardIds} rowLabel="Distância" side="opponent" interactive={false} />
        <BoardRow cardIds={opponentState.meleeRowCardIds} rowLabel="Corpo" side="opponent" interactive={false} />

        <CentralDivider />

        {/* Player rows: melee, ranged, siege (top to bottom) */}
        <BoardRow cardIds={myState.meleeRowCardIds} rowLabel="Corpo" side="player" interactive={isMyTurn} />
        <BoardRow cardIds={myState.rangedRowCardIds} rowLabel="Distância" side="player" interactive={isMyTurn} />
        <BoardRow cardIds={myState.siegeRowCardIds} rowLabel="Cerco" side="player" interactive={isMyTurn} />

        {/* Player hand */}
        <Hand
          cardIds={myState.handCardIds}
          isPlayer
          interactive={isMyTurn}
          onCardClick={(cardId) =>
            sendCommand({ commandType: 'PLAY_CARD', cardId })
          }
        />

        <ControlBar onSurrender={() => {}} />

        {/* Phase overlays */}
        {gameState.phase === 'REDRAW' && !myState.mulliganConfirmed && (
          <MulliganOverlay
            handCardIds={myState.handCardIds}
            mulligansRemaining={myState.mulligansRemaining}
            onMulligan={(cardId) => sendCommand({ commandType: 'MULLIGAN', cardId })}
            onConfirm={() => sendCommand({ commandType: 'CONFIRM_MULLIGAN' })}
          />
        )}
        {gameState.phase === 'ROUND_END' && (
          <RoundEndOverlay
            round={gameState.currentRound}
            myScore={myState.score}
            opponentScore={opponentState.score}
          />
        )}
        {gameState.phase === 'GAME_OVER' && (
          <GameOverOverlay
            myState={myState}
            opponentState={opponentState}
            onBack={() => navigate('/hub')}
          />
        )}
      </div>

      {/* Right Rail */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          backgroundColor: 'var(--bg-dark)',
          borderLeft: '1px solid var(--border-subtle)',
          padding: '12px 0',
          gap: 12,
        }}
      >
        <DeckStack count={opponentState.handCardIds?.length ?? 0} label="Mão" />
        <GraveyardStack count={opponentState.graveyardCardIds?.length ?? 0} />
        <div style={{ flex: 1 }} />
        <GraveyardStack count={myState.graveyardCardIds?.length ?? 0} />
        <DeckStack count={myState.handCardIds?.length ?? 0} label="Mão" />
      </div>
    </div>
  )
}
