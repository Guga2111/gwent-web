import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"

import { cn } from "@/lib/utils"

const separatorVariants = cva("h-px w-full", {
  variants: {
    variant: {
      default: "bg-border",
      parchment: "bg-[rgba(90,63,28,0.18)]",
      gold: "bg-gold-dim opacity-50",
    },
  },
  defaultVariants: {
    variant: "default",
  },
})

export interface HorizontalSeparatorProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof separatorVariants> {}

const HorizontalSeparator = React.forwardRef<
  HTMLDivElement,
  HorizontalSeparatorProps
>(({ className, variant, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(separatorVariants({ variant, className }))}
    {...props}
  />
))
HorizontalSeparator.displayName = "HorizontalSeparator"

export { HorizontalSeparator, separatorVariants }
