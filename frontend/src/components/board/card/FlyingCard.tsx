import { useEffect, useRef, useState } from 'react'
import type { CardDto } from '@/types/game'
import Card from './Card'

interface FlyingCardProps {
  card: CardDto
  fromRect: DOMRect
  toRect: DOMRect
  onComplete: () => void
  duration?: number
}

export default function FlyingCard({ card, fromRect, toRect, onComplete, duration = 450 }: FlyingCardProps) {
  const ref = useRef<HTMLDivElement>(null)
  const [landed, setLanded] = useState(false)

  useEffect(() => {
    const el = ref.current
    if (!el) return

    // Start at fromRect position
    el.style.top = `${fromRect.top}px`
    el.style.left = `${fromRect.left}px`
    el.style.transitionDuration = '0ms'

    // Trigger transition on next frame
    const raf = requestAnimationFrame(() => {
      el.style.transitionDuration = `${duration}ms`
      el.style.top = `${toRect.top + (toRect.height - fromRect.height) / 2}px`
      el.style.left = `${toRect.left + (toRect.width - fromRect.width) / 2}px`
    })

    const timer = setTimeout(() => {
      setLanded(true)
      onComplete()
    }, duration)

    return () => {
      cancelAnimationFrame(raf)
      clearTimeout(timer)
    }
  }, [])

  return (
    <div
      ref={ref}
      className={`flying-card${landed ? ' flying-card--landed' : ''}`}
      style={{
        top: fromRect.top,
        left: fromRect.left,
        width: fromRect.width,
        height: fromRect.height,
      }}
    >
      <Card card={card} />
    </div>
  )
}
