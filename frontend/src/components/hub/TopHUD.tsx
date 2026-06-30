import { useAuthStore } from '@/stores/authStore'

export default function TopHUD({ onSettingsClick }: { onSettingsClick: () => void }) {
  const user = useAuthStore((s) => s.user)

  return (
    <header
      style={{
        position: 'relative',
        zIndex: 3,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '16px 30px 10px',
        flexShrink: 0,
      }}
    >
      {/* Left: Avatar + Player info */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
        <div style={{ position: 'relative', flexShrink: 0 }}>
          <div
            style={{
              width: 52,
              height: 64,
              background: 'linear-gradient(180deg, var(--gold-light), var(--gold) 60%, var(--gold-dim))',
              clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              filter: 'drop-shadow(0 4px 8px rgba(0,0,0,.5))',
            }}
          >
            <div
              style={{
                width: 42,
                height: 54,
                clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
                background: 'repeating-linear-gradient(45deg, #2a4258 0 5px, #21384b 5px 10px)',
              }}
            />
          </div>
          <div
            style={{
              position: 'absolute',
              bottom: -6,
              left: '50%',
              transform: 'translateX(-50%)',
              background: 'linear-gradient(180deg, var(--gold-light), var(--gold-dark))',
              color: 'var(--bg-darkest)',
              fontFamily: 'var(--font-heading)',
              fontWeight: 700,
              fontSize: '10.5px',
              padding: '1px 8px',
              borderRadius: 9,
              boxShadow: '0 2px 4px rgba(0,0,0,.5)',
            }}
          >
            34
          </div>
        </div>
        <div>
          <div
            style={{
              fontFamily: 'var(--font-heading)',
              fontWeight: 700,
              fontSize: 18,
              color: 'var(--text-primary)',
              letterSpacing: '.3px',
            }}
          >
            {user?.username ?? user?.email ?? 'Jogador'}
          </div>
          <div
            style={{
              fontFamily: 'var(--font-body)',
              fontStyle: 'italic',
              fontSize: 13,
              color: 'var(--text-muted)',
              marginTop: 1,
            }}
          >
            &laquo; aventureiro &raquo;
          </div>
        </div>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            marginLeft: 6,
            paddingLeft: 16,
            borderLeft: '1px solid rgba(240,205,120,.16)',
          }}
        >
          <div
            style={{
              width: 15,
              height: 18,
              background: 'linear-gradient(180deg, #cfe0ee, var(--blue))',
              clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
            }}
          />
          <span
            style={{
              fontFamily: 'var(--font-heading)',
              fontWeight: 600,
              fontSize: 13,
              color: 'var(--gold)',
              letterSpacing: '.5px',
            }}
          >
            Prata II
          </span>
          <span
            style={{
              fontFamily: 'var(--font-body)',
              fontStyle: 'italic',
              fontSize: 13,
              color: 'var(--text-muted)',
            }}
          >
            · 2.480 PR
          </span>
        </div>
      </div>

      {/* Center: Logo */}
      <div
        style={{
          position: 'absolute',
          left: '50%',
          top: 14,
          transform: 'translateX(-50%)',
          textAlign: 'center',
          pointerEvents: 'none',
        }}
      >
        <div
          style={{
            fontFamily: 'var(--font-display)',
            fontWeight: 900,
            fontSize: 22,
            letterSpacing: 6,
            color: 'var(--gold-light)',
            textShadow: '0 1px 0 rgba(0,0,0,.7), 0 0 20px rgba(240,200,110,.3)',
          }}
        >
          GWENT
        </div>
        <div
          style={{
            fontFamily: 'var(--font-body)',
            fontStyle: 'italic',
            fontSize: 11,
            letterSpacing: 1,
            color: 'var(--text-muted)',
            marginTop: 1,
          }}
        >
          a taverna do Continente
        </div>
      </div>

      {/* Right: Currencies + Settings */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 11 }}>
        {/* Coroas */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 9,
            padding: '7px 15px 7px 7px',
            borderRadius: 24,
            background: 'linear-gradient(180deg, var(--bg-card), var(--bg-dark))',
            boxShadow: 'inset 0 0 0 1px rgba(240,205,120,.28), 0 4px 10px rgba(0,0,0,.4)',
          }}
        >
          <div
            style={{
              width: 28,
              height: 28,
              borderRadius: '50%',
              background: 'radial-gradient(circle at 35% 30%, var(--gold-light), var(--gold-dark))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--bg-darkest)',
            }}
          >
            <svg viewBox="0 0 24 24" style={{ width: 15, height: 15, fill: 'none', stroke: 'currentColor', strokeWidth: 2, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
              <circle cx="8" cy="8" r="6" />
              <path d="M18.09 10.37A6 6 0 1 1 10.34 18" />
              <path d="M7 6h1v4" />
            </svg>
          </div>
          <div style={{ lineHeight: 1 }}>
            <div style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, color: 'var(--gold-light)', fontSize: 15 }}>1.450</div>
            <div style={{ fontSize: '8.5px', letterSpacing: '1.5px', textTransform: 'uppercase', color: 'var(--text-muted)' }}>coroas</div>
          </div>
        </div>

        {/* Sucata */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 9,
            padding: '7px 15px 7px 7px',
            borderRadius: 24,
            background: 'linear-gradient(180deg, var(--bg-card), var(--bg-dark))',
            boxShadow: 'inset 0 0 0 1px rgba(140,180,210,.28), 0 4px 10px rgba(0,0,0,.4)',
          }}
        >
          <div
            style={{
              width: 28,
              height: 28,
              borderRadius: '50%',
              background: 'radial-gradient(circle at 35% 30%, #bfe0f4, var(--blue))',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#16314d',
            }}
          >
            <svg viewBox="0 0 24 24" style={{ width: 15, height: 15, fill: 'none', stroke: 'currentColor', strokeWidth: 2, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
              <path d="M6 3h12l4 6-10 13L2 9Z" />
              <path d="M2 9h20" />
            </svg>
          </div>
          <div style={{ lineHeight: 1 }}>
            <div style={{ fontFamily: 'var(--font-heading)', fontWeight: 700, color: '#c8def0', fontSize: 15 }}>820</div>
            <div style={{ fontSize: '8.5px', letterSpacing: '1.5px', textTransform: 'uppercase', color: '#5e7790' }}>sucata</div>
          </div>
        </div>

        {/* Settings gear */}
        <button
          onClick={onSettingsClick}
          style={{
            background: 'transparent',
            border: 'none',
            cursor: 'pointer',
            color: 'var(--gold)',
            padding: 8,
            borderRadius: '50%',
          }}
        >
          <svg viewBox="0 0 24 24" style={{ width: 18, height: 18, fill: 'none', stroke: 'currentColor', strokeWidth: 1.8, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
            <circle cx="12" cy="12" r="3" />
            <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 8 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H2a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 3.6 8a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H8a1.65 1.65 0 0 0 1-1.51V2a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V8a1.65 1.65 0 0 0 1.51 1H22a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
          </svg>
        </button>
      </div>
    </header>
  )
}
