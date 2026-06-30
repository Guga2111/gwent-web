import BoardCard from './BoardCard'

interface BoardRowProps {
  cardIds: string[]
  rowLabel: string
  side: 'player' | 'opponent'
  onCardClick?: (cardId: string) => void
  interactive: boolean
}

const ROW_TINTS: Record<string, string> = {
  Corpo: 'rgba(80, 50, 20, 0.3)',
  Distância: 'rgba(30, 60, 20, 0.3)',
  Cerco: 'rgba(20, 40, 70, 0.3)',
}

export default function BoardRow({ cardIds, rowLabel, side, onCardClick, interactive }: BoardRowProps) {
  const cards = cardIds ?? []
  const score = cards.length

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        flex: 1,
        position: 'relative',
        backgroundColor: ROW_TINTS[rowLabel] ?? 'transparent',
        borderBottom: '1px solid var(--border-subtle)',
        padding: '4px 0',
        minHeight: 0,
      }}
    >
      {/* Score badge */}
      <div
        style={{
          position: 'absolute',
          left: -14,
          width: 36,
          height: 36,
          borderRadius: '50%',
          backgroundColor: 'var(--bg-dark)',
          border: '1px solid var(--border-gold)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontFamily: 'var(--font-heading)',
          fontSize: 13,
          fontWeight: 700,
          color: 'var(--gold-light)',
          zIndex: 2,
        }}
      >
        {score}
      </div>

      {/* Horn slot */}
      <div
        style={{
          width: 34,
          height: 56,
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
          gap: 6,
          padding: '0 8px',
          overflowX: 'hidden',
          alignItems: 'center',
          minHeight: 0,
        }}
      >
        {cards.map((cardId) => (
          <BoardCard
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
          height: 56,
          border: '1px dashed var(--border-subtle)',
          borderRadius: 3,
          flexShrink: 0,
          marginRight: 4,
        }}
      />
    </div>
  )
}
