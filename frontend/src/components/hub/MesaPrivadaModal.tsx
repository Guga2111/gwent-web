import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { X, Plus, LogIn } from 'lucide-react'
import PrimaryButton from '@/components/ui/PrimaryButton'
import { getUserDecks } from '@/api/deck'
import type { DeckDto } from '@/types/deck'

interface MesaPrivadaModalProps {
  open: boolean
  onClose: () => void
  onCreateGame: (deckId: string) => Promise<string>
  onJoinGame: (code: string, deckId: string) => Promise<void>
  defaultDeckId: string | null
}

export default function MesaPrivadaModal({ open, onClose, onCreateGame, onJoinGame, defaultDeckId }: MesaPrivadaModalProps) {
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

  if (!open) return null

  function resetState() {
    setCreatedId('')
    setJoinCode('')
    setShowJoinInput(false)
    setLoading(false)
    setError('')
  }

  function handleClose() {
    resetState()
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

  return (
    <div
      className="fixed inset-0 z-[70] flex items-center justify-center"
      style={{ background: 'rgba(8,5,2,.86)', backdropFilter: 'blur(5px)' }}
    >
      <div
        className="relative w-[520px] rounded-lg px-[42px] py-10"
        style={{
          background: 'linear-gradient(180deg, var(--bg-card), var(--bg-dark))',
          boxShadow: 'inset 0 0 0 2px rgba(20,12,5,.9), inset 0 0 0 4px rgba(240,205,120,.3), 0 30px 80px rgba(0,0,0,.75), 0 0 60px rgba(240,200,110,.12)',
          animation: 'gw-rise .35s ease',
        }}
      >
        {/* Close button */}
        <button
          onClick={handleClose}
          className="absolute top-4 right-4 w-[30px] h-[30px] rounded-full border-none cursor-pointer flex items-center justify-center text-[var(--gold)]"
          style={{
            background: 'rgba(0,0,0,.3)',
            boxShadow: 'inset 0 0 0 1px rgba(240,205,120,.25)',
          }}
        >
          <X size={15} strokeWidth={2.2} />
        </button>

        {/* Header */}
        <div className="text-center mb-[26px]">
          <div
            className="text-[10.5px] tracking-[4px] uppercase font-bold text-[var(--gold)]"
            style={{ fontFamily: 'var(--font-heading)' }}
          >
            Mesa Privada
          </div>
          <h2
            className="mt-1.5 text-[23px] font-bold text-[var(--gold-light)] tracking-[1px]"
            style={{ fontFamily: 'var(--font-display)' }}
          >
            Uma Amistosa entre Amigos
          </h2>
        </div>

        {/* Deck selector */}
        {decks.length > 0 && (
          <div className="mb-5">
            <div
              className="text-[10px] tracking-[2px] uppercase font-bold text-[var(--gold)] mb-1.5"
              style={{ fontFamily: 'var(--font-heading)' }}
            >
              Baralho
            </div>
            <select
              value={selectedDeckId ?? ''}
              onChange={(e) => setSelectedDeckId(e.target.value)}
              className="w-full px-3 py-2 rounded-[7px] border-none outline-none text-sm text-[var(--text-primary)] bg-[var(--bg-darkest)] cursor-pointer"
              style={{
                fontFamily: 'var(--font-ui)',
                boxShadow: 'inset 0 0 0 1px var(--border-gold)',
              }}
            >
              {decks.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name} ({d.cards.reduce((s, c) => s + c.quantity, 0)} cartas)
                </option>
              ))}
            </select>
          </div>
        )}

        {decks.length === 0 && (
          <p className="text-[13px] text-center text-[var(--gold)] mb-4 italic" style={{ fontFamily: 'var(--font-body)' }}>
            Crie um baralho na Forja antes de jogar
          </p>
        )}

        {error && (
          <p className="text-[13px] text-center text-[var(--red)] mb-4">{error}</p>
        )}

        {/* Two side-by-side option cards */}
        <div className="flex gap-4">
          {/* Left card — Criar Mesa */}
          <div
            className="flex-1 flex flex-col items-center gap-3 px-[18px] py-[26px] rounded-[9px]"
            style={{
              background: 'linear-gradient(180deg, var(--bg-medium), var(--bg-dark))',
              boxShadow: 'inset 0 0 0 1px rgba(240,205,120,.32), 0 6px 16px rgba(0,0,0,.4)',
              cursor: createdId ? 'default' : 'pointer',
            }}
            onClick={!createdId && !loading ? handleCreate : undefined}
          >
            {!createdId ? (
              <>
                <div
                  className="w-[52px] h-[52px] rounded-full flex items-center justify-center text-[var(--bg-darkest)]"
                  style={{
                    background: 'radial-gradient(circle at 35% 30%, var(--gold-light), var(--gold-dark))',
                    boxShadow: '0 4px 10px rgba(0,0,0,.4)',
                  }}
                >
                  <Plus size={26} strokeWidth={1.8} />
                </div>
                <div
                  className="font-bold text-[15px] text-[var(--gold-light)] tracking-[.5px]"
                  style={{ fontFamily: 'var(--font-heading)' }}
                >
                  {loading ? '...' : 'Criar Mesa'}
                </div>
                <div
                  className="italic text-[12.5px] text-[var(--text-muted)] text-center leading-[1.35]"
                  style={{ fontFamily: 'var(--font-body)' }}
                >
                  Gere um código e convide um amigo pelo link
                </div>
              </>
            ) : (
              <>
                <div
                  className="italic text-[13px] text-[var(--green)] mb-1"
                  style={{ fontFamily: 'var(--font-body)' }}
                >
                  Partida criada!
                </div>
                <code
                  className="block px-4 py-3 rounded-[8px] bg-[var(--bg-darkest)] text-[var(--gold-light)] font-mono text-[13px] break-all select-all text-center"
                >
                  {createdId}
                </code>
                <PrimaryButton
                  variant="light"
                  onClick={() => navigate(`/game/${createdId}`)}
                  style={{ marginTop: 4, padding: '10px 20px', borderRadius: 7, fontSize: 13 }}
                >
                  Entrar na Partida
                </PrimaryButton>
              </>
            )}
          </div>

          {/* Right card — Entrar com Código */}
          <div
            className="flex-1 flex flex-col items-center gap-3 px-[18px] py-[26px] rounded-[9px]"
            style={{
              background: 'linear-gradient(180deg, var(--bg-medium), var(--bg-dark))',
              boxShadow: 'inset 0 0 0 1px rgba(240,205,120,.32), 0 6px 16px rgba(0,0,0,.4)',
              cursor: showJoinInput ? 'default' : 'pointer',
            }}
            onClick={!showJoinInput ? () => setShowJoinInput(true) : undefined}
          >
            {!showJoinInput ? (
              <>
                <div
                  className="w-[52px] h-[52px] rounded-full flex items-center justify-center text-[var(--gold)]"
                  style={{
                    background: 'rgba(0,0,0,.3)',
                    boxShadow: 'inset 0 0 0 1.5px rgba(240,205,120,.45)',
                  }}
                >
                  <LogIn size={26} strokeWidth={1.8} />
                </div>
                <div
                  className="font-bold text-[15px] text-[var(--gold-light)] tracking-[.5px]"
                  style={{ fontFamily: 'var(--font-heading)' }}
                >
                  Entrar com Código
                </div>
                <div
                  className="italic text-[12.5px] text-[var(--text-muted)] text-center leading-[1.35]"
                  style={{ fontFamily: 'var(--font-body)' }}
                >
                  Recebeu um convite? Digite o código aqui
                </div>
              </>
            ) : (
              <>
                <input
                  type="text"
                  value={joinCode}
                  onChange={(e) => setJoinCode(e.target.value)}
                  placeholder="Cole o código aqui"
                  autoFocus
                  className="w-full px-[14px] py-3 rounded-[7px] border-none outline-none text-center text-sm text-[var(--text-primary)] bg-[var(--bg-darkest)]"
                  style={{
                    fontFamily: 'var(--font-ui)',
                    boxShadow: 'inset 0 0 0 1px var(--border-gold)',
                  }}
                />
                <PrimaryButton
                  variant="light"
                  disabled={loading || !joinCode.trim()}
                  onClick={handleJoin}
                  style={{ padding: '10px 20px', borderRadius: 7, fontSize: 13 }}
                >
                  {loading ? '...' : 'Entrar'}
                </PrimaryButton>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
