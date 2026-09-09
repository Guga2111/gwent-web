import { useState } from 'react'
import { Flag, MessageCircle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import SurrenderDialog from './SurrenderDialog'

interface ControlBarProps {
  onSurrender: () => void
}

export default function ControlBar({ onSurrender }: ControlBarProps) {
  const [showDialog, setShowDialog] = useState(false)

  return (
    <>
      <div className="flex flex-row items-center gap-3">
        <Button
          onClick={() => setShowDialog(true)}
          data-tooltip="Desistir"
          variant="board-icon"
          size="icon"
          className="w-12 h-12 text-red"
        >
          <Flag size={20} strokeWidth={1.5} />
        </Button>
        <Button data-tooltip="Chat" data-tooltip-pos="right" variant="board-icon" size="icon" className='w-12 h-12 text-green-600'>
          <MessageCircle size={20} strokeWidth={1.5}/>
        </Button>
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
