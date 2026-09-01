import Card from '../card/Card'
import OverlayCountdown from './OverlayCountdown'
import type { CardDto } from '@/types/game'

interface MedicOverlayProps {
  graveyard: CardDto[]
  onSelectCard: (cardId: string) => void
  abilityDeadlineUtc: number | null
}

export default function MedicOverlay({ graveyard, onSelectCard, abilityDeadlineUtc }: MedicOverlayProps) {
  const revivableCards = graveyard.filter((c) => c.cardType === 'UNIT')

  return (
    <div className="board-overlay">
      <OverlayCountdown deadlineUtc={abilityDeadlineUtc} />
      <h2 className="overlay-title">Escolha uma carta para reviver</h2>

      <p className="overlay-body">
        O Medic permite restaurar uma unidade do cemiterio
      </p>

      <div className="flex gap-3 flex-wrap justify-center">
        {revivableCards.map((card) => (
          <div
            key={card.id}
            onClick={() => onSelectCard(card.id)}
            className="border-2 border-transparent rounded-md p-0.5 cursor-pointer transition-colors duration-150 hover:border-[var(--gold-light)]"
          >
            <Card card={card} interactive={false} />
          </div>
        ))}
      </div>
    </div>
  )
}
