import { Heart } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

import { REACTIONS } from './reactions'
import type { Video } from './types'
import { useFavorite, useReact } from './videosApi'

/** Uma reação por pessoa: tocar na mesma tira, tocar em outra troca. Favoritar fica ao lado. */
export function ReactionBar({ video }: { video: Video }) {
  const { t } = useTranslation()
  const react = useReact(video.id)
  const favorite = useFavorite(video.id)

  return (
    <div className="flex flex-wrap items-center justify-between gap-3">
      <div className="inline-flex flex-wrap gap-1 rounded-full bg-surface-3 p-1" role="group" aria-label={t('videos.reactAria')}>
        {REACTIONS.map(({ id, emoji }) => {
          const mine = video.myReaction === id
          const count = video.reactions[id] ?? 0
          return (
            <button key={id} type="button" aria-pressed={mine} disabled={react.isPending} title={t(`videos.reaction.${id}`)}
              aria-label={t('videos.reactionLabel', { name: t(`videos.reaction.${id}`), count })}
              onClick={() => react.mutate(mine ? null : id)}
              className={cn(
                'inline-flex h-9 min-w-11 cursor-pointer items-center justify-center gap-1.5 rounded-full border-0 bg-transparent px-3 text-lg transition-[background-color,box-shadow,transform] duration-150 hover:bg-card dark:hover:bg-white/10 focus-visible:outline-2 focus-visible:outline-ring active:scale-90 disabled:cursor-wait',
                mine && 'bg-card shadow-sm ring-1 ring-primary-soft-border dark:bg-white/10',
              )}>
              <span aria-hidden>{emoji}</span>
              {count > 0 && (
                <span className={cn('text-[13px] font-bold tabular-nums', mine ? 'text-brand' : 'text-foreground')}>{count}</span>
              )}
            </button>
          )
        })}
      </div>
      <Button variant={video.favorite ? 'soft' : 'outline'} aria-pressed={video.favorite} disabled={favorite.isPending}
        onClick={() => favorite.mutate(!video.favorite)}>
        <Heart size={16} aria-hidden className={cn(video.favorite && 'fill-current')} />
        {video.favorite ? t('videos.favorited') : t('videos.addFavorite')}
      </Button>
    </div>
  )
}
