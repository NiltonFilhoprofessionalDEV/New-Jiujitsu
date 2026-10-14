import { ChevronLeft, CircleCheck } from 'lucide-react'
import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router'

import { CommentsSection } from './CommentsSection'
import { queueLink } from './links'
import { PlaylistQueue } from './PlaylistQueue'
import { ReactionBar } from './ReactionBar'
import type { Video } from './types'
import { useLocalDate } from './useLocalDate'
import { useMarkWatched, usePlaylists, useVideos } from './videosApi'
import { YoutubePlayer } from './YoutubePlayer'

const PUBLISHED_DATE: Intl.DateTimeFormatOptions = { dateStyle: 'long' }

function BackLink() {
  const { t } = useTranslation()
  return (
    <Link to=".." relative="path" className="back-link">
      <ChevronLeft size={18} aria-hidden />
      {t('videos.backToLibrary')}
    </Link>
  )
}

/** Vídeos da playlist pedida na URL, na ordem do gestor; sem playlist (ou sem acesso), nada. */
function usePlaylistQueue(videos: Video[]) {
  const [params] = useSearchParams()
  const { data: playlists = [] } = usePlaylists()
  const playlist = playlists.find((p) => p.id === params.get('playlist'))
  if (!playlist) return null
  const byId = new Map(videos.map((v) => [v.id, v]))
  const queue = playlist.videoIds.map((id) => byId.get(id)).filter((v): v is Video => Boolean(v))
  return { playlist, queue }
}

function VideoView({ video, queue }: { video: Video; queue: ReturnType<typeof usePlaylistQueue> }) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const markWatched = useMarkWatched(video.id)
  const formatDate = useLocalDate(PUBLISHED_DATE)
  const next = queue?.queue[queue.queue.findIndex((v) => v.id === video.id) + 1]

  // Trocar de vídeo pela fila mantém a rolagem da página anterior e esconderia o player.
  useEffect(() => { window.scrollTo({ top: 0 }) }, [video.id])

  return (
    <div className={queue ? 'video-layout with-queue' : 'video-layout'}>
      <div className="video-main">
        <YoutubePlayer key={video.id} youtubeId={video.youtubeId} vertical={video.vertical} title={video.title}
          onWatched={() => { if (!video.watched) markWatched.mutate() }}
          onEnded={next && queue ? () => navigate(queueLink(next.id, queue.playlist.id), { relative: 'path' }) : undefined} />
        <div className="video-info">
          <h1 className="video-title">{video.title}</h1>
          <p className="video-meta">
            <span>{formatDate(video.createdAt)}</span>
            {video.watched && (
              <span className="video-watched">
                <CircleCheck size={16} aria-hidden />
                {t('videos.watched')}
              </span>
            )}
          </p>
          <ReactionBar video={video} />
          {video.description && <p className="video-description">{video.description}</p>}
        </div>
        <CommentsSection videoId={video.id} />
      </div>
      {queue && <PlaylistQueue playlist={queue.playlist} videos={queue.queue} currentId={video.id} />}
    </div>
  )
}

export function VideoPlayerPage() {
  const { t } = useTranslation()
  const { videoId } = useParams()
  const { data: videos = [], isLoading } = useVideos()
  const queue = usePlaylistQueue(videos)
  const video = videos.find((v) => v.id === videoId)

  return (
    <div className="page video-page">
      <BackLink />
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {!isLoading && !video && <div className="card empty-detail">{t('videos.notFound')}</div>}
      {video && <VideoView video={video} queue={queue} />}
    </div>
  )
}
