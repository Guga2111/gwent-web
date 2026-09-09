export function NilfgaardEmblem() {
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

export function MonstersEmblem() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
      <path d="M2 3 Q4.5 7 3 15 Q5 8 6 4 Z" fill="currentColor" />
      <path d="M6 1 Q8.5 6 7 16 Q9 7 10 2 Z" fill="currentColor" />
      <path d="M11 3 Q13 7 11.5 14 Q13.5 8 15 4 Z" fill="currentColor" />
    </svg>
  )
}

export function NorthernRealmsEmblem() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
      <path
        d="M9 1 L11 6 L9 5 L7 6 Z M9 5 L11 6 L12 10 L9 8 L6 10 L7 6 Z M6 10 L4 16 L9 13 L14 16 L12 10 L9 8 Z"
        fill="currentColor"
      />
    </svg>
  )
}

export function ScoiataelEmblem() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
      <line x1="9" y1="1" x2="9" y2="17" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <line x1="4" y1="3" x2="13" y2="15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <line x1="14" y1="3" x2="5" y2="15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    </svg>
  )
}

export function SkelligeEmblem() {
  return (
    <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
      <path
        d="M5 16 Q5 4 9 2 Q13 4 13 16 M7 14 Q7 6 9 4 M11 14 Q11 6 9 4"
        stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round"
      />
    </svg>
  )
}
