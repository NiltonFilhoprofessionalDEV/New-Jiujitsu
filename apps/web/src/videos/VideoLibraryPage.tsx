import { ListPlus, Plus } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { useAuth, useIsManager } from '@/auth/useAuth'
import { PageHeader } from '@/components/PageHeader'
import { SearchField } from '@/components/SearchField'
import { fold } from '@/lib/text'

import { LibraryEditor, type Deleting, type Editing } from './LibraryEditor'
import { PlaylistStrip } from './PlaylistStrip'
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
    <div className="toolbar">
      <SearchField value={search} onChange={onSearch} label={t('videos.search')} />
      <div className="chips" role="tablist" aria-label={t('videos.filterAria')}>
        {FILTERS.map((f) => (
          <button key={f} type="button" role="tab" aria-selected={filter === f}
            className={filter === f ? 'chip active' : 'chip'} onClick={() => onFilter(f)}>
            {t(`videos.filter.${f}`)} <span className="chip-count">{videos.filter(MATCHES[f]).length}</span>
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
      <button type="button" className="btn" onClick={() => onEdit({ playlist: undefined })}>
        <ListPlus size={16} aria-hidden />
        {t('videos.newPlaylist')}
      </button>
      <button type="button" className="btn btn-primary" onClick={() => onEdit({ video: undefined })}>
        <Plus size={16} aria-hidden />
        {t('videos.addVideo')}
      </button>
    </>
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

  return (
    <div className="page">
      <PageHeader eyebrow={activeMembership?.academy.name ?? t('common.yourAcademy')} title={t('videos.title')}
        description={t('videos.description')} actions={isManager ? <ManagerActions onEdit={setEditing} /> : undefined} />
      <div className="stack">
        {isError && <div className="alert alert-error">{t('videos.loadError')}</div>}
        <PlaylistStrip playlists={playlists} byId={byId}
          onEdit={isManager ? (p) => setEditing({ playlist: p }) : undefined}
          onDelete={isManager ? (p) => setDeleting({ playlist: p }) : undefined} />
        {videos.length > 0 && (
          <LibraryFilters videos={videos} filter={filter} onFilter={setFilter} search={search} onSearch={setSearch} />
        )}
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {!isLoading && !isError && videos.length === 0 && (
          <div className="card empty-detail">{isManager ? t('videos.emptyManager') : t('videos.empty')}</div>
        )}
        {videos.length > 0 && shown.length === 0 && <p className="empty">{t('videos.noMatch')}</p>}
        <ul className="video-grid">
          {shown.map((v) => (
            <li key={v.id}>
              <VideoCard video={v} to={v.id}
                onEdit={isManager ? () => setEditing({ video: v }) : undefined}
                onDelete={isManager ? () => setDeleting({ video: v }) : undefined} />
            </li>
          ))}
        </ul>
      </div>
      {isManager && (
        <LibraryEditor videos={videos} editing={editing} deleting={deleting}
          onDone={() => { setEditing(null); setDeleting(null) }} />
      )}
    </div>
  )
}
