export default function TownCrier() {
  return (
    <div
      style={{
        position: 'relative',
        zIndex: 3,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 11,
        padding: '7px 24px',
        color: 'var(--gold)',
        flexShrink: 0,
      }}
    >
      <svg
        viewBox="0 0 24 24"
        style={{
          width: 15,
          height: 15,
          fill: 'none',
          stroke: 'currentColor',
          strokeWidth: 2,
          strokeLinecap: 'round',
          strokeLinejoin: 'round',
          flexShrink: 0,
        }}
      >
        <path d="M3 11h3l9-5v12l-9-5H3z" />
        <path d="M16 9a3 3 0 0 1 0 6" />
      </svg>
      <span
        style={{
          fontFamily: 'var(--font-body)',
          fontStyle: 'italic',
          fontSize: 14,
          color: 'var(--text-secondary)',
        }}
      >
        Bem-vindo à Taverna do Continente. Encontre seus oponentes e forje seus baralhos.
      </span>
    </div>
  )
}
