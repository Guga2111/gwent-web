import { Settings, Coins, Gem } from 'lucide-react'
import type { AuthUser } from '@/types/auth'
import PlayerShield from './PlayerShield'

import CurrencyBadge from './CurrencyBadge'

interface TopHUDProps {
  user: AuthUser | null
  onSettingsClick: () => void
}

export default function TopHUD({ user, onSettingsClick }: TopHUDProps) {
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
        <PlayerShield level={34} />
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
              background: 'linear-gradient(180deg, var(--blue-light), var(--blue))',
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
        <CurrencyBadge
          count="1.450"
          label="coroas"
          Icon={Coins}
          iconBg="radial-gradient(circle at 35% 30%, var(--gold-light), var(--gold-dark))"
          iconColor="var(--bg-darkest)"
          accentShadow="rgba(240,205,120,.28)"
        />
        <CurrencyBadge
          count="820"
          label="sucata"
          Icon={Gem}
          iconBg="radial-gradient(circle at 35% 30%, var(--blue-light), var(--blue))"
          iconColor="var(--blue-dark)"
          accentShadow="rgba(140,180,210,.28)"
          countColor="var(--blue-light)"
          labelColor="var(--blue-dim)"
        />

        {/* Settings */}
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
          <Settings size={18} strokeWidth={1.8} />
        </button>
      </div>
    </header>
  )
}
