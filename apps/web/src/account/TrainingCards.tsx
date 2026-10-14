import { useTranslation } from 'react-i18next'

import { timeRange } from '@/classes/dates'
import { BeltBadge } from '@/students/BeltBadge'
import { formatDate } from '@/students/format'

import type { Overview } from './types'

/** Faixa atual, tempo e treinos na faixa e o histórico de graduações. */
export function BeltCard({ overview }: { overview: Overview }) {
  const { t } = useTranslation()
  const { student, progress, graduations } = overview
  return (
    <section className="card stack" aria-labelledby="belt-title">
      <h3 id="belt-title">{t('account.belt.title')}</h3>
      <BeltBadge belt={student.belt} degrees={student.degrees} />
      {progress && (
        <p className="hint">
          {t('account.belt.progress', {
            since: formatDate(progress.beltSince),
            months: t('graduation.months', { count: progress.monthsAtBelt }),
            trainings: t('graduation.trainings', { count: progress.trainingsSinceBelt }),
          })}
        </p>
      )}
      {graduations.length > 0 && (
        <ul className="plain-list">
          {graduations.map((g) => (
            <li key={`${g.graduatedOn}-${g.belt}-${g.degrees}`}>
              <span className="row-sub">{formatDate(g.graduatedOn)} · {t(`graduation.kind.${g.kind}`)}</span>
              <BeltBadge belt={g.belt} degrees={g.degrees} />
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

export function ClassesCard({ overview }: { overview: Overview }) {
  const { t } = useTranslation()
  return (
    <section className="card stack" aria-labelledby="classes-title">
      <h3 id="classes-title">{t('account.classes.title')}</h3>
      {overview.classes.length === 0 && <p className="empty">{t('account.classes.empty')}</p>}
      <ul className="plain-list">
        {overview.classes.map((c) => (
          <li key={c.classId}>
            <div className="row-title">{c.title}</div>
            <div className="row-sub">
              {[c.weekdays.map((d) => t(`weekdays.short.${d}`)).join(', '), timeRange(c.startTime, c.endTime),
                c.location, c.instructor?.name].filter(Boolean).join(' · ')}
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}

export function AttendanceCard({ overview }: { overview: Overview }) {
  const { t } = useTranslation()
  return (
    <section className="card stack" aria-labelledby="attendance-title">
      <div>
        <h3 id="attendance-title">{t('account.attendance.title')}</h3>
        <p className="hint">{t('account.attendance.total', { count: overview.attendanceTotal })}</p>
      </div>
      {overview.recentAttendance.length === 0 && <p className="empty">{t('account.attendance.empty')}</p>}
      <ul className="plain-list">
        {overview.recentAttendance.map((a) => (
          <li key={`${a.date}-${a.title}`}>
            <span>{a.title}</span>
            <span className="row-sub">{formatDate(a.date)}</span>
          </li>
        ))}
      </ul>
    </section>
  )
}
