import { Plus } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { apiErrorMessage } from '@/lib/apiErrors'

import { usePlaylists, useSavePlaylist } from './videosApi'

const MAX_TITLE = 80

type Props = { selected: string[]; onChange: (ids: string[]) => void }

/** Escolhe as playlists do vídeo e cria uma nova ali mesmo, já marcada. */
export function PlaylistPicker({ selected, onChange }: Props) {
  const { t } = useTranslation()
  const { data: playlists = [] } = usePlaylists()
  const save = useSavePlaylist()
  const [title, setTitle] = useState('')

  function toggle(id: string) {
    onChange(selected.includes(id) ? selected.filter((v) => v !== id) : [...selected, id])
  }

  function create() {
    const name = title.trim()
    if (!name) return
    save.mutate({ body: { title: name, description: null, videoIds: [] } }, {
      onSuccess: (created) => {
        onChange([...selected, created.id])
        setTitle('')
      },
    })
  }

  return (
    <fieldset className="field span-2">
      <legend>{t('videos.form.playlists')}</legend>
      {playlists.length > 0 && (
        <div className="choice-chips">
          {playlists.map((p) => (
            <label key={p.id} className="choice-chip">
              <input type="checkbox" checked={selected.includes(p.id)} onChange={() => toggle(p.id)} />
              <span>{p.title}</span>
            </label>
          ))}
        </div>
      )}
      <div className="inline-create">
        <input className="input" value={title} maxLength={MAX_TITLE} onChange={(e) => setTitle(e.target.value)}
          placeholder={t('videos.form.newPlaylist')} aria-label={t('videos.form.newPlaylist')}
          onKeyDown={(e) => {
            if (e.key !== 'Enter') return
            e.preventDefault()
            create()
          }} />
        <button type="button" className="btn" onClick={create} disabled={save.isPending || !title.trim()}>
          <Plus size={16} aria-hidden />
          {t('videos.form.createPlaylist')}
        </button>
      </div>
      {save.isError && <span className="field-error">{apiErrorMessage(t, save.error)}</span>}
    </fieldset>
  )
}
