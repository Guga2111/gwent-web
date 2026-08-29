import type { PlayerStateDto, OpponentStateDto } from '@/types/game'
import PrimaryButton from '@/components/ui/PrimaryButton'

interface GameOverOverlayProps {
  myState: PlayerStateDto
  opponentState: OpponentStateDto
  winner: string | null
  disconnectForfeit: boolean
  onBack: () => void
}

export default function GameOverOverlay({ myState, opponentState, winner, disconnectForfeit, onBack }: GameOverOverlayProps) {
  const won = winner === myState.playerId

  const title = disconnectForfeit
    ? (won ? 'Oponente Desconectou' : 'Desconectado')
    : (won ? 'Vitória!' : 'Derrota...')

  const subtitle = disconnectForfeit
    ? (won ? 'Vitória concedida' : 'Partida perdida por desconexão')
    : null

  const titleColor = disconnectForfeit ? 'var(--text-secondary)' : (won ? 'var(--gold-light)' : 'var(--text-muted)')
  const titleGlow = !disconnectForfeit && won ? '0 0 20px rgba(246, 221, 151, 0.5)' : 'none'

  return (
    <div className="absolute inset-0 bg-[var(--bg-darkest)] flex flex-col items-center justify-center gap-5 z-50">
      <h1
        className="text-[36px]"
        style={{
          fontFamily: 'var(--font-display)',
          color: titleColor,
          textShadow: titleGlow,
        }}
      >
        {title}
      </h1>
      {subtitle && (
        <p
          className="text-[18px]"
          style={{
            fontFamily: 'var(--font-heading)',
            color: 'var(--text-muted)',
          }}
        >
          {subtitle}
        </p>
      )}

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
