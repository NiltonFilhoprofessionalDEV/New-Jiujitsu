import { ArrowDown, ArrowUp, GripVertical, X } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { problemOf } from '@/lib/api'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import type { Playlist, Video } from './types'
import { useSavePlaylist } from './videosApi'
import { thumbnailUrl } from './youtube'

type Props = { playlist?: Playlist; videos: Video[]; onSaved: () => void; onCancel: () => void }

const MAX_TITLE = 80
const MAX_DESCRIPTION = 500

function move<T>(list: T[], from: number, to: number): T[] {
  if (to < 0 || to >= list.length || from === to) return list
  const next = [...list]
  const [item] = next.splice(from, 1)
  next.splice(to, 0, item as T)
  return next
}

type OrderProps = { ids: string[]; byId: Map<string, Video>; onChange: (ids: string[]) => void }

/** Arrastar (computador) ou setas (celular e teclado) definem a ordem em que os alunos assistem. */
function PlaylistOrder({ ids, byId, onChange }: OrderProps) {
  const { t } = useTranslation()
  const [dragging, setDragging] = useState<number | null>(null)
  if (ids.length === 0) return <p className="empty">{t('videos.playlistForm.empty')}</p>
  return (
    <ol className="playlist-order">
      {ids.map((id, index) => {
        const video = byId.get(id)
        if (!video) return null
        return (
          <li key={id} draggable className={dragging === index ? 'dragging' : undefined}
            onDragStart={() => setDragging(index)} onDragEnd={() => setDragging(null)}
            onDragOver={(e) => e.preventDefault()}
            onDrop={() => { if (dragging !== null) onChange(move(ids, dragging, index)); setDragging(null) }}>
            <GripVertical size={16} className="drag-handle" aria-hidden />
            <span className="playlist-order-index">{index + 1}</span>
            <img src={thumbnailUrl(video.youtubeId)} alt="" className="playlist-order-thumb" />
            <span className="playlist-order-title">{video.title}</span>
            <span className="row-actions">
              <button type="button" className="icon-btn" disabled={index === 0}
                aria-label={t('videos.playlistForm.moveUp', { title: video.title })}
                onClick={() => onChange(move(ids, index, index - 1))}>
                <ArrowUp size={16} aria-hidden />
              </button>
              <button type="button" className="icon-btn" disabled={index === ids.length - 1}
                aria-label={t('videos.playlistForm.moveDown', { title: video.title })}
                onClick={() => onChange(move(ids, index, index + 1))}>
                <ArrowDown size={16} aria-hidden />
              </button>
              <button type="button" className="icon-btn" aria-label={t('videos.playlistForm.remove', { title: video.title })}
                onClick={() => onChange(ids.filter((v) => v !== id))}>
                <X size={16} aria-hidden />
              </button>
            </span>
          </li>
        )
      })}
    </ol>
  )
}

export function PlaylistForm({ playlist, videos, onSaved, onCancel }: Props) {
  const { t } = useTranslation()
  const save = useSavePlaylist()
  const byId = new Map(videos.map((v) => [v.id, v]))
  const [ids, setIds] = useState<string[]>(() => (playlist?.videoIds ?? []).filter((id) => byId.has(id)))
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const available = videos.filter((v) => !ids.includes(v.id))

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setFieldErrors({})
    const form = new FormData(event.currentTarget)
    const body = { title: formText(form, 'title') ?? '', description: formText(form, 'description'), videoIds: ids }
    try {
      await save.mutateAsync({ id: playlist?.id, body })
      onSaved()
    } catch (err) {
      setError(apiErrorMessage(t, err))
      setFieldErrors(problemOf(err).fields ?? {})
    }
  }

  return (
    <form onSubmit={onSubmit} className="form-grid" noValidate>
      {error && <div className="alert alert-error span-2" role="alert">{error}</div>}
      <div className="field span-2">
        <label htmlFor="pf-title">{t('videos.playlistForm.title')}</label>
        <input id="pf-title" name="title" className="input" required maxLength={MAX_TITLE}
          defaultValue={playlist?.title} placeholder={t('videos.playlistForm.titlePlaceholder')} />
        {fieldErrors.title && <span className="field-error">{fieldErrors.title}</span>}
      </div>
      <div className="field span-2">
        <label htmlFor="pf-description">{t('videos.playlistForm.description')}</label>
        <textarea id="pf-description" name="description" className="input" rows={2} maxLength={MAX_DESCRIPTION}
          defaultValue={playlist?.description ?? ''} />
      </div>
      <div className="field span-2">
        <span className="field-label">{t('videos.playlistForm.order')}</span>
        <PlaylistOrder ids={ids} byId={byId} onChange={setIds} />
      </div>
      {available.length > 0 && (
        <div className="field span-2">
          <label htmlFor="pf-add">{t('videos.playlistForm.add')}</label>
          <select id="pf-add" className="input" value="" onChange={(e) => e.target.value && setIds([...ids, e.target.value])}>
            <option value="">{t('videos.playlistForm.addPlaceholder')}</option>
            {available.map((v) => <option key={v.id} value={v.id}>{v.title}</option>)}
          </select>
        </div>
      )}
      <div className="form-actions span-2">
        <button type="button" className="btn" onClick={onCancel}>{t('common.cancel')}</button>
        <button type="submit" className="btn btn-primary" disabled={save.isPending}>
          {save.isPending ? t('common.saving') : t('common.save')}
        </button>
      </div>
    </form>
  )
}
