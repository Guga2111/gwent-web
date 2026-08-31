import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getUserDecks } from '@/api/deck'
import type { DeckDto } from '@/types/deck'

export function useMesaPrivada(
  open: boolean,
  defaultDeckId: string | null,
  onCreateGame: (deckId: string) => Promise<string>,
  onJoinGame: (code: string, deckId: string) => Promise<void>,
  onClose: () => void,
) {
  const navigate = useNavigate()

  const [createdId, setCreatedId] = useState('')
  const [joinCode, setJoinCode] = useState('')
  const [showJoinInput, setShowJoinInput] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [decks, setDecks] = useState<DeckDto[]>([])
  const [selectedDeckId, setSelectedDeckId] = useState<string | null>(defaultDeckId)

  useEffect(() => {
    if (!open) return
    getUserDecks().then((d) => {
      setDecks(d)
      if (!selectedDeckId && d.length > 0) setSelectedDeckId(d[0].id)
    }).catch(() => {})
  }, [open])

  useEffect(() => {
    if (defaultDeckId) setSelectedDeckId(defaultDeckId)
  }, [defaultDeckId])

  function handleClose() {
    setCreatedId('')
    setJoinCode('')
    setShowJoinInput(false)
    setLoading(false)
    setError('')
    onClose()
  }

  async function handleCreate() {
    if (!selectedDeckId) {
      setError('Selecione um baralho antes de criar a partida')
      return
    }
    setError('')
    setLoading(true)
    try {
      const gameId = await onCreateGame(selectedDeckId)
      setCreatedId(gameId)
    } catch {
      setError('Falha ao criar partida')
    } finally {
      setLoading(false)
    }
  }

  async function handleJoin() {
    if (!joinCode.trim()) return
    if (!selectedDeckId) {
      setError('Selecione um baralho antes de entrar na partida')
      return
    }
    setError('')
    setLoading(true)
    try {
      await onJoinGame(joinCode.trim(), selectedDeckId)
      navigate(`/game/${joinCode.trim()}`)
    } catch {
      setError('Falha ao entrar na partida')
    } finally {
      setLoading(false)
    }
  }

  return {
    decks, selectedDeckId, setSelectedDeckId, createdId, joinCode, setJoinCode,
    showJoinInput, setShowJoinInput, loading, error, handleCreate, handleJoin, handleClose,
  }
}
