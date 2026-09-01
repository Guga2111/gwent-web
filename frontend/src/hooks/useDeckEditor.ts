import { useState, useEffect } from 'react'
import { getUserDecks, createDeck, updateDeck, deleteDeck } from '@/api/deck'
import { getCardsByFaction } from '@/api/catalog'
import type { DeckDto, CatalogCardDto, Faction, DeckCardEntryDto, SaveDeckRequest } from '@/types/deck'

interface EditorState {
  id: string | null
  name: string
  faction: Faction
  leaderId: string
  cards: DeckCardEntryDto[]
}

function emptyEditor(): EditorState {
  return { id: null, name: '', faction: 'NORTHERN_REALMS', leaderId: '', cards: [] }
}

function deckToEditor(d: DeckDto): EditorState {
  return { id: d.id, name: d.name, faction: d.faction, leaderId: d.leaderId, cards: d.cards }
}

function totalCount(cards: DeckCardEntryDto[]) {
  return cards.reduce((s, e) => s + e.quantity, 0)
}

export function useDeckEditor() {
  const [decks, setDecks] = useState<DeckDto[]>([])
  const [editor, setEditor] = useState<EditorState | null>(null)
  const [catalog, setCatalog] = useState<CatalogCardDto[]>([])
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    getUserDecks().then(setDecks).catch(() => {})
  }, [])

  useEffect(() => {
    if (!editor) return
    getCardsByFaction(editor.faction, true).then(setCatalog).catch(() => {})
  }, [editor?.faction])

  function openNew() {
    setEditor(emptyEditor())
    setError('')
  }

  function openEdit(d: DeckDto) {
    setEditor(deckToEditor(d))
    setError('')
  }

  function closeEditor() {
    setEditor(null)
    setError('')
  }

  async function handleDelete(id: string) {
    await deleteDeck(id)
    setDecks((prev) => prev.filter((d) => d.id !== id))
  }

  async function handleSave() {
    if (!editor) return
    setError('')

    const count = totalCount(editor.cards)
    if (!editor.name.trim()) { setError('O baralho precisa de um nome'); return }
    if (!editor.leaderId) { setError('Selecione um líder'); return }
    if (count < 22 || count > 40) { setError(`Total de cartas: ${count} (mín 22, máx 40)`); return }

    const request: SaveDeckRequest = {
      name: editor.name,
      faction: editor.faction,
      leaderId: editor.leaderId,
      cards: editor.cards,
    }

    setSaving(true)
    try {
      if (editor.id) {
        const updated = await updateDeck(editor.id, request)
        setDecks((prev) => prev.map((d) => (d.id === updated.id ? updated : d)))
      } else {
        const created = await createDeck(request)
        setDecks((prev) => [...prev, created])
      }
      closeEditor()
    } catch (e: any) {
      setError(e?.response?.data?.message ?? 'Falha ao salvar baralho')
    } finally {
      setSaving(false)
    }
  }

  function addCard(card: CatalogCardDto) {
    if (!editor) return
    const existing = editor.cards.find((e) => e.cardId === card.id)
    const maxCopies = card.ability === 'HERO' ? 1 : card.deckCopies
    if (existing) {
      if (existing.quantity >= maxCopies) return
      setEditor({ ...editor, cards: editor.cards.map((e) => e.cardId === card.id ? { ...e, quantity: e.quantity + 1 } : e) })
    } else {
      setEditor({ ...editor, cards: [...editor.cards, { cardId: card.id, quantity: 1 }] })
    }
  }

  function removeCard(cardId: string) {
    if (!editor) return
    const existing = editor.cards.find((e) => e.cardId === cardId)
    if (!existing) return
    if (existing.quantity > 1) {
      setEditor({ ...editor, cards: editor.cards.map((e) => e.cardId === cardId ? { ...e, quantity: e.quantity - 1 } : e) })
    } else {
      setEditor({ ...editor, cards: editor.cards.filter((e) => e.cardId !== cardId) })
    }
  }

  function setEditorField(partial: Partial<EditorState>) {
    if (!editor) return
    setEditor({ ...editor, ...partial })
  }

  const leaders = catalog.filter((c) => c.cardType === 'LEADER')
  const nonLeaderCatalog = catalog.filter((c) => c.cardType !== 'LEADER')
  const cardById = Object.fromEntries(catalog.map((c) => [c.id, c]))

  return {
    decks, editor, catalog, leaders, nonLeaderCatalog, cardById,
    saving, error, openNew, openEdit, closeEditor,
    handleDelete, handleSave, addCard, removeCard, setEditorField,
  }
}
