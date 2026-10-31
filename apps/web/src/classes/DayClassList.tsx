import { useTranslation } from 'react-i18next'

import { useDayClasses } from './classesApi'
import { CreateExtraClass } from './CreateExtraClass'
import { timeRange } from './dates'

type Props = {
  date: string
  selectedId: string | null
  onSelect: (classId: string) => void
}

export function DayClassList({ date, selectedId, onSelect }: Props) {
  const { t } = useTranslation()
  const { data: classes, isLoading, isError } = useDayClasses(date)

  return (
    <section className="stack day-classes" aria-labelledby="day-classes-title">
      <div className="day-classes-head">
        <h2 id="day-classes-title" className="section-title">{t('attendance.dayClasses')}</h2>
        <CreateExtraClass date={date} onOpened={onSelect} />
      </div>
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {isError && <div className="alert alert-error">{t('attendance.dayError')}</div>}
      {classes?.length === 0 && <div className="card empty-detail">{t('attendance.noClassesOnDay')}</div>}
      {classes?.map((c) => (
        <button key={c.classId} type="button" aria-pressed={c.classId === selectedId}
          className={c.classId === selectedId ? 'card day-class active' : 'card day-class'}
          onClick={() => onSelect(c.classId)}>
          <span className="day-class-time">{timeRange(c.startTime, c.endTime)}</span>
          <span className="row-title">
            {c.title}
            {c.extra && <span className="pill pill-outline">{t('attendance.extra')}</span>}
          </span>
          <span className="row-sub">
            {[c.instructor?.name, c.location].filter(Boolean).join(' · ') || t('classes.form.noInstructor')}
          </span>
          <span className="pill pill-soft">
            {t('attendance.presentOf', { present: c.present, total: c.enrolled })}
          </span>
        </button>
      ))}
    </section>
  )
}
