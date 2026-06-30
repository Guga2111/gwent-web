import React, { useState } from 'react'
import { useAuthStore } from '@/stores/authStore'
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
  const user = useAuthStore((s) => s.user)
  const ActiveTab = tabContent[activeTab]

  return (
    <div
      style={{
        height: '100vh',
        display: 'flex',
        flexDirection: 'column',
        background:
          'radial-gradient(135% 125% at 50% -8%, #2c1e10 0%, #160d06 46%, var(--bg-darkest) 100%)',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      {/* Atmosphere layers */}
      <div
        style={{
          position: 'absolute',
          inset: 0,
          background:
            'radial-gradient(60% 50% at 50% 30%, rgba(255,196,108,.22), rgba(150,90,30,.06) 44%, transparent 72%)',
          animation: 'gw-flicker 5s ease-in-out infinite',
          pointerEvents: 'none',
        }}
      />
      <div
        style={{
          position: 'absolute',
          left: 0,
          right: 0,
          bottom: 0,
          height: '42%',
          background:
            'linear-gradient(180deg, transparent, rgba(46,30,16,.5) 36%, rgba(20,12,6,.92)), repeating-linear-gradient(90deg, rgba(0,0,0,.18) 0 3px, rgba(255,220,160,.016) 3px 9px)',
          pointerEvents: 'none',
        }}
      />
      <div
        style={{
          position: 'absolute',
          inset: 0,
          background:
            'radial-gradient(125% 115% at 50% 44%, transparent 42%, rgba(0,0,0,.6) 100%)',
          pointerEvents: 'none',
        }}
      />

      <TopHUD user={user} onSettingsClick={() => setActiveTab('profile')} />

      {/* Content area */}
      <div
        style={{
          position: 'relative',
          zIndex: 2,
          flex: 1,
          minHeight: 0,
          overflow: 'hidden',
        }}
      >
        <ActiveTab />
      </div>

      <TownCrier />
      <BottomNav activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  )
}
