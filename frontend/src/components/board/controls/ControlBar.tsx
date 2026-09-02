import { useState } from 'react'
import { Flag, Check } from 'lucide-react'
import SurrenderDialog from './SurrenderDialog'

interface ControlBarProps {
  onSurrender: () => void
  selectedCardId: string | null
  onConfirmPlay: () => void
}

export default function ControlBar({ onSurrender, selectedCardId, onConfirmPlay }: ControlBarProps) {
  const [showDialog, setShowDialog] = useState(false)
  const canPlay = selectedCardId !== null

  return (
    <>
      <div className="flex flex-row items-center gap-3">
        <button
          onClick={() => setShowDialog(true)}
          data-tooltip="Desistir"
          className="w-12 h-12 flex items-center justify-center bg-bg-medium border border-border-subtle rounded cursor-pointer"
          style={{ color: 'var(--red)' }}
        >
          <Flag size={20} strokeWidth={1.5} />
        </button>
        <button
          onClick={canPlay ? onConfirmPlay : undefined}
          data-tooltip="Jogar carta"
          className="w-12 h-12 flex items-center justify-center bg-bg-medium border border-border-subtle rounded"
          style={{
            color: canPlay ? 'var(--text-muted)' : 'var(--text-muted)',
            opacity: canPlay ? 1 : 0.3,
            cursor: canPlay ? 'pointer' : 'not-allowed',
          }}
        >
          <Check size={20} strokeWidth={1.5} />
        </button>
      </div>
      <SurrenderDialog
        open={showDialog}
        onConfirm={() => {
          setShowDialog(false)
          onSurrender()
        }}
        onCancel={() => setShowDialog(false)}
      />
    </>
  )
}
