import { useState } from 'react'
import BoardCard from './BoardCard'

interface MulliganOverlayProps {
  handCardIds: string[]
  onMulligan: (cardId: string) => void
  onConfirm: () => void
  mulligansRemaining: number
}

export default function MulliganOverlay({ handCardIds, onMulligan, onConfirm, mulligansRemaining }: MulliganOverlayProps) {
  const [selected, setSelected] = useState<Set<string>>(new Set())
  const cardIds = handCardIds ?? []

  const toggleCard = (cardId: string) => {
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
    selected.forEach((cardId) => onMulligan(cardId))
    onConfirm()
  }

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        backgroundColor: 'rgba(13, 10, 7, 0.85)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 24,
        zIndex: 50,
      }}
    >
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
        {cardIds.map((cardId) => (
          <div
            key={cardId}
            onClick={() => toggleCard(cardId)}
            style={{
              border: selected.has(cardId) ? '2px solid var(--gold-light)' : '2px solid transparent',
              borderRadius: 6,
              padding: 2,
              cursor: 'pointer',
            }}
          >
            <BoardCard cardId={cardId} interactive={false} />
          </div>
        ))}
      </div>

      <button
        onClick={handleConfirm}
        style={{
          padding: '10px 32px',
          backgroundColor: 'var(--gold-dark)',
          border: '1px solid var(--gold)',
          borderRadius: 4,
          fontFamily: 'var(--font-heading)',
          fontSize: 16,
          color: 'var(--text-primary)',
          cursor: 'pointer',
        }}
      >
        Confirmar
      </button>
    </div>
  )
}
