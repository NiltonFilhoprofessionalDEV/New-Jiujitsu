import { useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'

import { EMBED_HOST, embedUrl, loadYoutubeApi, PLAYER_STATE_ENDED, type YtPlayer } from './youtube'

/** Fração assistida que conta como "assistido". */
const WATCHED_RATIO = 0.9
const PROGRESS_POLL_MS = 3000

type Props = {
  youtubeId: string
  vertical: boolean
  title: string
  /** Chamado uma vez por vídeo, ao passar de 90% ou ao terminar. */
  onWatched: () => void
  onEnded?: () => void
}

type ScreenOrientationWithLock = ScreenOrientation & { lock?: (orientation: 'landscape') => Promise<void> }

/**
 * Em tela cheia no celular, vira para paisagem quando o navegador permite (Android). No iPhone o player
 * nativo do YouTube já gira sozinho.
 */
function useLandscapeOnFullscreen(enabled: boolean) {
  useEffect(() => {
    if (!enabled) return
    const orientation = screen.orientation as ScreenOrientationWithLock | undefined
    const onChange = () => {
      if (document.fullscreenElement) orientation?.lock?.('landscape').catch(() => undefined)
      else orientation?.unlock?.()
    }
    document.addEventListener('fullscreenchange', onChange)
    return () => document.removeEventListener('fullscreenchange', onChange)
  }, [enabled])
}

/**
 * Player do YouTube (domínio sem cookies) que acompanha o progresso pela IFrame API. Se a API não
 * carregar (rede ou bloqueador), cai para o embed simples, sem marcar "assistido" sozinho.
 */
export function YoutubePlayer({ youtubeId, vertical, title, onWatched, onEnded }: Props) {
  const { t } = useTranslation()
  const hostRef = useRef<HTMLDivElement>(null)
  const callbacks = useRef({ onWatched, onEnded })
  const [fallback, setFallback] = useState(false)
  useLandscapeOnFullscreen(!vertical)

  useEffect(() => {
    callbacks.current = { onWatched, onEnded }
  })

  useEffect(() => {
    let player: YtPlayer | null = null
    let timer: number | undefined
    let cancelled = false
    let watched = false
    const markWatched = () => {
      if (watched) return
      watched = true
      callbacks.current.onWatched()
    }
    loadYoutubeApi()
      .then((YT) => {
        if (cancelled || !hostRef.current) return
        const target = document.createElement('div')
        hostRef.current.replaceChildren(target)
        player = new YT.Player(target, {
          host: EMBED_HOST,
          videoId: youtubeId,
          playerVars: { rel: 0, playsinline: 1 },
          events: {
            onStateChange: ({ data }) => {
              if (data !== PLAYER_STATE_ENDED) return
              markWatched()
              callbacks.current.onEnded?.()
            },
          },
        })
        timer = window.setInterval(() => {
          const duration = player?.getDuration?.() ?? 0
          if (duration > 0 && (player?.getCurrentTime?.() ?? 0) / duration >= WATCHED_RATIO) markWatched()
        }, PROGRESS_POLL_MS)
      })
      .catch(() => {
        if (!cancelled) setFallback(true)
      })
    return () => {
      cancelled = true
      window.clearInterval(timer)
      player?.destroy()
    }
  }, [youtubeId])

  return (
    <div className={vertical ? 'video-frame vertical' : 'video-frame'}>
      {fallback ? (
        <iframe src={embedUrl(youtubeId)} title={title} allowFullScreen
          allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture; fullscreen" />
      ) : (
        <div ref={hostRef} className="video-frame-host" role="region" aria-label={t('videos.player', { title })} />
      )}
    </div>
  )
}
