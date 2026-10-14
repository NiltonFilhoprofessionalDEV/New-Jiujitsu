import { MessageCircle, Play } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { buttonVariants } from '@/components/ui/button-variants'
import { cn } from '@/lib/utils'

import { HiResThumb } from './HiResThumb'
import { totalReactions } from './reactions'
import type { Video } from './types'

/** Destaque no topo da biblioteca: capa grande e um único convite, assistir. */
export function FeaturedVideo({ video }: { video: Video }) {
  const { t } = useTranslation()
  const reactions = totalReactions(video.reactions)
  return (
    <section aria-labelledby="featured-title"
      className="group relative isolate grid overflow-hidden rounded-3xl border border-ink-border bg-ink text-ink-foreground shadow-elevated md:min-h-[360px] md:grid-cols-[minmax(0,5fr)_minmax(0,6fr)]">
      <Link to={video.id} tabIndex={-1} aria-hidden
        className="relative block aspect-video overflow-hidden md:absolute md:inset-y-0 md:right-0 md:aspect-auto md:w-[62%]">
        <HiResThumb youtubeId={video.youtubeId}
          className="size-full object-cover transition-transform duration-700 ease-sheet group-hover:scale-[1.03]" />
        <span className="absolute inset-0 bg-gradient-to-t from-ink via-ink/20 to-transparent md:bg-gradient-to-r md:from-ink md:via-ink/50" />
      </Link>
      <div className="relative z-10 flex flex-col justify-end gap-4 px-6 pb-6 md:px-9 md:py-9">
        <p className="m-0 flex items-center gap-2 text-[11px] font-bold tracking-[0.2em] text-pulse uppercase">
          <span className="size-1.5 rounded-full bg-pulse shadow-glow" aria-hidden />
          {video.watched ? t('videos.featured.again') : t('videos.featured.new')}
        </p>
        <h2 id="featured-title" className="m-0 font-heading text-4xl leading-[0.95] font-normal tracking-wide uppercase md:text-5xl">
          {video.title}
        </h2>
        {video.description && <p className="m-0 line-clamp-2 max-w-prose text-sm text-ink-muted">{video.description}</p>}
        <div className="flex flex-wrap items-center gap-x-5 gap-y-3">
          <Link to={video.id} className={cn(buttonVariants({ variant: 'light', size: 'lg' }))}>
            <Play size={18} aria-hidden className="fill-current" />
            {t('videos.featured.watch')}
          </Link>
          <span className="flex items-center gap-4 text-sm text-ink-muted">
            {reactions > 0 && <span>{t('videos.reactionsCount', { count: reactions })}</span>}
            {video.commentCount > 0 && (
              <span className="inline-flex items-center gap-1.5">
                <MessageCircle size={15} aria-hidden />
                {t('videos.commentsCount', { count: video.commentCount })}
              </span>
            )}
          </span>
        </div>
      </div>
    </section>
  )
}
