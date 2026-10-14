import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import { StaffInviteLinks } from './StaffInviteLinks'
import { useCreateStaffInvite } from './teamApi'
import { INVITABLE_ROLES, type StaffInvite, type StaffRole } from './types'

/** Co-gestor só convida professor; a API também barra. */
export function StaffInviteForm({ isOwner }: { isOwner: boolean }) {
  const { t } = useTranslation()
  const create = useCreateStaffInvite()
  const [created, setCreated] = useState<StaffInvite | null>(null)
  const roles = isOwner ? INVITABLE_ROLES : INVITABLE_ROLES.filter((r) => r === 'INSTRUCTOR')

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const formElement = event.currentTarget
    const form = new FormData(formElement)
    const invite = await create.mutateAsync({
      role: String(form.get('role')) as StaffRole,
      name: formText(form, 'name'),
      email: formText(form, 'email'),
    })
    formElement.reset()
    setCreated(invite)
  }

  return (
    <section className="stack" aria-label={t('team.invite.title')}>
      <p className="hint">{t('team.invite.hint')}</p>
      {create.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, create.error)}</div>}
      {created && (
        <div className="notice" role="status">
          <strong>{t('team.invite.created', { role: t(`roles.${created.role}`) })}</strong>
          <StaffInviteLinks invite={created} />
        </div>
      )}
      <form className="form-grid" onSubmit={(e) => void onSubmit(e).catch(() => undefined)}>
        <div className="field">
          <label htmlFor="si-role">{t('team.invite.role')}</label>
          <select id="si-role" name="role" className="input" defaultValue="INSTRUCTOR">
            {roles.map((r) => <option key={r} value={r}>{t(`roles.${r}`)}</option>)}
          </select>
        </div>
        <div className="field">
          <label htmlFor="si-name">{t('team.invite.name')}</label>
          <input id="si-name" name="name" className="input" maxLength={120} />
        </div>
        <div className="field span-2">
          <label htmlFor="si-email">{t('team.invite.email')}</label>
          <input id="si-email" name="email" type="email" className="input" maxLength={254} />
          <span className="hint">{t('team.invite.emailHint')}</span>
        </div>
        <div className="form-actions span-2">
          <button type="submit" className="btn btn-primary" disabled={create.isPending}>
            {create.isPending ? t('invites.form.submitting') : t('invites.form.submit')}
          </button>
        </div>
      </form>
    </section>
  )
}
