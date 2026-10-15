import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { formInt } from '@/billing/money'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import { useGrantAccess } from './platformApi'
import { GRANT_KINDS, GRANT_MAX_DAYS, GRANT_NOTE_MAX, type AdminAcademy, type GrantKind } from './types'

const DEFAULT_DAYS = 30

type Props = { academy: AdminAcademy; onDone: () => void; onCancel: () => void }

/** Mais dias de teste ou um pagamento recebido fora do app; fica registrado quem liberou. */
export function GrantForm({ academy, onDone, onCancel }: Props) {
  const { t } = useTranslation()
  const grant = useGrantAccess()
  const [error, setError] = useState<string | null>(null)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    const form = new FormData(event.currentTarget)
    const days = formInt(form, 'days')
    if (days === null || days < 1 || days > GRANT_MAX_DAYS) {
      setError(t('admin.grant.invalidDays', { max: GRANT_MAX_DAYS }))
      return
    }
    try {
      await grant.mutateAsync({
        academyId: academy.id,
        body: { kind: (formText(form, 'kind') ?? 'TRIAL_EXTENSION') as GrantKind, days, note: formText(form, 'note') },
      })
      onDone()
    } catch (err) {
      setError(apiErrorMessage(t, err))
    }
  }

  return (
    <form onSubmit={onSubmit} className="form-grid" noValidate>
      {error && <div className="alert alert-error span-2" role="alert">{error}</div>}
      <p className="dialog-text span-2">{t('admin.grant.summary', { name: academy.name })}</p>
      <div className="field">
        <label htmlFor="grant-kind">{t('admin.grant.kind')}</label>
        <select id="grant-kind" name="kind" className="input" defaultValue="TRIAL_EXTENSION">
          {GRANT_KINDS.map((k) => <option key={k} value={k}>{t(`admin.grant.kinds.${k}`)}</option>)}
        </select>
      </div>
      <div className="field">
        <label htmlFor="grant-days">{t('admin.grant.days')}</label>
        <input id="grant-days" name="days" type="number" className="input" required min={1} max={GRANT_MAX_DAYS}
          defaultValue={DEFAULT_DAYS} />
      </div>
      <div className="field span-2">
        <label htmlFor="grant-note">{t('admin.grant.note')}</label>
        <input id="grant-note" name="note" className="input" maxLength={GRANT_NOTE_MAX}
          placeholder={t('admin.grant.notePlaceholder')} />
      </div>
      <p className="hint span-2">{t('admin.grant.hint')}</p>
      <div className="form-actions span-2">
        <button type="button" className="btn" onClick={onCancel}>{t('common.cancel')}</button>
        <button type="submit" className="btn btn-primary" disabled={grant.isPending}>
          {grant.isPending ? t('common.saving') : t('admin.grant.submit')}
        </button>
      </div>
    </form>
  )
}
