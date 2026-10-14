import { useTranslation } from 'react-i18next'

import { useAttendanceSession, useSaveAttendance } from './classesApi'
import { timeRange } from './dates'
import { RosterForm } from './RosterForm'

type Props = { classId: string; date: string }

export function AttendancePanel({ classId, date }: Props) {
  const { t } = useTranslation()
  const { data: session, isLoading, isError, dataUpdatedAt } = useAttendanceSession(classId, date)
  const save = useSaveAttendance(classId, date)

  if (isLoading) return <div className="card empty-detail">{t('common.loading')}</div>
  if (isError || !session) return <div className="alert alert-error">{t('attendance.sessionError')}</div>

  return (
    <section className="card stack" aria-labelledby="attendance-title">
      <header className="attendance-header">
        <div className="eyebrow">{t('attendance.title')}</div>
        <h2 id="attendance-title">{session.title}</h2>
        <p className="row-sub">
          {timeRange(session.startTime, session.endTime)}
          {' · '}
          {t('attendance.instructor')} {session.instructor?.name ?? t('classes.form.noInstructor')}
        </p>
      </header>
      {/* Remonta após salvar ou trocar de aula/dia, reiniciando a edição a partir do servidor. */}
      <RosterForm key={`${classId}-${date}-${dataUpdatedAt}`} session={session} save={save} />
    </section>
  )
}
