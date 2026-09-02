import Card from '../card/Card'
import OverlayCountdown from './OverlayCountdown'
import type { CardDto } from '@/types/game'

type LeaderPendingType =
  | 'LEADER_GRAVEYARD_PICK'
  | 'LEADER_OPPONENT_GRAVEYARD_PICK'
  | 'LEADER_DECK_PICK'
  | 'LEADER_HAND_DISCARD'

interface LeaderOverlayProps {
  pendingType: LeaderPendingType
  cards: CardDto[]
  onSelectCard: (cardId: string) => void
  abilityDeadlineUtc: number | null
}

const CONFIG: Record<LeaderPendingType, { title: string; description: string }> = {
  LEADER_GRAVEYARD_PICK: {
    title: 'Escolha uma carta do cemitério',
    description: 'Restaure uma unidade do seu cemitério para o campo de batalha.',
  },
  LEADER_OPPONENT_GRAVEYARD_PICK: {
    title: 'Escolha uma carta do cemitério inimigo',
    description: 'Tome uma unidade do cemitério do oponente para o seu lado.',
  },
  LEADER_DECK_PICK: {
    title: 'Escolha uma carta do baralho',
    description: 'Selecione uma carta do seu baralho.',
  },
  LEADER_HAND_DISCARD: {
    title: 'Descarte uma carta da mão',
    description: 'Escolha uma carta da sua mão para descartar.',
  },
}

export default function LeaderOverlay({ pendingType, cards, onSelectCard, abilityDeadlineUtc }: LeaderOverlayProps) {
  const { title, description } = CONFIG[pendingType]
  const selectableCards =
    pendingType === 'LEADER_GRAVEYARD_PICK' || pendingType === 'LEADER_OPPONENT_GRAVEYARD_PICK'
      ? cards.filter((c) => c.cardType === 'UNIT' || c.cardType === 'HERO')
      : cards

  return (
    <div className="board-overlay">
      <OverlayCountdown deadlineUtc={abilityDeadlineUtc} />
      <h2 className="text-2xl text-gold-light font-display">{title}</h2>
      <p className="text-sm text-text-secondary font-body">{description}</p>

      <div className="flex gap-3 flex-wrap justify-center max-h-[60vh] overflow-y-auto px-4">
        {selectableCards.length > 0 ? (
          selectableCards.map((card) => (
            <div
              key={card.id}
              onClick={() => onSelectCard(card.id)}
              className="border-2 border-transparent rounded-md p-0.5 cursor-pointer transition-colors duration-150 hover:border-gold-light"
            >
              <Card card={card} interactive={false} />
            </div>
          ))
        ) : (
          <p className="text-text-muted text-sm font-body">
            Nenhuma carta disponível.
          </p>
        )}
      </div>
    </div>
  )
}
