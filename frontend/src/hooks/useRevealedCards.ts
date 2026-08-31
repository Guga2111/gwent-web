import { useState, useEffect } from 'react'
import type { CardDto } from '@/types/game'

export function useRevealedCards(revealedCards: CardDto[] | null | undefined) {
  const [showRevealedCards, setShowRevealedCards] = useState(false)

  useEffect(() => {
    if (revealedCards && revealedCards.length > 0) {
      setShowRevealedCards(true)
    }
  }, [revealedCards])

  return { showRevealedCards, dismissRevealedCards: () => setShowRevealedCards(false) }
}
