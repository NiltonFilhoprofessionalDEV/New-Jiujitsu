import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { problemOf } from '@/lib/api'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import { AudienceFields } from './AudienceFields'
import { PlaylistPicker } from './PlaylistPicker'
import type { Audience, Video } from './types'
import { usePlaylists, useSaveVideo } from './videosApi'
import { previewYoutubeId, thumbnailUrl } from './youtube'

type Props = { video?: Video; onSaved: () => void; onCancel: () => void }

const MAX_URL = 300
const MAX_TITLE = 120
const MAX_DESCRIPTION = 2000

function youtubeUrl(video: Video | undefined): string {
  if (!video) return ''
  const path = video.vertical ? 'shorts/' : 'watch?v='
  return `https://www.youtube.com/${path}${video.youtubeId}`
}

/** Link do YouTube + título; prévia da capa assim que o link é reconhecido. */
export function VideoForm({ video, onSaved, onCancel }: Props) {
  const { t } = useTranslation()
  const save = useSaveVideo()
  const { data: playlists = [] } = usePlaylists()
  const [url, setUrl] = useState(() => youtubeUrl(video))
  const [audience, setAudience] = useState<Audience>(video?.audience ?? 'ALL')
  const [belts, setBelts] = useState<string[]>(video?.belts ?? [])
  const [classIds, setClassIds] = useState<string[]>(video?.classIds ?? [])
  const [playlistIds, setPlaylistIds] = useState<string[]>(() =>
    video ? playlists.filter((p) => p.videoIds.includes(video.id)).map((p) => p.id) : [])
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const previewId = previewYoutubeId(url)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setFieldErrors({})
    const form = new FormData(event.currentTarget)
    const body = {
      url, title: formText(form, 'title') ?? '', description: formText(form, 'description'),
      audience, belts, classIds, playlistIds,
    }
    try {
      await save.mutateAsync({ id: video?.id, body })
      onSaved()
    } catch (err) {
      setError(apiErrorMessage(t, err))
      setFieldErrors(problemOf(err).fields ?? {})
    }
  }

  const fieldError = (name: string) => fieldErrors[name] && <span className="field-error">{fieldErrors[name]}</span>

  return (
    <form onSubmit={onSubmit} className="form-grid" noValidate>
      {error && <div className="alert alert-error span-2" role="alert">{error}</div>}
      <div className="field span-2">
        <label htmlFor="vf-url">{t('videos.form.url')}</label>
        <input id="vf-url" className="input" type="url" inputMode="url" required maxLength={MAX_URL} value={url}
          onChange={(e) => setUrl(e.target.value)} placeholder="https://youtu.be/..." />
        {fieldError('url')}
      </div>
      {previewId && (
        <div className="video-preview span-2">
          <img src={thumbnailUrl(previewId)} alt={t('videos.form.preview')} />
        </div>
      )}
      <div className="field span-2">
        <label htmlFor="vf-title">{t('videos.form.title')}</label>
        <input id="vf-title" name="title" className="input" required maxLength={MAX_TITLE} defaultValue={video?.title}
          placeholder={t('videos.form.titlePlaceholder')} />
        {fieldError('title')}
      </div>
      <div className="field span-2">
        <label htmlFor="vf-description">{t('videos.form.description')}</label>
        <textarea id="vf-description" name="description" className="input" rows={3} maxLength={MAX_DESCRIPTION}
          defaultValue={video?.description ?? ''} />
      </div>
      <AudienceFields audience={audience} onAudience={setAudience} belts={belts} onBelts={setBelts}
        classIds={classIds} onClassIds={setClassIds} />
      <PlaylistPicker selected={playlistIds} onChange={setPlaylistIds} />
      <div className="form-actions span-2">
        <button type="button" className="btn" onClick={onCancel}>{t('common.cancel')}</button>
        <button type="submit" className="btn btn-primary" disabled={save.isPending}>
          {save.isPending ? t('common.saving') : video ? t('common.save') : t('videos.form.submitCreate')}
        </button>
      </div>
    </form>
  )
}
