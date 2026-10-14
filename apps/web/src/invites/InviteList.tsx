import { MessageCircle, X } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { CopyButton } from '@/components/CopyButton'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formatDate } from '@/students/format'

import { useInviteAction, useInvites } from './invitesApi'
import { inviteUrl, whatsappUrl } from './shareLinks'
import type { Invite, InviteTab } from './types'

const TABS: readonly InviteTab[] = ['PENDING', 'ACCEPTED', 'EXPIRED', 'ALL']
const STATUS_PILL: Record<Invite['status'], string> = {
  PENDING: 'pill pill-soft',
  ACCEPTED: 'pill pill-success',
  EXPIRED: 'pill pill-muted',
  REVOKED: 'pill pill-muted',
}

export function InviteList() {
  const { t } = useTranslation()
  const [tab, setTab] = useState<InviteTab>('PENDING')
  const { data: invites, isLoading, isError } = useInvites(tab)
  const action = useInviteAction()

  return (
    <section className="stack" aria-label={t('invites.listAria')}>
      <div className="chips" role="group" aria-label={t('invites.filterAria')}>
        {TABS.map((k) => (
          <button key={k} type="button" aria-pressed={tab === k}
            className={tab === k ? 'chip active' : 'chip'} onClick={() => setTab(k)}>
            {t(`invites.tab.${k}`)}
          </button>
        ))}
      </div>
      {action.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, action.error)}</div>}
      {isLoading && <p className="empty">{t('invites.loading')}</p>}
      {isError && <div className="alert alert-error">{t('invites.loadError')}</div>}
      {invites?.length === 0 && <p className="empty">{tab === 'PENDING' ? t('invites.empty') : t('invites.emptyFilter')}</p>}
      {invites && invites.length > 0 && (
        <div className="card list-card">
          <ul className="people-list">
            {invites.map((invite) => (
              <InviteRow key={invite.id} invite={invite} busy={action.isPending}
                onAction={(type) => action.mutate({ id: invite.id, action: type })} />
            ))}
          </ul>
        </div>
      )}
    </section>
  )
}

type RowProps = { invite: Invite; busy: boolean; onAction: (type: 'revoke' | 'renew') => void }

function InviteRow({ invite, busy, onAction }: RowProps) {
  const { t } = useTranslation()
  const url = inviteUrl(invite.token)
  const greeting = invite.name ? t('invites.waGreetingNamed', { name: invite.name }) : t('invites.waGreeting')
  const open = invite.status === 'PENDING'
  const details = [
    invite.email,
    invite.phone,
    t('invites.accesses', { count: invite.opens }),
    open && t('invites.validUntil', { date: formatDate(invite.expiresAt.slice(0, 10)) }),
  ].filter(Boolean)

  return (
    <li className="invite-row">
      <div className="person-main">
        <span className="person-name">
          <span className="row-title">{invite.name ?? invite.email ?? t('invites.openLink')}</span>
          {invite.kind === 'RESPONSIBLE' && <span className="pill pill-outline">{t('students.badge.responsible')}</span>}
          <span className={STATUS_PILL[invite.status]}>{t(`invites.status.${invite.status}`)}</span>
        </span>
        <span className="row-sub">{details.join(' · ')}</span>
      </div>
      <div className="row-actions">
        {open && <CopyButton text={url} label={t('invites.copy')} />}
        {open && (
          <a className="btn btn-sm" target="_blank" rel="noreferrer"
            href={whatsappUrl(invite.phone, t('invites.waMessage', { greeting, link: url }))}>
            <MessageCircle size={15} aria-hidden /> {t('invites.sendWa')}
          </a>
        )}
        {invite.status === 'EXPIRED' && (
          <button type="button" className="btn btn-sm" disabled={busy} onClick={() => onAction('renew')}>
            {t('invites.renew')}
          </button>
        )}
        {(open || invite.status === 'EXPIRED') && (
          <button type="button" className="btn btn-sm btn-icon btn-danger-ghost" disabled={busy}
            onClick={() => onAction('revoke')} aria-label={t('invites.revoke')} title={t('invites.revoke')}>
            <X size={16} aria-hidden />
          </button>
        )}
      </div>
    </li>
  )
}
