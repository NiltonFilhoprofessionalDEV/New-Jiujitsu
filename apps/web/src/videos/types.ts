import type { MembershipRole } from '@/auth/types'

export type Audience = 'ALL' | 'BELTS' | 'CLASSES'

export type Reaction = 'LIKE' | 'FIRE' | 'CLAP' | 'STRONG' | 'LAUGH'

export type Video = {
  id: string
  youtubeId: string
  /** Shorts: vídeo em pé (9:16). */
  vertical: boolean
  title: string
  description: string | null
  createdAt: string
  audience: Audience
  belts: string[]
  classIds: string[]
  reactions: Partial<Record<Reaction, number>>
  myReaction: Reaction | null
  commentCount: number
  favorite: boolean
  watched: boolean
}

export type VideoRequest = {
  url: string
  title: string
  description: string | null
  audience: Audience
  belts: string[]
  classIds: string[]
  playlistIds: string[]
}

/** {@code videoIds}: só os que a pessoa pode ver, na ordem do gestor. */
export type Playlist = {
  id: string
  title: string
  description: string | null
  videoIds: string[]
}

export type PlaylistRequest = {
  title: string
  description: string | null
  videoIds: string[]
}

export type VideoComment = {
  id: string
  authorName: string
  authorRole: MembershipRole
  mine: boolean
  canDelete: boolean
  body: string
  createdAt: string
}
