import { ListVideo, Pencil, Trash2 } from 'lucide-react'
import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { ActionMenu } from '@/components/ActionMenu'
import { Badge } from '@/components/ui/badge'

import { PlaylistProgress } from './PlaylistProgress'
import { SectionHeading } from './SectionHeading'
import type { Playlist, Video } from './types'
import { thumbnailUrl } from './youtube'

type Props = {
  playlists: Playlist[]
  byId: Map<string, Video>
  onEdit?: (playlist: Playlist) => void
  onDelete?: (playlist: Playlist) => void
}

const CARD_CLASS =
  'grid overflow-hidden rounded-2xl bg-ink text-ink-foreground no-underline ring-1 ring-ink-border transition-shadow duration-300 hover:no-underline hover:shadow-glow-sm focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring'

/** Playlist abre no primeiro vídeo que a pessoa ainda não assistiu (ou no primeiro, se viu todos). */
function startOf(playlist: Playlist, byId: Map<string, Video>): string | undefined {
  return playlist.videoIds.find((id) => byId.get(id)?.watched === false) ?? playlist.videoIds[0]
}

function PlaylistCardBody({ playlist, byId }: { playlist: Playlist; byId: Map<string, Video> }) {
  const { t } = useTranslation()
  const cover = playlist.videoIds.map((id) => byId.get(id)).find(Boolean)
  const count = playlist.videoIds.length
  const watched = playlist.videoIds.filter((id) => byId.get(id)?.watched).length
  return (
    <>
      <span className="relative block aspect-video overflow-hidden bg-ink-2">
        {cover && (
          <img src={thumbnailUrl(cover.youtubeId)} alt="" loading="lazy"
            className="size-full object-cover opacity-85 transition-transform duration-500 ease-sheet group-hover/pl:scale-[1.04]" />
        )}
        <span aria-hidden className="absolute inset-0 bg-gradient-to-t from-ink via-ink/10 to-transparent" />
        <Badge variant="overlay" className="absolute top-2.5 left-2.5">
          <ListVideo aria-hidden />
          {count}
        </Badge>
      </span>
      <span className="grid gap-3 px-4 pt-1 pb-4">
        <span className="truncate font-heading text-2xl leading-none tracking-wide uppercase">{playlist.title}</span>
        <PlaylistProgress watched={watched} total={count} />
        <span className="text-xs text-ink-muted">{t('videos.playlistProgress', { watched, count })}</span>
      </span>
    </>
  )
}

function PlaylistCard({ playlist, byId, menu }: { playlist: Playlist; byId: Map<string, Video>; menu: ReactNode }) {
  const start = startOf(playlist, byId)
  const body = <PlaylistCardBody playlist={playlist} byId={byId} />
  return (
    <li className="group/pl relative w-[78%] max-w-[300px] shrink-0 snap-start sm:w-[280px]">
      {start ? (
        <Link to={`${start}?playlist=${encodeURIComponent(playlist.id)}`} className={CARD_CLASS}>{body}</Link>
      ) : (
        <div className={CARD_CLASS}>{body}</div>
      )}
      {menu && <span className="absolute top-2 right-2 rounded-full bg-card/90 backdrop-blur-sm">{menu}</span>}
    </li>
  )
}

export function PlaylistStrip({ playlists, byId, onEdit, onDelete }: Props) {
  const { t } = useTranslation()
  if (playlists.length === 0) return null
  const menuFor = (p: Playlist) => onEdit && onDelete && (
    <ActionMenu actions={[
      { label: t('common.edit'), icon: Pencil, onSelect: () => onEdit(p) },
      { label: t('common.delete'), icon: Trash2, onSelect: () => onDelete(p) },
    ]} />
  )
  return (
    <section aria-labelledby="playlists-title" className="grid gap-4">
      <SectionHeading id="playlists-title" title={t('videos.playlists')} />
      <ul className="-mx-4 my-0 flex snap-x snap-mandatory list-none gap-4 overflow-x-auto px-4 pt-0 pb-2 [scrollbar-width:thin] md:mx-0 md:px-0">
        {playlists.map((p) => <PlaylistCard key={p.id} playlist={p} byId={byId} menu={menuFor(p)} />)}
      </ul>
    </section>
  )
}
