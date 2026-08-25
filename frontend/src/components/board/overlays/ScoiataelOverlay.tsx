interface ScoiataelOverlayProps {
  onChoose: (goFirst: boolean) => void
}

export default function ScoiataelOverlay({ onChoose }: ScoiataelOverlayProps) {
  return (
    <div className="board-overlay">
      <h2 className="overlay-title">Vantagem Scoia'tael</h2>
      <p className="overlay-body">
        Como líder dos Scoia'tael, você escolhe quem joga primeiro nesta rodada.
      </p>

      <div className="flex gap-6 justify-center mt-4">
        <button
          onClick={() => onChoose(true)}
          className="px-8 py-3 rounded-md font-semibold text-base cursor-pointer transition-colors duration-150 border-2 border-[var(--gold-dark)] bg-[var(--gold-dark)] text-[var(--bg-darkest)] hover:bg-[var(--gold)] hover:border-[var(--gold)]"
          style={{ fontFamily: 'var(--font-heading)' }}
        >
          Jogar Primeiro
        </button>
        <button
          onClick={() => onChoose(false)}
          className="px-8 py-3 rounded-md font-semibold text-base cursor-pointer transition-colors duration-150 border-2 border-[var(--gold-dark)] bg-transparent text-[var(--gold-light)] hover:bg-[var(--gold-dark)]/20"
          style={{ fontFamily: 'var(--font-heading)' }}
        >
          Jogar Segundo
        </button>
      </div>
    </div>
  )
}
