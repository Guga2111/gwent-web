import React from 'react'
import type { CatalogCardDto } from '@/types/deck'
import { getFactionConfig } from '@/utils/factionConfig'
import { getCardArtUrl } from '@/components/board/card/cardArt'

interface LeaderHubCardProps {
  leader: CatalogCardDto | null
}

export default function LeaderHubCard({ leader }: LeaderHubCardProps) {
  const config = getFactionConfig(leader?.faction ?? null)

  return (
    <div className="absolute left-0 bottom-0 z-6">
      <div
        className="card-base card-back shrink-0 relative rounded-[5px] overflow-hidden"
        style={{
          '--cb-primary': config.tokens.primary,
          '--cb-secondary': config.tokens.secondary,
          width: 'calc(var(--card-w) * 1.25)',
          height: 'calc(var(--card-h) * 1.25)',
          background: `linear-gradient(180deg, var(${config.secondaryVar}), color-mix(in srgb, var(${config.secondaryVar}) 60%, black) 70%)`,
          boxShadow: '0 12px 26px rgba(0,0,0,.6)',
        } as React.CSSProperties}
      >
        <div className="card-back-border" />
        {leader && (
          <img
            src={getCardArtUrl(leader.id, leader.faction)}
            alt=""
            onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
            className="card-art-img"
          />
        )}
      </div>
    </div>
  )
}
