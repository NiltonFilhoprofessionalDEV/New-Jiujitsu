import { CircleCheck, Play } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { Card } from '@/components/ui/card'
import { cn } from '@/lib/utils'

import { queueLink } from './links'
import { PlaylistProgress } from './PlaylistProgress'
import type { Playlist, Video } from './types'
import { thumbnailUrl } from './youtube'

type Props = { playlist: Playlist; videos: Video[]; currentId: string; className?: string }

function QueueItem({ video, index, current, playlistId }: { video: Video; index: number; current: boolean; playlistId: string }) {
  const { t } = useTranslation()
  return (
    <Link to={queueLink(video.id, playlistId)} relative="path" aria-current={current ? 'true' : undefined}
      className={cn(
        'grid grid-cols-[20px_104px_minmax(0,1fr)] items-center gap-3 rounded-xl p-2 text-foreground no-underline transition-colors hover:bg-accent hover:no-underline focus-visible:outline-2 focus-visible:outline-ring',
        current && 'bg-primary-soft hover:bg-primary-soft',
      )}>
      <span className="grid place-items-center text-[13px] font-semibold text-muted-foreground tabular-nums">
        {current ? <Play size={14} aria-hidden className="fill-current text-pulse" /> : index + 1}
      </span>
      <span className="relative block aspect-video overflow-hidden rounded-lg bg-surface-3">
        <img src={thumbnailUrl(video.youtubeId)} alt="" loading="lazy" className={cn('size-full object-cover', video.watched && 'opacity-60')} />
        {video.watched && (
          <span className="absolute right-1 bottom-1 grid size-5 place-items-center rounded-full bg-black/75">
            <CircleCheck size={13} className="text-success" aria-label={t('videos.watched')} />
          </span>
        )}
      </span>
      <span className={cn('line-clamp-2 text-sm leading-snug', current ? 'font-bold' : 'font-semibold')}>{video.title}</span>
    </Link>
  )
}

export function PlaylistQueue({ playlist, videos, currentId, className }: Props) {
  const { t } = useTranslation()
  const position = videos.findIndex((v) => v.id === currentId) + 1
  const watched = videos.filter((v) => v.watched).length
  return (
    <Card aria-labelledby="queue-title"
      className={cn('grid gap-3 overflow-hidden p-3 min-[1001px]:sticky min-[1001px]:top-4 min-[1001px]:max-h-[calc(100vh-32px)] min-[1001px]:grid-rows-[auto_minmax(0,1fr)]', className)}>
      <div className="grid gap-2.5 px-2 pt-2">
        <div className="flex items-baseline justify-between gap-3">
          <h2 id="queue-title" className="m-0 truncate font-heading text-2xl leading-none font-normal tracking-wide uppercase">
            {playlist.title}
          </h2>
          <span className="shrink-0 text-xs font-semibold text-muted-foreground tabular-nums">
            {t('videos.queuePosition', { position, count: videos.length })}
          </span>
        </div>
        <PlaylistProgress watched={watched} total={videos.length} />
        <span className="text-xs text-muted-foreground">{t('videos.playlistProgress', { watched, count: videos.length })}</span>
      </div>
      <ol className="m-0 grid list-none content-start gap-1 overflow-y-auto p-0">
        {videos.map((v, index) => (
          <li key={v.id}><QueueItem video={v} index={index} current={v.id === currentId} playlistId={playlist.id} /></li>
        ))}
      </ol>
    </Card>
  )
}
