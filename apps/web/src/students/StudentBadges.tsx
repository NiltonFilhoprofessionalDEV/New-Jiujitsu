import { useTranslation } from 'react-i18next'

import type { StudentListItem } from './types'

const NEW_STUDENT_DAYS = 30
const DAY_MS = 86_400_000

function isNew(enrollmentDate: string, today = new Date()): boolean {
  const enrolled = new Date(`${enrollmentDate}T00:00:00`)
  return today.getTime() - enrolled.getTime() < NEW_STUDENT_DAYS * DAY_MS
}

/** Só a situação da matrícula; o papel na família aparece no texto ao lado do nome. */
export function StudentBadges({ student }: { student: StudentListItem }) {
  const { t } = useTranslation()

  if (student.status === 'INACTIVE') return <span className="pill pill-muted">{t('students.badge.inactive')}</span>
  if (!isNew(student.enrollmentDate)) return null
  return (
    <span className="pill pill-soft" title={t('students.badge.newTitle')}>
      {t('students.badge.new')}
    </span>
  )
}
