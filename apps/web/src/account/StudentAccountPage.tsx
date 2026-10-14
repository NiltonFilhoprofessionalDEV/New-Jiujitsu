import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { useAuth } from '@/auth/useAuth'
import { cn } from '@/lib/utils'

import { InvoicesCard } from './InvoicesCard'
import { useOverview, useProfiles } from './portalApi'
import { StudentHero } from './StudentHero'
import { TodayClasses } from './TodayClasses'
import { AttendanceCard, ClassesCard, GraduationsCard } from './TrainingCards'
import type { Profile } from './types'

/** Início do aluno: o próprio cadastro e, para o responsável, os dependentes (troca pelas abas). */
export function StudentAccountPage() {
  const { t } = useTranslation()
  const { session, activeMembership } = useAuth()
  const { data: profiles, isLoading, isError } = useProfiles()
  const [chosenId, setChosenId] = useState<string | null>(null)

  if (!session || !activeMembership) return null
  const selected = profiles?.find((p) => p.id === chosenId) ?? profiles?.[0]
  const hero = { academy: activeMembership.academy.name, firstName: session.user.name.split(' ')[0] ?? '' }

  return (
    <div className="page grid gap-5">
      {profiles && profiles.length > 1 && (
        <ProfileTabs profiles={profiles} selectedId={selected?.id} onSelect={setChosenId} />
      )}
      {selected ? <ProfileView key={selected.id} profile={selected} {...hero} /> : <StudentHero {...hero} />}
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {isError && <div className="alert alert-error">{t('account.loadError')}</div>}
      {profiles?.length === 0 && <div className="card empty-detail">{t('account.noProfile')}</div>}
    </div>
  )
}

type TabsProps = { profiles: Profile[]; selectedId: string | undefined; onSelect: (id: string) => void }

function ProfileTabs({ profiles, selectedId, onSelect }: TabsProps) {
  const { t } = useTranslation()
  return (
    <div role="tablist" aria-label={t('account.profilesAria')}
      className="flex gap-1 overflow-x-auto rounded-full border border-border bg-card p-1 sm:self-start">
      {profiles.map((p) => (
        <button key={p.id} type="button" role="tab" aria-selected={p.id === selectedId} onClick={() => onSelect(p.id)}
          className={cn('h-9 cursor-pointer rounded-full border-0 px-4 text-sm font-semibold whitespace-nowrap transition-colors',
            p.id === selectedId ? 'bg-primary text-primary-foreground shadow-glow-sm' : 'bg-transparent text-muted-foreground hover:text-foreground')}>
          {p.self ? t('account.me') : p.nickname ?? p.name}
        </button>
      ))}
    </div>
  )
}

type ViewProps = { profile: Profile; academy: string; firstName: string }

function ProfileView({ profile, academy, firstName }: ViewProps) {
  const { t } = useTranslation()
  const { data: overview } = useOverview(profile.id)
  const training = profile.kind === 'TRAINING'
  return (
    <>
      <StudentHero academy={academy} firstName={firstName} profile={profile} overview={overview} />
      {profile.status === 'INACTIVE' && <div className="notice">{t('account.inactive')}</div>}
      <div className="grid items-start gap-5 lg:grid-cols-[minmax(0,1.5fr)_minmax(0,1fr)]">
        <div className="grid gap-5">
          {training && profile.status === 'ACTIVE' && <TodayClasses studentId={profile.id} />}
          {training && overview && <ClassesCard overview={overview} />}
        </div>
        <div className="grid gap-5">
          <InvoicesCard studentId={profile.id} />
          {training && overview && <AttendanceCard overview={overview} />}
          {training && overview && <GraduationsCard overview={overview} />}
        </div>
      </div>
    </>
  )
}
