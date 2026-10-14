import { useTranslation } from 'react-i18next'

import { Pagination } from '@/components/Pagination'

import { BeltBadge } from './BeltBadge'
import { formatDate } from './format'
import { StudentBadges } from './StudentBadges'
import type { Page, StudentListItem } from './types'

type Props = {
  page: Page<StudentListItem>
  selectedId: string | null
  onSelect: (id: string) => void
  onPage: (page: number) => void
}

export function StudentTable({ page, selectedId, onSelect, onPage }: Props) {
  const { t } = useTranslation()

  return (
    <div className="card table-card">
      <table className="table">
        <thead>
          <tr>
            <th scope="col">{t('students.col.name')}</th>
            <th scope="col">{t('students.col.belt')}</th>
            <th scope="col" className="hide-narrow">{t('students.col.enrollment')}</th>
          </tr>
        </thead>
        <tbody>
          {page.content.map((s) => (
            <tr key={s.id} className={s.id === selectedId ? 'selected' : undefined} aria-selected={s.id === selectedId}>
              <td>
                <button type="button" className="row-link" onClick={() => onSelect(s.id)}>
                  <span className="row-title">
                    {s.nickname ? t('students.nameWithNickname', { name: s.name, nickname: s.nickname }) : s.name}
                  </span>
                  <span className="row-sub">
                    {s.household ? t('students.familyName', { name: s.household.name }) : (s.email ?? t('students.noEmail'))}
                  </span>
                </button>
                <StudentBadges student={s} />
              </td>
              <td>{s.kind === 'TRAINING' ? <BeltBadge belt={s.belt} degrees={s.degrees} /> : <span className="empty">—</span>}</td>
              <td className="hide-narrow">{formatDate(s.enrollmentDate)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pagination page={page} onPage={onPage} />
    </div>
  )
}
