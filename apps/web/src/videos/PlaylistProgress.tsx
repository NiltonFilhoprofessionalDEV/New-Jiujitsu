import { cn } from '@/lib/utils'

/** Acima disso as listras ficam finas demais: vira uma barra contínua. */
const MAX_TAPES = 12
const TIP_BLACK = '#0a0a0b'

type Props = { watched: number; total: number; className?: string }

/** Progresso no formato da ponteira da faixa: cada vídeo assistido é uma listra branca. */
export function PlaylistProgress({ watched, total, className }: Props) {
  if (total === 0) return null
  const base = cn('flex h-2.5 gap-[3px] rounded-[3px] p-[2px] ring-1 ring-white/10', className)

  if (total > MAX_TAPES) {
    return (
      <div aria-hidden className={base} style={{ background: TIP_BLACK }}>
        <div className="h-full rounded-[1px] bg-white transition-[width]" style={{ width: `${(watched / total) * 100}%` }} />
      </div>
    )
  }
  return (
    <div aria-hidden className={base} style={{ background: TIP_BLACK }}>
      {Array.from({ length: total }, (_, i) => (
        <span key={i} className={cn('flex-1 rounded-[1px]', i < watched ? 'bg-white' : 'bg-white/15')} />
      ))}
    </div>
  )
}
