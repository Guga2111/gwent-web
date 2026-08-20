import { useState } from 'react'
import Card from '../card/Card'
import type { CardDto } from '@/types/game'

interface HandProps {
  cards?: CardDto[]
  opponentHandSize?: number
  isPlayer: boolean
  onCardClick?: (cardId: string) => void
  interactive: boolean
  selectedCardId?: string
}

export default function Hand({ cards: rawCards, opponentHandSize, isPlayer, onCardClick, interactive, selectedCardId }: HandProps) {
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null)
  const cards = rawCards ?? []
  const count = isPlayer ? cards.length : (opponentHandSize ?? 0)

  const getRotation = (i: number) => {
    if (count <= 1) return 0
    return -8 + (16 * i) / (count - 1)
  }

  if (!isPlayer) {
    return (
      <div className="flex justify-center py-1.5 min-h-[114px]">
        {Array.from({ length: count }, (_, i) => (
          <div
            key={i}
            style={{
              marginLeft: i === 0 ? 0 : -12,
              transform: `rotate(${getRotation(i)}deg)`,
              zIndex: i,
            }}
          >
            <Card />
          </div>
        ))}
      </div>
    )
  }

  return (
    <div className="text-center py-1">
      <div
        className="text-[10px] text-[var(--text-muted)] mb-0.5"
        style={{ fontFamily: 'var(--font-ui)' }}
      >
        Sua mão &middot; {count}
      </div>
      <div className="flex justify-center min-h-[114px]">
        {cards.map((card, i) => {
          const isHovered = hoveredIndex === i
          const isSelected = selectedCardId === card.id
          const rotation = isHovered || isSelected ? 0 : getRotation(i)
          const lift = isHovered || isSelected ? -20 : 0

          return (
            <div
              key={card.id}
              style={{
                marginLeft: i === 0 ? 0 : -12,
                transform: `rotate(${rotation}deg) translateY(${lift}px)`,
                transition: 'transform 0.15s, z-index 0s',
                zIndex: isSelected ? 101 : isHovered ? 100 : i,
                position: 'relative',
                filter: isSelected ? 'drop-shadow(0 0 6px var(--gold))' : 'none',
              }}
              onMouseEnter={() => setHoveredIndex(i)}
              onMouseLeave={() => setHoveredIndex(null)}
            >
              <Card
                card={card}
                onClick={onCardClick ? () => onCardClick(card.id) : undefined}
                interactive={interactive}
              />
            </div>
          )
        })}
      </div>
    </div>
  )
}
