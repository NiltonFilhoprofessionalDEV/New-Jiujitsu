import { useMemo } from 'react'
import { useTranslation } from 'react-i18next'

import { AttendancePanel } from './AttendancePanel'
import { ClassesHeader } from './ClassesHeader'
import { useClasses } from './classesApi'
import { DayClassList } from './DayClassList'
import type { Weekday } from './types'
import { useAttendanceParams } from './useAttendanceParams'
import { WeekStrip } from './WeekStrip'

export function AttendancePage() {
  const { t } = useTranslation()
  const { date, classId, selectDate, selectClass } = useAttendanceParams()
  const { data: classes } = useClasses()
  const classDays = useMemo(() => new Set<Weekday>((classes ?? []).flatMap((c) => c.weekdays)), [classes])

  return (
    <div className="page page-wide">
      <ClassesHeader />
      <WeekStrip date={date} classDays={classDays} onSelect={selectDate} />
      <div className="split attendance-split">
        <DayClassList date={date} selectedId={classId} onSelect={selectClass} />
        <div>
          {classId ? (
            <AttendancePanel key={`${classId}-${date}`} classId={classId} date={date} />
          ) : (
            <div className="card empty-detail">{t('attendance.pickClass')}</div>
          )}
        </div>
      </div>
    </div>
  )
}
