export default function CentralDivider() {
  return (
    <div
      style={{
        position: 'relative',
        height: 1,
        backgroundColor: 'var(--border-gold)',
        margin: '0 20px',
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
          fontSize: 16,
          color: 'var(--gold)',
          zIndex: 1,
        }}
      >
        ⚔
      </div>
    </div>
  )
}
