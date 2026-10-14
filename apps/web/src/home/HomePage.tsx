import { useTranslation } from 'react-i18next'

import { useAuth, useIsManager } from '@/auth/useAuth'
import { useDayClasses } from '@/classes/classesApi'
import { toIsoDate } from '@/classes/dates'
import { PageTitle } from '@/components/PageTitle'

import { Overview } from './Overview'
import { QuickActions } from './QuickActions'
import { RequestsCard } from './RequestsCard'
import { TodayPanel } from './TodayPanel'
import { useMinuteClock } from './useMinuteClock'

const DATE_FORMAT: Intl.DateTimeFormatOptions = { weekday: 'long', day: 'numeric', month: 'long' }

/** Painel do dia: o que está no tatame, o que vem a seguir e os atalhos da rotina. */
export function HomePage() {
  const { t, i18n } = useTranslation()
  const { activeMembership } = useAuth()
  const isManager = useIsManager()
  const now = useMinuteClock()
  const today = toIsoDate(now)
  const { data: classes } = useDayClasses(today)

  return (
    <div className="page page-narrow home">
      <header className="page-header">
        <div>
          <div className="eyebrow">{t('app.name')}</div>
          <PageTitle>{activeMembership?.academy.name ?? t('home.title')}</PageTitle>
          <p className="page-sub home-date">{new Intl.DateTimeFormat(i18n.language, DATE_FORMAT).format(now)}</p>
        </div>
      </header>
      <Overview classes={classes} />
      <TodayPanel classes={classes} now={now} date={today} />
      {isManager && <RequestsCard />}
      <QuickActions isManager={isManager} />
    </div>
  )
}
