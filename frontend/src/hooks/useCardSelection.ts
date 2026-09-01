import { useState, useEffect, useCallback } from 'react'
import type { CardDto } from '@/types/game'

export function useCardSelection(hand: CardDto[]) {
  const [selectedCardId, setSelectedCardId] = useState<string | null>(null)
  const [inspectedBoardCard, setInspectedBoardCard] = useState<CardDto | null>(null)
  const [inspectedLeader, setInspectedLeader] = useState<{ card: CardDto; side: 'player' | 'opponent' } | null>(null)

  const selectedCard = hand.find((c) => c.id === selectedCardId) ?? null

  useEffect(() => {
    if (selectedCardId) {
      setInspectedBoardCard(null)
      setInspectedLeader(null)
    }
  }, [selectedCardId])

  const selectHandCard = useCallback((cardId: string) => {
    setSelectedCardId((prev) => (prev === cardId ? null : cardId))
  }, [])

  const inspectHandCard = useCallback((card: CardDto) => {
    setInspectedBoardCard((prev) => (prev?.id === card.id ? null : card))
  }, [])

  const inspectBoardCard = useCallback((card: CardDto) => {
    if (selectedCardId) return // hand selection takes priority
    setInspectedLeader(null)
    setInspectedBoardCard((prev) => (prev?.id === card.id ? null : card))
  }, [selectedCardId])

  const inspectLeader = useCallback((card: CardDto, side: 'player' | 'opponent') => {
    setSelectedCardId(null)
    setInspectedBoardCard(null)
    setInspectedLeader((prev) => (prev?.card.id === card.id ? null : { card, side }))
  }, [])

  const clearSelection = useCallback(() => {
    setSelectedCardId(null)
  }, [])

  const clearInspectedBoardCard = useCallback(() => {
    setInspectedBoardCard(null)
  }, [])

  const clearInspectedLeader = useCallback(() => {
    setInspectedLeader(null)
  }, [])

  return {
    selectedCardId, selectedCard, inspectedBoardCard, inspectedLeader,
    selectHandCard, inspectHandCard, inspectBoardCard, inspectLeader,
    clearSelection, clearInspectedBoardCard, clearInspectedLeader,
  }
}
