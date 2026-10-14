import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import type { DayClass } from '@/classes/types'
import { useReadyStudents } from '@/graduations/graduationsApi'
import { useStudentCounts } from '@/students/studentsApi'

const ACTIVE_STUDENTS = { q: '', status: 'active', age: 'all' } as const

export function Overview({ classes }: { classes: DayClass[] | undefined }) {
  const { t } = useTranslation()
  const { data: counts } = useStudentCounts(ACTIVE_STUDENTS)
  const { data: ready } = useReadyStudents()
  const presentToday = classes?.reduce((sum, c) => sum + c.present, 0)

  const stats = [
    { key: 'activeStudents', value: counts?.training, to: '/alunos?kind=training' },
    { key: 'presentToday', value: presentToday, to: '/aulas/presenca' },
    { key: 'classesToday', value: classes?.length, to: '/aulas/presenca' },
    { key: 'readyToGraduate', value: ready?.length, to: '/aulas/faixas' },
  ] as const

  return (
    <section className="home-section card overview" aria-labelledby="home-overview">
      <h2 id="home-overview" className="section-title">{t('home.overview')}</h2>
      <div className="overview-grid">
        {stats.map(({ key, value, to }) => (
          <Link key={key} to={to} className="stat stat-link overview-stat">
            <span>{t(`home.stats.${key}`)}</span>
            <strong>{value ?? '—'}</strong>
          </Link>
        ))}
      </div>
    </section>
  )
}
