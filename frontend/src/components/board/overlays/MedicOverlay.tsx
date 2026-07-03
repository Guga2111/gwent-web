import Card from '../card/Card'
import type { CardDto } from '@/types/game'

interface MedicOverlayProps {
  graveyard: CardDto[]
  onSelectCard: (cardId: string) => void
}

export default function MedicOverlay({ graveyard, onSelectCard }: MedicOverlayProps) {
  const revivableCards = graveyard.filter((c) => c.cardType === 'UNIT')

  return (
    <div className="board-overlay">
      <h2
        style={{
          fontFamily: 'var(--font-display)',
          fontSize: 24,
          color: 'var(--gold-light)',
        }}
      >
        Escolha uma carta para reviver
      </h2>

      <p
        style={{
          fontFamily: 'var(--font-body)',
          fontSize: 14,
          color: 'var(--text-secondary)',
          fontStyle: 'italic',
        }}
      >
        O Medic permite restaurar uma unidade do cemiterio
      </p>

      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', justifyContent: 'center' }}>
        {revivableCards.map((card) => (
          <div
            key={card.id}
            onClick={() => onSelectCard(card.id)}
            style={{
              border: '2px solid transparent',
              borderRadius: 6,
              padding: 2,
              cursor: 'pointer',
              transition: 'border-color 0.15s',
            }}
            onMouseEnter={(e) => (e.currentTarget.style.borderColor = 'var(--gold-light)')}
            onMouseLeave={(e) => (e.currentTarget.style.borderColor = 'transparent')}
          >
            <Card card={card} interactive={false} />
          </div>
        ))}
      </div>
    </div>
  )
}
