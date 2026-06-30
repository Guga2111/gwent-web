import { useEffect, useState } from 'react'

interface RoundEndOverlayProps {
  round: number
  myScore: number
  opponentScore: number
}

export default function RoundEndOverlay({ round, myScore, opponentScore }: RoundEndOverlayProps) {
  const [visible, setVisible] = useState(true)

  useEffect(() => {
    const timer = setTimeout(() => setVisible(false), 3000)
    return () => clearTimeout(timer)
  }, [])

  if (!visible) return null

  const result =
    myScore > opponentScore
      ? 'Você venceu o round!'
      : myScore < opponentScore
        ? 'Oponente venceu o round!'
        : 'Empate!'

  const resultColor =
    myScore > opponentScore
      ? 'var(--green)'
      : myScore < opponentScore
        ? 'var(--red)'
        : 'var(--gold)'

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        backgroundColor: 'rgba(13, 10, 7, 0.8)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 16,
        zIndex: 50,
      }}
    >
      <div style={{ fontFamily: 'var(--font-heading)', fontSize: 16, color: 'var(--text-secondary)' }}>
        Round {round}
      </div>

      <div
        style={{
          display: 'flex',
          gap: 32,
          alignItems: 'center',
          fontFamily: 'var(--font-heading)',
          fontSize: 28,
        }}
      >
        <span style={{ color: 'var(--gold-light)' }}>{myScore}</span>
        <span style={{ color: 'var(--text-muted)', fontSize: 16 }}>vs</span>
        <span style={{ color: 'var(--text-secondary)' }}>{opponentScore}</span>
      </div>

      <div style={{ fontFamily: 'var(--font-display)', fontSize: 22, color: resultColor }}>
        {result}
      </div>
    </div>
  )
}
