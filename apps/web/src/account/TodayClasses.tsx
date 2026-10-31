import { CheckCircle2, Clock } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { shortTime } from '@/classes/dates'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { apiErrorMessage } from '@/lib/apiErrors'
import { cn } from '@/lib/utils'

import { useCheckIn, useTodayClasses } from './portalApi'
import type { TodayClass } from './types'

export function TodayClasses({ studentId }: { studentId: string }) {
  const { t } = useTranslation()
  const { data: classes, isLoading, isError } = useTodayClasses(studentId)
  const checkIn = useCheckIn(studentId)

  return (
    <Card aria-labelledby="today-title">
      <CardHeader>
        <div>
          <CardTitle id="today-title">{t('account.today.title')}</CardTitle>
          <CardDescription>{t('account.today.hint')}</CardDescription>
        </div>
      </CardHeader>
      <CardContent className="grid gap-2.5">
        {checkIn.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, checkIn.error)}</div>}
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {isError && <div className="alert alert-error">{t('account.loadError')}</div>}
        {classes?.length === 0 && <p className="m-0 text-sm text-muted-foreground">{t('account.today.empty')}</p>}
        {classes && classes.length > 0 && (
          <ul className="m-0 grid list-none gap-2.5 p-0">
            {classes.map((c) => (
              <TodayRow key={c.classId} item={c} busy={checkIn.isPending}
                onToggle={() => checkIn.mutate({ classId: c.classId, undo: c.checkedIn })} />
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}

type RowProps = { item: TodayClass; busy: boolean; onToggle: () => void }

/** Presença marcada fica verde; check-in aberto ganha o tom da marca para chamar o toque. */
function rowTone(item: TodayClass): string {
  if (item.checkedIn) return 'border-success/30 bg-success/5'
  if (item.open) return 'border-primary-soft-border bg-primary-soft/40'
  return 'border-border'
}

function TodayRow({ item, busy, onToggle }: RowProps) {
  const { t } = useTranslation()
  const details = [item.location, item.instructor?.name].filter(Boolean)
  return (
    <li className={cn('flex flex-wrap items-center gap-x-4 gap-y-3 rounded-2xl border p-3.5 transition-colors', rowTone(item))}>
      <div className="w-16 shrink-0 text-center">
        <div className="font-heading text-3xl leading-none tracking-wide">{shortTime(item.startTime)}</div>
        {item.endTime && <div className="mt-1 text-xs text-muted-foreground">{shortTime(item.endTime)}</div>}
      </div>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2 font-semibold">
          {item.title}
          {item.extra && <Badge variant="outline">{t('account.today.extra')}</Badge>}
          {!item.enrolled && <Badge variant="outline">{t('account.today.walkIn')}</Badge>}
        </div>
        {details.length > 0 && <div className="mt-0.5 text-[13px] text-muted-foreground">{details.join(' · ')}</div>}
      </div>
      <TodayAction item={item} busy={busy} onToggle={onToggle} />
    </li>
  )
}

function TodayAction({ item, busy, onToggle }: RowProps) {
  const { t } = useTranslation()
  if (item.checkedIn) {
    return (
      <div className="flex items-center gap-2 max-sm:w-full max-sm:justify-between">
        <Badge variant="success" className="py-1 text-[13px]"><CheckCircle2 aria-hidden /> {t('account.today.present')}</Badge>
        {item.open && item.canUndo && (
          <Button variant="ghost" size="sm" disabled={busy} onClick={onToggle}>{t('account.today.undo')}</Button>
        )}
      </div>
    )
  }
  if (!item.open) {
    return (
      <span className="inline-flex items-center gap-1.5 text-[13px] text-muted-foreground max-sm:w-full">
        <Clock size={14} aria-hidden />
        {t('account.today.window', { from: shortTime(item.opensAt), to: shortTime(item.closesAt) })}
      </span>
    )
  }
  return (
    <Button disabled={busy} onClick={onToggle} className="max-sm:w-full">{t('account.today.checkIn')}</Button>
  )
}
