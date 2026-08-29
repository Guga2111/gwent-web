import { useState, useEffect } from "react"

interface DisconnectedBannerProps {
  opponentConnected: boolean
  forfeitDeadlineUtc: number | null
}

export default function DisconnectedBanner({ opponentConnected, forfeitDeadlineUtc }: DisconnectedBannerProps) {
  const [remaining, setRemaining] = useState<number | null>(null)

  useEffect(() => {
    if (opponentConnected || forfeitDeadlineUtc == null) {
      setRemaining(null)
      return
    }

    const tick = () => {
      const left = Math.max(0, Math.ceil((forfeitDeadlineUtc - Date.now()) / 1000))
      setRemaining(left)
    }

    tick()
    const id = setInterval(tick, 1000)
    return () => clearInterval(id)
  }, [opponentConnected, forfeitDeadlineUtc])

  if (opponentConnected || remaining == null) return null

  const minutes = Math.floor(remaining / 60)
  const seconds = remaining % 60
  const display = `${minutes}:${String(seconds).padStart(2, "0")}`

  return (
    <div className="disconnected-banner">
      Oponente desconectado. Desistencia automatica em {display}
    </div>
  )
}
