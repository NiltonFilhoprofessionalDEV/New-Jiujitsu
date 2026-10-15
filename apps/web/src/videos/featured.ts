import type { Video } from './types'

/**
 * Mais novo ainda não assistido (a API manda do mais novo ao mais antigo); se a pessoa já viu tudo, não há
 * destaque. Vídeo deitado tem preferência: a capa de um Shorts fica com faixas pretas num destaque largo.
 */
export function pickFeatured(videos: Video[]): Video | undefined {
  const unwatched = videos.filter((v) => !v.watched)
  return unwatched.find((v) => !v.vertical) ?? unwatched[0]
}
