import { useState, useRef } from 'react'
import Card from '../card/Card'
import PrimaryButton from '@/components/ui/PrimaryButton'
import type { CardDto } from '@/types/game'

interface MulliganOverlayProps {
  hand: CardDto[]
  onMulligan: (cardId: string) => void
  onConfirm: () => void
  mulligansRemaining: number
}

export default function MulliganOverlay({ hand, onMulligan, onConfirm, mulligansRemaining }: MulliganOverlayProps) {
  const [selected, setSelected] = useState<Set<string>>(new Set())
  const submittedRef = useRef(false)
  const cards = hand ?? []

  const toggleCard = (cardId: string) => {
    if (submittedRef.current) return
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
    if (submittedRef.current) return
    submittedRef.current = true
    selected.forEach((cardId) => onMulligan(cardId))
    onConfirm()
  }

  return (
    <div className="board-overlay">
      <h2
        style={{
          fontFamily: 'var(--font-display)',
          fontSize: 24,
          color: 'var(--gold-light)',
        }}
      >
        Escolha cartas para trocar
      </h2>

      <p
        style={{
          fontFamily: 'var(--font-body)',
          fontSize: 14,
          color: 'var(--text-secondary)',
          fontStyle: 'italic',
        }}
      >
        Você pode trocar até {mulligansRemaining} cartas
      </p>

      <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', justifyContent: 'center' }}>
        {cards.map((card) => (
          <div
            key={card.id}
            onClick={() => toggleCard(card.id)}
            style={{
              border: selected.has(card.id) ? '2px solid var(--gold-light)' : '2px solid transparent',
              borderRadius: 6,
              padding: 2,
              cursor: 'pointer',
            }}
          >
            <Card card={card} interactive={false} />
          </div>
        ))}
      </div>

      <PrimaryButton onClick={handleConfirm} disabled={submittedRef.current}>Confirmar</PrimaryButton>
    </div>
  )
}