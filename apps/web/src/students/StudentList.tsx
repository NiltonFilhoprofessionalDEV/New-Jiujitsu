import { useTranslation } from 'react-i18next'

import { Pagination } from '@/components/Pagination'
import { initials } from '@/lib/text'

import { BeltBadge } from './BeltBadge'
import { StudentBadges } from './StudentBadges'
import type { Page, StudentListItem } from './types'

type Props = {
  page: Page<StudentListItem>
  selectedId: string | null
  onSelect: (id: string) => void
  onPage: (page: number) => void
}

/** Uma linha de contexto só: a família (com o papel nela) ou, sem família, o e-mail. */
function useContextLine() {
  const { t } = useTranslation()
  return (s: StudentListItem) => {
    if (!s.household) return s.email ?? t('students.noEmail')
    const family = t('students.familyName', { name: s.household.name })
    if (s.householdResponsible || s.kind === 'RESPONSIBLE') return `${family} · ${t('students.badge.guardian')}`
    return s.email ? family : `${family} · ${t('students.badge.dependent')}`
  }
}

export function StudentList({ page, selectedId, onSelect, onPage }: Props) {
  const { t } = useTranslation()
  const contextLine = useContextLine()

  return (
    <div className="card list-card">
      <ul className="people-list">
        {page.content.map((s) => (
          <li key={s.id}>
            <button type="button" className="person-row" aria-current={s.id === selectedId || undefined}
              onClick={() => onSelect(s.id)}>
              <span className="avatar" aria-hidden>{initials(s.name)}</span>
              <span className="person-main">
                <span className="person-name">
                  <span className="row-title">{s.name}</span>
                  {s.nickname && <span className="person-nick">“{s.nickname}”</span>}
                  <StudentBadges student={s} />
                </span>
                <span className="row-sub">{contextLine(s)}</span>
              </span>
              {s.kind === 'TRAINING' && (
                <span className="person-belt"><BeltBadge belt={s.belt} degrees={s.degrees} /></span>
              )}
            </button>
          </li>
        ))}
      </ul>
      <Pagination page={page} onPage={onPage} />
      <span className="sr-only">{t('students.countOf', { shown: page.content.length, total: page.totalElements })}</span>
    </div>
  )
}
