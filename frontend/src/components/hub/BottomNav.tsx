type TabId = 'home' | 'deck' | 'shop' | 'rank' | 'profile'

interface BottomNavProps {
  activeTab: TabId
  setActiveTab: (tab: TabId) => void
}

const tabs: { id: TabId; label: string; icon: JSX.Element }[] = [
  {
    id: 'home',
    label: 'Taverna',
    icon: (
      <svg viewBox="0 0 24 24" style={{ width: 19, height: 19, fill: 'none', stroke: 'currentColor', strokeWidth: 1.9, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
        <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
        <path d="M9 22V12h6v10" />
      </svg>
    ),
  },
  {
    id: 'deck',
    label: 'Forja de Baralhos',
    icon: (
      <svg viewBox="0 0 24 24" style={{ width: 19, height: 19, fill: 'none', stroke: 'currentColor', strokeWidth: 1.9, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
        <path d="m12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83Z" />
        <path d="M2 12a1 1 0 0 0 .58.91l8.6 3.91a2 2 0 0 0 1.65 0l8.58-3.9A1 1 0 0 0 22 12" />
      </svg>
    ),
  },
  {
    id: 'shop',
    label: 'Mercador',
    icon: (
      <svg viewBox="0 0 24 24" style={{ width: 19, height: 19, fill: 'none', stroke: 'currentColor', strokeWidth: 1.9, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
        <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z" />
        <path d="M3 6h18" />
        <path d="M16 10a4 4 0 0 1-8 0" />
      </svg>
    ),
  },
  {
    id: 'rank',
    label: 'Salão da Glória',
    icon: (
      <svg viewBox="0 0 24 24" style={{ width: 19, height: 19, fill: 'none', stroke: 'currentColor', strokeWidth: 1.9, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
        <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6" />
        <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18" />
        <path d="M4 22h16" />
        <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22" />
        <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22" />
        <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z" />
      </svg>
    ),
  },
  {
    id: 'profile',
    label: 'Brasão',
    icon: (
      <svg viewBox="0 0 24 24" style={{ width: 19, height: 19, fill: 'none', stroke: 'currentColor', strokeWidth: 1.9, strokeLinecap: 'round', strokeLinejoin: 'round' }}>
        <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
        <circle cx="12" cy="7" r="4" />
      </svg>
    ),
  },
]

export default function BottomNav({ activeTab, setActiveTab }: BottomNavProps) {
  return (
    <nav
      style={{
        position: 'relative',
        zIndex: 3,
        display: 'flex',
        alignItems: 'flex-end',
        justifyContent: 'center',
        gap: 14,
        padding: '6px 0 22px',
        flexShrink: 0,
      }}
    >
      {tabs.map((tab) => {
        const active = activeTab === tab.id
        return (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            style={{
              position: 'relative',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 7,
              width: 122,
              padding: '13px 10px 12px',
              border: 'none',
              cursor: 'pointer',
              borderRadius: 9,
              background: 'linear-gradient(180deg, #352616, #241809)',
              boxShadow:
                'inset 0 0 0 1px rgba(20,12,5,.9), inset 0 0 0 2px rgba(240,205,120,.14), 0 9px 20px rgba(0,0,0,.45)',
            }}
          >
            {/* Glow overlay */}
            <div
              style={{
                position: 'absolute',
                inset: 0,
                borderRadius: 9,
                background: active
                  ? 'linear-gradient(180deg, rgba(240,210,122,.2), rgba(240,210,122,.04))'
                  : 'transparent',
                boxShadow: active
                  ? 'inset 0 0 0 2px rgba(240,210,122,.6), 0 0 22px rgba(240,200,110,.3)'
                  : 'none',
                pointerEvents: 'none',
                transition: 'all .2s',
              }}
            />
            {/* Medallion icon */}
            <div
              style={{
                position: 'relative',
                flexShrink: 0,
                width: 40,
                height: 40,
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: active
                  ? 'radial-gradient(circle at 35% 30%, var(--gold-light), var(--gold-dark))'
                  : 'rgba(0,0,0,.32)',
                boxShadow: active
                  ? '0 3px 8px rgba(0,0,0,.45)'
                  : 'inset 0 0 0 1px rgba(240,205,120,.18)',
                color: active ? 'var(--bg-darkest)' : 'var(--text-muted)',
                transition: 'all .2s',
              }}
            >
              {tab.icon}
            </div>
            {/* Label */}
            <span
              style={{
                position: 'relative',
                fontFamily: 'var(--font-heading)',
                fontWeight: 600,
                fontSize: '11.5px',
                letterSpacing: '.4px',
                lineHeight: '1.15',
                textAlign: 'center',
                color: active ? 'var(--gold-light)' : 'var(--text-muted)',
                textShadow: active ? '0 0 8px rgba(202,160,87,0.6)' : 'none',
                transition: 'color .2s',
              }}
            >
              {tab.label}
            </span>
          </button>
        )
      })}
    </nav>
  )
}
