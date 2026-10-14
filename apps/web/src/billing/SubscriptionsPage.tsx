import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { ConfirmDialog } from '@/components/ConfirmDialog'
import { Dialog } from '@/components/Dialog'
import { apiErrorMessage } from '@/lib/apiErrors'
import { fold } from '@/lib/text'

import { useCancelSubscription, usePlans, useSubscriptions } from './billingApi'
import { SubscriptionForm } from './SubscriptionForm'
import { SubscriptionsTable } from './SubscriptionsTable'
import type { Subscription } from './types'

type Modal = { type: 'link' } | { type: 'edit'; s: Subscription } | { type: 'cancel'; s: Subscription } | null

export function SubscriptionsPage() {
  const { t } = useTranslation()
  const { data: subscriptions, isLoading, isError } = useSubscriptions()
  const { data: plans = [] } = usePlans()
  const cancel = useCancelSubscription()
  const [modal, setModal] = useState<Modal>(null)
  const [search, setSearch] = useState('')
  const close = () => {
    setModal(null)
    cancel.reset()
  }

  const hasActivePlan = plans.some((p) => p.active)
  const term = fold(search)
  const shown = (subscriptions ?? []).filter((s) => !term || fold(s.student.name).includes(term))

  async function onCancel(id: string) {
    await cancel.mutateAsync(id)
    close()
  }

  return (
    <div className="page page-wide">
      <div className="page-header">
        <div>
          <div className="eyebrow">{t('nav.groups.financeiro')}</div>
          <h1>{t('subscriptions.title')}</h1>
        </div>
        <div className="page-actions">
          <button type="button" className="btn btn-primary" disabled={!hasActivePlan}
            onClick={() => setModal({ type: 'link' })}>
            {t('subscriptions.link')}
          </button>
        </div>
      </div>

      <div className="stack">
        <p className="hint">{t('subscriptions.intro')}</p>
        {plans.length > 0 && !hasActivePlan && <div className="callout">{t('subscriptions.noActivePlan')}</div>}
        {!isLoading && plans.length === 0 && (
          <div className="callout">
            {t('subscriptions.noPlans')} <Link to="/mensalidades">{t('subscriptions.goToPlans')}</Link>
          </div>
        )}
        <div className="toolbar">
          <input type="search" className="input grow" value={search} onChange={(e) => setSearch(e.target.value)}
            placeholder={t('subscriptions.search')} aria-label={t('subscriptions.search')} />
          <span className="toolbar-count">{t('subscriptions.count', { count: shown.length })}</span>
        </div>
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {isError && <div className="alert alert-error">{t('subscriptions.loadError')}</div>}
        {subscriptions && shown.length === 0 && <div className="card empty-detail">{t('subscriptions.empty')}</div>}
        {shown.length > 0 && (
          <SubscriptionsTable subscriptions={shown} onEdit={(s) => setModal({ type: 'edit', s })}
            onCancel={(s) => setModal({ type: 'cancel', s })} />
        )}
      </div>

      <Dialog open={modal?.type === 'link' || modal?.type === 'edit'} onClose={close} wide
        title={modal?.type === 'edit' ? t('subscriptions.edit') : t('subscriptions.link')}>
        {(modal?.type === 'link' || modal?.type === 'edit') && (
          <SubscriptionForm key={modal.type === 'edit' ? modal.s.id : 'link'} plans={plans}
            subscription={modal.type === 'edit' ? modal.s : undefined}
            subscribedIds={(subscriptions ?? []).map((s) => s.student.id)} onSaved={close} onCancel={close} />
        )}
      </Dialog>
      <ConfirmDialog
        open={modal?.type === 'cancel'}
        title={t('subscriptions.cancel')}
        body={modal?.type === 'cancel' ? t('subscriptions.cancelConfirm', { name: modal.s.student.name }) : ''}
        confirmLabel={t('subscriptions.cancel')}
        pending={cancel.isPending}
        error={cancel.error ? apiErrorMessage(t, cancel.error) : null}
        onConfirm={() => modal?.type === 'cancel' && void onCancel(modal.s.id).catch(() => undefined)}
        onClose={close}
      />
    </div>
  )
}
