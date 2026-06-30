export default function Shop() {
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
          background: 'linear-gradient(155deg, #e9d6a6, #dcc795 55%, #cbb279)',
          boxShadow: '0 16px 34px rgba(0,0,0,.55), inset 0 0 36px rgba(150,115,60,.32)',
          textAlign: 'center',
          color: '#3d2b14',
        }}
      >
        <div
          style={{
            fontFamily: 'var(--font-display)',
            fontWeight: 700,
            fontSize: 26,
            color: '#5a3f1c',
            marginBottom: 8,
          }}
        >
          Mercador
        </div>
        <div
          style={{
            fontFamily: 'var(--font-body)',
            fontStyle: 'italic',
            fontSize: 15,
            color: '#7a5e34',
          }}
        >
          Em breve
        </div>
      </div>
    </div>
  )
}
