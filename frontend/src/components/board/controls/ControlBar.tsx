import { Flag, ZoomIn, CornerDownLeft } from 'lucide-react'

interface ControlBarProps {
  onSurrender: () => void
}

const ACTIONS = [
  { label: 'Desistir', Icon: Flag, danger: true },
  { label: 'Ampliar', Icon: ZoomIn, danger: false },
  { label: 'Selecionar', Icon: CornerDownLeft, danger: false },
]

export default function ControlBar({ onSurrender }: ControlBarProps) {
  const handlers = [onSurrender, () => {}, () => {}]

  return (
    <div className="flex flex-col items-center gap-1.5">
      {ACTIONS.map(({ label, Icon, danger }, i) => (
        <button
          key={label}
          onClick={handlers[i]}
          title={label}
          className="w-9 h-9 flex items-center justify-center bg-[var(--bg-medium)] border border-[var(--border-subtle)] rounded cursor-pointer"
          style={{ color: danger ? 'var(--red)' : 'var(--text-muted)' }}
        >
          <Icon size={16} strokeWidth={1.5} />
        </button>
      ))}
    </div>
  )
}
