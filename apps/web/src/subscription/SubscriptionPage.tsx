import { useTranslation } from 'react-i18next'

import { formatMoney } from '@/billing/money'
import { DataItem } from '@/components/DataItem'
import { PageHeader } from '@/components/PageHeader'

import { useAcademyAccess } from './accessApi'
import { CheckoutForm } from './CheckoutForm'
import { formatInstantDate } from './dates'
import type { AcademyAccess } from './types'

const CENTS_PER_REAL = 100

/** Situação da academia na plataforma e o pagamento da assinatura (só gestores). */
export function SubscriptionPage() {
  const { t } = useTranslation()
  const { data: access, isLoading, isError } = useAcademyAccess()
  return (
    <div className="page">
      <PageHeader eyebrow={t('common.yourAcademy')} title={t('subscription.title')}
        description={t('pages.subscription')} />
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {isError && <div className="alert alert-error">{t('subscription.loadError')}</div>}
      {access && (
        <div className="subscription-grid">
          <StatusCard access={access} />
          <section className="card plan-card" aria-labelledby="plan-title">
            <div className="eyebrow">{t('subscription.plan.name')}</div>
            <h2 id="plan-title" className="plan-price">
              {formatMoney(Math.round(access.monthlyPrice * CENTS_PER_REAL))}
              <span>{t('subscription.plan.perMonth')}</span>
            </h2>
            <p className="hint">{t('subscription.plan.includes')}</p>
            <CheckoutForm access={access} />
          </section>
        </div>
      )}
    </div>
  )
}

function StatusCard({ access }: { access: AcademyAccess }) {
  const { t } = useTranslation()
  const locked = access.status === 'GRACE' || access.status === 'SUSPENDED'
  return (
    <section className="card access-card" data-status={access.status} aria-labelledby="access-title">
      <span className="pill access-pill">{t(`subscription.status.${access.status}`)}</span>
      <h2 id="access-title">{t(`subscription.headline.${access.status}`)}</h2>
      {access.status !== 'SUSPENDED' && (
        <div className="access-days">
          <strong>{access.daysLeft}</strong>
          <span>{t(`subscription.daysLabel.${access.status}`, { count: access.daysLeft })}</span>
        </div>
      )}
      <dl className="data-list">
        <DataItem label={t('subscription.dates.trialEndsAt')} value={formatInstantDate(access.trialEndsAt)} />
        <DataItem label={t('subscription.dates.paidUntil')} value={formatInstantDate(access.paidUntil)} />
        {locked && <DataItem label={t('subscription.dates.suspendsAt')} value={formatInstantDate(access.suspendsAt)} />}
      </dl>
      <p className="hint">{t('subscription.studentsKeepAccess')}</p>
    </section>
  )
}
