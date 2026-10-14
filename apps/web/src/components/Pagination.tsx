import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import type { Page } from '@/students/types'

/** Some sozinha quando há uma página só. */
export function Pagination({ page, onPage }: { page: Page<unknown>; onPage: (page: number) => void }) {
  const { t } = useTranslation()
  if (page.totalPages <= 1) return null
  const from = page.number * page.size + 1
  const to = page.number * page.size + page.content.length

  return (
    <nav className="pagination" aria-label={t('common.pagination')}>
      <span>{t('common.pageRange', { from, to, total: page.totalElements })}</span>
      <button type="button" className="icon-btn" disabled={page.number === 0} onClick={() => onPage(page.number - 1)}
        aria-label={t('common.previous')}>
        <ChevronLeft size={18} aria-hidden />
      </button>
      <button type="button" className="icon-btn" disabled={page.number + 1 >= page.totalPages}
        onClick={() => onPage(page.number + 1)} aria-label={t('common.next')}>
        <ChevronRight size={18} aria-hidden />
      </button>
    </nav>
  )
}
