interface ComingSoonPageProps {
  title: string
}

export default function ComingSoonPage({ title }: ComingSoonPageProps) {
  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <div
        style={{
          padding: '40px 60px',
          borderRadius: 9,
          background: 'linear-gradient(155deg, var(--parchment-light), var(--parchment-mid) 55%, var(--parchment-dark))',
          boxShadow: '0 16px 34px rgba(0,0,0,.55), inset 0 0 36px rgba(150,115,60,.32)',
          textAlign: 'center',
        }}
      >
        <div
          style={{
            fontFamily: 'var(--font-display)',
            fontWeight: 700,
            fontSize: 26,
            color: 'var(--parchment-heading)',
            marginBottom: 8,
          }}
        >
          {title}
        </div>
        <div
          style={{
            fontFamily: 'var(--font-body)',
            fontStyle: 'italic',
            fontSize: 15,
            color: 'var(--parchment-muted)',
          }}
        >
          Em breve
        </div>
      </div>
    </div>
  )
}
