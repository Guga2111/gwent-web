import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMatchmakingStore } from '@/stores/matchmakingStore'
import { useMatchmakingSocket } from '@/hooks/useMatchmakingSocket'
import { useAuthStore } from '@/stores/authStore'
import { getFactionConfig } from '@/utils/factionConfig'
import { useHubStore } from '@/stores/hubStore'
import type { Faction } from '@/types/game'

interface MatchmakingModalProps {
  open: boolean
  onCancel: () => void
}

function FactionShield({ faction }: { faction: string | null }) {
  const config = getFactionConfig(faction as Faction | null)
  return (
    <div
      className="flex items-center justify-center"
      style={{
        width: 64,
        height: 78,
        clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
        background: `linear-gradient(180deg, var(${config.secondaryVar}), color-mix(in srgb, var(${config.secondaryVar}) 60%, black) 70%)`,
      }}
    >
      <div className="relative" style={{ width: 12, height: 44, marginTop: -4 }}>
        <div
          className="absolute left-1/2 top-0 -translate-x-1/2 rounded-sm"
          style={{ width: 4, height: 32, background: 'linear-gradient(180deg, var(--gold-light), var(--gold))' }}
        />
        <div
          className="absolute left-1/2 top-[7px] -translate-x-1/2 rounded-sm bg-gold-light"
          style={{ width: 18, height: 3 }}
        />
        <div
          className="absolute left-1/2 top-px -translate-x-1/2 rounded-full bg-gold-light"
          style={{ width: 8, height: 8 }}
        />
      </div>
    </div>
  )
}

export default function MatchmakingModal({ open, onCancel }: MatchmakingModalProps) {
  const navigate = useNavigate()
  const phase = useMatchmakingStore((s) => s.phase)
  const matchedGameId = useMatchmakingStore((s) => s.matchedGameId)
  const user = useAuthStore((s) => s.user)
  const activeDeck = useHubStore((s) => s.activeDeck)

  useMatchmakingSocket(open)

  useEffect(() => {
    if (phase !== 'found' || !matchedGameId) return
    const timer = setTimeout(() => {
      navigate(`/game/${matchedGameId}`)
    }, 1500)
    return () => clearTimeout(timer)
  }, [phase, matchedGameId, navigate])

  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-[70] flex items-center justify-center"
      style={{ background: 'rgba(8,5,2,.86)', backdropFilter: 'blur(5px)' }}
    >
      <div
        className="mq-panel relative w-[440px] rounded-lg px-[42px] py-12 flex flex-col items-center gap-6"
      >
        {/* Header label */}
        <div
          className="text-[10.5px] tracking-[4px] uppercase font-bold text-gold font-heading"
        >
          {phase === 'found' ? 'Partida encontrada!' : 'Matchmaking'}
        </div>

        {phase === 'searching' && (
          <>
            <div className="mq-spinner" />
            <div
              className="text-[20px] font-bold text-gold-light tracking-[1px] font-display"
            >
              Procurando adversário...
            </div>
            <p
              className="text-[13px] text-text-muted text-center font-body"
            >
              Aguarde enquanto buscamos um oponente digno.
            </p>
            <button className="mq-cancel-btn" onClick={onCancel}>
              Cancelar
            </button>
          </>
        )}

        {phase === 'found' && (
          <>
            <div className="flex items-center gap-8 mt-2">
              <div className="flex flex-col items-center gap-2">
                <FactionShield faction={activeDeck?.faction ?? null} />
                <span
                  className="text-[11px] tracking-[2px] uppercase font-bold text-gold font-heading"
                >
                  {user?.username ?? 'Você'}
                </span>
              </div>
              <div className="mq-vs-text">VS</div>
              <div className="flex flex-col items-center gap-2">
                <FactionShield faction={null} />
                <span
                  className="text-[11px] tracking-[2px] uppercase font-bold text-text-muted font-heading"
                >
                  Oponente
                </span>
              </div>
            </div>
            <p
              className="text-[13px] text-text-secondary font-body"
            >
              Iniciando duelo...
            </p>
          </>
        )}
      </div>
    </div>
  )
}
