import type { PlayerStateDto, OpponentStateDto } from '@/types/game'
import CountBadge from '@/components/ui/CountBadge'
import { getFactionConfig } from '@/utils/factionConfig'
import { Layers } from 'lucide-react'


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
  const { tokens, Emblem, label: factionName } = getFactionConfig(faction)
  const username = player.playerId.split('@')[0]
  const handCount = getHandCount(player)

  return (
    <div className={`player-panel${isActive ? ' player-panel--active' : ''}`}>
      {/* Avatar + Faction Emblem */}
      <div className="flex flex-col items-center relative shrink-0">
        <div className="player-panel__avatar" />
        <div className="player-panel__faction-emblem" style={{ color: tokens.primary }}>
          {Emblem && <Emblem />}
        </div>
      </div>

      {/* Name + Faction + Passed */}
      <div className="flex flex-col gap-0.5 min-w-0 flex-1">
        <div className="player-panel__name">{username}</div>
        <div className="player-panel__faction-name">{factionName}</div>
        {player.passed && <div className="font-ui text-[10px] uppercase text-text-muted">PASSOU</div>}
      </div>

      {/* Hand Count + Lives */}
      <div className="flex items-center gap-2.5 shrink-0">
        <div className="flex items-center gap-1 font-ui text-[15px] text-text-secondary">
          <Layers size={16} style={{ color: 'var(--text-muted)' }} />
          <span>{handCount}</span>
        </div>
        <div className="flex gap-[5px]">
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
