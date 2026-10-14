import type { ComponentProps } from 'react'

import { cn } from '@/lib/utils'

type Props = Omit<ComponentProps<'button'>, 'onChange'> & {
  checked: boolean
  onCheckedChange: (checked: boolean) => void
}

/** Interruptor acessível ({@code role="switch"}) sem dependência extra. */
export function Switch({ checked, onCheckedChange, className, ...props }: Props) {
  return (
    <button type="button" role="switch" aria-checked={checked} onClick={() => onCheckedChange(!checked)}
      className={cn(
        'relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border-0 p-0.5 transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring',
        checked ? 'bg-primary' : 'bg-surface-3',
        className,
      )}
      {...props}>
      <span className={cn('block size-5 rounded-full bg-white shadow-sm transition-transform duration-200',
        checked && 'translate-x-5')} />
    </button>
  )
}
