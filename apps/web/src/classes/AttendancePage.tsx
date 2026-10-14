import { useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'

import { Dialog } from '@/components/Dialog'
import { PageHeader } from '@/components/PageHeader'
import { COMPACT_LAYOUT, useMediaQuery } from '@/lib/useMediaQuery'

import { AttendancePanel } from './AttendancePanel'
import { useClasses } from './classesApi'
import { DayClassList } from './DayClassList'
import type { Weekday } from './types'
import { useAttendanceParams } from './useAttendanceParams'
import { WeekStrip } from './WeekStrip'

export function AttendancePage() {
  const { t } = useTranslation()
  const { date, classId, selectDate, selectClass, closeClass } = useAttendanceParams()
  const { data: classes } = useClasses()
  const classDays = useMemo(() => new Set<Weekday>((classes ?? []).flatMap((c) => c.weekdays)), [classes])
  const compact = useMediaQuery(COMPACT_LAYOUT)
  const [savedTitle, setSavedTitle] = useState<string | null>(null)

  function pickDate(next: string) {
    setSavedTitle(null)
    selectDate(next)
  }

  function pickClass(id: string) {
    setSavedTitle(null)
    selectClass(id)
  }

  function onSaved(title: string) {
    setSavedTitle(title)
    closeClass()
  }

  const panel = classId && <AttendancePanel key={`${classId}-${date}`} classId={classId} date={date} onSaved={onSaved} />

  return (
    <div className="page page-wide">
      <PageHeader eyebrow={t('common.yourAcademy')} title={t('classes.tabs.attendance')} description={t('pages.attendance')} />
      <WeekStrip date={date} classDays={classDays} onSelect={pickDate} />
      {savedTitle && (
        <div className="alert alert-success attendance-saved" role="status">
          {t('attendance.saved', { title: savedTitle })}
        </div>
      )}
      <div className="split attendance-split">
        <DayClassList date={date} selectedId={classId} onSelect={pickClass} />
        {!compact && (
          <section className="stack" aria-labelledby="attendance-column-title">
            <h2 id="attendance-column-title" className="section-title">{t('attendance.title')}</h2>
            {panel ? <div className="card">{panel}</div> : <div className="card empty-detail">{t('attendance.pickClass')}</div>}
          </section>
        )}
      </div>
      {compact && (
        <Dialog open={Boolean(classId)} onClose={closeClass} title={t('attendance.title')} wide>
          {panel}
        </Dialog>
      )}
    </div>
  )
}
