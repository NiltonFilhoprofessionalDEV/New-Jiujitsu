import { CalendarCheck2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { timeRange } from '@/classes/dates'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { BeltBadge } from '@/students/BeltBadge'
import { formatDate } from '@/students/format'

import type { Overview } from './types'

const EMPTY_TEXT = 'm-0 text-sm text-muted-foreground'

export function ClassesCard({ overview }: { overview: Overview }) {
  const { t } = useTranslation()
  return (
    <Card aria-labelledby="classes-title">
      <CardHeader><CardTitle id="classes-title">{t('account.classes.title')}</CardTitle></CardHeader>
      <CardContent>
        {overview.classes.length === 0 && <p className={EMPTY_TEXT}>{t('account.classes.empty')}</p>}
        <ul className="m-0 grid list-none gap-2 p-0">
          {overview.classes.map((c) => (
            <li key={c.classId} className="rounded-xl bg-muted px-4 py-3">
              <div className="flex flex-wrap items-baseline justify-between gap-x-3">
                <span className="font-semibold">{c.title}</span>
                <span className="text-[13px] font-semibold text-brand">{timeRange(c.startTime, c.endTime)}</span>
              </div>
              <div className="mt-2 flex flex-wrap gap-1">
                {c.weekdays.map((d) => (
                  <span key={d} className="rounded-md bg-card px-1.5 py-0.5 text-[11px] font-bold tracking-wide text-muted-foreground uppercase">
                    {t(`weekdays.short.${d}`)}
                  </span>
                ))}
              </div>
              {(c.location || c.instructor) && (
                <div className="mt-2 text-[13px] text-muted-foreground">
                  {[c.location, c.instructor?.name].filter(Boolean).join(' · ')}
                </div>
              )}
            </li>
          ))}
        </ul>
      </CardContent>
    </Card>
  )
}

export function AttendanceCard({ overview }: { overview: Overview }) {
  const { t } = useTranslation()
  return (
    <Card aria-labelledby="attendance-title">
      <CardHeader>
        <div>
          <CardTitle id="attendance-title">{t('account.attendance.title')}</CardTitle>
          <CardDescription>{t('account.attendance.total', { count: overview.attendanceTotal })}</CardDescription>
        </div>
      </CardHeader>
      <CardContent>
        {overview.recentAttendance.length === 0 && <p className={EMPTY_TEXT}>{t('account.attendance.empty')}</p>}
        <ul className="m-0 grid list-none gap-1 p-0">
          {overview.recentAttendance.map((a) => (
            <li key={`${a.date}-${a.title}`} className="flex items-center gap-3 py-1.5">
              <span className="grid size-8 shrink-0 place-items-center rounded-full bg-success/12 text-success">
                <CalendarCheck2 size={15} aria-hidden />
              </span>
              <span className="min-w-0 flex-1 truncate text-sm font-medium">{a.title}</span>
              <span className="text-[13px] text-muted-foreground">{formatDate(a.date)}</span>
            </li>
          ))}
        </ul>
      </CardContent>
    </Card>
  )
}

/** Linha do tempo das graduações, da mais recente para a mais antiga. */
export function GraduationsCard({ overview }: { overview: Overview }) {
  const { t } = useTranslation()
  if (overview.graduations.length === 0) return null
  return (
    <Card aria-labelledby="graduations-title">
      <CardHeader><CardTitle id="graduations-title">{t('account.graduations.title')}</CardTitle></CardHeader>
      <CardContent>
        <ol className="m-0 grid list-none gap-4 border-l-2 border-border p-0 pl-5">
          {overview.graduations.map((g) => (
            <li key={`${g.graduatedOn}-${g.belt}-${g.degrees}`} className="relative">
              <span aria-hidden className="absolute top-1.5 -left-[27px] size-3 rounded-full border-2 border-card bg-pulse" />
              <div className="text-xs font-semibold text-muted-foreground">
                {formatDate(g.graduatedOn)} · {t(`graduation.kind.${g.kind}`)}
              </div>
              <div className="mt-1"><BeltBadge belt={g.belt} degrees={g.degrees} /></div>
            </li>
          ))}
        </ol>
      </CardContent>
    </Card>
  )
}
