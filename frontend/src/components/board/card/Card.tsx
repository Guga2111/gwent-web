import type { CardDto } from '@/types/game'

interface CardProps {
  card?: CardDto        // present = face-up, absent = face-down
  onClick?: () => void
  interactive?: boolean
}

const CARD_WIDTH = 72
const CARD_HEIGHT = 106

const sharedStyle: React.CSSProperties = {
  width: CARD_WIDTH,
  height: CARD_HEIGHT,
  backgroundColor: 'var(--bg-card)',
  border: '1px solid var(--border-gold)',
  borderRadius: 4,
  flexShrink: 0,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
}

export default function Card({ card, onClick, interactive = false }: CardProps) {
  if (!card) {
    return (
      <div
        style={{
          ...sharedStyle,
          backgroundImage:
            'repeating-linear-gradient(45deg, transparent, transparent 8px, rgba(106, 85, 48, 0.15) 8px, rgba(106, 85, 48, 0.15) 9px), repeating-linear-gradient(-45deg, transparent, transparent 8px, rgba(106, 85, 48, 0.15) 8px, rgba(106, 85, 48, 0.15) 9px)',
        }}
      />
    )
  }

  return (
    <div
      onClick={interactive ? onClick : undefined}
      style={{
        ...sharedStyle,
        position: 'relative',
        cursor: interactive ? 'pointer' : 'default',
        transition: 'transform 0.15s, border-color 0.15s',
        flexDirection: 'column',
        gap: 2,
        padding: '4px',
      }}
      onMouseEnter={(e) => {
        if (interactive) {
          e.currentTarget.style.transform = 'translateY(-6px)'
          e.currentTarget.style.borderColor = 'var(--gold-light)'
        }
      }}
      onMouseLeave={(e) => {
        if (interactive) {
          e.currentTarget.style.transform = 'translateY(0)'
          e.currentTarget.style.borderColor = 'var(--border-gold)'
        }
      }}
    >
      {card.basePower != null && (
        <div
          style={{
            fontSize: 14,
            fontFamily: 'var(--font-heading)',
            color: 'var(--gold-light)',
            lineHeight: 1,
          }}
        >
          {card.basePower}
        </div>
      )}
      <div
        style={{
          fontSize: 8,
          fontFamily: 'var(--font-heading)',
          color: 'var(--text-secondary)',
          textAlign: 'center',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
          maxWidth: '100%',
        }}
      >
        {card.name}
      </div>
    </div>
  )
}