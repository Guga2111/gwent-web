import type { PlayerStateDto, OpponentStateDto } from '@/types/game'
import PrimaryButton from '@/components/ui/PrimaryButton'

interface GameOverOverlayProps {
  myState: PlayerStateDto
  opponentState: OpponentStateDto
  onBack: () => void
}

export default function GameOverOverlay({ myState, opponentState, onBack }: GameOverOverlayProps) {
  const won = myState.lives > opponentState.lives

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        backgroundColor: 'var(--bg-darkest)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 20,
        zIndex: 50,
      }}
    >
      <h1
        style={{
          fontFamily: 'var(--font-display)',
          fontSize: 36,
          color: won ? 'var(--gold-light)' : 'var(--text-muted)',
          textShadow: won ? '0 0 20px rgba(246, 221, 151, 0.5)' : 'none',
        }}
      >
        {won ? 'Vitória!' : 'Derrota...'}
      </h1>

      <div
        style={{
          display: 'flex',
          gap: 48,
          fontFamily: 'var(--font-heading)',
          fontSize: 18,
        }}
      >
        <div style={{ textAlign: 'center' }}>
          <div style={{ color: 'var(--text-muted)', fontSize: 12, marginBottom: 4 }}>Você</div>
          <div style={{ color: 'var(--gold-light)', fontSize: 28 }}>{myState.score}</div>
          <div style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
            {myState.lives} vidas
          </div>
        </div>
        <div style={{ textAlign: 'center' }}>
          <div style={{ color: 'var(--text-muted)', fontSize: 12, marginBottom: 4 }}>Oponente</div>
          <div style={{ color: 'var(--text-secondary)', fontSize: 28 }}>{opponentState.score}</div>
          <div style={{ color: 'var(--text-secondary)', fontSize: 14 }}>
            {opponentState.lives} vidas
          </div>
        </div>
      </div>

      <PrimaryButton onClick={onBack} style={{ marginTop: 12 }}>
        Voltar à Taverna
      </PrimaryButton>
    </div>
  )
}
