import { useTranslation } from 'react-i18next'

import { PageTitle } from '@/components/PageTitle'
import { apiErrorMessage } from '@/lib/apiErrors'

import { perSession } from './average'
import { PeriodPicker } from './PeriodPicker'
import { usePeriod, useReport } from './reportsApi'
import type { InstructorRow } from './types'

export function InstructorsReportPage() {
  const { t } = useTranslation()
  const [period, setPeriod] = usePeriod()
  const { data: report, isLoading, error } = useReport(period)

  return (
    <div className="page page-wide">
      <div className="page-header">
        <div>
          <div className="eyebrow">{t('nav.groups.relatorios')}</div>
          <PageTitle>{t('reports.instructors.title')}</PageTitle>
        </div>
      </div>
      <div className="stack">
        <p className="hint">{t('reports.instructors.intro')}</p>
        <PeriodPicker value={period} onChange={setPeriod} />
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {error && <div className="alert alert-error">{apiErrorMessage(t, error)}</div>}
        {report && <InstructorsTable rows={report.instructors} />}
      </div>
    </div>
  )
}

function InstructorsTable({ rows }: { rows: InstructorRow[] }) {
  const { t } = useTranslation()
  return (
    <div className="card table-card">
      <table className="table">
        <thead>
          <tr>
            <th scope="col">{t('reports.instructors.col.name')}</th>
            <th scope="col" className="num">{t('reports.instructors.col.classes')}</th>
            <th scope="col" className="num">{t('reports.col.sessions')}</th>
            <th scope="col" className="num">{t('reports.col.checkIns')}</th>
            <th scope="col" className="num">{t('reports.col.average')}</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r.membershipId}>
              <td className="row-title">{r.name}</td>
              <td className="num">{r.classes}</td>
              <td className="num">{r.sessions}</td>
              <td className="num">{r.checkIns}</td>
              <td className="num">{perSession(r.checkIns, r.sessions)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
