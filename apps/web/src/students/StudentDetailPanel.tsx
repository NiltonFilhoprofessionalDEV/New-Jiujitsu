import { Pencil, Trash2, Users } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { ConfirmDialog } from '@/components/ConfirmDialog'
import { DataItem } from '@/components/DataItem'
import { GraduationSection } from '@/graduations/GraduationSection'
import { apiErrorMessage } from '@/lib/apiErrors'

import { BeltBadge } from './BeltBadge'
import { ageFrom } from './belts'
import { formatCpf, formatDate } from './format'
import { StudentBadges } from './StudentBadges'
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

  const age = ageFrom(student.birthDate)
  const household = student.household
  const active = student.status === 'ACTIVE'
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
        <div>
          <h2>{student.name}</h2>
          {student.nickname && <p className="empty">“{student.nickname}”</p>}
          <StudentBadges student={student} />
        </div>
        {canEdit && (
          <button type="button" className="btn" onClick={() => onEdit(student)}>
            <Pencil size={16} aria-hidden /> {t('common.edit')}
          </button>
        )}
      </header>

      {student.kind === 'TRAINING' && <BeltBadge belt={student.belt} degrees={student.degrees} />}

      {household && (
        <button type="button" className="btn btn-ghost family-link" onClick={() => onOpenHousehold(household.id)}>
          <Users size={16} aria-hidden />
          {t('students.familyName', { name: household.name })}
          {student.householdResponsible && <span className="pill pill-soft">{t('students.badge.responsible')}</span>}
        </button>
      )}

      <dl className="data-list">
        <DataItem label={t('students.form.email')} value={student.email ?? t('students.noEmail')} />
        <DataItem label={t('students.form.cpf')} value={formatCpf(student.cpf)} />
        <DataItem label={t('students.form.phone')} value={student.phone} />
        <DataItem
          label={t('students.form.birthDate')}
          value={student.birthDate && `${formatDate(student.birthDate)} · ${t('students.years', { count: age ?? 0 })}`}
        />
        <DataItem label={t('students.form.gender')} value={student.gender && t(`students.gender.${student.gender}`)} />
        <DataItem
          label={t('students.form.enrollmentDate', { context: student.kind })}
          value={formatDate(student.enrollmentDate)}
        />
        {student.kind === 'TRAINING' && (
          <>
            <DataItem label={t('students.form.degreeAwardedAt')} value={formatDate(student.degreeAwardedAt)} />
            <DataItem label={t('students.form.beltRuler')} value={t(`students.ruler.${student.beltRuler}`)} />
            <DataItem label={t('students.form.giSize')} value={student.giSize} />
            <DataItem label={t('students.form.beltSize')} value={student.beltSize} />
          </>
        )}
      </dl>

      {student.kind === 'TRAINING' && <GraduationSection student={student} canEdit={canEdit} />}

      {canEdit && (
        <footer className="detail-actions">
          {active ? (
            <button type="button" className="btn" onClick={() => setConfirm('deactivate')}>
              {t('students.detail.deactivate')}
            </button>
          ) : (
            <button
              type="button"
              className="btn btn-outline-primary"
              disabled={setActive.isPending}
              onClick={() => setActive.mutate({ id: student.id, active: true })}
            >
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
