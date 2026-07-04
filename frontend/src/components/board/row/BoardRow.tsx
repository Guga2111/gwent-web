import type { BoardRowDto, RowType } from '@/types/game'
import Card from '../card/Card'
import CountBadge from '@/components/ui/CountBadge'

interface BoardRowProps {
  row: BoardRowDto
  rowLabel: string
  rowType: RowType
  side: 'player' | 'opponent'
  onCardClick?: (cardId: string) => void
  onRowClick?: () => void
  isPlacementTarget?: boolean
  interactive: boolean
}

const ROW_TINTS: Record<string, string> = {
  Corpo: 'rgba(80, 50, 20, 0.3)',
  Distância: 'rgba(30, 60, 20, 0.3)',
  Cerco: 'rgba(20, 40, 70, 0.3)',
}

export default function BoardRow({ row, rowLabel, onCardClick, onRowClick, isPlacementTarget, interactive }: BoardRowProps) {
  const cards = row.cards ?? []
  const score = cards.reduce((sum, c) => sum + (c.basePower ?? 0), 0)

  return (
    <div
      onClick={onRowClick}
      className="flex items-center flex-1 relative border-b border-[var(--border-subtle)] min-h-0 py-1"
      style={{
        backgroundColor: ROW_TINTS[rowLabel] ?? 'transparent',
        outline: isPlacementTarget ? '2px solid var(--gold)' : 'none',
        outlineOffset: -2,
        cursor: onRowClick ? 'pointer' : 'default',
        transition: 'outline 0.15s',
      }}
    >
      {/* Score badge */}
      <div className="absolute left-1 z-10">
        <CountBadge value={score} size={36} fontSize={13} />
      </div>

      {/* Horn slot */}
      <div
        className="w-[34px] h-[72px] ml-7 border border-dashed border-[var(--border-subtle)] rounded-sm shrink-0 flex items-center justify-center"
        style={{
          backgroundColor: row.hornActive ? 'rgba(218, 165, 32, 0.3)' : 'var(--bg-medium)',
        }}
      />

      {/* Row label */}
      <div
        className="absolute right-[52px] text-[9px] text-[var(--text-muted)] opacity-50 pointer-events-none"
        style={{ fontFamily: 'var(--font-ui)' }}
      >
        {rowLabel}
      </div>

      {/* Cards area */}
      <div className="flex-1 flex justify-center gap-1.5 px-2 overflow-x-hidden items-center min-h-0">
        {cards.map((card) => (
          <Card
            key={card.id}
            card={card}
            onClick={onCardClick ? () => onCardClick(card.id) : undefined}
            interactive={interactive}
          />
        ))}
      </div>

      {/* Right cap */}
      <div className="w-[46px] h-[72px] border border-dashed border-[var(--border-subtle)] rounded-sm shrink-0 ml-3 mr-1" />
    </div>
  )
}
