import Card from '../card/Card'
import type { CardDto } from '@/types/game'

interface RevealedCardsOverlayProps {
  cards: CardDto[]
  onDismiss: () => void
}

export default function RevealedCardsOverlay({ cards, onDismiss }: RevealedCardsOverlayProps) {
  return (
    <div className="board-overlay">
      <h2 className="overlay-title">Cartas Reveladas</h2>
      <p className="overlay-body">
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
        className="mt-6 px-8 py-2 rounded text-[var(--text-primary)] border border-[var(--gold-dark)] cursor-pointer transition-colors duration-150 hover:bg-[var(--gold-dark)]"
        style={{
          fontFamily: 'var(--font-ui)',
          backgroundColor: 'var(--bg-card)',
        }}
      >
        Fechar
      </button>
    </div>
  )
}
