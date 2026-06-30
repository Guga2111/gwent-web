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
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 6,
      }}
    >
      {ACTIONS.map(({ label, Icon, danger }, i) => (
        <button
          key={label}
          onClick={handlers[i]}
          title={label}
          style={{
            width: 36,
            height: 36,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: 'var(--bg-medium)',
            border: '1px solid var(--border-subtle)',
            borderRadius: 4,
            color: danger ? 'var(--red)' : 'var(--text-muted)',
            cursor: 'pointer',
          }}
        >
          <Icon size={16} strokeWidth={1.5} />
        </button>
      ))}
    </div>
  )
}
