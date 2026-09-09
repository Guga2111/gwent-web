import React from 'react'
import type { Faction } from '@/types/deck'
import { getFactionConfig } from '@/utils/factionConfig'

interface CardBackProps {
  faction?: Faction
}

export default function CardBack({ faction }: CardBackProps) {
  const config = getFactionConfig(faction ?? null)
  const { tokens, Emblem } = config

  const style = faction
    ? { '--cb-primary': tokens.primary, '--cb-secondary': tokens.secondary } as React.CSSProperties
    : undefined

  return (
    <div className="card-base card-back shrink-0 flex items-center justify-center" style={style}>
      <div className="card-back-border" />
      {Emblem && (
        <div className="card-back-circle" style={{ color: tokens.primary }}>
          <Emblem />
        </div>
      )}
      {/* Corner ornaments */}
      <div className="card-back-corner" style={{ top: 7, left: 7 }} />
      <div className="card-back-corner" style={{ top: 7, right: 7 }} />
      <div className="card-back-corner" style={{ bottom: 7, left: 7 }} />
      <div className="card-back-corner" style={{ bottom: 7, right: 7 }} />
    </div>
  )
}
