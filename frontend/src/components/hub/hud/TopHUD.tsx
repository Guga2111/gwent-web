import { Settings, Coins, Gem } from 'lucide-react'
import type { AuthUser } from '@/types/auth'
import PlayerShield from './PlayerShield'
import CurrencyBadge from './CurrencyBadge'
import { useHubStore } from '@/stores/hubStore'
import { getFactionConfig } from '@/utils/factionConfig'

interface TopHUDProps {
  user: AuthUser | null
  onSettingsClick: () => void
}

export default function TopHUD({ user, onSettingsClick }: TopHUDProps) {
  const activeDeck = useHubStore((s) => s.activeDeck)
  const config = getFactionConfig(activeDeck?.faction ?? null)

  return (
    <header className="relative z-30 flex items-center justify-between px-[30px] pt-4 pb-[10px] shrink-0">
      {/* Left: Avatar + Player info */}
      <div className="flex items-center gap-3.5">
        <PlayerShield level={1} />
        <div>
          <div className="font-heading font-bold text-lg text-[var(--text-primary)] tracking-[.3px]">
            {user?.username ?? user?.email ?? 'Jogador'}
          </div>
          <div className="font-bodytext-[13px] text-[var(--text-muted)] mt-px">
            &laquo; &mdash; &raquo;
          </div>
        </div>
        <div className="flex items-center gap-2 ml-1.5 pl-4 top-hud-faction-divider">
          <div
            className="w-[15px] h-[18px]"
            style={{
              background: `var(${config.secondaryVar})`,
              clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
            }}
          />
          <span className="font-heading font-semibold text-[13px] text-[var(--gold)] tracking-[.5px]">
            {activeDeck ? config.label : '—'}
          </span>
          <span className="font-bodytext-[13px] text-[var(--text-muted)]">
            · —
          </span>
        </div>
      </div>

      {/* Center: Logo */}
      <div className="absolute left-1/2 top-[14px] -translate-x-1/2 text-center pointer-events-none">
        <div className="font-display top-hud-logo font-black text-[22px] tracking-[6px] text-[var(--gold-light)]">
          GWENT
        </div>
      </div>

      {/* Right: Currencies + Settings */}
      <div className="flex items-center gap-[11px]">
        <CurrencyBadge
          count="—"
          label="coroas"
          Icon={Coins}
          iconBg="radial-gradient(circle at 35% 30%, var(--gold-light), var(--gold-dark))"
          iconColor="var(--bg-darkest)"
          accentShadow="rgba(240,205,120,.28)"
        />
        <CurrencyBadge
          count="—"
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
          className="bg-transparent border-none cursor-pointer text-[var(--gold)] p-2 rounded-full"
        >
          <Settings size={18} strokeWidth={1.8} />
        </button>
      </div>
    </header>
  )
}
