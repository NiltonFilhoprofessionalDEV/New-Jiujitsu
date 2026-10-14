import { useTranslation } from 'react-i18next'

import { useAttendanceSession } from './classesApi'
import { timeRange } from './dates'
import { RosterForm } from './RosterForm'

type Props = { classId: string; date: string; onSaved: (title: string) => void }

export function AttendancePanel({ classId, date, onSaved }: Props) {
  const { t } = useTranslation()
  const { data: session, isLoading, isError } = useAttendanceSession(classId, date)

  if (isLoading) return <p className="empty">{t('common.loading')}</p>
  if (isError || !session) return <div className="alert alert-error">{t('attendance.sessionError')}</div>

  return (
    <section className="stack" aria-labelledby="attendance-title">
      <header className="attendance-header">
        <h3 id="attendance-title">{session.title}</h3>
        <p className="row-sub">
          {timeRange(session.startTime, session.endTime)}
          {' · '}
          {t('attendance.instructor')} {session.instructor?.name ?? t('classes.form.noInstructor')}
        </p>
      </header>
      <RosterForm classId={classId} date={date} session={session} onSaved={() => onSaved(session.title)} />
    </section>
  )
}
