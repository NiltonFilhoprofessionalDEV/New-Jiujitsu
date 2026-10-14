import { cva, type VariantProps } from 'class-variance-authority'

/** Separado do componente para que links ({@code <Link>}) também possam ter cara de botão. */
export const buttonVariants = cva(
  'inline-flex shrink-0 cursor-pointer items-center justify-center gap-2 whitespace-nowrap rounded-full border border-transparent font-semibold no-underline transition-[background-color,border-color,color,box-shadow,transform] duration-150 hover:no-underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring active:scale-[0.97] disabled:pointer-events-none disabled:opacity-50 [&_svg]:shrink-0',
  {
    variants: {
      variant: {
        default: 'bg-primary text-primary-foreground shadow-glow-sm hover:bg-primary-hover',
        outline: 'border-border-strong bg-card text-foreground hover:border-subtle hover:bg-accent',
        secondary: 'bg-secondary text-secondary-foreground hover:bg-surface-3',
        ghost: 'text-foreground hover:bg-accent',
        soft: 'border-primary-soft-border bg-primary-soft text-brand hover:border-primary',
        light: 'bg-white text-[#0a0a0b] hover:bg-white/90',
      },
      size: {
        default: 'h-10 px-5 text-sm',
        sm: 'h-8 px-3.5 text-[13px]',
        lg: 'h-12 px-6 text-[15px]',
        icon: 'size-10',
        'icon-sm': 'size-8',
      },
    },
    defaultVariants: { variant: 'default', size: 'default' },
  },
)

export type ButtonVariantProps = VariantProps<typeof buttonVariants>
