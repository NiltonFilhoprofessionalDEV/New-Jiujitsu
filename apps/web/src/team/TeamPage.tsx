import { UserPlus } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { useAuth } from '@/auth/useAuth'
import { Dialog } from '@/components/Dialog'
import { HelpNote } from '@/components/HelpNote'
import { PageHeader } from '@/components/PageHeader'

import { StaffInviteForm } from './StaffInviteForm'
import { StaffInviteList } from './StaffInviteList'
import { TeamList } from './TeamList'
import { useTeam } from './teamApi'

export function TeamPage() {
  const { t } = useTranslation()
  const { activeMembership } = useAuth()
  const isOwner = activeMembership?.role === 'OWNER'
  const { data: members, isLoading, isError } = useTeam()
  const [inviting, setInviting] = useState(false)

  return (
    <div className="page page-wide">
      <PageHeader eyebrow={t('common.yourAcademy')} title={t('team.title')} description={t('pages.team')}
        actions={(
          <button type="button" className="btn btn-primary" onClick={() => setInviting(true)}>
            <UserPlus size={16} aria-hidden /> {t('team.invite.title')}
          </button>
        )} />

      <div className="stack">
        <HelpNote><p>{t('team.intro')}</p></HelpNote>
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {isError && <div className="alert alert-error">{t('team.loadError')}</div>}
        {members && <TeamList members={members} isOwner={isOwner} />}
        <StaffInviteList />
      </div>

      <Dialog open={inviting} onClose={() => setInviting(false)} title={t('team.invite.title')}>
        {inviting && <StaffInviteForm isOwner={isOwner} />}
      </Dialog>
    </div>
  )
}
