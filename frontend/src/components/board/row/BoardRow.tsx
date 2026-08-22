import { useEffect, useRef, useState } from 'react'
import type { BoardRowDto, CardDto, RowType } from '@/types/game'
import Card from '../card/Card'
import CountBadge from '@/components/ui/CountBadge'

interface BoardRowProps {
  row: BoardRowDto
  rowLabel: string
  rowType: RowType
  side: 'player' | 'opponent'
  onCardClick?: (cardId: string) => void
  onRowClick?: () => void
  onInspectCard?: (card: CardDto) => void
  isPlacementTarget?: boolean
  interactive: boolean
  suppressEnterCardId?: string | null
}

function useScoreFlash(score: number) {
  const [flash, setFlash] = useState(false)
  const prevRef = useRef(score)

  useEffect(() => {
    if (score !== prevRef.current) {
      setFlash(true)
      prevRef.current = score
      const t = setTimeout(() => setFlash(false), 400)
      return () => clearTimeout(t)
    }
  }, [score])

  return flash
}

export default function BoardRow({ row, rowType, side, onCardClick, onRowClick, onInspectCard, isPlacementTarget, interactive, suppressEnterCardId }: BoardRowProps) {
  const cards = row.cards ?? []
  const score = cards.reduce((sum, c) => sum + (c.currentPower ?? c.basePower ?? 0), 0)
  const scoreFlash = useScoreFlash(score)

  const rowTypeClass = `board-row--${rowType.toLowerCase()}`
  const weatherClass = row.weatherActive
    ? rowType === 'MELEE' ? 'board-row--frost'
    : rowType === 'RANGED' ? 'board-row--fog'
    : 'board-row--rain'
    : ''

  return (
    <div
      onClick={onRowClick}
      data-row-type={rowType}
      data-row-side={side}
      className={`flex items-center flex-1 relative min-h-0 py-1 board-row ${rowTypeClass}${isPlacementTarget ? ' board-row--target' : ''}${weatherClass ? ` ${weatherClass}` : ''}`}
      style={{ cursor: onRowClick ? 'pointer' : 'default' }}
    >
      {/* Score badge */}
      <div className={`absolute left-1 z-10${scoreFlash ? ' score-flash' : ''}`}>
        <CountBadge value={score} size={36} fontSize={13} />
      </div>

      {/* Horn slot */}
      <div
        className={`w-[var(--card-w)] h-[var(--card-h)] ml-7 shrink-0 flex items-center justify-center board-row__horn${row.hornActive ? ' board-row__horn--active' : ''}`}
      />

      {/* Cards area */}
      <div className="flex-1 flex justify-center gap-1.5 px-2 overflow-x-hidden items-center min-h-0">
        {cards.map((card) => (
          <div
            key={card.id}
            data-card-id={card.id}
            onClick={onInspectCard ? (e) => { e.stopPropagation(); onInspectCard(card); } : undefined}
            style={{ cursor: onInspectCard ? 'pointer' : undefined }}
          >
            <Card
              card={card}
              onClick={onCardClick ? () => onCardClick(card.id) : undefined}
              interactive={false}
              suppressEnterAnimation={card.id === suppressEnterCardId}
            />
          </div>
        ))}
      </div>

      {/* Right cap — mirrors horn slot width */}
      <div className="w-[calc(var(--card-w)+12px)] shrink-0 ml-3 mr-1" />

    </div>
  )
}
