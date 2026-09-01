import { useState, useEffect } from 'react'
import { getUserDecks } from '@/api/deck'
import { getCardsByFaction } from '@/api/catalog'
import type { DeckDto, CatalogCardDto } from '@/types/deck'
import { useHubStore } from '@/stores/hubStore'

export function useTavernaData() {
  const [decks, setDecks] = useState<DeckDto[]>([])
  const [activeDeck, setActiveDeck] = useState<DeckDto | null>(null)
  const [leaderCard, setLeaderCard] = useState<CatalogCardDto | null>(null)
  const setActiveDeckInStore = useHubStore((s) => s.setActiveDeck)

  function selectDeck(deck: DeckDto) {
    setActiveDeck(deck)
    setActiveDeckInStore(deck)
    setLeaderCard(null)
    getCardsByFaction(deck.faction)
      .then((cards) => setLeaderCard(cards.find(c => c.id === deck.leaderId) ?? null))
      .catch(() => {})
  }

  useEffect(() => {
    getUserDecks().then((fetched) => {
      setDecks(fetched)
      if (fetched.length > 0) selectDeck(fetched[0])
    }).catch(() => {})
  }, [])

  return { decks, activeDeck, leaderCard, selectDeck }
}
