import type { CardDto } from '@/types/game'
import CardArtImage from './CardArtImage'

interface LeaderCardProps {
  leader: CardDto
  leaderUsed: boolean
  onClick: () => void
  side?: 'top' | 'bottom'
}

export default function LeaderCard({ leader, leaderUsed, onClick, side = 'bottom' }: LeaderCardProps) {
  const rotation = side === 'top' ? -4 : 4
  const shortName = leader.name.includes(':') ? leader.name.split(':')[0].trim() : leader.name

  return (
    <div className="flex justify-center py-4">
      <div
        onClick={onClick}
        className="leader-card-base"
        style={{
          transform: `rotate(${rotation}deg)`,
          cursor: 'pointer',
          opacity: leaderUsed ? 0.5 : 1,
          filter: leaderUsed ? 'saturate(0.3)' : undefined,
          position: 'relative',
          overflow: 'hidden',
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.transform = `rotate(${rotation}deg) translateY(-4px)`
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.transform = `rotate(${rotation}deg)`
        }}
      >
        <CardArtImage cardId={leader.id} faction={leader.faction} />
        <div
          className="text-[9px] text-center text-[var(--gold-light)] px-1"
          style={{ fontFamily: 'var(--font-heading)', position: 'relative', zIndex: 2 }}
        >
          {shortName}
        </div>
      </div>
    </div>
  )
}
