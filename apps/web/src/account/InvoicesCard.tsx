import { useTranslation } from 'react-i18next'

import { formatMoney } from '@/billing/money'
import type { InvoiceSituation } from '@/billing/types'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { formatDate } from '@/students/format'

import { usePortalInvoices } from './portalApi'

const SITUATION_BADGE: Record<InvoiceSituation, 'outline' | 'soft' | 'success'> = {
  OPEN: 'outline',
  OVERDUE: 'soft',
  PAID: 'success',
  CANCELED: 'outline',
}

export function InvoicesCard({ studentId }: { studentId: string }) {
  const { t } = useTranslation()
  const { data: invoices, isLoading, isError } = usePortalInvoices(studentId)
  return (
    <Card aria-labelledby="invoices-title">
      <CardHeader>
        <div>
          <CardTitle id="invoices-title">{t('account.invoices.title')}</CardTitle>
          <CardDescription>{t('account.invoices.hint')}</CardDescription>
        </div>
      </CardHeader>
      <CardContent>
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {isError && <div className="alert alert-error">{t('account.loadError')}</div>}
        {invoices?.length === 0 && <p className="m-0 text-sm text-muted-foreground">{t('account.invoices.empty')}</p>}
        {invoices && invoices.length > 0 && (
          <ul className="m-0 grid list-none divide-y divide-border p-0">
            {invoices.map((i) => (
              <li key={i.id} className="flex items-center justify-between gap-3 py-3 first:pt-0 last:pb-0">
                <div className="min-w-0">
                  <div className="truncate text-sm font-semibold">{i.description}</div>
                  <div className="text-[13px] text-muted-foreground">
                    {i.paidOn
                      ? t('account.invoices.paidOn', { date: formatDate(i.paidOn) })
                      : t('account.invoices.dueOn', { date: formatDate(i.dueDate) })}
                  </div>
                </div>
                <div className="flex shrink-0 flex-col items-end gap-1">
                  <strong className="text-[15px] tabular-nums">{formatMoney(i.amountCents)}</strong>
                  <Badge variant={SITUATION_BADGE[i.situation]}>{t(`invoices.situation.${i.situation}`)}</Badge>
                </div>
              </li>
            ))}
          </ul>
        )}
      </CardContent>
    </Card>
  )
}
