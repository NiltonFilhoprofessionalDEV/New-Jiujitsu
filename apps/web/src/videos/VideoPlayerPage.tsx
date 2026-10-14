import { ChevronLeft, CircleCheck } from 'lucide-react'
import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router'

import { Badge } from '@/components/ui/badge'
import { buttonVariants } from '@/components/ui/button-variants'
import { cn } from '@/lib/utils'

import { CommentsSection } from './CommentsSection'
import { queueLink } from './links'
import { PlaylistQueue } from './PlaylistQueue'
import { ReactionBar } from './ReactionBar'
import type { Playlist, Video } from './types'
import { useLocalDate } from './useLocalDate'
import { useMarkWatched, usePlaylists, useVideos } from './videosApi'
import { YoutubePlayer } from './YoutubePlayer'

const PUBLISHED_DATE: Intl.DateTimeFormatOptions = { dateStyle: 'long' }

function BackLink() {
  const { t } = useTranslation()
  return (
    <Link to=".." relative="path"
      className={cn(buttonVariants({ variant: 'ghost', size: 'sm' }), '-ml-2 mb-4 w-fit gap-1 text-muted-foreground hover:text-foreground')}>
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

function VideoInfo({ video, playlist }: { video: Video; playlist?: Playlist }) {
  const { t } = useTranslation()
  const formatDate = useLocalDate(PUBLISHED_DATE)
  return (
    <div className="grid gap-5">
      <div className="grid gap-2.5">
        <p className="m-0 flex flex-wrap items-center gap-x-3 gap-y-1 text-[13px] text-muted-foreground">
          {playlist && <span className="font-bold tracking-[0.14em] text-brand uppercase">{playlist.title}</span>}
          <time dateTime={video.createdAt}>{formatDate(video.createdAt)}</time>
          {video.watched && (
            <Badge variant="success">
              <CircleCheck aria-hidden />
              {t('videos.watched')}
            </Badge>
          )}
        </p>
        <h1 className="m-0 font-heading text-4xl leading-[0.95] font-normal tracking-wide uppercase md:text-5xl">{video.title}</h1>
      </div>
      <ReactionBar video={video} />
      {video.description && (
        <p className="m-0 rounded-2xl border border-border bg-card px-4 py-3.5 text-[15px] leading-relaxed whitespace-pre-line text-muted-foreground">
          {video.description}
        </p>
      )}
    </div>
  )
}

function VideoView({ video, queue }: { video: Video; queue: ReturnType<typeof usePlaylistQueue> }) {
  const navigate = useNavigate()
  const markWatched = useMarkWatched(video.id)
  const next = queue?.queue[queue.queue.findIndex((v) => v.id === video.id) + 1]

  // Trocar de vídeo pela fila mantém a rolagem da página anterior e esconderia o player.
  useEffect(() => { window.scrollTo({ top: 0 }) }, [video.id])

  // No celular a fila vem logo depois do vídeo; no desktop ela ocupa a coluna da direita nas duas linhas.
  return (
    <div className={cn('grid items-start gap-7',
      queue && 'min-[1001px]:grid-cols-[minmax(0,1fr)_360px] min-[1001px]:grid-rows-[auto_1fr] min-[1001px]:gap-x-6')}>
      <div className="grid min-w-0 gap-7 min-[1001px]:col-start-1">
        <YoutubePlayer key={video.id} youtubeId={video.youtubeId} vertical={video.vertical} title={video.title}
          onWatched={() => { if (!video.watched) markWatched.mutate() }}
          onEnded={next && queue ? () => navigate(queueLink(next.id, queue.playlist.id), { relative: 'path' }) : undefined} />
        <VideoInfo video={video} playlist={queue?.playlist} />
      </div>
      {queue && (
        <PlaylistQueue playlist={queue.playlist} videos={queue.queue} currentId={video.id}
          className="min-[1001px]:col-start-2 min-[1001px]:row-span-2 min-[1001px]:row-start-1" />
      )}
      <div className="min-w-0 min-[1001px]:col-start-1">
        <CommentsSection videoId={video.id} />
      </div>
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
    <div className="page">
      <BackLink />
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {!isLoading && !video && <div className="card empty-detail">{t('videos.notFound')}</div>}
      {video && <VideoView video={video} queue={queue} />}
    </div>
  )
}
