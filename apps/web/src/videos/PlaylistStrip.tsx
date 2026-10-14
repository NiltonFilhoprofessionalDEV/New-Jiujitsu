import { ListVideo, Pencil, Trash2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { ActionMenu } from '@/components/ActionMenu'

import type { Playlist, Video } from './types'
import { thumbnailUrl } from './youtube'

type Props = {
  playlists: Playlist[]
  byId: Map<string, Video>
  onEdit?: (playlist: Playlist) => void
  onDelete?: (playlist: Playlist) => void
}

/** Playlist abre no primeiro vídeo que a pessoa ainda não assistiu (ou no primeiro, se viu todos). */
function startOf(playlist: Playlist, byId: Map<string, Video>): string | undefined {
  return playlist.videoIds.find((id) => byId.get(id)?.watched === false) ?? playlist.videoIds[0]
}

export function PlaylistStrip({ playlists, byId, onEdit, onDelete }: Props) {
  const { t } = useTranslation()
  if (playlists.length === 0) return null
  return (
    <section aria-labelledby="playlists-title" className="playlist-strip">
      <h2 id="playlists-title" className="section-title">{t('videos.playlists')}</h2>
      <ul className="playlist-cards">
        {playlists.map((p) => {
          const start = startOf(p, byId)
          const cover = p.videoIds.map((id) => byId.get(id)).find(Boolean)
          const watched = p.videoIds.filter((id) => byId.get(id)?.watched).length
          const content = (
            <>
              <span className="playlist-cover">
                {cover && <img src={thumbnailUrl(cover.youtubeId)} alt="" loading="lazy" />}
                <span className="playlist-cover-count">
                  <ListVideo size={14} aria-hidden />
                  {p.videoIds.length}
                </span>
              </span>
              <span className="playlist-card-title">{p.title}</span>
              <span className="row-sub">
                {t('videos.playlistProgress', { watched, count: p.videoIds.length })}
              </span>
            </>
          )
          return (
            <li key={p.id} className="playlist-card">
              {start ? (
                <Link to={`${start}?playlist=${encodeURIComponent(p.id)}`} className="playlist-card-link">{content}</Link>
              ) : (
                <div className="playlist-card-link">{content}</div>
              )}
              {onEdit && onDelete && (
                <ActionMenu actions={[
                  { label: t('common.edit'), icon: Pencil, onSelect: () => onEdit(p) },
                  { label: t('common.delete'), icon: Trash2, onSelect: () => onDelete(p) },
                ]} />
              )}
            </li>
          )
        })}
      </ul>
    </section>
  )
}
