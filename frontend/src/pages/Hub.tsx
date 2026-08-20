import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { getActiveGame } from '@/api/game'
import TopHUD from '@/components/hub/hud/TopHUD'
import BottomNav from '@/components/hub/BottomNav'
import TownCrier from '@/components/hub/TownCrier'
import Taverna from '@/pages/hub/Taverna'
import DeckForge from '@/pages/hub/DeckForge'
import Shop from '@/pages/hub/Shop'
import Leaderboard from '@/pages/hub/Leaderboard'
import Profile from '@/pages/hub/Profile'

type TabId = 'home' | 'deck' | 'shop' | 'rank' | 'profile'

const tabContent: Record<TabId, React.ComponentType> = {
  home: Taverna,
  deck: DeckForge,
  shop: Shop,
  rank: Leaderboard,
  profile: Profile,
}

export default function Hub() {
  const [activeTab, setActiveTab] = useState<TabId>('home')
  const [activeGameId, setActiveGameId] = useState<string | null>(null)
  const user = useAuthStore((s) => s.user)
  const navigate = useNavigate()
  const ActiveTab = tabContent[activeTab]

  useEffect(() => {
    getActiveGame().then(setActiveGameId).catch(() => {})
  }, [])

  return (
    <div
      className="h-screen flex flex-col relative overflow-hidden"
      style={{
        background: 'radial-gradient(135% 125% at 50% -8%, #2c1e10 0%, #160d06 46%, var(--bg-darkest) 100%)',
      }}
    >
      {/* Atmosphere layers */}
      <div
        className="absolute inset-0 pointer-events-none"
        style={{
          background: 'radial-gradient(60% 50% at 50% 30%, rgba(255,196,108,.22), rgba(150,90,30,.06) 44%, transparent 72%)',
          animation: 'gw-flicker 5s ease-in-out infinite',
        }}
      />
      <div
        className="absolute left-0 right-0 bottom-0 h-[42%] pointer-events-none"
        style={{
          background: 'linear-gradient(180deg, transparent, rgba(46,30,16,.5) 36%, rgba(20,12,6,.92)), repeating-linear-gradient(90deg, rgba(0,0,0,.18) 0 3px, rgba(255,220,160,.016) 3px 9px)',
        }}
      />
      <div
        className="absolute inset-0 pointer-events-none"
        style={{
          background: 'radial-gradient(125% 115% at 50% 44%, transparent 42%, rgba(0,0,0,.6) 100%)',
        }}
      />

      <TopHUD user={user} onSettingsClick={() => setActiveTab('profile')} />

      {activeGameId && (
        <div className="relative z-10 flex items-center justify-between px-5 py-2 border-b border-[rgba(240,205,120,0.25)] shadow-[0_2px_12px_rgba(0,0,0,0.5)] bg-[linear-gradient(90deg,rgba(30,18,6,0.92),rgba(50,32,10,0.92))]">
          <div className="flex items-center gap-2.5">
            <div className="w-2 h-2 rounded-full bg-[#4ade80] shadow-[0_0_6px_#4ade80]" />
            <span
              className="text-[11px] tracking-[1.5px] uppercase font-bold text-[var(--gold-light)]"
              style={{ fontFamily: 'var(--font-heading)' }}
            >
              Partida em andamento
            </span>
          </div>
          <button
            onClick={() => navigate(`/game/${activeGameId}`)}
            className="px-4 py-1 rounded border border-[rgba(240,205,120,0.45)] bg-[rgba(240,205,120,0.12)] text-[var(--gold-light)] text-[11px] font-bold tracking-[1.5px] uppercase cursor-pointer"
            style={{ fontFamily: 'var(--font-heading)' }}
          >
            Retomar
          </button>
        </div>
      )}

      {/* Content area */}
      <div className="relative z-20 flex-1 min-h-0 overflow-hidden">
        <ActiveTab />
      </div>

      <TownCrier />
      <BottomNav activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  )
}
