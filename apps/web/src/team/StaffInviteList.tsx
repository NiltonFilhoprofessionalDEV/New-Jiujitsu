import { useTranslation } from 'react-i18next'

import { apiErrorMessage } from '@/lib/apiErrors'
import { formatDate } from '@/students/format'

import { StaffInviteLinks } from './StaffInviteLinks'
import { useRevokeStaffInvite, useStaffInvites } from './teamApi'

export function StaffInviteList() {
  const { t } = useTranslation()
  const { data: invites, isLoading, isError } = useStaffInvites()
  const revoke = useRevokeStaffInvite()

  return (
    <section className="stack" aria-labelledby="staff-pending-title">
      <h3 id="staff-pending-title">{t('team.pending.title')}</h3>
      {revoke.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, revoke.error)}</div>}
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {isError && <div className="alert alert-error">{t('team.pending.loadError')}</div>}
      {invites?.length === 0 && <p className="empty">{t('team.pending.empty')}</p>}
      {invites && invites.length > 0 && (
        <div className="card table-card">
          <table className="table">
            <thead>
              <tr>
                <th scope="col">{t('invites.th.guest')}</th>
                <th scope="col">{t('team.col.role')}</th>
                <th scope="col" className="hide-narrow">{t('invites.th.expires')}</th>
                <th scope="col">{t('invites.th.actions')}</th>
              </tr>
            </thead>
            <tbody>
              {invites.map((invite) => (
                <tr key={invite.id}>
                  <td>
                    <div className="row-title">{invite.name ?? invite.email ?? t('invites.openLink')}</div>
                    {invite.name && invite.email && <div className="row-sub">{invite.email}</div>}
                  </td>
                  <td><span className="pill pill-soft">{t(`roles.${invite.role}`)}</span></td>
                  <td className="hide-narrow">{formatDate(invite.expiresAt)}</td>
                  <td>
                    <div className="row-actions">
                      <StaffInviteLinks invite={invite} />
                      <button type="button" className="btn btn-sm btn-danger-ghost" disabled={revoke.isPending}
                        onClick={() => revoke.mutate(invite.id)}>
                        {t('invites.revoke')}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
