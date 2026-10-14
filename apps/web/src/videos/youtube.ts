/** Domínio sem cookies de rastreamento até o play; precisa estar no frame-src da CSP. */
export const EMBED_HOST = 'https://www.youtube-nocookie.com'
const API_SRC = 'https://www.youtube.com/iframe_api'
const THUMB_HOST = 'https://i.ytimg.com'

export const PLAYER_STATE_ENDED = 0

/** Só o pedaço da IFrame API que o player usa. */
export type YtPlayer = {
  getCurrentTime: () => number
  getDuration: () => number
  destroy: () => void
}

type YtPlayerOptions = {
  host: string
  videoId: string
  playerVars: Record<string, number | string>
  events: { onStateChange: (event: { data: number }) => void }
}

type YtNamespace = { Player: new (element: HTMLElement, options: YtPlayerOptions) => YtPlayer }

declare global {
  interface Window {
    YT?: YtNamespace
    onYouTubeIframeAPIReady?: () => void
  }
}

let apiPromise: Promise<YtNamespace> | null = null

/** Carrega a IFrame API uma única vez; numa falha, a próxima chamada tenta de novo. */
export function loadYoutubeApi(): Promise<YtNamespace> {
  if (window.YT?.Player) return Promise.resolve(window.YT)
  apiPromise ??= new Promise<YtNamespace>((resolve, reject) => {
    const previous = window.onYouTubeIframeAPIReady
    window.onYouTubeIframeAPIReady = () => {
      previous?.()
      if (window.YT) resolve(window.YT)
    }
    const script = document.createElement('script')
    script.src = API_SRC
    script.async = true
    script.onerror = () => {
      apiPromise = null
      script.remove()
      reject(new Error('YouTube IFrame API indisponível'))
    }
    document.head.append(script)
  })
  return apiPromise
}

/** {@code maxres} (1280px) nem sempre existe; {@code hq} (480px) existe para todo vídeo. */
export type ThumbQuality = 'hq' | 'maxres'

const THUMB_FILE: Record<ThumbQuality, string> = { hq: 'hqdefault.jpg', maxres: 'maxresdefault.jpg' }

/** Largura da imagem cinza que o YouTube devolve quando a capa HD não existe. */
export const MISSING_THUMB_WIDTH = 120

export function thumbnailUrl(youtubeId: string, quality: ThumbQuality = 'hq'): string {
  return `${THUMB_HOST}/vi/${encodeURIComponent(youtubeId)}/${THUMB_FILE[quality]}`
}

export function embedUrl(youtubeId: string): string {
  return `${EMBED_HOST}/embed/${encodeURIComponent(youtubeId)}?rel=0&playsinline=1`
}

const VIDEO_ID = /^[A-Za-z0-9_-]{11}$/
const ID_PATHS = ['embed', 'shorts', 'live', 'v']
const WATCH_HOSTS = ['youtube.com', 'm.youtube.com', 'music.youtube.com', 'youtube-nocookie.com']

/**
 * Prévia no formulário: id do vídeo num link colado. A API valida de novo (e com mais rigor) ao salvar.
 */
export function previewYoutubeId(input: string): string | null {
  const text = input.trim()
  const href = text.includes('://') ? text : `https://${text}`
  if (!text || !URL.canParse(href)) return null
  const candidate = idFromUrl(new URL(href))
  return candidate && VIDEO_ID.test(candidate) ? candidate : null
}

function idFromUrl(url: URL): string | null | undefined {
  const host = url.hostname.replace(/^www\./, '')
  const [first, second] = url.pathname.split('/').filter(Boolean)
  if (host === 'youtu.be') return first
  if (!WATCH_HOSTS.includes(host)) return null
  if (first === 'watch') return url.searchParams.get('v')
  return first && ID_PATHS.includes(first) ? second : null
}
