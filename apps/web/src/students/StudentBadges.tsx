import { useTranslation } from 'react-i18next'

import type { StudentListItem } from './types'

const NEW_STUDENT_DAYS = 30
const DAY_MS = 86_400_000

function isNew(enrollmentDate: string, today = new Date()): boolean {
  const enrolled = new Date(`${enrollmentDate}T00:00:00`)
  return today.getTime() - enrolled.getTime() < NEW_STUDENT_DAYS * DAY_MS
}

export function StudentBadges({ student }: { student: StudentListItem }) {
  const { t } = useTranslation()
  const dependent = student.household && !student.householdResponsible && student.kind === 'TRAINING' && !student.email

  return (
    <span className="badges">
      {student.status === 'INACTIVE' && <span className="pill pill-muted">{t('students.badge.inactive')}</span>}
      {student.status === 'ACTIVE' && isNew(student.enrollmentDate) && (
        <span className="pill pill-soft" title={t('students.badge.newTitle')}>
          {t('students.badge.new')}
        </span>
      )}
      {student.kind === 'RESPONSIBLE' && (
        <span className="pill pill-outline" title={t('students.badge.guardianTitle')}>
          {t('students.badge.guardian')}
        </span>
      )}
      {dependent && (
        <span className="pill pill-outline" title={t('students.badge.dependentTitle')}>
          {t('students.badge.dependent')}
        </span>
      )}
    </span>
  )
}
