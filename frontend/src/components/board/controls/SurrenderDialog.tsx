interface SurrenderDialogProps {
  open: boolean
  onConfirm: () => void
  onCancel: () => void
}

export default function SurrenderDialog({ open, onConfirm, onCancel }: SurrenderDialogProps) {
  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-100 flex items-center justify-center"
      style={{ backgroundColor: 'rgba(13, 10, 7, 0.85)' }}
      onClick={onCancel}
    >
      <div
        className="flex flex-col items-center gap-5 px-10 py-8 rounded-lg border border-[var(--border-gold)] bg-[var(--bg-dark)]"
        onClick={(e) => e.stopPropagation()}
      >
        <h2
          className="text-xl"
          style={{ fontFamily: 'var(--font-heading)', color: 'var(--gold-light)' }}
        >
          Desistir da Partida?
        </h2>
        <p
          className="text-sm"
          style={{ fontFamily: 'var(--font-body)', color: 'var(--text-secondary)' }}
        >
          Esta acao nao pode ser desfeita.
        </p>
        <div className="flex gap-4 mt-2">
          <button
            onClick={onCancel}
            className="px-5 py-2 rounded border border-[var(--border-subtle)] bg-transparent text-[var(--text-secondary)] text-sm cursor-pointer hover:bg-[var(--bg-medium)]"
          >
            Cancelar
          </button>
          <button
            onClick={onConfirm}
            className="px-5 py-2 rounded border border-[var(--red)] text-sm cursor-pointer"
            style={{ backgroundColor: 'var(--red)', color: 'var(--text-primary)' }}
          >
            Confirmar
          </button>
        </div>
      </div>
    </div>
  )
}
