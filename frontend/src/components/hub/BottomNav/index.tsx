import { useRef, useLayoutEffect, useState, type ComponentType } from 'react'
import { IconCards, IconCrown, IconMoneybag, IconShield } from '@tabler/icons-react'
import { BeerMealOutlineRoundedIcon } from '@/components/icons/material-symbols-beer-meal-outline-rounded'
import { Tabs, TabsList } from '@/components/ui/tabs'
import NavigationCard from './NavigationCard'

type TabId = 'home' | 'deck' | 'shop' | 'rank' | 'profile'
type IconComponent = ComponentType<{ size?: number; strokeWidth?: number }>

interface BottomNavProps {
  activeTab: TabId
  setActiveTab: (tab: TabId) => void
}

const tabs: { id: TabId; label: string; icon: IconComponent }[] = [
  { id: 'home',    label: 'Taverna',           icon: BeerMealOutlineRoundedIcon },
  { id: 'deck',    label: 'Forja de Baralhos', icon: IconCards },
  { id: 'shop',    label: 'Mercador',          icon: IconMoneybag },
  { id: 'rank',    label: 'Salão da Glória',   icon: IconCrown },
  { id: 'profile', label: 'Brasão',            icon: IconShield },
]

export default function BottomNav({ activeTab, setActiveTab }: BottomNavProps) {
  const btnRefs = useRef<(HTMLButtonElement | null)[]>([])
  const rowRef = useRef<HTMLDivElement>(null)
  const [diamondOffset, setDiamondOffset] = useState(0)

  const EXTEND = 40
  const [barWidth, setBarWidth] = useState(0)

  useLayoutEffect(() => {
    const activeIndex = tabs.findIndex((t) => t.id === activeTab)
    const btn = btnRefs.current[activeIndex]
    const row = rowRef.current
    if (!btn || !row) return
    const rowRect = row.getBoundingClientRect()
    const btnRect = btn.getBoundingClientRect()
    setBarWidth(row.offsetWidth + EXTEND * 2)
    // Button center relative to the row, plus the left extension
    setDiamondOffset(btnRect.left + btnRect.width / 2 - rowRect.left + EXTEND)
  }, [activeTab])

  return (
    <Tabs
      value={activeTab}
      onValueChange={(value) => setActiveTab(value as TabId)}
    >
      <nav className="relative z-30 flex flex-col items-center pt-1.5 pb-[22px] shrink-0">
        {/* Icon row */}
        <TabsList
          ref={rowRef}
          className="flex items-center justify-center gap-6 h-auto bg-transparent p-0 rounded-none"
        >
          {tabs.map((tab, i) => (
            <NavigationCard
              key={tab.id}
              ref={(el) => { btnRefs.current[i] = el }}
              id={tab.id}
              label={tab.label}
              icon={tab.icon}
            />
          ))}
        </TabsList>

        {/* Bar with diamond */}
        <div
          className="relative self-center mt-4"
          style={{ width: barWidth || undefined, visibility: barWidth ? 'visible' : 'hidden' }}
        >
          <div className="nav-bar__line w-full" />
          <div
            className="absolute top-0 transition-all duration-300"
            style={{
              left: diamondOffset,
              transform: 'translateX(-50%) translateY(-50%)',
            }}
          >
            <div className="nav-item__diamond w-[7px] h-[7px] rotate-45" />
          </div>
        </div>
      </nav>
    </Tabs>
  )
}
