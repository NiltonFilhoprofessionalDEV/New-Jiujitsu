import { useTranslation } from 'react-i18next'

import { PageHeader } from '@/components/PageHeader'
import { apiErrorMessage } from '@/lib/apiErrors'

import { PeriodPicker } from './PeriodPicker'
import { AbsentStudents, ClassesTable, TopStudents } from './ReportLists'
import { usePeriod, useReport } from './reportsApi'
import { AcademyStats, FinanceStats } from './ReportStats'

export function ReportsPage() {
  const { t } = useTranslation()
  const [period, setPeriod] = usePeriod()
  const { data: report, isLoading, error } = useReport(period)

  return (
    <div className="page page-wide">
      <PageHeader eyebrow={t('nav.groups.relatorios')} title={t('reports.title')} description={t('pages.reports')} />
      <div className="stack">
        <PeriodPicker value={period} onChange={setPeriod} />
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {error && <div className="alert alert-error">{apiErrorMessage(t, error)}</div>}
        {report && (
          <>
            <AcademyStats report={report} />
            {report.finance && <FinanceStats finance={report.finance} />}
            <ClassesTable rows={report.classes} />
            <div className="account-grid">
              <TopStudents rows={report.topStudents} />
              <AbsentStudents absent={report.absent} />
            </div>
          </>
        )}
      </div>
    </div>
  )
}
