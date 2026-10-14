import { useTranslation } from 'react-i18next'

import { useAuth } from '@/auth/useAuth'

import { StaffInviteForm } from './StaffInviteForm'
import { StaffInviteList } from './StaffInviteList'
import { TeamTable } from './TeamTable'
import { useTeam } from './teamApi'

export function TeamPage() {
  const { t } = useTranslation()
  const { activeMembership } = useAuth()
  const isOwner = activeMembership?.role === 'OWNER'
  const { data: members, isLoading, isError } = useTeam()

  return (
    <div className="page page-wide">
      <div className="page-header">
        <div>
          <div className="eyebrow">{t('common.yourAcademy')}</div>
          <h1>{t('team.title')}</h1>
        </div>
      </div>

      <div className="stack">
        <p className="hint">{t('team.intro')}</p>
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {isError && <div className="alert alert-error">{t('team.loadError')}</div>}
        {members && <TeamTable members={members} isOwner={isOwner} />}
        <StaffInviteForm isOwner={isOwner} />
        <StaffInviteList />
      </div>
    </div>
  )
}
