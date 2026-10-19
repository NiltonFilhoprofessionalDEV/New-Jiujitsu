import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { apiErrorMessage } from '@/lib/apiErrors'

import { useDeleteAcademy } from './platformApi'
import type { AdminAcademy } from './types'

type Props = { academy: AdminAcademy; onDone: () => void; onCancel: () => void }

function sameName(left: string, right: string) {
  return left.trim().toLocaleLowerCase('pt-BR') === right.trim().toLocaleLowerCase('pt-BR')
}

/** Exige o nome da academia digitado. O servidor repete essa conferência. */
export function DeleteAcademyDialog({ academy, onDone, onCancel }: Props) {
  const { t } = useTranslation()
  const remove = useDeleteAcademy()
  const [typed, setTyped] = useState('')
  const [error, setError] = useState<string | null>(null)
  const confirmed = sameName(typed, academy.name)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!confirmed) return
    setError(null)
    try {
      await remove.mutateAsync({ academyId: academy.id, confirmName: typed.trim() })
      onDone()
    } catch (err) {
      setError(apiErrorMessage(t, err))
    }
  }

  return (
    <form onSubmit={(event) => void onSubmit(event)} className="field" style={{ gap: 14 }}>
      <p className="hint">{t('admin.delete.warning', { name: academy.name })}</p>
      {academy.activeStudents === 0 && <p className="hint">{t('admin.delete.emptyHint')}</p>}
      <p className="hint">{t('admin.delete.ownerHint')}</p>
      {error && <div className="alert alert-error" role="alert">{error}</div>}
      <div className="field">
        <label htmlFor="confirmName">{t('admin.delete.confirmLabel')}</label>
        <input
          id="confirmName"
          className="input"
          value={typed}
          autoComplete="off"
          onChange={(event) => setTyped(event.target.value)}
        />
      </div>
      <div className="row-actions">
        <button type="button" className="btn" onClick={onCancel}>{t('common.cancel')}</button>
        <button type="submit" className="btn btn-danger-ghost" disabled={!confirmed || remove.isPending}>
          {t('admin.delete.submit')}
        </button>
      </div>
    </form>
  )
}
