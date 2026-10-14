import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { useAuth } from '@/auth/useAuth'
import { PageHeader } from '@/components/PageHeader'
import { PasswordForm } from '@/settings/PasswordForm'

import { InvoicesCard } from './InvoicesCard'
import { useOverview, useProfiles } from './portalApi'
import { TodayClasses } from './TodayClasses'
import { AttendanceCard, BeltCard, ClassesCard } from './TrainingCards'
import type { Profile } from './types'

/** Área do aluno: o próprio cadastro e, para o responsável, os dependentes (troca pelas abas). */
export function StudentAccountPage() {
  const { t } = useTranslation()
  const { session, activeMembership } = useAuth()
  const { data: profiles, isLoading, isError } = useProfiles()
  const [chosenId, setChosenId] = useState<string | null>(null)

  if (!session || !activeMembership) return null
  const selected = profiles?.find((p) => p.id === chosenId) ?? profiles?.[0]

  return (
    <div className="page">
      <PageHeader eyebrow={activeMembership.academy.name}
        title={t('account.hello', { name: session.user.name.split(' ')[0] })} />
      <div className="stack">
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {isError && <div className="alert alert-error">{t('account.loadError')}</div>}
        {profiles?.length === 0 && <div className="card empty-detail">{t('account.noProfile')}</div>}
        {profiles && profiles.length > 1 && (
          <ProfileTabs profiles={profiles} selectedId={selected?.id} onSelect={setChosenId} />
        )}
        {selected && <ProfileView key={selected.id} profile={selected} />}
        <PasswordForm />
      </div>
    </div>
  )
}

type TabsProps = { profiles: Profile[]; selectedId: string | undefined; onSelect: (id: string) => void }

function ProfileTabs({ profiles, selectedId, onSelect }: TabsProps) {
  const { t } = useTranslation()
  return (
    <div className="chips" role="tablist" aria-label={t('account.profilesAria')}>
      {profiles.map((p) => (
        <button key={p.id} type="button" role="tab" aria-selected={p.id === selectedId}
          className={p.id === selectedId ? 'chip active' : 'chip'} onClick={() => onSelect(p.id)}>
          {p.self ? t('account.me') : p.nickname ?? p.name}
        </button>
      ))}
    </div>
  )
}

function ProfileView({ profile }: { profile: Profile }) {
  const { t } = useTranslation()
  const { data: overview } = useOverview(profile.id)
  const training = profile.kind === 'TRAINING'
  return (
    <>
      {profile.status === 'INACTIVE' && <div className="notice">{t('account.inactive')}</div>}
      {training && profile.status === 'ACTIVE' && <TodayClasses studentId={profile.id} />}
      <div className="account-grid">
        {training && overview && <BeltCard overview={overview} />}
        {training && overview && <ClassesCard overview={overview} />}
        {training && overview && <AttendanceCard overview={overview} />}
        <InvoicesCard studentId={profile.id} />
      </div>
    </>
  )
}
