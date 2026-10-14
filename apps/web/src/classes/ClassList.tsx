import { Pencil, Trash2, Users } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { shortTime } from './dates'
import type { TrainingClass } from './types'

const WEEK_LENGTH = 7

type Props = {
  classes: TrainingClass[]
  canEdit: boolean
  onStudents: (c: TrainingClass) => void
  onEdit: (c: TrainingClass) => void
  onArchive: (c: TrainingClass) => void
}

/** Grade semanal: o horário na frente, porque é por ele que se procura uma turma. */
export function ClassList({ classes, canEdit, onStudents, onEdit, onArchive }: Props) {
  const { t } = useTranslation()

  function details(c: TrainingClass) {
    const days = c.weekdays.length === WEEK_LENGTH
      ? t('classes.everyDay')
      : c.weekdays.map((d) => t(`weekdays.short.${d}`)).join(', ')
    return [days, c.location, c.instructor?.name ?? t('classes.form.noInstructor')].filter(Boolean).join(' · ')
  }

  return (
    <div className="card list-card">
      <ul className="people-list">
        {classes.map((c) => (
          <li key={c.id} className="class-row">
            <span className="class-time">
              <strong>{shortTime(c.startTime)}</strong>
              {c.endTime && <span>{shortTime(c.endTime)}</span>}
            </span>
            <span className="person-main">
              <span className="row-title">{c.title}</span>
              <span className="row-sub">{details(c)}</span>
            </span>
            <span className="row-actions">
              <button type="button" className="btn btn-sm" onClick={() => onStudents(c)}
                aria-label={t('classes.students.title', { title: c.title })} title={t('classes.students.short')}>
                <Users size={15} aria-hidden />
                {c.capacity === null ? c.enrolledCount : `${c.enrolledCount}/${c.capacity}`}
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
          </li>
        ))}
      </ul>
    </div>
  )
}
