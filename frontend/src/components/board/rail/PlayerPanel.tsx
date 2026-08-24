import type { PlayerStateDto, OpponentStateDto, Faction } from '@/types/game'
import CountBadge from '@/components/ui/CountBadge'
import { factionTokens, emblems } from '@/components/board/card/CardBack'
import { Layers } from 'lucide-react'

const FACTION_DISPLAY_NAMES: Record<Faction, string> = {
  NORTHERN_REALMS: 'Reinos do Norte',
  NILFGAARD: 'Nilfgaard',
  MONSTER: 'Monstros',
  SCOIATAEL: "Scoia'tael",
  SKELLIGE: 'Skellige',
}

interface PlayerPanelProps {
  player: PlayerStateDto | OpponentStateDto
  isActive: boolean
  side: 'top' | 'bottom'
}

function getHandCount(player: PlayerStateDto | OpponentStateDto): number {
  return 'hand' in player ? player.hand.length : player.handSize
}

export default function PlayerPanel({ player, isActive, side }: PlayerPanelProps) {
  const maxLives = 2
  const gems = Array.from({ length: maxLives }, (_, i) => i < player.lives)
  const faction = player.leader.faction
  const tokens = factionTokens[faction] ?? factionTokens.NORTHERN_REALMS
  const Emblem = emblems[faction]
  const username = player.playerId.split('@')[0]
  const handCount = getHandCount(player)

  return (
    <div className={`player-panel${isActive ? ' player-panel--active' : ''}`}>
      {/* Avatar + Faction Emblem */}
      <div className="player-panel__avatar-group">
        <div className="player-panel__avatar" />
        <div className="player-panel__faction-emblem" style={{ color: tokens.primary }}>
          {Emblem && <Emblem />}
        </div>
      </div>

      {/* Name + Faction + Passed */}
      <div className="player-panel__info">
        <div className="player-panel__name">{username}</div>
        <div className="player-panel__faction-name">{FACTION_DISPLAY_NAMES[faction]}</div>
        {player.passed && <div className="player-panel__passed">PASSOU</div>}
      </div>

      {/* Hand Count + Lives */}
      <div className="player-panel__stats">
        <div className="player-panel__hand-count">
          <Layers size={16} style={{ color: 'var(--text-muted)' }} />
          <span>{handCount}</span>
        </div>
        <div className="player-panel__lives">
          {gems.map((alive, i) => (
            <div
              key={i}
              className={`player-panel__gem ${alive ? 'player-panel__gem--alive' : 'player-panel__gem--lost'}`}
            />
          ))}
        </div>
      </div>

      {/* Score */}
      <CountBadge
        value={player.score}
        size={44}
        fontSize={18}
        bg={side === 'bottom' ? 'var(--gold-dark)' : 'var(--bg-medium)'}
      />
    </div>
  )
}
