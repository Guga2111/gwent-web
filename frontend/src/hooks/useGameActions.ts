import { useCallback } from 'react'
import { useAuthStore } from '@/stores/authStore'
import type { CardDto, RowType, GameStateDto, CommandRequest } from '@/types/game'

export function useGameActions(
  sendCommand: (cmd: CommandRequest) => void,
  gameState: GameStateDto | null,
  selectedCard: CardDto | null,
  selectedCardId: string | null,
  clearSelection: () => void,
  launchCard: (card: CardDto, targetRow: RowType, isSpy: boolean) => void,
) {
  const playerId = useAuthStore((s) => s.user)?.email

  const isMyTurn = gameState
    ? gameState.currentTurn === gameState.myTurn
    : false

  const canInteract = isMyTurn && gameState?.phase === 'PLAY' && !gameState?.pendingAbility

  const canPlayOnRow = useCallback((row: RowType): boolean => {
    if (!selectedCard || !isMyTurn) return false
    if (selectedCard.cardType === 'SPECIAL') return true
    if (selectedCard.ability === 'AGILE')
      return row === 'MELEE' || row === 'RANGED'
    return selectedCard.rowType === row
  }, [selectedCard, isMyTurn])

  const playWeatherCard = useCallback(() => {
    if (!selectedCardId || !isMyTurn) return
    sendCommand({
      commandType: 'PLAY_CARD',
      playerId,
      cardId: selectedCardId,
      targetRow: 'MELEE',
    })
    clearSelection()
  }, [selectedCardId, isMyTurn, sendCommand, playerId, clearSelection])

  const playCard = useCallback((targetRow: RowType) => {
    if (!selectedCardId || !isMyTurn) return
    const card = gameState?.me?.hand.find((c) => c.id === selectedCardId)
    if (card) {
      launchCard(card, targetRow, card.ability === 'SPY')
    }
    sendCommand({
      commandType: 'PLAY_CARD',
      playerId,
      cardId: selectedCardId,
      targetRow,
    })
    clearSelection()
  }, [selectedCardId, isMyTurn, gameState?.me?.hand, sendCommand, playerId, clearSelection, launchCard])

  const confirmPlay = useCallback(() => {
    if (!selectedCard || !isMyTurn) return
    if (selectedCard.cardType === 'WEATHER') {
      playWeatherCard()
      return
    }
    if (selectedCard.cardType === 'SPECIAL') return
    if (selectedCard.ability === 'AGILE') return
    if (selectedCard.rowType) playCard(selectedCard.rowType)
  }, [selectedCard, isMyTurn, playWeatherCard, playCard])

  const pass = useCallback(() => {
    sendCommand({ commandType: 'PASS', playerId })
  }, [sendCommand, playerId])

  const useLeader = useCallback(() => {
    sendCommand({ commandType: 'USE_LEADER', playerId })
  }, [sendCommand, playerId])

  return { isMyTurn, canInteract, playCard, playWeatherCard, confirmPlay, pass, useLeader, canPlayOnRow }
}
