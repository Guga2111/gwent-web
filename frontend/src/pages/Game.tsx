import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useGameStore } from '@/stores/gameStore'
import { useAuthStore } from '@/stores/authStore'
import { useWebSocket } from '@/hooks/useWebSocket'
import { getGameState } from '@/api/game'
import type { Turn, RowType } from '@/types/game'

import PlayerPanel from '@/components/board/rail/PlayerPanel'
import LeaderCard from '@/components/board/card/LeaderCard'
import WeatherZone from '@/components/board/rail/WeatherZone'
import PassButton from '@/components/board/rail/PassButton'
import BoardRow from '@/components/board/row/BoardRow'
import Hand from '@/components/board/row/Hand'
import DeckStack from '@/components/board/rail/DeckStack'
import GraveyardStack from '@/components/board/rail/GraveyardStack'
import CentralDivider from '@/components/board/row/CentralDivider'
import ControlBar from '@/components/board/controls/ControlBar'
import MulliganOverlay from '@/components/board/overlays/MulliganOverlay'
import RoundEndOverlay from '@/components/board/overlays/RoundEndOverlay'
import GameOverOverlay from '@/components/board/overlays/GameOverOverlay'

export default function Game() {
  const { gameId } = useParams<{ gameId: string }>()
  const navigate = useNavigate()

  const gameState = useGameStore((s) => s.gameState)
  const connected = useGameStore((s) => s.connected)
  const error = useGameStore((s) => s.error)
  const setGameId = useGameStore((s) => s.setGameId)
  const setGameState = useGameStore((s) => s.setGameState)
  const setError = useGameStore((s) => s.setError)
  const reset = useGameStore((s) => s.reset)
  const user = useAuthStore((s) => s.user)

  const [selectedCardId, setSelectedCardId] = useState<string | null>(null)

  const { sendCommand } = useWebSocket(gameId ?? null)

  useEffect(() => {
    if (!gameId) return
    setGameId(gameId)
    getGameState(gameId).then(setGameState).catch(() => {})
    return () => reset()
  }, [gameId])

  // Auto-dismiss errors after 3s
  useEffect(() => {
    if (!error) return
    const t = setTimeout(() => setError(null), 3000)
    return () => clearTimeout(t)
  }, [error, setError])

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

  const playerId = user?.email

  const handlePlayCard = (targetRow: RowType) => {
    if (!selectedCardId || !isMyTurn) return
    sendCommand({ commandType: 'PLAY_CARD', playerId, cardId: selectedCardId, targetRow })
    setSelectedCardId(null)
  }

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
        <div style={{ flex: 1 }} />
        {/* TODO: pass real weatherEffects once GameStateDto includes them */}
        <WeatherZone weatherEffects={[]} />
        <PassButton
          onClick={() => sendCommand({ commandType: 'PASS', playerId })}
          disabled={!isMyTurn || myState.passed}
        />
        <div style={{ flex: 1 }} />
        <PlayerPanel player={myState} isActive={isMyTurn} side="bottom" />
        <LeaderCard
          leaderUsed={myState.leaderUsed}
          disabled={!isMyTurn || myState.leaderUsed}
          onClick={() => sendCommand({ commandType: 'USE_LEADER', playerId })}
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
        {/* Error notification */}
        {error && (
          <div
            style={{
              position: 'absolute',
              top: 12,
              left: '50%',
              transform: 'translateX(-50%)',
              zIndex: 20,
              backgroundColor: 'rgba(var(--red-rgb, 204,68,68), 0.92)',
              border: '1px solid rgba(255,100,100,0.4)',
              borderRadius: 6,
              padding: '8px 18px',
              fontFamily: 'var(--font-ui)',
              fontSize: 13,
              color: '#fff',
              pointerEvents: 'none',
            }}
          >
            {error}
          </div>
        )}

        {/* Opponent hand */}
        <Hand cardIds={opponentState.handCardIds} isPlayer={false} interactive={false} />

        {/* Opponent rows: siege, ranged, melee (top to bottom) */}
        <BoardRow cardIds={opponentState.siegeRowCardIds} rowLabel="Cerco" rowType="SIEGE" side="opponent" interactive={false} />
        <BoardRow cardIds={opponentState.rangedRowCardIds} rowLabel="Distância" rowType="RANGED" side="opponent" interactive={false} />
        <BoardRow cardIds={opponentState.meleeRowCardIds} rowLabel="Corpo" rowType="MELEE" side="opponent" interactive={false} />

        <CentralDivider />

        {/* Player rows: melee, ranged, siege (top to bottom) */}
        <BoardRow
          cardIds={myState.meleeRowCardIds}
          rowLabel="Corpo"
          rowType="MELEE"
          side="player"
          interactive={isMyTurn}
          isPlacementTarget={!!selectedCardId && isMyTurn}
          onRowClick={selectedCardId && isMyTurn ? () => handlePlayCard('MELEE') : undefined}
        />
        <BoardRow
          cardIds={myState.rangedRowCardIds}
          rowLabel="Distância"
          rowType="RANGED"
          side="player"
          interactive={isMyTurn}
          isPlacementTarget={!!selectedCardId && isMyTurn}
          onRowClick={selectedCardId && isMyTurn ? () => handlePlayCard('RANGED') : undefined}
        />
        <BoardRow
          cardIds={myState.siegeRowCardIds}
          rowLabel="Cerco"
          rowType="SIEGE"
          side="player"
          interactive={isMyTurn}
          isPlacementTarget={!!selectedCardId && isMyTurn}
          onRowClick={selectedCardId && isMyTurn ? () => handlePlayCard('SIEGE') : undefined}
        />

        {/* Player hand */}
        <Hand
          cardIds={myState.handCardIds}
          isPlayer
          interactive={isMyTurn}
          selectedCardId={selectedCardId ?? undefined}
          onCardClick={(cardId) =>
            setSelectedCardId((prev) => (prev === cardId ? null : cardId))
          }
        />

        {/* Phase overlays */}
        {gameState.phase === 'REDRAW' && !myState.mulliganConfirmed && (
          <MulliganOverlay
            handCardIds={myState.handCardIds}
            mulligansRemaining={myState.mulligansRemaining}
            onMulligan={(cardId) => sendCommand({ commandType: 'MULLIGAN', playerId, cardId })}
            onConfirm={() => sendCommand({ commandType: 'CONFIRM_MULLIGAN', playerId })}
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
        {/* TODO: replace handCardIds.length with deckSize once PlayerStateDto includes it */}
        <DeckStack count={opponentState.handCardIds?.length ?? 0} label="Mão" />
        <GraveyardStack count={opponentState.graveyardCardIds?.length ?? 0} />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <ControlBar onSurrender={() => {}} />
        </div>
        <GraveyardStack count={myState.graveyardCardIds?.length ?? 0} />
        {/* TODO: replace handCardIds.length with deckSize once PlayerStateDto includes it */}
        <DeckStack count={myState.handCardIds?.length ?? 0} label="Mão" />
      </div>
    </div>
  )
}
