import { useState } from 'react'
import OverlayCountdown from './OverlayCountdown'

interface ScoiataelOverlayProps {
  onChoose: (goFirst: boolean) => void
  abilityDeadlineUtc: number | null
}

export default function ScoiataelOverlay({ onChoose, abilityDeadlineUtc }: ScoiataelOverlayProps) {
  const [submitted, setSubmitted] = useState(false);

  const handleChoose = (goFirst: boolean) => {
    if (submitted) return;
    setSubmitted(true);
    onChoose(goFirst);
  }

  return (
    <div className="board-overlay">
      <OverlayCountdown deadlineUtc={abilityDeadlineUtc} />
      <h2 className="text-2xl text-gold-light font-display">Vantagem Scoia'tael</h2>
      <p className="text-sm text-text-secondary font-body">
        Como líder dos Scoia'tael, você escolhe quem joga primeiro nesta rodada.
      </p>

      <div className="flex gap-6 justify-center mt-4">
        <button
          onClick={() => handleChoose(true)}
          disabled={submitted}
          className="px-8 py-3 rounded-md font-semibold text-base cursor-pointer transition-colors duration-150 border-2 border-gold-dark bg-gold-dark text-bg-darkest hover:bg-gold hover:border-gold font-heading"
        >
          Jogar Primeiro
        </button>
        <button
          onClick={() => handleChoose(false)}
          disabled={submitted}
          className="px-8 py-3 rounded-md font-semibold text-base cursor-pointer transition-colors duration-150 border-2 border-gold-dark bg-transparent text-gold-light hover:bg-gold-dark/20 font-heading"
        >
          Jogar Segundo
        </button>
      </div>
    </div>
  )
}
