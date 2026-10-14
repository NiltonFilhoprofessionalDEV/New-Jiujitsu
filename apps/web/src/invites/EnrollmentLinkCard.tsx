import { ExternalLink, MessageCircle } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { CopyButton } from '@/components/CopyButton'
import { apiErrorMessage } from '@/lib/apiErrors'

import { useEnrollmentLink, useLinkAction } from './invitesApi'
import { LinkOptions } from './LinkOptions'
import { enrollUrl, whatsappUrl } from './shareLinks'

export function EnrollmentLinkCard() {
  const { t } = useTranslation()
  const { data: link, isLoading, isError } = useEnrollmentLink()
  const action = useLinkAction()

  if (isLoading) return <div className="card empty">{t('invites.link.loading')}</div>
  if (isError || !link) return <div className="card alert alert-error">{t('invites.link.loadError')}</div>

  const url = enrollUrl(link.token)

  return (
    <section className="card stack" aria-labelledby="enroll-link-title">
      <header className="link-header">
        <h3 id="enroll-link-title">{t('invites.link.title')}</h3>
        <span className={link.state === 'OPEN' ? 'pill pill-soft' : 'pill pill-muted'}>
          {t(`invites.link.state.${link.state}`)}
        </span>
      </header>
      <p className="hint">{t('invites.link.hint')}</p>

      {action.error && (
        <div className="alert alert-error" role="alert">{apiErrorMessage(t, action.error)}</div>
      )}

      <div className="inline-form">
        <input className="input grow" readOnly value={url} aria-label={t('invites.link.title')}
          onFocus={(e) => e.currentTarget.select()} />
        <CopyButton text={url} className="btn" />
      </div>

      <div className="link-actions">
        <a className="btn btn-sm btn-outline-primary" href={whatsappUrl(null, t('invites.link.waMessage', { link: url }))}
          target="_blank" rel="noreferrer">
          <MessageCircle size={15} aria-hidden /> {t('invites.sendWa')}
        </a>
        <a className="btn btn-sm" href={url} target="_blank" rel="noreferrer">
          <ExternalLink size={15} aria-hidden /> {t('invites.link.open')}
        </a>
        <span className="hint">{t('invites.link.accesses', { count: link.opens })}</span>
      </div>

      <LinkOptions link={link} action={action} />
    </section>
  )
}
