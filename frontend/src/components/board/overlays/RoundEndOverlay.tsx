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
    <div className="board-overlay" style={{ backgroundColor: 'rgba(13, 10, 7, 0.8)' }}>
      <div
        className="text-[16px] text-[var(--text-secondary)]"
        style={{ fontFamily: 'var(--font-heading)' }}
      >
        Round {round}
      </div>

      <div
        className="flex gap-8 items-center text-[28px]"
        style={{ fontFamily: 'var(--font-heading)' }}
      >
        <span className="text-[var(--gold-light)]">{myScore}</span>
        <span className="text-[var(--text-muted)] text-[16px]">vs</span>
        <span className="text-[var(--text-secondary)]">{opponentScore}</span>
      </div>

      <div
        className="text-[22px]"
        style={{ fontFamily: 'var(--font-display)', color: resultColor }}
      >
        {result}
      </div>
    </div>
  )
}
