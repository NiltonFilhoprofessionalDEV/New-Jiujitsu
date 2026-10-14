import { X } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { apiErrorMessage } from '@/lib/apiErrors'
import { BeltBadge } from '@/students/BeltBadge'
import { StudentPicker } from '@/students/StudentPicker'

import { useClassStudents, useEnrollment } from './classesApi'
import type { TrainingClass } from './types'

type Props = { trainingClass: TrainingClass; canEdit: boolean }

export function ClassStudentsPanel({ trainingClass, canEdit }: Props) {
  const { t } = useTranslation()
  const { data: students, isLoading, isError } = useClassStudents(trainingClass.id)
  const enrollment = useEnrollment(trainingClass.id)
  const { capacity } = trainingClass

  if (isLoading) return <p className="empty">{t('common.loading')}</p>
  if (isError || !students) return <div className="alert alert-error">{t('classes.students.loadError')}</div>

  const full = capacity !== null && students.length >= capacity

  return (
    <div className="stack">
      {enrollment.error && (
        <div className="alert alert-error" role="alert">
          {apiErrorMessage(t, enrollment.error)}
        </div>
      )}
      <div>
        <div className="section-title">
          {capacity === null
            ? t('classes.students.count', { count: students.length })
            : t('classes.students.countOf', { count: students.length, capacity })}
        </div>
        {students.length === 0 && <p className="empty">{t('classes.students.empty')}</p>}
        <ul className="member-list">
          {students.map((s) => (
            <li key={s.id}>
              <span className="row-grow">{s.name}</span>
              {s.status === 'INACTIVE' && <span className="pill pill-muted">{t('students.badge.inactive')}</span>}
              <span className="person-belt"><BeltBadge belt={s.belt} degrees={s.degrees} /></span>
              {canEdit && (
                <button type="button" className="icon-btn" disabled={enrollment.isPending}
                  aria-label={t('classes.students.remove', { name: s.name })}
                  onClick={() => enrollment.mutate({ studentId: s.id, enroll: false })}>
                  <X size={16} aria-hidden />
                </button>
              )}
            </li>
          ))}
        </ul>
      </div>
      {canEdit && (
        <div>
          <div className="section-title">{t('classes.students.add')}</div>
          {full ? (
            <p className="hint">{t('classes.students.full')}</p>
          ) : (
            <StudentPicker scope="training" exclude={students.map((s) => s.id)} disabled={enrollment.isPending}
              actionLabel={() => t('classes.students.enroll')}
              onPick={(s) => enrollment.mutate({ studentId: s.id, enroll: true })} />
          )}
        </div>
      )}
    </div>
  )
}
