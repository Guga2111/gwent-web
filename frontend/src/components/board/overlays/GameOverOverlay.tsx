import type { PlayerStateDto, OpponentStateDto } from '@/types/game'
import PrimaryButton from '@/components/ui/PrimaryButton'

interface GameOverOverlayProps {
  myState: PlayerStateDto
  opponentState: OpponentStateDto
  winner: string | null
  onBack: () => void
}

export default function GameOverOverlay({ myState, opponentState, winner, onBack }: GameOverOverlayProps) {
  const won = winner === myState.playerId

  return (
    <div className="absolute inset-0 bg-[var(--bg-darkest)] flex flex-col items-center justify-center gap-5 z-50">
      <h1
        className="text-[36px]"
        style={{
          fontFamily: 'var(--font-display)',
          color: won ? 'var(--gold-light)' : 'var(--text-muted)',
          textShadow: won ? '0 0 20px rgba(246, 221, 151, 0.5)' : 'none',
        }}
      >
        {won ? 'Vitória!' : 'Derrota...'}
      </h1>

      <div
        className="flex gap-12 text-[18px]"
        style={{ fontFamily: 'var(--font-heading)' }}
      >
        <div className="text-center">
          <div className="text-xs text-[var(--text-muted)] mb-1">Você</div>
          <div className="text-[28px] text-[var(--gold-light)]">{myState.score}</div>
          <div className="text-sm text-[var(--text-secondary)]">{myState.lives} vidas</div>
        </div>
        <div className="text-center">
          <div className="text-xs text-[var(--text-muted)] mb-1">Oponente</div>
          <div className="text-[28px] text-[var(--text-secondary)]">{opponentState.score}</div>
          <div className="text-sm text-[var(--text-secondary)]">{opponentState.lives} vidas</div>
        </div>
      </div>

      <PrimaryButton onClick={onBack} style={{ marginTop: 12 }}>
        Voltar à Taverna
      </PrimaryButton>
    </div>
  )
}
