import { useTranslation } from 'react-i18next'

import { formatMoney } from '@/billing/money'

import type { Report } from './types'

type Stat = { key: string; value: string | number; label: string; tone?: string }

function StatGrid({ stats }: { stats: Stat[] }) {
  return (
    <div className="stat-grid">
      {stats.map(({ key, value, label, tone }) => (
        <div key={key} className={tone ? `card stat ${tone}` : 'card stat'}>
          <strong>{value}</strong>
          <span>{label}</span>
        </div>
      ))}
    </div>
  )
}

export function AcademyStats({ report }: { report: Report }) {
  const { t } = useTranslation()
  const { students, attendance } = report
  return (
    <StatGrid stats={[
      { key: 'active', value: students.active, label: t('reports.stats.active') },
      { key: 'new', value: students.newInPeriod, label: t('reports.stats.new') },
      { key: 'checkIns', value: attendance.checkIns, label: t('reports.stats.checkIns') },
      { key: 'trained', value: attendance.studentsTrained, label: t('reports.stats.trained') },
    ]} />
  )
}

export function FinanceStats({ finance }: { finance: NonNullable<Report['finance']> }) {
  const { t } = useTranslation()
  return (
    <section className="stack" aria-labelledby="finance-title">
      <h2 id="finance-title" className="section-title">{t('reports.finance.title')}</h2>
      <StatGrid stats={[
        { key: 'billed', value: formatMoney(finance.billed.cents),
          label: t('reports.finance.billed', { count: finance.billed.count }) },
        { key: 'received', value: formatMoney(finance.received.cents), tone: 'stat-paid',
          label: t('reports.finance.received', { count: finance.received.count }) },
        { key: 'overdue', value: formatMoney(finance.overdue.cents), tone: 'stat-overdue',
          label: t('reports.finance.overdue', { count: finance.overdue.students }) },
      ]} />
    </section>
  )
}
