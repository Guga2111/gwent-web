interface PlayerShieldProps {
  level?: number
  size?: 'sm' | 'md'
}

const sizes = {
  sm: { outer: 'w-[52px] h-[64px]', inner: 'w-[42px] h-[54px]' },
  md: { outer: 'w-[90px] h-[106px]', inner: 'w-[76px] h-[92px]' },
}

const shieldClip = { clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)' }

export default function PlayerShield({ level, size = 'sm' }: PlayerShieldProps) {
  const { outer, inner } = sizes[size]

  return (
    <div className="relative shrink-0">
      <div
        className={`${outer} flex items-center justify-center bg-linear-to-b from-gold-light via-gold via-60% to-gold-dim drop-shadow-[0_4px_8px_rgba(0,0,0,.5)]`}
        style={shieldClip}
      >
        <div
          className={`${inner} bg-bg-dark`}
          style={shieldClip}
        />
      </div>
      {level !== undefined && (
        <div className="absolute -bottom-1.5 left-1/2 -translate-x-1/2 bg-linear-to-b from-gold-light to-gold-dark text-bg-darkest font-heading font-bold text-[10.5px] px-2 py-px rounded-[9px] shadow-[0_2px_4px_rgba(0,0,0,.5)] whitespace-nowrap">
          {level}
        </div>
      )}
    </div>
  )
}
