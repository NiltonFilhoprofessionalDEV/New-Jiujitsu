import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { addDays, dayOfMonth, isoWeekday, todayIso, weekOf } from './dates'
import type { Weekday } from './types'

type Props = {
  date: string
  /** Dias da semana com ao menos uma aula ativa, para marcar na faixa. */
  classDays: ReadonlySet<Weekday>
  onSelect: (date: string) => void
}

const DAYS_IN_WEEK = 7

export function WeekStrip({ date, classDays, onSelect }: Props) {
  const { t } = useTranslation()
  const today = todayIso()

  return (
    <div className="week-strip card">
      <div className="week-nav">
        <button type="button" className="icon-btn" aria-label={t('attendance.prevWeek')}
          onClick={() => onSelect(addDays(date, -DAYS_IN_WEEK))}>
          <ChevronLeft size={18} aria-hidden />
        </button>
        <button type="button" className="btn btn-sm" disabled={date === today} onClick={() => onSelect(today)}>
          {t('attendance.today')}
        </button>
        <button type="button" className="icon-btn" aria-label={t('attendance.nextWeek')}
          onClick={() => onSelect(addDays(date, DAYS_IN_WEEK))}>
          <ChevronRight size={18} aria-hidden />
        </button>
      </div>
      <div className="week-days" role="group" aria-label={t('attendance.weekAria')}>
        {weekOf(date).map((d) => {
          const weekday = isoWeekday(d)
          const classes = ['week-day', d === date && 'active', d === today && 'today'].filter(Boolean).join(' ')
          return (
            <button key={d} type="button" className={classes} aria-pressed={d === date} onClick={() => onSelect(d)}
              aria-label={t('attendance.dayAria', { weekday: t(`weekdays.long.${weekday}`), day: dayOfMonth(d) })}>
              <span className="week-day-name">{t(`weekdays.short.${weekday}`)}</span>
              <strong>{dayOfMonth(d)}</strong>
              <span className={classDays.has(weekday) ? 'week-dot' : 'week-dot empty-dot'} aria-hidden />
            </button>
          )
        })}
      </div>
    </div>
  )
}
