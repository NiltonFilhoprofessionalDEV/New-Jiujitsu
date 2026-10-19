import { MessageCircle, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { CopyButton } from '@/components/CopyButton'
import { apiErrorMessage } from '@/lib/apiErrors'
import { whatsappUrl } from '@/lib/phone'
import { formatDate } from '@/students/format'

import { academyInviteUrl, useAcademyInvites, useCreateAcademyInvite, useRevokeAcademyInvite } from './platformApi'

/** O admin gera o link. Sem ele, ninguém abre academia nova. */
export function AcademyInvitePanel() {
  const { t } = useTranslation()
  const { data: invites, isError } = useAcademyInvites()
  const create = useCreateAcademyInvite()
  const revoke = useRevokeAcademyInvite()
  const error = create.error ?? revoke.error

  return (
    <section className="stack" aria-labelledby="academy-invite-title">
      <div className="row-actions" style={{ justifyContent: 'space-between' }}>
        <h2 id="academy-invite-title" className="section-title">{t('admin.invite.title')}</h2>
        <button type="button" className="btn btn-sm btn-primary" disabled={create.isPending}
          onClick={() => create.mutate()}>
          {t('admin.invite.create')}
        </button>
      </div>
      <p className="hint">{t('admin.invite.hint')}</p>
      {isError && <div className="alert alert-error">{t('admin.invite.loadError')}</div>}
      {error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, error)}</div>}
      {invites && invites.length === 0 && <p className="empty">{t('admin.invite.empty')}</p>}
      {invites && invites.length > 0 && (
        <div className="card list-card">
          <ul className="people-list">
            {invites.map((invite) => {
              const url = academyInviteUrl(invite.token)
              const message = t('admin.invite.waMessage', { link: url })
              return (
                <li key={invite.id} className="list-row">
                  <span className="person-main">
                    <span className="row-title">{t('admin.invite.openLink')}</span>
                    <span className="row-sub">{t('invites.validUntil', { date: formatDate(invite.expiresAt) })}</span>
                  </span>
                  <span className="row-actions">
                    <div className="link-actions">
                      <CopyButton text={url} label={t('invites.copy')} />
                      <a className="btn btn-sm" target="_blank" rel="noreferrer" href={whatsappUrl(null, message)}>
                        <MessageCircle size={15} aria-hidden /> {t('invites.sendWa')}
                      </a>
                    </div>
                    <button type="button" className="btn btn-sm btn-icon btn-danger-ghost" disabled={revoke.isPending}
                      onClick={() => revoke.mutate(invite.id)} aria-label={t('admin.invite.revoke')}
                      title={t('admin.invite.revoke')}>
                      <X size={16} aria-hidden />
                    </button>
                  </span>
                </li>
              )
            })}
          </ul>
        </div>
      )}
    </section>
  )
}
