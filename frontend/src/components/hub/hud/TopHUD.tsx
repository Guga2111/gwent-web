import { Settings, Coins, Gem } from 'lucide-react'
import type { AuthUser } from '@/types/auth'
import PlayerShield from './PlayerShield'
import CurrencyBadge from './CurrencyBadge'
import { useHubStore } from '@/stores/hubStore'
import { getFactionConfig } from '@/utils/factionConfig'
import { Button } from '@/components/ui/button'

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
          <div className="font-heading font-bold text-lg text-text-primary tracking-[.3px]">
            {user?.username ?? user?.email ?? 'Jogador'}
          </div>
          <div className="font-bodytext-[13px] text-text-muted mt-px">
            &laquo; &mdash; &raquo;
          </div>
        </div>
        <div className="flex items-center gap-2 ml-1.5 pl-4 border-l border-border-subtle">
          <div
            className="w-[15px] h-[18px] [clip-path:polygon(0_0,100%_0,100%_64%,50%_100%,0_64%)]"
            style={{ background: `var(${config.secondaryVar})` }}
          />
          <span className="font-heading font-semibold text-[13px] text-gold tracking-[.5px]">
            {activeDeck ? config.label : '—'}
          </span>
        </div>
      </div>

      {/* Center: Logo */}
      <div className="absolute left-1/2 top-[14px] -translate-x-1/2 text-center pointer-events-none">
        <div className="font-display font-black text-[22px] tracking-[6px] text-gold-light">
          GWENT
        </div>
      </div>

      {/* Right: Currencies + Settings */}
      <div className="flex items-center gap-[11px]">
        <CurrencyBadge count="—" label="coroas" Icon={Coins} />
        <CurrencyBadge count="—" label="sucata" Icon={Gem} variant="blue" />

        {/* Settings */}
        <Button
          onClick={onSettingsClick}
          className="bg-transparent border-none cursor-pointer text-gold p-2 rounded-full"
          variant="ghost"
        >
          <Settings size={16} />
        </Button>
      </div>
    </header>
  )
}
