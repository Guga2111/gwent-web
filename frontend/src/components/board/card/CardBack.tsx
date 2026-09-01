import type { Faction } from '@/types/game'

interface CardBackProps {
  faction?: Faction
}

export const factionTokens: Record<Faction, { primary: string; secondary: string }> = {
  NILFGAARD: {
    primary: 'var(--faction-nilfgaard-primary)',
    secondary: 'var(--faction-nilfgaard-secondary)',
  },
  MONSTER: {
    primary: 'var(--faction-monsters-primary)',
    secondary: 'var(--faction-monsters-secondary)',
  },
  NORTHERN_REALMS: {
    primary: 'var(--faction-northern-primary)',
    secondary: 'var(--faction-northern-secondary)',
  },
  SCOIATAEL: {
    primary: 'var(--faction-scoiatael-primary)',
    secondary: 'var(--faction-scoiatael-secondary)',
  },
  SKELLIGE: {
    primary: 'var(--faction-skellige-primary)',
    secondary: 'var(--faction-skellige-secondary)',
  },
}

function NilfgaardEmblem() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
      <circle cx="9" cy="9" r="3" fill="currentColor" />
      {[0, 45, 90, 135, 180, 225, 270, 315].map((angle) => {
        const rad = (angle * Math.PI) / 180
        const x1 = 9 + Math.cos(rad) * 4
        const y1 = 9 + Math.sin(rad) * 4
        const x2 = 9 + Math.cos(rad) * 8
        const y2 = 9 + Math.sin(rad) * 8
        return (
          <line
            key={angle}
            x1={x1} y1={y1} x2={x2} y2={y2}
            stroke="currentColor" strokeWidth="2" strokeLinecap="round"
          />
        )
      })}
    </svg>
  )
}

function MonstersEmblem() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
      {/* 3 diagonal claw slashes — tapered leaf shapes */}
      {/* Left claw */}
      <path d="M2 3 Q4.5 7 3 15 Q5 8 6 4 Z" fill="currentColor" />
      {/* Center claw */}
      <path d="M6 1 Q8.5 6 7 16 Q9 7 10 2 Z" fill="currentColor" />
      {/* Right claw */}
      <path d="M11 3 Q13 7 11.5 14 Q13.5 8 15 4 Z" fill="currentColor" />
    </svg>
  )
}

function NorthernRealmsEmblem() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
      <path
        d="M9 1 L11 6 L9 5 L7 6 Z M9 5 L11 6 L12 10 L9 8 L6 10 L7 6 Z M6 10 L4 16 L9 13 L14 16 L12 10 L9 8 Z"
        fill="currentColor"
      />
    </svg>
  )
}

function ScoiataelEmblem() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
      <line x1="9" y1="1" x2="9" y2="17" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <line x1="4" y1="3" x2="13" y2="15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <line x1="14" y1="3" x2="5" y2="15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

function SkelligeEmblem() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
      <path
        d="M5 16 Q5 4 9 2 Q13 4 13 16 M7 14 Q7 6 9 4 M11 14 Q11 6 9 4"
        stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round"
      />
    </svg>
  )
}

export const emblems: Record<Faction, () => JSX.Element> = {
  NILFGAARD: NilfgaardEmblem,
  MONSTER: MonstersEmblem,
  NORTHERN_REALMS: NorthernRealmsEmblem,
  SCOIATAEL: ScoiataelEmblem,
  SKELLIGE: SkelligeEmblem,
}

export default function CardBack({ faction }: CardBackProps) {
  const tokens = faction ? factionTokens[faction] : undefined
  const Emblem = faction ? emblems[faction] : undefined

  const style = tokens
    ? { '--cb-primary': tokens.primary, '--cb-secondary': tokens.secondary } as React.CSSProperties
    : undefined

  return (
    <div className="card-base card-back" style={style}>
      <div className="card-back-border" />
      {Emblem && (
        <div className="card-back-circle" style={{ color: tokens!.primary }}>
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
