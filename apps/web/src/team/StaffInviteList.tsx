import { X } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { apiErrorMessage } from '@/lib/apiErrors'
import { formatDate } from '@/students/format'

import { StaffInviteLinks } from './StaffInviteLinks'
import { useRevokeStaffInvite, useStaffInvites } from './teamApi'

/** Some quando não há convite pendente: a tela mostra só a equipe. */
export function StaffInviteList() {
  const { t } = useTranslation()
  const { data: invites, isError } = useStaffInvites()
  const revoke = useRevokeStaffInvite()

  if (isError) return <div className="alert alert-error">{t('team.pending.loadError')}</div>
  if (!invites || invites.length === 0) return null

  return (
    <section className="stack" aria-labelledby="staff-pending-title">
      <h2 id="staff-pending-title" className="section-title">{t('team.pending.title')}</h2>
      {revoke.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, revoke.error)}</div>}
      <div className="card list-card">
        <ul className="people-list">
          {invites.map((invite) => (
            <li key={invite.id} className="list-row">
              <span className="person-main">
                <span className="person-name">
                  <span className="row-title">{invite.name ?? invite.email ?? t('invites.openLink')}</span>
                  <span className="pill pill-soft">{t(`roles.${invite.role}`)}</span>
                </span>
                <span className="row-sub">
                  {[invite.name && invite.email, t('invites.validUntil', { date: formatDate(invite.expiresAt) })]
                    .filter(Boolean).join(' · ')}
                </span>
              </span>
              <span className="row-actions">
                <StaffInviteLinks invite={invite} />
                <button type="button" className="btn btn-sm btn-icon btn-danger-ghost" disabled={revoke.isPending}
                  onClick={() => revoke.mutate(invite.id)} aria-label={t('invites.revoke')} title={t('invites.revoke')}>
                  <X size={16} aria-hidden />
                </button>
              </span>
            </li>
          ))}
        </ul>
      </div>
    </section>
  )
}
