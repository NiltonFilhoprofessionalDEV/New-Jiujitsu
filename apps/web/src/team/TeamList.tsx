import { UserMinus } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { ConfirmDialog } from '@/components/ConfirmDialog'
import { apiErrorMessage } from '@/lib/apiErrors'
import { initials } from '@/lib/text'
import { formatDate } from '@/students/format'

import { useChangeRole, useRemoveMember } from './teamApi'
import { INVITABLE_ROLES, type StaffRole, type TeamMember } from './types'

/** Espelha a regra da API: o dono é intocável e só o dono mexe em co-gestor. */
function canManage(member: TeamMember, isOwner: boolean): boolean {
  if (member.you || member.role === 'OWNER') return false
  return isOwner || member.role === 'INSTRUCTOR'
}

type RowProps = {
  member: TeamMember
  isOwner: boolean
  busy: boolean
  onRole: (role: StaffRole) => void
  onRemove: () => void
}

function MemberRow({ member: m, isOwner, busy, onRole, onRemove }: RowProps) {
  const { t } = useTranslation()
  const manageable = canManage(m, isOwner)
  return (
    <li className="list-row">
      <span className="avatar" aria-hidden>{initials(m.name)}</span>
      <span className="person-main">
        <span className="person-name">
          <span className="row-title">{m.name}</span>
          {m.you && <span className="pill pill-outline">{t('team.you')}</span>}
        </span>
        <span className="row-sub">{m.email} · {t('team.since', { date: formatDate(m.joinedAt) })}</span>
      </span>
      <span className="row-actions">
        {isOwner && manageable ? (
          <select className="input input-sm" value={m.role} disabled={busy}
            aria-label={t('team.roleOf', { name: m.name })} onChange={(e) => onRole(e.target.value as StaffRole)}>
            {INVITABLE_ROLES.map((r) => <option key={r} value={r}>{t(`roles.${r}`)}</option>)}
          </select>
        ) : (
          <span className="pill pill-soft">{t(`roles.${m.role}`)}</span>
        )}
        {manageable && (
          <button type="button" className="icon-btn" onClick={onRemove}
            aria-label={t('team.removeNamed', { name: m.name })} title={t('team.remove')}>
            <UserMinus size={16} aria-hidden />
          </button>
        )}
      </span>
    </li>
  )
}

export function TeamList({ members, isOwner }: { members: TeamMember[]; isOwner: boolean }) {
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
      <div className="card list-card">
        <ul className="people-list">
          {members.map((m) => (
            <MemberRow key={m.membershipId} member={m} isOwner={isOwner} busy={changeRole.isPending}
              onRole={(role) => changeRole.mutate({ membershipId: m.membershipId, role })}
              onRemove={() => setRemoving(m)} />
          ))}
        </ul>
      </div>

      <ConfirmDialog open={removing !== null} onClose={closeRemove}
        title={t('team.remove')} body={t('team.removeConfirm', { name: removing?.name ?? '' })}
        confirmLabel={t('team.remove')} pending={remove.isPending}
        error={remove.error ? apiErrorMessage(t, remove.error) : null}
        onConfirm={() => removing && remove.mutate(removing.membershipId, { onSuccess: closeRemove })} />
    </>
  )
}
