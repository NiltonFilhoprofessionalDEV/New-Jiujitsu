import { Pencil, Trash2, Users } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { ConfirmDialog } from '@/components/ConfirmDialog'
import { GraduationSection } from '@/graduations/GraduationSection'
import { apiErrorMessage } from '@/lib/apiErrors'
import { initials } from '@/lib/text'

import { BeltBadge } from './BeltBadge'
import { BELT_HERO_SIZE } from './belts'
import { formatDate } from './format'
import { StudentBadges } from './StudentBadges'
import { StudentFacts } from './StudentFacts'
import { useDeleteStudent, useSetStudentActive, useStudent } from './studentsApi'
import type { StudentDetail } from './types'

type Props = {
  studentId: string
  canEdit: boolean
  onEdit: (student: StudentDetail) => void
  onOpenHousehold: (householdId: string) => void
  onDeleted: () => void
}

type Confirm = 'deactivate' | 'delete' | null

export function StudentDetailPanel({ studentId, canEdit, onEdit, onOpenHousehold, onDeleted }: Props) {
  const { t } = useTranslation()
  const { data: student, isLoading, isError } = useStudent(studentId)
  const setActive = useSetStudentActive()
  const remove = useDeleteStudent()
  const [confirm, setConfirm] = useState<Confirm>(null)

  if (isLoading) return <div className="card empty">{t('common.loading')}</div>
  if (isError || !student) return <div className="card alert alert-error">{t('students.loadError')}</div>

  const household = student.household
  const active = student.status === 'ACTIVE'
  const training = student.kind === 'TRAINING'
  const actionError = setActive.error ?? remove.error

  async function onConfirm() {
    if (!student) return
    if (confirm === 'delete') {
      await remove.mutateAsync(student.id)
      setConfirm(null)
      onDeleted()
      return
    }
    await setActive.mutateAsync({ id: student.id, active: false })
    setConfirm(null)
  }

  return (
    <section className="card detail-panel" aria-label={t('students.detail.aria', { name: student.name })}>
      <header className="detail-header">
        <span className="avatar avatar-lg" aria-hidden>{initials(student.name)}</span>
        <div className="detail-title">
          <h2>{student.name}</h2>
          <p className="row-sub">
            {[student.nickname && `“${student.nickname}”`, !training && t('students.badge.guardian')]
              .filter(Boolean).join(' · ')}
          </p>
          <StudentBadges student={student} />
        </div>
        {canEdit && (
          <button type="button" className="btn btn-icon" onClick={() => onEdit(student)} aria-label={t('common.edit')}
            title={t('common.edit')}>
            <Pencil size={16} aria-hidden />
          </button>
        )}
      </header>

      {training && (
        <div className="belt-hero">
          <BeltBadge belt={student.belt} degrees={student.degrees} size={BELT_HERO_SIZE} />
          <span className="row-sub">{t('students.trainsSince', { date: formatDate(student.enrollmentDate) })}</span>
        </div>
      )}

      {household && (
        <button type="button" className="family-link" onClick={() => onOpenHousehold(household.id)}>
          <Users size={16} aria-hidden />
          <span>{t('students.familyName', { name: household.name })}</span>
          {student.householdResponsible && <span className="pill pill-soft">{t('students.badge.responsible')}</span>}
        </button>
      )}

      {training && <GraduationSection student={student} canEdit={canEdit} />}

      <StudentFacts student={student} />

      {canEdit && (
        <footer className="detail-actions">
          {active ? (
            <button type="button" className="btn btn-ghost" onClick={() => setConfirm('deactivate')}>
              {t('students.detail.deactivate')}
            </button>
          ) : (
            <button type="button" className="btn btn-outline-primary" disabled={setActive.isPending}
              onClick={() => setActive.mutate({ id: student.id, active: true })}>
              {t('students.detail.reactivate')}
            </button>
          )}
          <button type="button" className="btn btn-danger-ghost" onClick={() => setConfirm('delete')}>
            <Trash2 size={16} aria-hidden /> {t('students.detail.delete')}
          </button>
        </footer>
      )}

      <ConfirmDialog
        open={confirm !== null}
        title={confirm === 'delete' ? t('students.detail.delete') : t('students.detail.deactivate')}
        body={
          confirm === 'delete'
            ? t('students.detail.deleteConfirm', { name: student.name })
            : t('students.detail.deactivateConfirm', { name: student.name })
        }
        confirmLabel={confirm === 'delete' ? t('common.delete') : t('common.confirm')}
        pending={setActive.isPending || remove.isPending}
        error={actionError ? apiErrorMessage(t, actionError) : null}
        onConfirm={() => void onConfirm().catch(() => undefined)}
        onClose={() => setConfirm(null)}
      />
    </section>
  )
}
