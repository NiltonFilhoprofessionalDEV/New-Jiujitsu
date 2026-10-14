import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { ConfirmDialog } from '@/components/ConfirmDialog'
import { apiErrorMessage } from '@/lib/apiErrors'

import { BeltBadge } from './BeltBadge'
import { StudentForm } from './StudentForm'
import { StudentPicker } from './StudentPicker'
import { useDeleteHousehold, useHousehold, useHouseholdAction } from './studentsApi'

type Props = {
  householdId: string
  canEdit: boolean
  onOpenStudent: (id: string) => void
  onClose: () => void
}

export function HouseholdPanel({ householdId, canEdit, onOpenStudent, onClose }: Props) {
  const { t } = useTranslation()
  const { data: household, isLoading, isError } = useHousehold(householdId)
  const action = useHouseholdAction(householdId)
  const remove = useDeleteHousehold()
  const [name, setName] = useState<string | null>(null)
  const [addingDependent, setAddingDependent] = useState(false)
  const [confirmDelete, setConfirmDelete] = useState(false)

  if (isLoading) return <p className="empty">{t('common.loading')}</p>
  if (isError || !household) return <div className="alert alert-error">{t('households.loadError')}</div>

  const currentName = name ?? household.name
  const hasResponsible = household.responsibleStudentId !== null
  const error = action.error ?? remove.error

  async function onDelete() {
    await remove.mutateAsync(householdId)
    onClose()
  }

  if (addingDependent) {
    return (
      <div className="stack">
        <p className="hint">{t('households.dependentHint')}</p>
        <StudentForm householdId={householdId} onSaved={() => setAddingDependent(false)}
          onCancel={() => setAddingDependent(false)} />
      </div>
    )
  }

  return (
    <div className="stack">
      {error && (
        <div className="alert alert-error" role="alert">
          {apiErrorMessage(t, error)}
        </div>
      )}

      {canEdit && (
        <div className="inline-form">
          <div className="field grow">
            <label htmlFor="hp-name">{t('households.name')}</label>
            <input id="hp-name" className="input" maxLength={120} value={currentName} onChange={(e) => setName(e.target.value)} />
          </div>
          <button type="button" className="btn" disabled={currentName.trim() === '' || currentName === household.name || action.isPending}
            onClick={() => action.mutate({ type: 'rename', name: currentName.trim() }, { onSuccess: () => setName(null) })}>
            {t('common.save')}
          </button>
        </div>
      )}

      <div>
        <div className="section-title">{t('households.membersCount', { count: household.members.length })}</div>
        {household.members.length === 0 && <p className="empty">{t('households.membersEmpty')}</p>}
        <ul className="member-list">
          {household.members.map((m) => {
            const isResponsible = m.id === household.responsibleStudentId
            return (
              <li key={m.id}>
                <button type="button" className="link-btn" onClick={() => onOpenStudent(m.id)}>
                  {m.name}
                </button>
                {isResponsible && <span className="pill pill-soft">{t('students.badge.responsible')}</span>}
                {m.kind === 'TRAINING' && <BeltBadge belt={m.belt} degrees={m.degrees} />}
                {canEdit && (
                  <span className="row-actions">
                    {!isResponsible && (
                      <button type="button" className="btn btn-sm" disabled={action.isPending}
                        onClick={() => action.mutate({ type: 'responsible', studentId: m.id })}>
                        {t('households.makeResponsible')}
                      </button>
                    )}
                    <button type="button" className="btn btn-sm" disabled={action.isPending}
                      onClick={() => action.mutate({ type: 'removeMember', studentId: m.id })}>
                      {t('households.removeFromFamily')}
                    </button>
                  </span>
                )}
              </li>
            )
          })}
        </ul>
      </div>

      {canEdit && (
        <>
          <div>
            <div className="section-title">{t('households.addExisting')}</div>
            <StudentPicker actionLabel={() => t('common.add')} disabled={action.isPending}
              onPick={(s) => action.mutate({ type: 'addMember', studentId: s.id })} />
          </div>
          <div className="callout">
            <div>
              <strong>{t('households.dependentCta')}</strong>
              <p className="hint">{hasResponsible ? t('households.dependentCtaSub') : t('households.needResponsible')}</p>
            </div>
            <button type="button" className="btn btn-outline-primary" disabled={!hasResponsible} onClick={() => setAddingDependent(true)}>
              {t('households.addDependent')}
            </button>
          </div>
          <div className="form-actions">
            <button type="button" className="btn btn-danger-ghost" onClick={() => setConfirmDelete(true)}>
              {t('households.delete')}
            </button>
          </div>
        </>
      )}

      <ConfirmDialog
        open={confirmDelete}
        title={t('households.delete')}
        body={t('households.deleteConfirm', { name: household.name })}
        confirmLabel={t('common.delete')}
        pending={remove.isPending}
        onConfirm={() => void onDelete().catch(() => undefined)}
        onClose={() => setConfirmDelete(false)}
      />
    </div>
  )
}
