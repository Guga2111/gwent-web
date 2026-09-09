import { forwardRef, useState, type ComponentType } from 'react'
import { motion } from 'motion/react'
import { TabsTrigger } from '@/components/ui/tabs'
import { cn } from '@/lib/utils'

interface Ripple {
  id: number
  x: number
  y: number
}

interface NavigationCardProps {
  id: string
  label: string
  icon: ComponentType<{ size?: number; strokeWidth?: number }>
}

const NavigationCard = forwardRef<HTMLButtonElement, NavigationCardProps>(
  function NavigationCard({ id, label, icon: Icon }, ref) {
    const [ripples, setRipples] = useState<Ripple[]>([])

    const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
      const circle = event.currentTarget.querySelector('[data-slot="nav-circle"]')
      if (!circle) return
      const rect = circle.getBoundingClientRect()
      const rid = Date.now()
      setRipples(prev => [...prev, { id: rid, x: event.clientX - rect.left, y: event.clientY - rect.top }])
      setTimeout(() => setRipples(prev => prev.filter(r => r.id !== rid)), 600)
    }

    return (
      <TabsTrigger
        ref={ref}
        value={id}
        onClick={handleClick}
        className={cn(
          'group relative flex flex-col items-center border-none cursor-pointer bg-transparent p-0',
          'rounded-none shadow-none ring-0 ring-offset-0',
          'focus-visible:ring-0 focus-visible:ring-offset-0',
          'data-[state=inactive]:bg-transparent data-[state=active]:bg-transparent data-[state=active]:shadow-none',
        )}
      >
        {/* Circle icon */}
        <div
          data-slot="nav-circle"
          className="nav-item__circle relative shrink-0 w-14 h-14 rounded-full flex items-center justify-center transition-all duration-200 overflow-hidden border border-border-subtle"
        >
          <Icon size={22} strokeWidth={1.8} />
          {ripples.map(r => (
            <motion.span
              aria-hidden
              key={r.id}
              initial={{ scale: 0, opacity: 0.4 }}
              animate={{ scale: 6, opacity: 0 }}
              transition={{ duration: 0.6, ease: 'easeOut' }}
              className="pointer-events-none absolute size-3 rounded-full bg-current"
              style={{ top: r.y - 6, left: r.x - 6 }}
            />
          ))}
        </div>

        {/* Hover label */}
        <span className="font-heading font-semibold text-[10.5px] tracking-[2px] uppercase text-gold-light absolute -top-5 opacity-0 group-hover:opacity-100 transition-opacity duration-200 whitespace-nowrap pointer-events-none">
          {label}
        </span>
      </TabsTrigger>
    )
  },
)

export default NavigationCard
