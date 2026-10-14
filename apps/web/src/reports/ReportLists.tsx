import { useTranslation } from 'react-i18next'

import { BeltBadge } from '@/students/BeltBadge'
import { formatDate } from '@/students/format'

import { perSession } from './average'
import type { Report } from './types'

export function ClassesTable({ rows }: { rows: Report['classes'] }) {
  const { t } = useTranslation()
  return (
    <section className="stack" aria-labelledby="classes-report-title">
      <h2 id="classes-report-title" className="section-title">{t('reports.classes.title')}</h2>
      {rows.length === 0 ? (
        <div className="card empty-detail">{t('reports.classes.empty')}</div>
      ) : (
        <div className="card table-card">
          <table className="table">
            <thead>
              <tr>
                <th scope="col">{t('reports.classes.col.class')}</th>
                <th scope="col" className="num">{t('reports.col.sessions')}</th>
                <th scope="col" className="num">{t('reports.col.checkIns')}</th>
                <th scope="col" className="num">{t('reports.col.average')}</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((c) => (
                <tr key={c.classId}>
                  <td>
                    <div className="row-title">{c.title}</div>
                    <div className="row-sub">{c.instructor ?? t('reports.classes.noInstructor')}</div>
                  </td>
                  <td className="num">{c.sessions}</td>
                  <td className="num">{c.checkIns}</td>
                  <td className="num">{perSession(c.checkIns, c.sessions)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}

export function TopStudents({ rows }: { rows: Report['topStudents'] }) {
  const { t } = useTranslation()
  return (
    <section className="card stack" aria-labelledby="top-title">
      <h3 id="top-title">{t('reports.top.title')}</h3>
      {rows.length === 0 && <p className="empty">{t('reports.top.empty')}</p>}
      <ol className="plain-list">
        {rows.map((s) => (
          <li key={s.studentId}>
            <span>
              <span className="row-title">{s.name}</span> <BeltBadge belt={s.belt} degrees={s.degrees} />
            </span>
            <span className="row-sub">{t('reports.top.trainings', { count: s.checkIns })}</span>
          </li>
        ))}
      </ol>
    </section>
  )
}

export function AbsentStudents({ absent }: { absent: Report['absent'] }) {
  const { t } = useTranslation()
  const hidden = absent.total - absent.students.length
  return (
    <section className="card stack" aria-labelledby="absent-title">
      <div>
        <h3 id="absent-title">{t('reports.absent.title')}</h3>
        <p className="hint">{t('reports.absent.hint', { date: formatDate(absent.since) })}</p>
      </div>
      {absent.total === 0 && <p className="empty">{t('reports.absent.empty')}</p>}
      <ul className="plain-list">
        {absent.students.map((s) => (
          <li key={s.studentId}>
            <span className="row-title">{s.name}</span>
            <span className="row-sub">
              {s.lastCheckIn ? t('reports.absent.last', { date: formatDate(s.lastCheckIn) }) : t('reports.absent.never')}
            </span>
          </li>
        ))}
      </ul>
      {hidden > 0 && <p className="hint">{t('reports.absent.more', { count: hidden })}</p>}
    </section>
  )
}
