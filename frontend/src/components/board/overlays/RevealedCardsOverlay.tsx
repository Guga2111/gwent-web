import Card from '../card/Card'
import type { CardDto } from '@/types/game'

interface RevealedCardsOverlayProps {
  cards: CardDto[]
  onDismiss: () => void
}

export default function RevealedCardsOverlay({ cards, onDismiss }: RevealedCardsOverlayProps) {
  return (
    <div className="board-overlay">
      <h2 className="text-2xl text-gold-light font-display">Cartas Reveladas</h2>
      <p className="text-sm text-text-secondary font-body">
        O Imperador de Nilfgaard revelou cartas da mão do oponente.
      </p>

      <div className="flex gap-3 flex-wrap justify-center">
        {cards.map((card) => (
          <div key={card.id} className="rounded-md p-0.5">
            <Card card={card} interactive={false} />
          </div>
        ))}
      </div>

      <button
        onClick={onDismiss}
        className="mt-6 px-8 py-2 rounded text-text-primary border border-gold-dark cursor-pointer transition-colors duration-150 hover:bg-gold-dark font-ui bg-bg-card"
      >
        Fechar
      </button>
    </div>
  )
}
