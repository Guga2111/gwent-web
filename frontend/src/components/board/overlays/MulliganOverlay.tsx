import { useState } from 'react'
import Card from '../card/Card'
import PrimaryButton from '@/components/ui/PrimaryButton'
import OverlayCountdown from './OverlayCountdown'
import type { CardDto } from '@/types/game'

interface MulliganOverlayProps {
  hand: CardDto[]
  onConfirm: (cardIds: string[]) => void
  mulligansRemaining: number
  abilityDeadlineUtc: number | null
}

export default function MulliganOverlay({ hand, onConfirm, mulligansRemaining, abilityDeadlineUtc }: MulliganOverlayProps) {
  const [selected, setSelected] = useState<Set<string>>(new Set())
  const [submitted, setSubmitted] = useState(false);
  const cards = hand ?? []

  const toggleCard = (cardId: string) => {
    if (submitted) return
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(cardId)) {
        next.delete(cardId)
      } else if (next.size < mulligansRemaining) {
        next.add(cardId)
      }
      return next
    })
  }

  const handleConfirm = () => {
    if (submitted) return
    setSubmitted(true);
    onConfirm([...selected])
  }

  return (
    <div className="board-overlay">
      <OverlayCountdown deadlineUtc={abilityDeadlineUtc} />
      <h2 className="overlay-title">Escolha cartas para trocar</h2>

      <p className="overlay-body">
        Você pode trocar até {mulligansRemaining} cartas
      </p>

      <div className="flex gap-3 flex-wrap justify-center">
        {cards.map((card) => (
          <div
            key={card.id}
            onClick={() => toggleCard(card.id)}
            className="cursor-pointer rounded-md p-0.5"
            style={{
              border: selected.has(card.id) ? '2px solid var(--gold-light)' : '2px solid transparent',
            }}
          >
            <Card card={card} interactive={false} />
          </div>
        ))}
      </div>

      <PrimaryButton onClick={handleConfirm} disabled={submitted}>Confirmar</PrimaryButton>
    </div>
  )
}
