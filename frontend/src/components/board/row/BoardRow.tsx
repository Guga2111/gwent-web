import type { RowType } from '@/types/game'
import Card from '../card/Card'
import CountBadge from '@/components/ui/CountBadge'

interface BoardRowProps {
  cardIds: string[]
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

export default function BoardRow({ cardIds, rowLabel, onCardClick, onRowClick, isPlacementTarget, interactive }: BoardRowProps) {
  const cards = cardIds ?? []
  const score = cards.length // TODO: replace with sum of card powers once cards carry power data

  return (
    <div
      onClick={onRowClick}
      style={{
        display: 'flex',
        alignItems: 'center',
        flex: 1,
        position: 'relative',
        backgroundColor: ROW_TINTS[rowLabel] ?? 'transparent',
        borderBottom: '1px solid var(--border-subtle)',
        outline: isPlacementTarget ? '2px solid var(--gold)' : 'none',
        outlineOffset: -2,
        cursor: onRowClick ? 'pointer' : 'default',
        padding: '4px 0',
        minHeight: 0,
        transition: 'outline 0.15s',
      }}
    >
      {/* Score badge */}
      <div style={{ position: 'absolute', left: 4, zIndex: 2 }}>
        <CountBadge value={score} size={36} fontSize={13} />
      </div>

      {/* Horn slot */}
      <div
        style={{
          width: 34,
          height: 72,
          marginLeft: 28,
          border: '1px dashed var(--border-subtle)',
          borderRadius: 3,
          backgroundColor: 'var(--bg-medium)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0,
        }}
      />

      {/* Row label */}
      <div
        style={{
          position: 'absolute',
          right: 52,
          fontSize: 9,
          fontFamily: 'var(--font-ui)',
          color: 'var(--text-muted)',
          opacity: 0.5,
          pointerEvents: 'none',
        }}
      >
        {rowLabel}
      </div>

      {/* Cards area */}
      <div
        style={{
          flex: 1,
          display: 'flex',
          justifyContent: 'center',
          gap: 6,
          padding: '0 8px',
          overflowX: 'hidden',
          alignItems: 'center',
          minHeight: 0,
        }}
      >
        {cards.map((cardId) => (
          <Card
            key={cardId}
            cardId={cardId}
            onClick={onCardClick ? () => onCardClick(cardId) : undefined}
            interactive={interactive}
          />
        ))}
      </div>

      {/* Right cap */}
      <div
        style={{
          width: 46,
          height: 72,
          border: '1px dashed var(--border-subtle)',
          borderRadius: 3,
          flexShrink: 0,
          marginLeft: 12,
          marginRight: 4,
        }}
      />
    </div>
  )
}
