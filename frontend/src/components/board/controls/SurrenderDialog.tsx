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
        className="flex flex-col items-center gap-5 px-10 py-8 rounded-lg border border-border-gold bg-bg-dark"
        onClick={(e) => e.stopPropagation()}
      >
        <h2
          className="text-xl font-heading text-gold-light"
        >
          Desistir da Partida?
        </h2>
        <p
          className="text-sm font-body text-text-secondary"
        >
          Esta acao nao pode ser desfeita.
        </p>
        <div className="flex gap-4 mt-2">
          <button
            onClick={onCancel}
            className="px-5 py-2 rounded border border-border-subtle bg-transparent text-text-secondary text-sm cursor-pointer hover:bg-bg-medium"
          >
            Cancelar
          </button>
          <button
            onClick={onConfirm}
            className="px-5 py-2 rounded border border-red text-sm cursor-pointer bg-red text-text-primary"
          >
            Confirmar
          </button>
        </div>
      </div>
    </div>
  )
}
