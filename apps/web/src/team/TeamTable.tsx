import { UserMinus } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { ConfirmDialog } from '@/components/ConfirmDialog'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formatDate } from '@/students/format'

import { useChangeRole, useRemoveMember } from './teamApi'
import { INVITABLE_ROLES, type StaffRole, type TeamMember } from './types'

/** Espelha a regra da API: o dono é intocável e só o dono mexe em co-gestor. */
function canManage(member: TeamMember, isOwner: boolean): boolean {
  if (member.you || member.role === 'OWNER') return false
  return isOwner || member.role === 'INSTRUCTOR'
}

export function TeamTable({ members, isOwner }: { members: TeamMember[]; isOwner: boolean }) {
  const { t } = useTranslation()
  const changeRole = useChangeRole()
  const remove = useRemoveMember()
  const [removing, setRemoving] = useState<TeamMember | null>(null)

  const closeRemove = () => {
    setRemoving(null)
    remove.reset()
  }

  return (
    <>
      {changeRole.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, changeRole.error)}</div>}
      <div className="card table-card">
        <table className="table">
          <thead>
            <tr>
              <th scope="col">{t('team.col.member')}</th>
              <th scope="col">{t('team.col.role')}</th>
              <th scope="col" className="hide-narrow">{t('team.col.since')}</th>
              <th scope="col" className="col-actions">{t('team.col.actions')}</th>
            </tr>
          </thead>
          <tbody>
            {members.map((m) => (
              <tr key={m.membershipId}>
                <td>
                  <div className="row-title">
                    {m.name} {m.you && <span className="pill pill-outline">{t('team.you')}</span>}
                  </div>
                  <div className="row-sub">{m.email}</div>
                </td>
                <td>
                  {isOwner && canManage(m, isOwner) ? (
                    <select className="input input-sm" value={m.role} disabled={changeRole.isPending}
                      aria-label={t('team.roleOf', { name: m.name })}
                      onChange={(e) => changeRole.mutate({ membershipId: m.membershipId, role: e.target.value as StaffRole })}>
                      {INVITABLE_ROLES.map((r) => <option key={r} value={r}>{t(`roles.${r}`)}</option>)}
                    </select>
                  ) : (
                    <span className="pill pill-soft">{t(`roles.${m.role}`)}</span>
                  )}
                </td>
                <td className="hide-narrow nowrap">{formatDate(m.joinedAt)}</td>
                <td className="col-actions">
                  {canManage(m, isOwner) && (
                    <button type="button" className="icon-btn" onClick={() => setRemoving(m)}
                      aria-label={t('team.removeNamed', { name: m.name })} title={t('team.remove')}>
                      <UserMinus size={16} aria-hidden />
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <ConfirmDialog open={removing !== null} onClose={closeRemove}
        title={t('team.remove')} body={t('team.removeConfirm', { name: removing?.name ?? '' })}
        confirmLabel={t('team.remove')} pending={remove.isPending}
        error={remove.error ? apiErrorMessage(t, remove.error) : null}
        onConfirm={() => removing && remove.mutate(removing.membershipId, { onSuccess: closeRemove })} />
    </>
  )
}
