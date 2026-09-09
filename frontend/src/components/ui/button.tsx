import * as React from "react"
import { Slot } from "@radix-ui/react-slot"
import { motion, type Transition } from "motion/react"
import { cva, type VariantProps } from "class-variance-authority"

import { cn } from "@/lib/utils"

interface Ripple {
  id: number
  x: number
  y: number
}

const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0",
  {
    variants: {
      variant: {
        default:
          "bg-primary text-primary-foreground shadow hover:bg-primary/90",
        destructive:
          "bg-destructive text-destructive-foreground shadow-sm hover:bg-destructive/90",
        outline:
          "border border-input bg-background shadow-sm hover:bg-accent hover:text-accent-foreground",
        secondary:
          "bg-secondary text-secondary-foreground shadow-sm hover:bg-secondary/80",
        ghost: "hover:bg-accent hover:text-accent-foreground",
        link: "text-primary underline-offset-4 hover:underline",
        dark: "bg-gold-dark border border-gold text-text-primary hover:bg-gold-dark/90",
        light: "border-none text-bg-darkest shadow-[0_5px_12px_rgba(0,0,0,.4)] hover:opacity-90",
        deck: "bg-gold-dark/80 text-gold-light font-body rounded-lg border-none cursor-pointer hover:bg-gold-dark transition-colors",
        cta: "border-none cursor-pointer text-bg-darkest font-bold [background:linear-gradient(180deg,var(--gold-light)_0%,var(--gold)_62%,var(--gold)_100%)] hover:brightness-110 hover:shadow-[0_0_18px_rgba(240,205,120,.35)] active:brightness-95 transition-all",
        pass: "w-full py-2 [background:linear-gradient(180deg,rgba(42,33,24,0.6)_0%,rgba(26,20,16,0.8)_100%)] border border-gold-dim rounded font-heading text-sm tracking-[2px] text-gold cursor-pointer transition-all hover:[background:linear-gradient(180deg,rgba(50,40,28,0.7)_0%,rgba(32,25,18,0.9)_100%)] hover:border-gold hover:shadow-[0_0_10px_rgba(var(--gold-rgb),0.15)] active:[background:linear-gradient(180deg,rgba(26,20,16,0.9)_0%,rgba(42,33,24,0.7)_100%)] disabled:opacity-40 disabled:cursor-not-allowed",
        "board-icon": "bg-bg-medium border border-border-subtle rounded-full cursor-pointer",
        cancel: "bg-transparent border border-[rgba(240,205,120,.3)] text-text-muted font-heading text-[11px] font-bold tracking-[2px] uppercase rounded-md cursor-pointer transition-colors hover:border-[rgba(240,205,120,.6)] hover:text-text-secondary",
        parchment: "bg-linear-to-b from-gold to-gold-dark border-none rounded-[3px] text-bg-darkest font-heading text-[11px] font-bold tracking-[1.5px] uppercase cursor-pointer shadow-[0_2px_6px_rgba(0,0,0,.35)] hover:brightness-110 hover:shadow-[0_2px_10px_rgba(0,0,0,.45)] active:brightness-95 transition-all",
      },
      size: {
        default: "h-9 px-4 py-2",
        sm: "h-8 rounded-md px-3 text-xs",
        lg: "h-10 rounded-md px-8",
        icon: "h-9 w-9",
        pill: "h-7 rounded-full px-3 text-xs",
      },
    },
    defaultVariants: {
      variant: "default",
      size: "default",
    },
  }
)

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
  ripple?: boolean
  rippleScale?: number
  rippleTransition?: Transition
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ripple = false, rippleScale = 10, rippleTransition = { duration: 0.6, ease: "easeOut" }, onClick, children, ...props }, ref) => {
    const [ripples, setRipples] = React.useState<Ripple[]>([])

    const handleClick = ripple
      ? (event: React.MouseEvent<HTMLButtonElement>) => {
          const rect = event.currentTarget.getBoundingClientRect()
          const id = Date.now()
          setRipples(prev => [...prev, { id, x: event.clientX - rect.left, y: event.clientY - rect.top }])
          setTimeout(() => setRipples(prev => prev.filter(r => r.id !== id)), 600)
          onClick?.(event)
        }
      : onClick

    if (asChild) {
      return (
        <Slot
          className={cn(buttonVariants({ variant, size, className }))}
          ref={ref}
          onClick={handleClick}
          {...props}
        >
          {children}
        </Slot>
      )
    }

    return (
      <button
        className={cn(buttonVariants({ variant, size, className }), ripple && "relative overflow-hidden")}
        ref={ref}
        onClick={handleClick}
        {...props}
      >
        {children}
        {ripple && ripples.map(r => (
          <motion.span
            aria-hidden
            key={r.id}
            initial={{ scale: 0, opacity: 0.5 }}
            animate={{ scale: rippleScale, opacity: 0 }}
            transition={rippleTransition}
            className="pointer-events-none absolute size-5 rounded-full bg-current"
            style={{ top: r.y - 10, left: r.x - 10 }}
          />
        ))}
      </button>
    )
  }
)
Button.displayName = "Button"

export { Button, buttonVariants }
