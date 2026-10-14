import type { ComponentProps } from 'react'

import { cn } from '@/lib/utils'

import { buttonVariants, type ButtonVariantProps } from './button-variants'

type Props = ComponentProps<'button'> & ButtonVariantProps

export function Button({ className, variant, size, type = 'button', ...props }: Props) {
  return <button type={type} className={cn(buttonVariants({ variant, size }), className)} {...props} />
}
