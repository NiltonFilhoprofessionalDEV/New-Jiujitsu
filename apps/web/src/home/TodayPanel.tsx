import { ChevronRight } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { shortTime } from '@/classes/dates'
import type { DayClass } from '@/classes/types'

import { attendanceHref, minutesNow, planToday } from './schedule'

type Props = { classes: DayClass[] | undefined; now: Date; date: string }

/** "Agora no tatame" e "Próxima aula": o que o professor precisa abrir neste momento. */
export function TodayPanel({ classes, now, date }: Props) {
  const { t } = useTranslation()
  const { live, next } = planToday(classes ?? [], minutesNow(now))

  return (
    <section className="home-section" aria-labelledby="home-now">
      <div className="home-section-head">
        <h2 id="home-now" className="section-title">{t('home.now.title')}</h2>
        {live.length > 0 && <span className="live-tag">{t('home.now.live', { count: live.length })}</span>}
      </div>
      {classes === undefined && <div className="card home-placeholder" aria-busy="true" />}
      {classes && live.length === 0 && <div className="card home-empty">{t('home.now.empty')}</div>}
      {live.map((item) => (
        <LiveClass key={item.classId} item={item} date={date} />
      ))}
      {classes && <NextClass next={next} date={date} />}
    </section>
  )
}

function LiveClass({ item, date }: { item: DayClass; date: string }) {
  const { t } = useTranslation()
  return (
    <Link to={attendanceHref(date, item.classId)} className="card live-class">
      <span className="live-dot" aria-hidden />
      <span className="live-main">
        <strong>{item.title}</strong>
        <span className="hint">
          {t('home.now.since', { time: shortTime(item.startTime) })}
          {item.endTime && ` · ${t('home.now.until', { time: shortTime(item.endTime) })}`}
        </span>
      </span>
      <span className="live-count">
        <strong>{t('home.now.present', { count: item.present })}</strong>
        <span className="hint">{t('home.now.enrolled', { count: item.enrolled })}</span>
      </span>
      <ChevronRight size={18} aria-hidden className="live-chevron" />
    </Link>
  )
}

function NextClass({ next, date }: { next: DayClass | null; date: string }) {
  const { t } = useTranslation()
  if (!next) {
    return (
      <div className="card next-class">
        <div>
          <div className="next-label">{t('home.next.title')}</div>
          <p className="hint">{t('home.next.none')}</p>
        </div>
        <Link to="/aulas" className="btn">{t('home.next.seeClasses')}</Link>
      </div>
    )
  }
  return (
    <div className="card next-class">
      <div>
        <div className="next-label">{t('home.next.title')}</div>
        <strong className="next-title">{next.title}</strong>
        <p className="hint">
          {t('home.next.today', { time: shortTime(next.startTime) })}
          {next.instructor && ` · ${t('home.next.instructor', { name: next.instructor.name })}`}
        </p>
      </div>
      <Link to={attendanceHref(date, next.classId)} className="btn btn-primary">{t('home.next.open')}</Link>
    </div>
  )
}
