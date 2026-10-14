import { ListPlus, Plus } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { useAuth, useIsManager } from '@/auth/useAuth'
import { PageHeader } from '@/components/PageHeader'
import { SearchField } from '@/components/SearchField'
import { Button } from '@/components/ui/button'
import { fold } from '@/lib/text'
import { cn } from '@/lib/utils'

import { pickFeatured } from './featured'
import { FeaturedVideo } from './FeaturedVideo'
import { LibraryEditor, type Deleting, type Editing } from './LibraryEditor'
import { PlaylistStrip } from './PlaylistStrip'
import { SectionHeading } from './SectionHeading'
import type { Video } from './types'
import { VideoCard } from './VideoCard'
import { usePlaylists, useVideos } from './videosApi'

const FILTERS = ['all', 'unwatched', 'favorites'] as const
type Filter = (typeof FILTERS)[number]

const MATCHES: Record<Filter, (v: Video) => boolean> = {
  all: () => true,
  unwatched: (v) => !v.watched,
  favorites: (v) => v.favorite,
}

type FiltersProps = {
  videos: Video[]
  filter: Filter
  onFilter: (filter: Filter) => void
  search: string
  onSearch: (search: string) => void
}

function LibraryFilters({ videos, filter, onFilter, search, onSearch }: FiltersProps) {
  const { t } = useTranslation()
  return (
    <div className="toolbar mb-0 gap-3">
      <SearchField value={search} onChange={onSearch} label={t('videos.search')} />
      <div className="inline-flex max-w-full overflow-x-auto rounded-full bg-surface-3 p-1" role="tablist"
        aria-label={t('videos.filterAria')}>
        {FILTERS.map((f) => (
          <button key={f} type="button" role="tab" aria-selected={filter === f} onClick={() => onFilter(f)}
            className={cn(
              'inline-flex h-8 shrink-0 cursor-pointer items-center gap-1.5 rounded-full border-0 bg-transparent px-3.5 text-[13px] font-semibold text-muted-foreground transition-colors hover:text-foreground focus-visible:outline-2 focus-visible:outline-ring',
              filter === f && 'bg-card text-foreground shadow-sm dark:bg-white/10',
            )}>
            {t(`videos.filter.${f}`)}
            <span className="tabular-nums opacity-60">{videos.filter(MATCHES[f]).length}</span>
          </button>
        ))}
      </div>
    </div>
  )
}

function ManagerActions({ onEdit }: { onEdit: (editing: Editing) => void }) {
  const { t } = useTranslation()
  return (
    <>
      <Button variant="outline" onClick={() => onEdit({ playlist: undefined })}>
        <ListPlus size={16} aria-hidden />
        {t('videos.newPlaylist')}
      </Button>
      <Button onClick={() => onEdit({ video: undefined })}>
        <Plus size={16} aria-hidden />
        {t('videos.addVideo')}
      </Button>
    </>
  )
}

type GridProps = { videos: Video[]; onEdit?: (v: Video) => void; onDelete?: (v: Video) => void }

function VideoGrid({ videos, onEdit, onDelete }: GridProps) {
  return (
    <ul className="m-0 grid list-none grid-cols-[repeat(auto-fill,minmax(min(250px,100%),1fr))] gap-x-5 gap-y-7 p-0">
      {videos.map((v) => (
        <li key={v.id}>
          <VideoCard video={v} to={v.id}
            onEdit={onEdit && (() => onEdit(v))} onDelete={onDelete && (() => onDelete(v))} />
        </li>
      ))}
    </ul>
  )
}

/** Biblioteca: equipe e alunos assistem; o gestor também cadastra vídeos e monta playlists. */
export function VideoLibraryPage() {
  const { t } = useTranslation()
  const { activeMembership } = useAuth()
  const isManager = useIsManager()
  const { data: videos = [], isLoading, isError } = useVideos()
  const { data: playlists = [] } = usePlaylists()
  const [filter, setFilter] = useState<Filter>('all')
  const [search, setSearch] = useState('')
  const [editing, setEditing] = useState<Editing>(null)
  const [deleting, setDeleting] = useState<Deleting>(null)
  const byId = new Map(videos.map((v) => [v.id, v]))
  const query = fold(search)
  const shown = videos.filter((v) => MATCHES[filter](v) && (!query || fold(v.title).includes(query)))
  const featured = pickFeatured(videos)

  return (
    <div className="page">
      <PageHeader eyebrow={activeMembership?.academy.name ?? t('common.yourAcademy')} title={t('videos.title')}
        description={t('videos.description')} actions={isManager ? <ManagerActions onEdit={setEditing} /> : undefined} />
      <div className="grid gap-10">
        {isError && <div className="alert alert-error">{t('videos.loadError')}</div>}
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {!isLoading && !isError && videos.length === 0 && (
          <div className="card empty-detail">{isManager ? t('videos.emptyManager') : t('videos.empty')}</div>
        )}
        {featured && <FeaturedVideo video={featured} />}
        <PlaylistStrip playlists={playlists} byId={byId}
          onEdit={isManager ? (p) => setEditing({ playlist: p }) : undefined}
          onDelete={isManager ? (p) => setDeleting({ playlist: p }) : undefined} />
        {videos.length > 0 && (
          <section aria-labelledby="all-videos-title" className="grid gap-5">
            <SectionHeading id="all-videos-title" title={t('videos.allVideos')} />
            <LibraryFilters videos={videos} filter={filter} onFilter={setFilter} search={search} onSearch={setSearch} />
            {shown.length === 0 && <p className="empty">{t('videos.noMatch')}</p>}
            <VideoGrid videos={shown}
              onEdit={isManager ? (v) => setEditing({ video: v }) : undefined}
              onDelete={isManager ? (v) => setDeleting({ video: v }) : undefined} />
          </section>
        )}
      </div>
      {isManager && (
        <LibraryEditor videos={videos} editing={editing} deleting={deleting}
          onDone={() => { setEditing(null); setDeleting(null) }} />
      )}
    </div>
  )
}
