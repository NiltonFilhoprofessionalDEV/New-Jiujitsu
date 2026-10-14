import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { useReadyStudents } from '@/graduations/graduationsApi'
import { useHouseholds, useStudentCounts } from '@/students/studentsApi'

const STEPS = [
  { key: 'stepPlan', to: '/mensalidades' },
  { key: 'stepClass', to: '/aulas' },
  { key: 'stepStudent', to: '/alunos' },
] as const

export function HomePage() {
  const { t } = useTranslation()
  const { data: counts } = useStudentCounts({ q: '', status: 'active', age: 'all' })
  const { data: households } = useHouseholds()
  const { data: ready } = useReadyStudents()

  const stats = [
    { key: 'activeStudents', value: counts?.training, to: '/alunos?kind=training' },
    { key: 'responsibles', value: counts?.responsible, to: '/alunos?kind=responsible' },
    { key: 'families', value: households?.length, to: '/alunos' },
    { key: 'readyToGraduate', value: ready?.length, to: '/aulas/faixas' },
  ] as const

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <div className="eyebrow">{t('common.yourAcademy')}</div>
          <h1>{t('home.title')}</h1>
        </div>
        <div className="card" style={{ padding: 16, minWidth: 260 }}>
          <div className="section-title" style={{ margin: '0 0 10px' }}>
            {t('home.firstSteps')}
          </div>
          <div className="page-actions">
            {STEPS.map(({ key, to }) => (
              <Link key={key} to={to} className="btn">
                {t(`home.${key}`)}
              </Link>
            ))}
          </div>
        </div>
      </div>
      <div className="stat-grid">
        {stats.map(({ key, value, to }) => (
          <Link key={key} to={to} className="card stat stat-link">
            <strong>{value ?? '—'}</strong>
            <span>{t(`home.stats.${key}`)}</span>
          </Link>
        ))}
      </div>
    </div>
  )
}
