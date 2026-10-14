import { CircleCheck } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { queueLink } from './links'
import type { Playlist, Video } from './types'
import { thumbnailUrl } from './youtube'

type Props = { playlist: Playlist; videos: Video[]; currentId: string }

export function PlaylistQueue({ playlist, videos, currentId }: Props) {
  const { t } = useTranslation()
  const position = videos.findIndex((v) => v.id === currentId) + 1
  return (
    <aside className="card playlist-queue" aria-labelledby="queue-title">
      <div className="playlist-queue-head">
        <h2 id="queue-title" className="playlist-queue-title">{playlist.title}</h2>
        <span className="row-sub">{t('videos.queuePosition', { position, count: videos.length })}</span>
      </div>
      <ol className="queue-list">
        {videos.map((v, index) => (
          <li key={v.id}>
            <Link to={queueLink(v.id, playlist.id)} relative="path" aria-current={v.id === currentId ? 'true' : undefined}
              className={v.id === currentId ? 'queue-item current' : 'queue-item'}>
              <span className="queue-index">{index + 1}</span>
              <img src={thumbnailUrl(v.youtubeId)} alt="" loading="lazy" className="queue-thumb" />
              <span className="queue-title">{v.title}</span>
              {v.watched && <CircleCheck size={16} className="queue-watched" aria-label={t('videos.watched')} />}
            </Link>
          </li>
        ))}
      </ol>
    </aside>
  )
}
