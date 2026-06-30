import { useState } from 'react'
import BoardCard from './BoardCard'
import CardBack from './CardBack'

interface HandProps {
  cardIds: string[]
  isPlayer: boolean
  onCardClick?: (cardId: string) => void
  interactive: boolean
}

export default function Hand({ cardIds: rawCardIds, isPlayer, onCardClick, interactive }: HandProps) {
  const [hoveredIndex, setHoveredIndex] = useState<number | null>(null)
  const cardIds = rawCardIds ?? []
  const count = cardIds.length

  const getRotation = (i: number) => {
    if (count <= 1) return 0
    return -8 + (16 * i) / (count - 1)
  }

  if (!isPlayer) {
    return (
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          padding: '6px 0',
          minHeight: 90,
        }}
      >
        {Array.from({ length: count }, (_, i) => (
          <div
            key={i}
            style={{
              marginLeft: i === 0 ? 0 : -12,
              transform: `rotate(${getRotation(i)}deg)`,
              zIndex: i,
            }}
          >
            <CardBack />
          </div>
        ))}
      </div>
    )
  }

  return (
    <div style={{ textAlign: 'center', padding: '4px 0' }}>
      <div
        style={{
          fontSize: 10,
          fontFamily: 'var(--font-ui)',
          color: 'var(--text-muted)',
          marginBottom: 2,
        }}
      >
        Sua mão &middot; {count}
      </div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          minHeight: 90,
        }}
      >
        {cardIds.map((cardId, i) => {
          const isHovered = hoveredIndex === i
          const rotation = isHovered ? 0 : getRotation(i)
          const lift = isHovered ? -20 : 0

          return (
            <div
              key={cardId}
              style={{
                marginLeft: i === 0 ? 0 : -12,
                transform: `rotate(${rotation}deg) translateY(${lift}px)`,
                transition: 'transform 0.15s, z-index 0s',
                zIndex: isHovered ? 100 : i,
                position: 'relative',
              }}
              onMouseEnter={() => setHoveredIndex(i)}
              onMouseLeave={() => setHoveredIndex(null)}
            >
              <BoardCard
                cardId={cardId}
                onClick={onCardClick ? () => onCardClick(cardId) : undefined}
                interactive={interactive}
              />
            </div>
          )
        })}
      </div>
    </div>
  )
}
