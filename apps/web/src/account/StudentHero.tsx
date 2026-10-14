import { useTranslation } from 'react-i18next'

import { BeltBand } from '@/students/BeltBand'
import { BeltIcon } from '@/students/BeltIcon'
import { beltInfo } from '@/students/belts'

import type { Overview, Profile } from './types'

type Props = {
  academy: string
  firstName: string
  profile?: Profile
  overview?: Overview
}

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="flex min-w-0 flex-col justify-between gap-1">
      <dt className="text-[11px] leading-tight font-bold tracking-[0.14em] text-ink-muted uppercase">{label}</dt>
      <dd className="m-0 font-heading text-4xl leading-none tracking-wide">{value}</dd>
    </div>
  )
}

function BeltLine({ profile }: { profile: Profile }) {
  const { t } = useTranslation()
  const info = beltInfo(profile.belt)
  if (!info) return null
  const degree = profile.degrees > 0 ? t('students.degreeShort', { count: profile.degrees }) : t('students.noDegree')
  return (
    <p className="m-0 mt-4 flex items-center gap-3 text-sm text-ink-muted">
      <BeltIcon belt={info.slug} degrees={profile.degrees} size={38} />
      <span>
        {!profile.self && <span className="font-semibold text-ink-foreground">{profile.nickname ?? profile.name} · </span>}
        <span className="font-semibold text-ink-foreground">{t('account.hero.belt', { belt: t(`belts.${info.slug}`) })}</span>
        {' · '}{degree}
      </span>
    </p>
  )
}

/** Abertura da área do aluno: saudação e, para quem treina, a faixa atual "vestindo" o card. */
export function StudentHero({ academy, firstName, profile, overview }: Props) {
  const { t } = useTranslation()
  const training = profile?.kind === 'TRAINING'
  const progress = overview?.progress

  return (
    <section aria-labelledby="hero-title"
      className="relative isolate overflow-hidden rounded-3xl border border-ink-border bg-ink text-ink-foreground shadow-elevated">
      <div aria-hidden className="pointer-events-none absolute -top-28 -right-20 -z-10 size-80 rounded-full bg-pulse/30 blur-3xl" />
      <div aria-hidden className="pointer-events-none absolute inset-0 -z-10 bg-[linear-gradient(rgb(255_255_255/0.03)_1px,transparent_1px),linear-gradient(90deg,rgb(255_255_255/0.03)_1px,transparent_1px)] bg-[size:40px_40px]" />
      <div className="px-6 pt-6 pb-6 sm:px-8 sm:pt-8">
        <p className="m-0 text-[11px] font-bold tracking-[0.2em] text-pulse uppercase">{academy}</p>
        <h1 id="hero-title" className="mt-2 font-heading text-5xl leading-[0.9] font-normal tracking-wide uppercase sm:text-6xl">
          {t('account.hello', { name: firstName })}
        </h1>
        {training && profile && <BeltLine profile={profile} />}
        {training && overview && (
          <dl className="m-0 mt-6 grid grid-cols-3 gap-4 border-t border-ink-border pt-5">
            <Stat label={t('account.hero.months')} value={progress ? (progress.monthsAtBelt || '<1') : '—'} />
            <Stat label={t('account.hero.beltTrainings')} value={progress?.trainingsSinceBelt ?? '—'} />
            <Stat label={t('account.hero.total')} value={overview.attendanceTotal} />
          </dl>
        )}
      </div>
      {training && profile && <BeltBand belt={profile.belt} degrees={profile.degrees} />}
    </section>
  )
}
