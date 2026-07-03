import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useGameStore } from '@/stores/gameStore'
import { useAuthStore } from '@/stores/authStore'
import { useWebSocket } from '@/hooks/useWebSocket'
import { getGameState } from '@/api/game'
import type { RowType } from '@/types/game'

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
import MedicOverlay from '@/components/board/overlays/MedicOverlay'

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

  const me = gameState?.me
  const opponent = gameState?.opponent
  const isMyTurn = gameState ? gameState.currentTurn === gameState.myTurn : false

  const playerId = user?.email

  const selectedCard = me?.hand.find((c) => c.id === selectedCardId) ?? null

  const canPlayOnRow = (row: RowType): boolean => {
    if (!selectedCard || !isMyTurn) return false
    if (selectedCard.ability === 'AGILE') return row === 'MELEE' || row === 'RANGED'
    return selectedCard.rowType === row
  }

  const handlePlayCard = (targetRow: RowType) => {
    if (!selectedCardId || !isMyTurn) return
    sendCommand({ commandType: 'PLAY_CARD', playerId, cardId: selectedCardId, targetRow })
    setSelectedCardId(null)
  }

  if (!connected || !gameState || !me || !opponent) {
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
          leaderUsed={opponent.leaderUsed}
          disabled
          onClick={() => {}}
          side="top"
        />
        <PlayerPanel player={opponent} isActive={!isMyTurn} side="top" />
        <div style={{ flex: 1 }} />
        <WeatherZone weatherEffects={gameState.weatherCards.map((c) => c.ability ?? '')} />
        <PassButton
          onClick={() => sendCommand({ commandType: 'PASS', playerId })}
          disabled={!isMyTurn || me.passed}
        />
        <div style={{ flex: 1 }} />
        <PlayerPanel player={me} isActive={isMyTurn} side="bottom" />
        <LeaderCard
          leaderUsed={me.leaderUsed}
          disabled={!isMyTurn || me.leaderUsed}
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

        {/* Opponent hand (face-down) */}
        <Hand opponentHandSize={opponent.handSize} isPlayer={false} interactive={false} />

        {/* Opponent rows: siege, ranged, melee (top to bottom) */}
        <BoardRow row={opponent.siegeRow} rowLabel="Cerco" rowType="SIEGE" side="opponent" interactive={false} />
        <BoardRow row={opponent.rangedRow} rowLabel="Distância" rowType="RANGED" side="opponent" interactive={false} />
        <BoardRow row={opponent.meleeRow} rowLabel="Corpo" rowType="MELEE" side="opponent" interactive={false} />

        <CentralDivider />

        {/* Player rows: melee, ranged, siege (top to bottom) */}
        <BoardRow
          row={me.meleeRow}
          rowLabel="Corpo"
          rowType="MELEE"
          side="player"
          interactive={isMyTurn}
          isPlacementTarget={canPlayOnRow('MELEE')}
          onRowClick={canPlayOnRow('MELEE') ? () => handlePlayCard('MELEE') : undefined}
        />
        <BoardRow
          row={me.rangedRow}
          rowLabel="Distância"
          rowType="RANGED"
          side="player"
          interactive={isMyTurn}
          isPlacementTarget={canPlayOnRow('RANGED')}
          onRowClick={canPlayOnRow('RANGED') ? () => handlePlayCard('RANGED') : undefined}
        />
        <BoardRow
          row={me.siegeRow}
          rowLabel="Cerco"
          rowType="SIEGE"
          side="player"
          interactive={isMyTurn}
          isPlacementTarget={canPlayOnRow('SIEGE')}
          onRowClick={canPlayOnRow('SIEGE') ? () => handlePlayCard('SIEGE') : undefined}
        />

        {/* Player hand */}
        <Hand
          cards={me.hand}
          isPlayer
          interactive={isMyTurn}
          selectedCardId={selectedCardId ?? undefined}
          onCardClick={(cardId) =>
            setSelectedCardId((prev) => (prev === cardId ? null : cardId))
          }
        />

        {/* Phase overlays */}
        {gameState.phase === 'REDRAW' && !me.mulliganConfirmed && (
          <MulliganOverlay
            hand={me.hand}
            mulligansRemaining={me.mulligansRemaining}
            onConfirm={(cardIds) => sendCommand({ commandType: 'CONFIRM_MULLIGAN', playerId, cardIds })}
          />
        )}
        {gameState.phase === 'ROUND_END' && (
          <RoundEndOverlay
            round={gameState.currentRound}
            myScore={me.score}
            opponentScore={opponent.score}
          />
        )}
        {gameState.pendingAbility === 'MEDIC_CHOICE' && isMyTurn && (
          <MedicOverlay
            graveyard={me.graveyard}
            onSelectCard={(cardId) => sendCommand({ commandType: 'RESOLVE_MEDIC', playerId, cardId })}
          />
        )}
        {gameState.phase === 'GAME_OVER' && (
          <GameOverOverlay
            myState={me}
            opponentState={opponent}
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
        <DeckStack count={opponent.deckSize} label="Deck" />
        <GraveyardStack count={opponent.graveyard.length} />
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
          <ControlBar onSurrender={() => {}} />
        </div>
        <GraveyardStack count={me.graveyard.length} />
        <DeckStack count={me.deckSize} label="Deck" />
      </div>
    </div>
  )
}
