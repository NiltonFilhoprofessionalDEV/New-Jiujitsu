import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { useDebouncedValue } from '@/lib/useDebouncedValue'

import { useStudentsPage } from './studentsApi'
import type { StudentListItem, StudentQuery } from './types'

const PICKER_SIZE = 50

/**
 * {@code household}: alunos sem família. {@code training}: alunos ativos que treinam (aulas e chamada).
 * {@code billing}: o mesmo filtro de {@code training}, com textos do financeiro.
 */
export type PickerScope = 'household' | 'training' | 'billing'

const SCOPES: Record<PickerScope, { filter: Pick<StudentQuery, 'status' | 'kind' | 'household'>; textKey: string }> = {
  household: { filter: { status: 'all', kind: 'all', household: 'none' }, textKey: 'households' },
  training: { filter: { status: 'active', kind: 'training' }, textKey: 'classes.picker' },
  billing: { filter: { status: 'active', kind: 'training' }, textKey: 'subscriptions.picker' },
}

type Props = {
  /** Rótulo do botão de cada linha (ex.: "Adicionar"). */
  actionLabel: (student: StudentListItem) => string
  onPick: (student: StudentListItem) => void
  scope?: PickerScope
  exclude?: readonly string[]
  disabled?: boolean
}

export function StudentPicker({ actionLabel, onPick, scope = 'household', exclude = [], disabled }: Props) {
  const { t } = useTranslation()
  const [q, setQ] = useState('')
  const debouncedQ = useDebouncedValue(q)
  const { filter, textKey } = SCOPES[scope]
  const { data, isLoading } = useStudentsPage({ ...filter, q: debouncedQ, age: 'all', page: 0, size: PICKER_SIZE })
  const options = (data?.content ?? []).filter((s) => !exclude.includes(s.id))
  const placeholder = t(`${textKey}.searchStudent`)

  return (
    <div className="picker">
      <input
        type="search"
        className="input"
        placeholder={placeholder}
        aria-label={placeholder}
        value={q}
        onChange={(e) => setQ(e.target.value)}
      />
      <ul className="picker-list">
        {isLoading && <li className="empty">{t('common.loading')}</li>}
        {!isLoading && options.length === 0 && <li className="empty">{t(`${textKey}.noAvailable`)}</li>}
        {options.map((s) => (
          <li key={s.id}>
            <span>
              {s.name}
              {s.kind === 'RESPONSIBLE' && <span className="empty"> · {t('students.badge.guardian')}</span>}
            </span>
            <button type="button" className="btn btn-sm" disabled={disabled} onClick={() => onPick(s)}>
              {actionLabel(s)}
            </button>
          </li>
        ))}
      </ul>
    </div>
  )
}
