import { CheckCircle2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { shortTime, timeRange } from '@/classes/dates'
import { apiErrorMessage } from '@/lib/apiErrors'

import { useCheckIn, useTodayClasses } from './portalApi'
import type { TodayClass } from './types'

export function TodayClasses({ studentId }: { studentId: string }) {
  const { t } = useTranslation()
  const { data: classes, isLoading, isError } = useTodayClasses(studentId)
  const checkIn = useCheckIn(studentId)

  return (
    <section className="card stack" aria-labelledby="today-title">
      <div>
        <h3 id="today-title">{t('account.today.title')}</h3>
        <p className="hint">{t('account.today.hint')}</p>
      </div>
      {checkIn.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, checkIn.error)}</div>}
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {isError && <div className="alert alert-error">{t('account.loadError')}</div>}
      {classes?.length === 0 && <p className="empty">{t('account.today.empty')}</p>}
      {classes && classes.length > 0 && (
        <ul className="today-list">
          {classes.map((c) => (
            <TodayRow key={c.classId} item={c} busy={checkIn.isPending}
              onToggle={() => checkIn.mutate({ classId: c.classId, undo: c.checkedIn })} />
          ))}
        </ul>
      )}
    </section>
  )
}

type RowProps = { item: TodayClass; busy: boolean; onToggle: () => void }

function TodayRow({ item, busy, onToggle }: RowProps) {
  const { t } = useTranslation()
  const details = [timeRange(item.startTime, item.endTime), item.location, item.instructor?.name].filter(Boolean)
  return (
    <li className="today-row">
      <div>
        <div className="row-title">
          {item.title} {!item.enrolled && <span className="pill pill-outline">{t('account.today.walkIn')}</span>}
        </div>
        <div className="row-sub">{details.join(' · ')}</div>
      </div>
      <TodayAction item={item} busy={busy} onToggle={onToggle} />
    </li>
  )
}

function TodayAction({ item, busy, onToggle }: RowProps) {
  const { t } = useTranslation()
  if (item.checkedIn) {
    return (
      <div className="today-action">
        <span className="pill pill-success"><CheckCircle2 size={14} aria-hidden /> {t('account.today.present')}</span>
        {item.open && item.canUndo && (
          <button type="button" className="btn btn-sm" disabled={busy} onClick={onToggle}>
            {t('account.today.undo')}
          </button>
        )}
      </div>
    )
  }
  if (!item.open) {
    return <span className="hint">{t('account.today.window', { from: shortTime(item.opensAt), to: shortTime(item.closesAt) })}</span>
  }
  return (
    <button type="button" className="btn btn-primary btn-sm" disabled={busy} onClick={onToggle}>
      {t('account.today.checkIn')}
    </button>
  )
}
