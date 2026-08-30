import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'
import { getActiveGame } from '@/api/game'
import TopHUD from '@/components/hub/hud/TopHUD'
import BottomNav from '@/components/hub/BottomNav'
import RulesModal from '@/components/hub/RulesModal'
import Taverna from '@/pages/hub/Taverna'
import DeckForge from '@/pages/hub/DeckForge'
import Shop from '@/pages/hub/Shop'
import Leaderboard from '@/pages/hub/Leaderboard'
import Profile from '@/pages/hub/Profile'
import { useTutorial } from '@/hooks/useTutorial'
import { useHubStore } from '@/stores/hubStore'

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
  const { tutorialOpen, openTutorial, closeTutorial } = useTutorial()
  const registerSetTab = useHubStore((s) => s.registerSetTab)

  useEffect(() => {
    getActiveGame().then(setActiveGameId).catch(() => {})
  }, [])

  useEffect(() => {
    registerSetTab((tab) => setActiveTab(tab as TabId))
  }, [])

  return (
    <div className="h-screen flex flex-col relative overflow-hidden hub-bg">
      {/* Atmosphere layers */}
      <div className="absolute inset-0 pointer-events-none hub-atm-warm" />
      <div className="absolute left-0 right-0 bottom-0 h-[42%] pointer-events-none hub-atm-bottom" />
      <div className="absolute inset-0 pointer-events-none hub-atm-vignette" />

      <TopHUD user={user} onSettingsClick={() => setActiveTab('profile')} />

      {activeGameId && (
        <div className="relative z-10 flex items-center justify-between px-5 py-2 hub-active-banner">
          <div className="flex items-center gap-2.5">
            <div className="w-2 h-2 rounded-full bg-[#4ade80] shadow-[0_0_6px_#4ade80]" />
            <span className="font-heading text-[11px] tracking-[1.5px] uppercase font-bold text-[var(--gold-light)]">
              Partida em andamento
            </span>
          </div>
          <button
            onClick={() => navigate(`/game/${activeGameId}`)}
            className="font-heading hub-active-banner__btn px-4 py-1 rounded text-[var(--gold-light)] text-[11px] font-bold tracking-[1.5px] uppercase cursor-pointer"
          >
            Retomar
          </button>
        </div>
      )}

      {/* Content area */}
      <div className="relative z-20 flex-1 min-h-0 overflow-hidden">
        <ActiveTab />
        <button onClick={openTutorial} className="hub-help-btn">
          ?
        </button>
      </div>

      <RulesModal open={tutorialOpen} onClose={closeTutorial} />
      <BottomNav activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  )
}
