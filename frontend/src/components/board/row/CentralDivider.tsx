import { Swords } from 'lucide-react'

export default function CentralDivider() {
  return (
    <div
      style={{
        position: 'relative',
        height: 1,
        backgroundColor: 'var(--border-gold)',
        flexShrink: 0,
      }}
    >
      <div
        style={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: 32,
          height: 32,
          borderRadius: '50%',
          backgroundColor: 'var(--bg-dark)',
          border: '1px solid var(--border-gold)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'var(--gold)',
          zIndex: 1,
        }}
      >
        <Swords size={16} strokeWidth={1.5} />
      </div>
    </div>
  )
}
