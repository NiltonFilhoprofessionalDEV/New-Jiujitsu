import { useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'

import { Dialog } from '@/components/Dialog'
import { apiErrorMessage } from '@/lib/apiErrors'

import { useClasses, useDayClasses, useOpenExtraSession } from './classesApi'
import { isoWeekday, timeRange } from './dates'
import type { TrainingClass } from './types'

type Props = { date: string; onOpened: (classId: string) => void }

/** Lista turmas que não estão na grade do dia para o professor abrir uma aula extra. */
export function CreateExtraClass({ date, onOpened }: Props) {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)
  const { data: allClasses } = useClasses()
  const { data: dayClasses } = useDayClasses(date)
  const openExtra = useOpenExtraSession(date)

  const available = useMemo(() => {
    const weekday = isoWeekday(date)
    const already = new Set((dayClasses ?? []).map((c) => c.classId))
    return (allClasses ?? []).filter((c) => !c.weekdays.includes(weekday) && !already.has(c.id))
  }, [allClasses, date, dayClasses])

  if (available.length === 0) return null

  async function pick(trainingClass: TrainingClass) {
    try {
      await openExtra.mutateAsync(trainingClass.id)
      setOpen(false)
      onOpened(trainingClass.id)
    } catch {
      /* erro mostrado abaixo */
    }
  }

  return (
    <>
      <button type="button" className="btn btn-sm create-extra-btn" onClick={() => setOpen(true)}>
        {t('attendance.createExtra')}
      </button>
      <Dialog open={open} onClose={() => setOpen(false)} title={t('attendance.createExtraTitle')}>
        <div className="stack">
          <p className="hint">{t('attendance.createExtraHint')}</p>
          {openExtra.error && (
            <div className="alert alert-error" role="alert">{apiErrorMessage(t, openExtra.error)}</div>
          )}
          <ul className="extra-class-options">
            {available.map((c) => (
              <li key={c.id}>
                <button type="button" className="card day-class" disabled={openExtra.isPending}
                  onClick={() => void pick(c)}>
                  <span className="day-class-time">{timeRange(c.startTime, c.endTime)}</span>
                  <span className="row-title">{c.title}</span>
                  <span className="row-sub">
                    {[c.instructor?.name, c.location].filter(Boolean).join(' · ')
                      || t('classes.form.noInstructor')}
                  </span>
                </button>
              </li>
            ))}
          </ul>
        </div>
      </Dialog>
    </>
  )
}
