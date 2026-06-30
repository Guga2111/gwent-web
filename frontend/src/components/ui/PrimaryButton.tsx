import type { CSSProperties, ReactNode } from 'react'

interface PrimaryButtonProps {
  onClick?: () => void
  disabled?: boolean
  children: ReactNode
  variant?: 'dark' | 'light'
  style?: CSSProperties
}

const variantStyles: Record<'dark' | 'light', CSSProperties> = {
  dark: {
    backgroundColor: 'var(--gold-dark)',
    border: '1px solid var(--gold)',
    color: 'var(--text-primary)',
  },
  light: {
    background: 'linear-gradient(180deg, var(--gold-light), var(--gold))',
    border: 'none',
    color: 'var(--bg-darkest)',
    boxShadow: '0 5px 12px rgba(0,0,0,.4)',
  },
}

export default function PrimaryButton({
  onClick,
  disabled,
  children,
  variant = 'dark',
  style,
}: PrimaryButtonProps) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      style={{
        padding: '10px 32px',
        borderRadius: 4,
        fontFamily: 'var(--font-heading)',
        fontWeight: 700,
        fontSize: 16,
        cursor: disabled ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.5 : 1,
        ...variantStyles[variant],
        ...style,
      }}
    >
      {children}
    </button>
  )
}
