import { ExternalLink, MessageCircle, RefreshCw } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { ConfirmDialog } from '@/components/ConfirmDialog'
import { CopyButton } from '@/components/CopyButton'
import { apiErrorMessage } from '@/lib/apiErrors'

import { useEnrollmentLink, useLinkAction } from './invitesApi'
import { enrollUrl, whatsappUrl } from './shareLinks'

export function EnrollmentLinkCard() {
  const { t } = useTranslation()
  const { data: link, isLoading, isError } = useEnrollmentLink()
  const action = useLinkAction()
  const [confirmRotate, setConfirmRotate] = useState(false)

  if (isLoading) return <div className="card empty">{t('invites.link.loading')}</div>
  if (isError || !link) return <div className="card alert alert-error">{t('invites.link.loadError')}</div>

  const url = enrollUrl(link.token)
  const configure = (enabled: boolean, expiresOn: string | null) =>
    action.mutate({ type: 'configure', enabled, expiresOn })

  return (
    <section className="card stack" aria-labelledby="enroll-link-title">
      <header className="detail-header">
        <div>
          <h3 id="enroll-link-title">{t('invites.link.title')}</h3>
          <p className="hint">{t('invites.link.hint')}</p>
        </div>
        <span className={link.state === 'OPEN' ? 'pill pill-soft' : 'pill pill-muted'}>
          {t(`invites.link.state.${link.state}`)}
        </span>
      </header>

      {action.error && (
        <div className="alert alert-error" role="alert">{apiErrorMessage(t, action.error)}</div>
      )}

      <div className="inline-form">
        <input className="input grow" readOnly value={url} aria-label={t('invites.link.title')}
          onFocus={(e) => e.currentTarget.select()} />
        <CopyButton text={url} className="btn" />
      </div>

      <div className="link-actions">
        <a className="btn btn-sm" href={url} target="_blank" rel="noreferrer">
          <ExternalLink size={15} aria-hidden /> {t('invites.link.open')}
        </a>
        <a className="btn btn-sm" href={whatsappUrl(null, t('invites.link.waMessage', { link: url }))}
          target="_blank" rel="noreferrer">
          <MessageCircle size={15} aria-hidden /> {t('invites.sendWa')}
        </a>
        <button type="button" className="btn btn-sm btn-ghost" onClick={() => setConfirmRotate(true)}>
          <RefreshCw size={15} aria-hidden /> {t('invites.link.rotate')}
        </button>
        <span className="hint">{t('invites.link.accesses', { count: link.opens })}</span>
      </div>

      <div className="link-settings">
        <label className="checkbox-inline">
          <input type="checkbox" checked={link.enabled} disabled={action.isPending}
            onChange={(e) => configure(e.target.checked, link.expiresOn)} />
          {t('invites.link.acceptSignups')}
        </label>
        <label className="field">
          <span>{t('invites.link.expiresAt')}</span>
          <input type="date" className="input" value={link.expiresOn ?? ''} disabled={action.isPending}
            onChange={(e) => configure(link.enabled, e.target.value || null)} />
        </label>
      </div>
      <p className="hint">{t('invites.link.approvalHint')}</p>

      <ConfirmDialog
        open={confirmRotate}
        title={t('invites.link.rotate')}
        body={t('invites.link.rotateConfirm')}
        confirmLabel={t('invites.link.rotate')}
        pending={action.isPending}
        error={null}
        onConfirm={() => action.mutate({ type: 'rotate' }, { onSettled: () => setConfirmRotate(false) })}
        onClose={() => setConfirmRotate(false)}
      />
    </section>
  )
}
