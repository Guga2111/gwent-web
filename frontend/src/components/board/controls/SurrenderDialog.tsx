import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog'

interface SurrenderDialogProps {
  open: boolean
  onConfirm: () => void
  onCancel: () => void
}

export default function SurrenderDialog({ open, onConfirm, onCancel }: SurrenderDialogProps) {
  return (
    <Dialog open={open} onOpenChange={(v) => !v && onCancel()}>
      <DialogContent className="mq-panel border-none max-w-[340px] sm:max-w-[340px] px-10 py-8" showCloseButton={false}>
        <DialogHeader className="text-center sm:text-center items-center">
          <DialogTitle className="text-xl font-heading text-gold-light">
            Desistir da Partida?
          </DialogTitle>
          <DialogDescription className="text-sm font-body text-text-secondary">
            Esta acao nao pode ser desfeita.
          </DialogDescription>
        </DialogHeader>

        <div className="flex gap-4 justify-center">
          <Button
            onClick={onCancel}
            variant="cancel"
            className="px-5 py-2"
          >
            Cancelar
          </Button>
          <Button
            onClick={onConfirm}
            variant="destructive"
            className="px-5 py-2"
          >
            Confirmar
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  )
}
