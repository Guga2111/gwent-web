import { useEffect, useState } from 'react'

const SIZE = 52
const STROKE = 3
const RADIUS = (SIZE - STROKE) / 2
const CIRCUMFERENCE = 2 * Math.PI * RADIUS
const DURATION_MS = 30_000

interface OverlayCountdownProps {
  deadlineUtc: number | null
}

export default function OverlayCountdown({ deadlineUtc }: OverlayCountdownProps) {
  const [remainingMs, setRemainingMs] = useState(() =>
    deadlineUtc ? Math.max(0, deadlineUtc - Date.now()) : DURATION_MS,
  )

  useEffect(() => {
    if (deadlineUtc == null) return
    const tick = () => setRemainingMs(Math.max(0, deadlineUtc - Date.now()))
    tick()
    const id = setInterval(tick, 100)
    return () => clearInterval(id)
  }, [deadlineUtc])

  const remainingSeconds = Math.ceil(remainingMs / 1000)
  const progress = Math.min(1, remainingMs / DURATION_MS)
  const offset = CIRCUMFERENCE * (1 - progress)
  const isUrgent = remainingMs <= 10_000

  return (
    <div className="absolute top-6 right-6">
      <svg width={SIZE} height={SIZE} className="-rotate-90">
        <circle
          cx={SIZE / 2}
          cy={SIZE / 2}
          r={RADIUS}
          fill="none"
          stroke="var(--bg-medium)"
          strokeWidth={STROKE}
        />
        <circle
          cx={SIZE / 2}
          cy={SIZE / 2}
          r={RADIUS}
          fill="none"
          stroke={isUrgent ? 'var(--red)' : 'var(--gold-dark)'}
          strokeWidth={STROKE}
          strokeDasharray={CIRCUMFERENCE}
          strokeDashoffset={offset}
          strokeLinecap="round"
        />
      </svg>
      <span
        className="absolute inset-0 flex items-center justify-center text-sm font-bold"
        style={{
          fontFamily: 'var(--font-heading)',
          color: isUrgent ? 'var(--red)' : 'var(--gold-light)',
        }}
      >
        {remainingSeconds}
      </span>
    </div>
  )
}
