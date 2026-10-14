import { CircleCheck, Heart, MessageCircle, Pencil, Trash2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { ActionMenu } from '@/components/ActionMenu'

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

/** Capa, título e contadores. Gestor ganha o menu "⋯" com editar e excluir. */
export function VideoCard({ video, to, onEdit, onDelete }: Props) {
  const { t } = useTranslation()
  const reactions = totalReactions(video.reactions)
  return (
    <article className={video.watched ? 'video-card watched' : 'video-card'}>
      <Link to={to} className="video-card-link">
        <span className="video-thumb">
          <img src={thumbnailUrl(video.youtubeId)} alt="" loading="lazy" />
          {video.watched && (
            <span className="video-thumb-badge">
              <CircleCheck size={14} aria-hidden />
              {t('videos.watched')}
            </span>
          )}
        </span>
        <span className="video-card-title">{video.title}</span>
      </Link>
      <div className="video-card-meta">
        {video.favorite && (
          <span className="video-stat favorite">
            <Heart size={14} aria-hidden />
            <span className="sr-only">{t('videos.favorite')}</span>
          </span>
        )}
        {reactions > 0 && <span className="video-stat">{t('videos.reactionsCount', { count: reactions })}</span>}
        {video.commentCount > 0 && (
          <span className="video-stat">
            <MessageCircle size={14} aria-hidden />
            {video.commentCount}
            <span className="sr-only">{t('videos.commentsCount', { count: video.commentCount })}</span>
          </span>
        )}
        {onEdit && onDelete && (
          <ActionMenu actions={[
            { label: t('common.edit'), icon: Pencil, onSelect: onEdit },
            { label: t('common.delete'), icon: Trash2, onSelect: onDelete },
          ]} />
        )}
      </div>
    </article>
  )
}
