import { Pencil, Trash2, Users } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { timeRange } from './dates'
import type { TrainingClass } from './types'

type Props = {
  classes: TrainingClass[]
  canEdit: boolean
  onStudents: (c: TrainingClass) => void
  onEdit: (c: TrainingClass) => void
  onArchive: (c: TrainingClass) => void
}

export function ClassesTable({ classes, canEdit, onStudents, onEdit, onArchive }: Props) {
  const { t } = useTranslation()
  const days = (c: TrainingClass) => c.weekdays.map((d) => t(`weekdays.short.${d}`)).join(', ')

  return (
    <div className="card table-card">
      <table className="table">
        <thead>
          <tr>
            <th>{t('classes.table.title')}</th>
            <th>{t('classes.table.days')}</th>
            <th>{t('classes.table.time')}</th>
            <th className="hide-narrow">{t('classes.table.location')}</th>
            <th className="hide-narrow">{t('classes.table.instructor')}</th>
            <th>{t('classes.table.enrolled')}</th>
            <th className="col-actions">{t('classes.table.actions')}</th>
          </tr>
        </thead>
        <tbody>
          {classes.map((c) => (
            <tr key={c.id}>
              <td>
                <div className="row-title">{c.title}</div>
                {c.description && <div className="row-sub">{c.description}</div>}
              </td>
              <td>{days(c)}</td>
              <td className="nowrap">{timeRange(c.startTime, c.endTime)}</td>
              <td className="hide-narrow">{c.location ?? '—'}</td>
              <td className="hide-narrow">{c.instructor?.name ?? t('classes.form.noInstructor')}</td>
              <td className="nowrap">
                {c.capacity === null ? c.enrolledCount : `${c.enrolledCount}/${c.capacity}`}
              </td>
              <td className="col-actions">
                <span className="row-actions">
                  <button type="button" className="icon-btn" onClick={() => onStudents(c)}
                    aria-label={t('classes.students.title', { title: c.title })} title={t('classes.students.short')}>
                    <Users size={16} aria-hidden />
                  </button>
                  {canEdit && (
                    <>
                      <button type="button" className="icon-btn" onClick={() => onEdit(c)}
                        aria-label={t('classes.editNamed', { title: c.title })} title={t('common.edit')}>
                        <Pencil size={16} aria-hidden />
                      </button>
                      <button type="button" className="icon-btn" onClick={() => onArchive(c)}
                        aria-label={t('classes.archiveNamed', { title: c.title })} title={t('classes.archive')}>
                        <Trash2 size={16} aria-hidden />
                      </button>
                    </>
                  )}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
