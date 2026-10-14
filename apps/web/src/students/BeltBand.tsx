import { cn } from '@/lib/utils'

import { beltInfo } from './belts'

/** Igual ao ícone: acima de 6 graus (faixas vermelhas) as listras não cabem. */
const MAX_TAPES = 6
const TIP_BLACK = '#0a0a0b'
const TIP_RED = '#dc2626'

type Props = { belt: string | null; degrees: number; className?: string }

/**
 * A faixa esticada na horizontal: cor da faixa (listra central nas de duas cores) e, perto da ponta, a
 * ponteira — vermelha na faixa preta — com uma listra branca por grau.
 */
export function BeltBand({ belt, degrees, className }: Props) {
  const info = beltInfo(belt)
  if (!info) return null
  const [main, stripe] = info.colors
  const background = stripe ? `linear-gradient(${main} 33%, ${stripe} 33% 67%, ${main} 67%)` : main
  const tapes = degrees >= 1 && degrees <= MAX_TAPES ? degrees : 0

  return (
    <div aria-hidden className={cn('relative h-5 w-full shadow-[inset_0_1px_0_rgb(255_255_255/0.18),inset_0_-1px_0_rgb(0_0_0/0.25)]', className)}
      style={{ background }}>
      <div className="absolute inset-y-0 right-[7%] flex w-[min(26%,180px)] items-center justify-center gap-[7%]"
        style={{ background: info.slug === 'preta' ? TIP_RED : TIP_BLACK }}>
        {Array.from({ length: tapes }, (_, i) => <span key={i} className="h-full w-[6%] min-w-1 bg-white/95" />)}
      </div>
    </div>
  )
}
