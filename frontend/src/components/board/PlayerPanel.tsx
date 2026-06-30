import type { PlayerStateDto } from '@/types/game'

interface PlayerPanelProps {
  player: PlayerStateDto
  isActive: boolean
  side: 'top' | 'bottom'
}

export default function PlayerPanel({ player, isActive, side }: PlayerPanelProps) {
  const maxLives = 2
  const gems = Array.from({ length: maxLives }, (_, i) => i < player.lives)

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 6,
        padding: '12px 8px',
        borderLeft: '2px solid transparent',
        ...(isActive
          ? { animation: 'tb-turn 2s ease-in-out infinite', borderRadius: 4 }
          : {}),
      }}
    >
      {/* Avatar */}
      <div
        style={{
          width: 48,
          height: 48,
          borderRadius: '50%',
          backgroundColor: 'var(--bg-medium)',
          border: '2px solid var(--border-gold)',
        }}
      />

      {/* Name */}
      <div
        style={{
          fontFamily: 'var(--font-ui)',
          fontSize: 12,
          color: 'var(--text-primary)',
          maxWidth: 180,
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          textAlign: 'center',
        }}
      >
        {player.playerId}
      </div>

      {/* Passed label */}
      {player.passed && (
        <div
          style={{
            fontFamily: 'var(--font-body)',
            fontSize: 11,
            color: 'var(--text-muted)',
            fontStyle: 'italic',
          }}
        >
          PASSOU
        </div>
      )}

      {/* Lives gems */}
      <div style={{ display: 'flex', gap: 6 }}>
        {gems.map((alive, i) => (
          <div
            key={i}
            style={{
              width: 12,
              height: 12,
              borderRadius: '50%',
              backgroundColor: alive ? 'var(--gold)' : 'transparent',
              border: alive ? '1px solid var(--gold-light)' : '1px solid var(--border-subtle)',
            }}
          />
        ))}
      </div>

      {/* Score */}
      <div
        style={{
          width: 48,
          height: 48,
          borderRadius: '50%',
          backgroundColor: side === 'bottom' ? 'var(--gold-dark)' : 'var(--bg-medium)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontFamily: 'var(--font-heading)',
          fontSize: 20,
          fontWeight: 700,
          color: 'var(--gold-light)',
          border: '2px solid var(--border-gold)',
        }}
      >
        {player.score}
      </div>
    </div>
  )
}
