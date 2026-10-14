import { useMutation, useQuery, useQueryClient, type QueryClient } from '@tanstack/react-query'

import { api } from '@/lib/api'

import type { Playlist, PlaylistRequest, Reaction, Video, VideoComment, VideoRequest } from './types'

const VIDEOS = 'videos'
const PLAYLISTS = 'playlists'
const LIST_KEY = [VIDEOS, 'list']

const videoPath = (id: string) => `/videos/${encodeURIComponent(id)}`

export function useVideos() {
  return useQuery({
    queryKey: LIST_KEY,
    queryFn: async () => (await api.get<Video[]>('/videos')).data,
  })
}

export function usePlaylists() {
  return useQuery({
    queryKey: [PLAYLISTS],
    queryFn: async () => (await api.get<Playlist[]>('/playlists')).data,
  })
}

/** Troca o vídeo na lista em cache pela versão que a API acabou de devolver. */
function replaceInList(queryClient: QueryClient, video: Video) {
  queryClient.setQueryData<Video[]>(LIST_KEY, (list) => list?.map((v) => (v.id === video.id ? video : v)))
}

/** Cadastro muda vídeos e playlists ao mesmo tempo (o vídeo pode entrar ou sair de playlists). */
function useLibraryMutation<TVars, TResult>(fn: (vars: TVars) => Promise<TResult>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: [VIDEOS] }),
        queryClient.invalidateQueries({ queryKey: [PLAYLISTS] }),
      ]),
  })
}

export function useSaveVideo() {
  return useLibraryMutation(async ({ id, body }: { id?: string; body: VideoRequest }) =>
    id ? (await api.put<Video>(videoPath(id), body)).data : (await api.post<Video>('/videos', body)).data,
  )
}

export function useDeleteVideo() {
  return useLibraryMutation(async (id: string) => {
    await api.delete(videoPath(id))
  })
}

export function useSavePlaylist() {
  return useLibraryMutation(async ({ id, body }: { id?: string; body: PlaylistRequest }) =>
    id
      ? (await api.put<Playlist>(`/playlists/${encodeURIComponent(id)}`, body)).data
      : (await api.post<Playlist>('/playlists', body)).data,
  )
}

export function useDeletePlaylist() {
  return useLibraryMutation(async (id: string) => {
    await api.delete(`/playlists/${encodeURIComponent(id)}`)
  })
}

/** Reação, favorito e "assistido": a resposta já é o vídeo atualizado. */
function useVideoUpdate<TVars>(fn: (vars: TVars) => Promise<Video>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: (video) => replaceInList(queryClient, video),
  })
}

export function useReact(videoId: string) {
  return useVideoUpdate(async (reaction: Reaction | null) =>
    (await api.put<Video>(`${videoPath(videoId)}/reaction`, { reaction })).data,
  )
}

export function useFavorite(videoId: string) {
  return useVideoUpdate(async (favorite: boolean) => {
    const url = `${videoPath(videoId)}/favorite`
    return (await (favorite ? api.put<Video>(url) : api.delete<Video>(url))).data
  })
}

export function useMarkWatched(videoId: string) {
  return useVideoUpdate(async () => (await api.put<Video>(`${videoPath(videoId)}/watched`)).data)
}

const commentsKey = (videoId: string) => [VIDEOS, videoId, 'comments']

export function useComments(videoId: string) {
  return useQuery({
    queryKey: commentsKey(videoId),
    queryFn: async () => (await api.get<VideoComment[]>(`${videoPath(videoId)}/comments`)).data,
  })
}

/** A resposta é a lista completa de comentários; o total no card do vídeo é atualizado na mão. */
function useCommentMutation<TVars>(videoId: string, fn: (vars: TVars) => Promise<VideoComment[]>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: (comments) => {
      queryClient.setQueryData(commentsKey(videoId), comments)
      queryClient.setQueryData<Video[]>(LIST_KEY, (list) =>
        list?.map((v) => (v.id === videoId ? { ...v, commentCount: comments.length } : v)),
      )
    },
  })
}

export function useAddComment(videoId: string) {
  return useCommentMutation(videoId, async (body: string) =>
    (await api.post<VideoComment[]>(`${videoPath(videoId)}/comments`, { body })).data,
  )
}

export function useDeleteComment(videoId: string) {
  return useCommentMutation(videoId, async (commentId: string) =>
    (await api.delete<VideoComment[]>(`${videoPath(videoId)}/comments/${encodeURIComponent(commentId)}`)).data,
  )
}
