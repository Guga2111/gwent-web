import { useState, useEffect, useRef, useCallback } from 'react'
import type { CardDto, RowType } from '@/types/game'
import type { PlayerStateDto, OpponentStateDto } from '@/types/game'

export interface FlyingCardState {
  card: CardDto
  fromRect: DOMRect
  toRect: DOMRect
  targetRow: RowType
}

export function useFlyingCard(
  me: PlayerStateDto | undefined,
  opponent: OpponentStateDto | undefined,
  error: string | null,
) {
  const [flyingCard, setFlyingCard] = useState<FlyingCardState | null>(null)
  const [landedCardId, setLandedCardId] = useState<string | null>(null)
  const flyingAnimDone = useRef(false)
  const flyingCardInRow = useRef(false)

  const clearFlyingCard = useCallback(() => {
    setFlyingCard((prev) => {
      if (prev) {
        setLandedCardId(prev.card.id)
        setTimeout(() => setLandedCardId(null), 100)
      }
      return null
    })
  }, [])

  // Check if the flying card has arrived in row data (WS update)
  // For SPECIAL cards (Commander's Horn, Dummy), the card goes to graveyard
  // instead of a row, so we also check if the card left the hand.
  useEffect(() => {
    if (!flyingCard || !me || !opponent) return
    const isSpy = flyingCard.card.ability === 'SPY'
    const source = isSpy ? opponent : me
    const rowMap: Record<RowType, CardDto[]> = {
      MELEE: source.meleeRow.cards ?? [],
      RANGED: source.rangedRow.cards ?? [],
      SIEGE: source.siegeRow.cards ?? [],
    }
    const inRow = rowMap[flyingCard.targetRow].some(
      (c) => c.id === flyingCard.card.id
    )
    const leftHand = !me.hand.some((c) => c.id === flyingCard.card.id)
    if (inRow || (leftHand && flyingCard.card.cardType === 'SPECIAL')) {
      flyingCardInRow.current = true
      if (flyingAnimDone.current) {
        clearFlyingCard()
      }
    }
  }, [me?.meleeRow, me?.rangedRow, me?.siegeRow, me?.hand, opponent?.meleeRow, opponent?.rangedRow, opponent?.siegeRow, flyingCard, clearFlyingCard])

  // Clear on error (backend rejected command) — card stays in hand
  useEffect(() => {
    if (!flyingCard) return
    if (error) {
      setFlyingCard(null)
    }
  }, [error, flyingCard])

  const launchCard = useCallback((card: CardDto, targetRow: RowType, isSpy: boolean) => {
    const cardEl = document.querySelector(`[data-card-id="${card.id}"]`)
    const rowEl = document.querySelector(
      `[data-row-type="${targetRow}"][data-row-side="${isSpy ? 'opponent' : 'player'}"]`
    )
    if (cardEl && rowEl) {
      const fromRect = cardEl.getBoundingClientRect()
      const isHornCard = card.cardType === 'SPECIAL' && card.ability === 'COMMANDERS_HORN'
      const hornSlot = isHornCard ? rowEl.querySelector('.board-row__horn') : null
      const toRect = (hornSlot ?? rowEl).getBoundingClientRect()
      flyingAnimDone.current = false
      flyingCardInRow.current = false
      setFlyingCard({ card, fromRect, toRect, targetRow })
    }
  }, [])

  const handleFlightComplete = useCallback(() => {
    flyingAnimDone.current = true
    if (flyingCardInRow.current) {
      clearFlyingCard()
    }
  }, [clearFlyingCard])

  return { flyingCard, landedCardId, launchCard, handleFlightComplete }
}
