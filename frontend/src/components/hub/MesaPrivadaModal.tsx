import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createGame, joinGame } from '@/api/game'

interface MesaPrivadaModalProps {
  open: boolean
  onClose: () => void
}

export default function MesaPrivadaModal({ open, onClose }: MesaPrivadaModalProps) {
  const navigate = useNavigate()

  const [createdId, setCreatedId] = useState('')
  const [joinCode, setJoinCode] = useState('')
  const [showJoinInput, setShowJoinInput] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

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
    setError('')
    setLoading(true)
    try {
      const { gameId } = await createGame()
      setCreatedId(gameId)
    } catch {
      setError('Falha ao criar partida')
    } finally {
      setLoading(false)
    }
  }

  async function handleJoin() {
    if (!joinCode.trim()) return
    setError('')
    setLoading(true)
    try {
      await joinGame(joinCode.trim())
      navigate(`/game/${joinCode.trim()}`)
    } catch {
      setError('Falha ao entrar na partida')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 70,
        background: 'rgba(8,5,2,.86)',
        backdropFilter: 'blur(5px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <div
        style={{
          position: 'relative',
          width: 520,
          borderRadius: 8,
          background: 'linear-gradient(180deg, var(--bg-card), var(--bg-dark))',
          padding: '40px 42px',
          boxShadow:
            'inset 0 0 0 2px rgba(20,12,5,.9), inset 0 0 0 4px rgba(240,205,120,.3), 0 30px 80px rgba(0,0,0,.75), 0 0 60px rgba(240,200,110,.12)',
          animation: 'gw-rise .35s ease',
        }}
      >
        {/* Close button */}
        <button
          onClick={handleClose}
          style={{
            position: 'absolute',
            top: 16,
            right: 16,
            width: 30,
            height: 30,
            borderRadius: '50%',
            border: 'none',
            cursor: 'pointer',
            background: 'rgba(0,0,0,.3)',
            boxShadow: 'inset 0 0 0 1px rgba(240,205,120,.25)',
            color: 'var(--gold)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <svg viewBox="0 0 24 24" style={{ width: 15, height: 15, fill: 'none', stroke: 'currentColor', strokeWidth: 2.2, strokeLinecap: 'round' }}>
            <line x1="5" y1="5" x2="19" y2="19" />
            <line x1="19" y1="5" x2="5" y2="19" />
          </svg>
        </button>

        {/* Header */}
        <div style={{ textAlign: 'center', marginBottom: 26 }}>
          <div
            style={{
              fontFamily: 'var(--font-heading)',
              fontSize: '10.5px',
              letterSpacing: 4,
              textTransform: 'uppercase',
              color: 'var(--gold)',
              fontWeight: 700,
            }}
          >
            Mesa Privada
          </div>
          <h2
            style={{
              margin: '6px 0 0',
              fontFamily: 'var(--font-display)',
              fontWeight: 700,
              fontSize: 23,
              color: 'var(--gold-light)',
              letterSpacing: 1,
            }}
          >
            Uma Amistosa entre Amigos
          </h2>
        </div>

        {error && (
          <p style={{ color: 'var(--red)', fontSize: 13, textAlign: 'center', marginBottom: 16 }}>
            {error}
          </p>
        )}

        {/* Two side-by-side option cards */}
        <div style={{ display: 'flex', gap: 16 }}>
          {/* Left card — Criar Mesa */}
          <div
            style={{
              flex: 1,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 12,
              padding: '26px 18px',
              borderRadius: 9,
              background: 'linear-gradient(180deg, var(--bg-medium), var(--bg-dark))',
              boxShadow: 'inset 0 0 0 1px rgba(240,205,120,.32), 0 6px 16px rgba(0,0,0,.4)',
              cursor: createdId ? 'default' : 'pointer',
            }}
            onClick={!createdId && !loading ? handleCreate : undefined}
          >
            {!createdId ? (
              <>
                <div
                  style={{
                    width: 52,
                    height: 52,
                    borderRadius: '50%',
                    background: 'radial-gradient(circle at 35% 30%, var(--gold-light), var(--gold-dark))',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'var(--bg-darkest)',
                    boxShadow: '0 4px 10px rgba(0,0,0,.4)',
                  }}
                >
                  <svg viewBox="0 0 24 24" style={{ width: 26, height: 26, fill: 'none', stroke: 'currentColor', strokeWidth: 1.8, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
                    <line x1="12" y1="5" x2="12" y2="19" />
                    <line x1="5" y1="12" x2="19" y2="12" />
                  </svg>
                </div>
                <div
                  style={{
                    fontFamily: 'var(--font-heading)',
                    fontWeight: 700,
                    fontSize: 15,
                    color: 'var(--gold-light)',
                    letterSpacing: '.5px',
                  }}
                >
                  {loading ? '...' : 'Criar Mesa'}
                </div>
                <div
                  style={{
                    fontFamily: 'var(--font-body)',
                    fontStyle: 'italic',
                    fontSize: '12.5px',
                    color: 'var(--text-muted)',
                    textAlign: 'center',
                    lineHeight: 1.35,
                  }}
                >
                  Gere um código e convide um amigo pelo link
                </div>
              </>
            ) : (
              <>
                <div
                  style={{
                    fontFamily: 'var(--font-body)',
                    fontStyle: 'italic',
                    fontSize: 13,
                    color: 'var(--green)',
                    marginBottom: 4,
                  }}
                >
                  Partida criada!
                </div>
                <code
                  style={{
                    display: 'block',
                    padding: '12px 16px',
                    borderRadius: 8,
                    background: 'var(--bg-darkest)',
                    color: 'var(--gold-light)',
                    fontFamily: 'monospace',
                    fontSize: 13,
                    wordBreak: 'break-all',
                    userSelect: 'all',
                    textAlign: 'center',
                  }}
                >
                  {createdId}
                </code>
                <button
                  onClick={(e) => {
                    e.stopPropagation()
                    navigate(`/game/${createdId}`)
                  }}
                  style={{
                    marginTop: 4,
                    padding: '10px 20px',
                    borderRadius: 7,
                    border: 'none',
                    background: 'linear-gradient(180deg, var(--gold-light), var(--gold))',
                    color: 'var(--bg-darkest)',
                    fontFamily: 'var(--font-heading)',
                    fontWeight: 700,
                    fontSize: 13,
                    letterSpacing: '.5px',
                    cursor: 'pointer',
                    boxShadow: '0 5px 12px rgba(0,0,0,.4)',
                  }}
                >
                  Entrar na Partida
                </button>
              </>
            )}
          </div>

          {/* Right card — Entrar com Código */}
          <div
            style={{
              flex: 1,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 12,
              padding: '26px 18px',
              borderRadius: 9,
              background: 'linear-gradient(180deg, var(--bg-medium), var(--bg-dark))',
              boxShadow: 'inset 0 0 0 1px rgba(240,205,120,.32), 0 6px 16px rgba(0,0,0,.4)',
              cursor: showJoinInput ? 'default' : 'pointer',
            }}
            onClick={!showJoinInput ? () => setShowJoinInput(true) : undefined}
          >
            {!showJoinInput ? (
              <>
                <div
                  style={{
                    width: 52,
                    height: 52,
                    borderRadius: '50%',
                    background: 'rgba(0,0,0,.3)',
                    boxShadow: 'inset 0 0 0 1.5px rgba(240,205,120,.45)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'var(--gold)',
                  }}
                >
                  <svg viewBox="0 0 24 24" style={{ width: 26, height: 26, fill: 'none', stroke: 'currentColor', strokeWidth: 1.8, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
                    <polyline points="10 17 15 12 10 7" />
                    <line x1="15" y1="12" x2="3" y2="12" />
                  </svg>
                </div>
                <div
                  style={{
                    fontFamily: 'var(--font-heading)',
                    fontWeight: 700,
                    fontSize: 15,
                    color: 'var(--gold-light)',
                    letterSpacing: '.5px',
                  }}
                >
                  Entrar com Código
                </div>
                <div
                  style={{
                    fontFamily: 'var(--font-body)',
                    fontStyle: 'italic',
                    fontSize: '12.5px',
                    color: 'var(--text-muted)',
                    textAlign: 'center',
                    lineHeight: 1.35,
                  }}
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
                  style={{
                    width: '100%',
                    padding: '12px 14px',
                    borderRadius: 7,
                    border: 'none',
                    outline: 'none',
                    background: 'var(--bg-darkest)',
                    boxShadow: 'inset 0 0 0 1px var(--border-gold)',
                    color: 'var(--text-primary)',
                    fontFamily: 'var(--font-ui)',
                    fontSize: 14,
                    textAlign: 'center',
                  }}
                />
                <button
                  onClick={(e) => {
                    e.stopPropagation()
                    handleJoin()
                  }}
                  disabled={loading || !joinCode.trim()}
                  style={{
                    padding: '10px 20px',
                    borderRadius: 7,
                    border: 'none',
                    background: 'linear-gradient(180deg, var(--gold-light), var(--gold))',
                    color: 'var(--bg-darkest)',
                    fontFamily: 'var(--font-heading)',
                    fontWeight: 700,
                    fontSize: 13,
                    letterSpacing: '.5px',
                    cursor: 'pointer',
                    boxShadow: '0 5px 12px rgba(0,0,0,.4)',
                    opacity: loading || !joinCode.trim() ? 0.5 : 1,
                  }}
                >
                  {loading ? '...' : 'Entrar'}
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
