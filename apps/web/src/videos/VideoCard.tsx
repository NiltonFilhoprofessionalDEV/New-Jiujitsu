import { CircleCheck, Heart, MessageCircle, Pencil, Play, Trash2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { ActionMenu } from '@/components/ActionMenu'
import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'

import { totalReactions } from './reactions'
import type { Video } from './types'
import { thumbnailUrl } from './youtube'

type Props = {
  video: Video
  /** Link relativo à tela atual (equipe e área do aluno têm bases diferentes). */
  to: string
  onEdit?: () => void
  onDelete?: () => void
}

function Thumb({ video }: { video: Video }) {
  const { t } = useTranslation()
  return (
    <span className="relative block aspect-video overflow-hidden rounded-2xl bg-surface-3 ring-1 ring-border transition-shadow duration-300 group-hover/card:shadow-glow-sm group-hover/card:ring-primary-soft-border">
      <img src={thumbnailUrl(video.youtubeId)} alt="" loading="lazy"
        className={cn('size-full object-cover transition-transform duration-500 ease-sheet group-hover/card:scale-[1.04]',
          video.watched && 'opacity-55')} />
      <span aria-hidden className="absolute inset-0 bg-gradient-to-t from-black/55 via-transparent to-transparent" />
      <span aria-hidden className="absolute inset-0 grid place-items-center opacity-0 transition-opacity duration-300 group-hover/card:opacity-100">
        <span className="grid size-12 place-items-center rounded-full bg-white/95 text-ink shadow-elevated">
          <Play size={20} className="translate-x-px fill-current" />
        </span>
      </span>
      {video.vertical && <Badge variant="overlay" className="absolute top-2.5 left-2.5">{t('videos.shorts')}</Badge>}
      {video.favorite && (
        <span className="absolute top-2.5 right-2.5 grid size-7 place-items-center rounded-full bg-black/70 text-pulse">
          <Heart size={14} aria-hidden className="fill-current" />
          <span className="sr-only">{t('videos.favorite')}</span>
        </span>
      )}
      {video.watched && (
        <>
          <Badge variant="overlay" className="absolute bottom-3 left-2.5 gap-1">
            <CircleCheck size={13} aria-hidden className="text-success" />
            {t('videos.watched')}
          </Badge>
          <span aria-hidden className="absolute inset-x-0 bottom-0 h-1 bg-pulse" />
        </>
      )}
    </span>
  )
}

/** Capa, título e contadores. Gestor ganha o menu "⋯" com editar e excluir. */
export function VideoCard({ video, to, onEdit, onDelete }: Props) {
  const { t } = useTranslation()
  const reactions = totalReactions(video.reactions)
  return (
    <article className="group/card grid gap-2.5">
      <Link to={to}
        className="grid gap-3 rounded-2xl text-foreground no-underline hover:no-underline focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-ring">
        <Thumb video={video} />
        <span className="line-clamp-2 px-0.5 leading-snug font-semibold">{video.title}</span>
      </Link>
      <div className="flex min-h-8 items-center gap-3 px-0.5 text-[13px] text-muted-foreground">
        {reactions > 0 && <span>{t('videos.reactionsCount', { count: reactions })}</span>}
        {video.commentCount > 0 && (
          <span className="inline-flex items-center gap-1">
            <MessageCircle size={14} aria-hidden />
            {video.commentCount}
            <span className="sr-only">{t('videos.commentsCount', { count: video.commentCount })}</span>
          </span>
        )}
        {onEdit && onDelete && (
          <span className="ml-auto">
            <ActionMenu actions={[
              { label: t('common.edit'), icon: Pencil, onSelect: onEdit },
              { label: t('common.delete'), icon: Trash2, onSelect: onDelete },
            ]} />
          </span>
        )}
      </div>
    </article>
  )
}
