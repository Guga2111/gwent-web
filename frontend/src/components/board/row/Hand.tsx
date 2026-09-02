import { useState } from 'react'
import Card from '../card/Card'
import type { CardDto, Faction } from '@/types/game'

interface HandProps {
  cards?: CardDto[]
  opponentHandSize?: number
  isPlayer: boolean
  onCardClick?: (cardId: string) => void
  interactive: boolean
  selectedCardId?: string
  faction?: Faction
  departingCardId?: string | null
}

export default function Hand({ cards: rawCards, opponentHandSize, isPlayer, onCardClick, selectedCardId, faction, departingCardId }: HandProps) {
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null)
  const cards = rawCards ?? []
  const count = isPlayer ? cards.length : (opponentHandSize ?? 0)

  const getRotation = (i: number) => {
    if (count <= 1) return 0
    return -8 + (16 * i) / (count - 1)
  }

  if (!isPlayer) {
    return (
      <div className="hand-fan hand-fan-mirrored flex justify-center py-1.5" style={{ minHeight: 'var(--card-h)' }}>
        {Array.from({ length: count }, (_, i) => {
          const rotation = count <= 1 ? 0 : 8 - (16 * i) / (count - 1)
          const offsetY = count <= 1 ? 0 : Math.abs(i - (count - 1) / 2) * 3
          return (
            <div
              key={i}
              className="hand-card"
              style={{
                transform: `rotate(${rotation}deg) translateY(${offsetY}px)`,
                zIndex: i,
              }}
            >
              <Card faction={faction} />
            </div>
          )
        })}
      </div>
    )
  }

  return (
    <div className="text-center py-1">
      <div
        className="text-[10px] text-text-muted mb-0.5 font-ui"
      >
        Sua mão &middot; {count}
      </div>
      <div className="hand-fan flex justify-center" style={{ minHeight: 'var(--card-h)' }}>
        {cards.map((card, i) => {
          const isHovered = hoveredIndex === i
          const isSelected = selectedCardId === card.id
          const rotation = isHovered || isSelected ? 0 : getRotation(i)
          const lift = isHovered || isSelected ? -20 : 0

          const isDeparting = departingCardId === card.id

          return (
            <div
              key={card.id}
              data-card-id={card.id}
              className={`hand-card${isSelected ? ' hand-card--selected' : ''}${isDeparting ? ' hand-card--departing' : ''}`}
              style={{
                transform: `rotate(${rotation}deg) translateY(${lift}px)`,
                zIndex: isSelected ? 101 : isHovered ? 100 : i,
                position: 'relative',
                filter: isSelected ? 'drop-shadow(0 0 8px var(--gold))' : 'none',
              }}
              onMouseEnter={() => setHoveredIndex(i)}
              onMouseLeave={() => setHoveredIndex(null)}
            >
              <Card
                card={card}
                onClick={onCardClick ? () => onCardClick(card.id) : undefined}
                interactive={true}
              />
            </div>
          )
        })}
      </div>
    </div>
  )
}
