import { MessageCircle } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { CopyButton } from '@/components/CopyButton'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'
import { MaskedInput } from '@/components/MaskedInput'
import { maskPhone } from '@/lib/masks'
import { whatsappUrl } from '@/lib/phone'

import { useCreateInvite } from './invitesApi'
import { inviteUrl } from './shareLinks'
import type { Invite } from './types'

export function NewInviteForm() {
  const { t } = useTranslation()
  const create = useCreateInvite()
  const [created, setCreated] = useState<Invite | null>(null)

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const formElement = event.currentTarget
    const form = new FormData(formElement)
    const invite = await create.mutateAsync({
      kind: form.get('responsible') === 'on' ? 'RESPONSIBLE' : 'TRAINING',
      name: formText(form, 'name'),
      email: formText(form, 'email'),
      phone: formText(form, 'phone'),
    })
    formElement.reset()
    setCreated(invite)
  }

  return (
    <section className="card stack" aria-label={t('invites.form.title')}>
      <p className="hint">{t('invites.form.hint')}</p>
      {create.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, create.error)}</div>}
      {created && <CreatedInvite invite={created} />}

      <form className="form-grid" onSubmit={(e) => void onSubmit(e).catch(() => undefined)}>
        <div className="field span-2">
          <label htmlFor="inv-name">{t('invites.form.name')}</label>
          <input id="inv-name" name="name" className="input" maxLength={120} />
        </div>
        <div className="field">
          <label htmlFor="inv-email">{t('invites.form.email')}</label>
          <input id="inv-email" name="email" type="email" className="input" maxLength={254} />
        </div>
        <div className="field">
          <label htmlFor="inv-phone">{t('invites.form.phone')}</label>
          <MaskedInput id="inv-phone" name="phone" type="tel" className="input" mask={maskPhone} maxLength={15}
            placeholder="(11) 99999-9999" autoComplete="tel" />
        </div>
        <label className="checkbox-inline span-2" title={t('invites.form.responsibleTitle')}>
          <input type="checkbox" name="responsible" />
          {t('invites.form.responsible')}
        </label>
        <div className="form-actions span-2">
          <button type="submit" className="btn btn-primary" disabled={create.isPending}>
            {create.isPending ? t('invites.form.submitting') : t('invites.form.submit')}
          </button>
        </div>
      </form>
    </section>
  )
}

function CreatedInvite({ invite }: { invite: Invite }) {
  const { t } = useTranslation()
  const url = inviteUrl(invite.token)
  const greeting = invite.name ? t('invites.waGreetingNamed', { name: invite.name }) : t('invites.waGreeting')
  return (
    <div className="notice" role="status">
      <strong>{t('invites.created', { context: invite.kind })}</strong>
      <div className="link-actions">
        <CopyButton text={url} label={t('invites.copy')} />
        <a className="btn btn-sm" target="_blank" rel="noreferrer"
          href={whatsappUrl(invite.phone, t('invites.waMessage', { greeting, link: url }))}>
          <MessageCircle size={15} aria-hidden /> {t('invites.sendWa')}
        </a>
      </div>
    </div>
  )
}
