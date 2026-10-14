import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { apiErrorMessage } from '@/lib/apiErrors'
import { BeltBadge } from '@/students/BeltBadge'
import { StudentPicker } from '@/students/StudentPicker'
import type { StudentListItem } from '@/students/types'

import { useSaveAttendance } from './classesApi'
import type { AttendanceSession, RosterEntry } from './types'

const MAX_NOTES = 2000

function walkInEntry(s: StudentListItem): RosterEntry {
  return {
    student: { id: s.id, name: s.name, nickname: s.nickname, belt: s.belt, degrees: s.degrees, status: s.status },
    enrolled: false,
    present: true,
  }
}

type Props = {
  classId: string
  date: string
  session: AttendanceSession
  /** Depois de salvar, quem abriu a chamada a fecha. */
  onSaved: () => void
}

export function RosterForm({ classId, date, session, onSaved }: Props) {
  const { t } = useTranslation()
  const save = useSaveAttendance(classId, date)
  const [roster, setRoster] = useState(session.roster)
  const [present, setPresent] = useState(
    () => new Set(session.roster.filter((e) => e.present).map((e) => e.student.id)),
  )
  const [notes, setNotes] = useState(session.notes ?? '')
  const [adding, setAdding] = useState(false)
  const { canMark } = session

  /** Editar depois de uma falha tira o aviso de erro. */
  function markEdited() {
    if (save.isError) save.reset()
  }

  function toggle(id: string) {
    markEdited()
    setPresent((current) => {
      const next = new Set(current)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  function addWalkIn(s: StudentListItem) {
    markEdited()
    setRoster((current) => [...current, walkInEntry(s)])
    setPresent((current) => new Set(current).add(s.id))
  }

  const onSave = () =>
    save.mutate({ presentStudentIds: [...present], notes: notes.trim() || null }, { onSuccess: onSaved })

  return (
    <div className="stack">
      {save.error && (
        <div className="alert alert-error" role="alert">
          {apiErrorMessage(t, save.error)}
        </div>
      )}
      {!canMark && <p className="hint">{t('attendance.futureHint')}</p>}

      <div className="field">
        <label htmlFor="attendance-notes">{t('attendance.notes')}</label>
        <textarea id="attendance-notes" className="input" rows={2} maxLength={MAX_NOTES} value={notes}
          placeholder={t('attendance.notesPlaceholder')} onChange={(e) => {
            markEdited()
            setNotes(e.target.value)
          }} />
      </div>

      <div>
        <div className="roster-head">
          <span className="section-title">
            {t('attendance.studentsCount', { present: present.size, total: roster.length })}
          </span>
          {canMark && (
            <button type="button" className="btn btn-sm" aria-expanded={adding} onClick={() => setAdding((v) => !v)}>
              {t('attendance.addStudent')}
            </button>
          )}
        </div>
        {adding && (
          <StudentPicker scope="training" exclude={roster.map((e) => e.student.id)}
            actionLabel={() => t('attendance.markPresent')} onPick={addWalkIn} />
        )}
        {roster.length === 0 && <p className="empty">{t('attendance.noStudents')}</p>}
        <ul className="roster">
          {roster.map(({ student, enrolled }) => (
            <li key={student.id}>
              <label className="checkbox-inline">
                <input type="checkbox" checked={present.has(student.id)} disabled={!canMark}
                  onChange={() => toggle(student.id)} />
                <span>{student.name}</span>
              </label>
              {!enrolled && <span className="pill pill-outline">{t('attendance.walkIn')}</span>}
              <BeltBadge belt={student.belt} degrees={student.degrees} />
            </li>
          ))}
        </ul>
      </div>

      <div className="form-actions sheet-footer">
        <button type="button" className="btn btn-primary" disabled={save.isPending} onClick={onSave}>
          {save.isPending ? t('common.saving') : t('attendance.save')}
        </button>
      </div>
    </div>
  )
}
