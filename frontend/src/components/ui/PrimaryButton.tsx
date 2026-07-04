import type { CSSProperties, ReactNode } from 'react'

interface PrimaryButtonProps {
  onClick?: () => void
  disabled?: boolean
  children: ReactNode
  variant?: 'dark' | 'light'
  style?: CSSProperties
}

const variantClasses: Record<'dark' | 'light', string> = {
  dark: 'bg-[var(--gold-dark)] border border-[var(--gold)] text-[var(--text-primary)]',
  light: 'border-none text-[var(--bg-darkest)] shadow-[0_5px_12px_rgba(0,0,0,.4)]',
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
      className={`px-8 py-2.5 rounded font-bold text-base ${disabled ? 'cursor-not-allowed opacity-50' : 'cursor-pointer'} ${variantClasses[variant]}`}
      style={{
        fontFamily: 'var(--font-heading)',
        ...(variant === 'light' ? { background: 'linear-gradient(180deg, var(--gold-light), var(--gold))' } : {}),
        ...style,
      }}
    >
      {children}
    </button>
  )
}
