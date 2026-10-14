/** Link para outro vídeo da mesma playlist, a partir da URL do vídeo atual (usar com relative="path"). */
export function queueLink(videoId: string, playlistId: string): string {
  return `../${encodeURIComponent(videoId)}?playlist=${encodeURIComponent(playlistId)}`
}
