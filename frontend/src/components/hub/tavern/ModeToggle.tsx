import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { cn } from '@/lib/utils'

interface TabItem<T extends string> {
  value: T
  label: string
}

interface ModeToggleProps<T extends string> {
  items: TabItem<T>[]
  value: T
  onChange: (value: T) => void
  className?: string
}

export default function ModeToggle<T extends string>({ items, value, onChange, className }: ModeToggleProps<T>) {
  return (
    <Tabs value={value} onValueChange={(v) => onChange(v as T)}>
      <TabsList className={cn('taverna-mode-tab h-auto gap-0.5 rounded-full p-1 bg-transparent', className)}>
        {items.map((item) => (
          <TabsTrigger
            key={item.value}
            value={item.value}
            className="taverna-mode-tab__item font-heading font-bold text-[11px] tracking-[2px] uppercase px-5 py-1.5 rounded-full border-none cursor-pointer shadow-none"
          >
            {item.label}
          </TabsTrigger>
        ))}
      </TabsList>
    </Tabs>
  )
}
