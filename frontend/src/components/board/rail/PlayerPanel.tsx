import type { PlayerStateDto, OpponentStateDto } from '@/types/game'
import CountBadge from '@/components/ui/CountBadge'

interface PlayerPanelProps {
  player: PlayerStateDto | OpponentStateDto
  isActive: boolean
  side: 'top' | 'bottom'
}

export default function PlayerPanel({ player, isActive, side }: PlayerPanelProps) {
  const maxLives = 2
  const gems = Array.from({ length: maxLives }, (_, i) => i < player.lives)

  return (
    <div
      className="flex flex-col items-center gap-1.5 px-2 py-3 rounded w-full"
      style={isActive ? { animation: 'tb-turn 2s ease-in-out infinite' } : undefined}
    >
      {/* Avatar */}
      <div className="w-12 h-12 rounded-full bg-[var(--bg-medium)] border-2 border-[var(--border-gold)]" />

      {/* Name */}
      <div
        className="text-xs text-[var(--text-primary)] max-w-[180px] overflow-hidden text-ellipsis whitespace-nowrap text-center"
        style={{ fontFamily: 'var(--font-ui)' }}
      >
        {player.playerId}
      </div>

      {/* Passed label */}
      {player.passed && (
        <div
          className="text-[11px] italic text-[var(--text-muted)]"
          style={{ fontFamily: 'var(--font-body)' }}
        >
          PASSOU
        </div>
      )}

      {/* Lives gems */}
      <div className="flex gap-1.5">
        {gems.map((alive, i) => (
          <div
            key={i}
            className="w-3 h-3 rounded-full"
            style={{
              backgroundColor: alive ? 'var(--gold)' : 'transparent',
              border: alive ? '1px solid var(--gold-light)' : '1px solid var(--border-subtle)',
            }}
          />
        ))}
      </div>

      {/* Score */}
      <CountBadge
        value={player.score}
        size={48}
        fontSize={20}
        bg={side === 'bottom' ? 'var(--gold-dark)' : 'var(--bg-medium)'}
      />
    </div>
  )
}
