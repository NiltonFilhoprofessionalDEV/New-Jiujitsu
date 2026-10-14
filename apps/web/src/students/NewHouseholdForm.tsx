import { X } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { apiErrorMessage } from '@/lib/apiErrors'

import { StudentPicker } from './StudentPicker'
import { useCreateHousehold } from './studentsApi'
import type { HouseholdDetail, StudentListItem } from './types'

type Props = {
  onCreated: (household: HouseholdDetail) => void
  onCancel: () => void
}

export function NewHouseholdForm({ onCreated, onCancel }: Props) {
  const { t } = useTranslation()
  const create = useCreateHousehold()
  const [name, setName] = useState('')
  const [members, setMembers] = useState<StudentListItem[]>([])
  const [responsibleId, setResponsibleId] = useState<string | null>(null)

  function add(student: StudentListItem) {
    setMembers((current) => [...current, student])
    if (!responsibleId && student.kind === 'RESPONSIBLE') setResponsibleId(student.id)
  }

  function remove(id: string) {
    setMembers((current) => current.filter((s) => s.id !== id))
    if (responsibleId === id) setResponsibleId(null)
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    const created = await create.mutateAsync({
      name: name.trim(),
      responsibleStudentId: responsibleId,
      memberIds: members.map((m) => m.id),
    })
    onCreated(created)
  }

  return (
    <form className="stack" onSubmit={(e) => void onSubmit(e).catch(() => undefined)}>
      {create.error && (
        <div className="alert alert-error" role="alert">
          {apiErrorMessage(t, create.error)}
        </div>
      )}
      <div className="field">
        <label htmlFor="hh-name">{t('households.name')}</label>
        <input id="hh-name" className="input" required maxLength={120} placeholder={t('households.namePlaceholder')}
          value={name} onChange={(e) => setName(e.target.value)} />
        <span className="hint">{t('households.nameHint')}</span>
      </div>

      <div>
        <div className="section-title">{t('households.members')}</div>
        {members.length === 0 ? (
          <p className="empty">{t('households.membersEmpty')}</p>
        ) : (
          <ul className="member-list">
            {members.map((m) => (
              <li key={m.id}>
                <label className="radio-inline">
                  <input type="radio" name="responsible" checked={responsibleId === m.id}
                    onChange={() => setResponsibleId(m.id)} />
                  {m.name}
                </label>
                <button type="button" className="icon-btn" aria-label={t('households.removeMember', { name: m.name })}
                  onClick={() => remove(m.id)}>
                  <X size={16} aria-hidden />
                </button>
              </li>
            ))}
          </ul>
        )}
        {members.length > 0 && <p className="hint">{t('households.responsibleHint')}</p>}
      </div>

      <StudentPicker actionLabel={() => t('common.add')} onPick={add} exclude={members.map((m) => m.id)} />

      <div className="form-actions">
        <button type="button" className="btn" onClick={onCancel}>
          {t('common.cancel')}
        </button>
        <button type="submit" className="btn btn-primary"
          disabled={create.isPending || name.trim() === '' || members.length === 0}>
          {t('households.create')}
        </button>
      </div>
    </form>
  )
}
