import { Home, Layers, ShoppingBag, Trophy, User } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import NavigationCard from './NavigationCard'

type TabId = 'home' | 'deck' | 'shop' | 'rank' | 'profile'

interface BottomNavProps {
  activeTab: TabId
  setActiveTab: (tab: TabId) => void
}

const tabs: { id: TabId; label: string; icon: LucideIcon }[] = [
  { id: 'home',    label: 'Taverna',           icon: Home },
  { id: 'deck',    label: 'Forja de Baralhos', icon: Layers },
  { id: 'shop',    label: 'Mercador',          icon: ShoppingBag },
  { id: 'rank',    label: 'Salão da Glória',   icon: Trophy },
  { id: 'profile', label: 'Brasão',            icon: User },
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
      {tabs.map((tab) => (
        <NavigationCard
          key={tab.id}
          id={tab.id}
          label={tab.label}
          icon={tab.icon}
          active={activeTab === tab.id}
          onClick={() => setActiveTab(tab.id)}
        />
      ))}
    </nav>
  )
}
