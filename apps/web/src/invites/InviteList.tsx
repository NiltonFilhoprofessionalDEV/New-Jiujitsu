import { MessageCircle } from 'lucide-react'
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
      <div className="chips" role="tablist">
        {TABS.map((k) => (
          <button key={k} type="button" role="tab" aria-selected={tab === k}
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
        <div className="card table-card">
          <table className="table">
            <thead>
              <tr>
                <th>{t('invites.th.guest')}</th>
                <th>{t('invites.th.status')}</th>
                <th className="hide-narrow">{t('invites.th.accesses')}</th>
                <th className="hide-narrow">{t('invites.th.expires')}</th>
                <th>{t('invites.th.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {invites.map((invite) => (
                <InviteRow key={invite.id} invite={invite} busy={action.isPending}
                  onAction={(type) => action.mutate({ id: invite.id, action: type })} />
              ))}
            </tbody>
          </table>
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
  return (
    <tr>
      <td>
        <div className="row-title">{invite.name ?? invite.email ?? t('invites.openLink')}</div>
        <div className="row-sub">
          {[invite.email, invite.phone].filter(Boolean).join(' · ')}
          {invite.kind === 'RESPONSIBLE' && <span className="pill pill-outline">{t('students.badge.responsible')}</span>}
        </div>
      </td>
      <td><span className={STATUS_PILL[invite.status]}>{t(`invites.status.${invite.status}`)}</span></td>
      <td className="hide-narrow">{t('invites.accesses', { count: invite.opens })}</td>
      <td className="hide-narrow">{formatDate(invite.expiresAt.slice(0, 10))}</td>
      <td>
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
            <button type="button" className="btn btn-sm btn-danger-ghost" disabled={busy} onClick={() => onAction('revoke')}>
              {t('invites.revoke')}
            </button>
          )}
        </div>
      </td>
    </tr>
  )
}
