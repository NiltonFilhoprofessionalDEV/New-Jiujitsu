import type { ComponentProps } from 'react'

import { cn } from '@/lib/utils'

export function Textarea({ className, ...props }: ComponentProps<'textarea'>) {
  return (
    <textarea
      className={cn(
        'block min-h-16 w-full resize-y rounded-xl border border-input bg-card px-3.5 py-2.5 text-[15px] text-foreground placeholder:text-subtle focus-visible:border-ring focus-visible:outline-2 focus-visible:outline-offset-0 focus-visible:outline-ring/30',
        className,
      )}
      {...props}
    />
  )
}
