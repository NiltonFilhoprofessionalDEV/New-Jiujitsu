import { Pencil, Plus } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { Dialog } from '@/components/Dialog'
import { HelpNote } from '@/components/HelpNote'
import { PageHeader } from '@/components/PageHeader'

import { usePlans } from './billingApi'
import { formatMoney } from './money'
import { PlanForm } from './PlanForm'
import type { Plan } from './types'

type Modal = { type: 'new' } | { type: 'edit'; plan: Plan } | null

export function PlansPage() {
  const { t } = useTranslation()
  const { data: plans, isLoading, isError } = usePlans()
  const [modal, setModal] = useState<Modal>(null)
  const close = () => setModal(null)

  return (
    <div className="page page-wide">
      <PageHeader eyebrow={t('nav.groups.financeiro')} title={t('plans.title')} description={t('pages.plans')}
        actions={(
          <button type="button" className="btn btn-primary" onClick={() => setModal({ type: 'new' })}>
            <Plus size={16} aria-hidden /> {t('plans.new')}
          </button>
        )} />

      <div className="stack">
        <HelpNote><p>{t('plans.intro')}</p></HelpNote>
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {isError && <div className="alert alert-error">{t('plans.loadError')}</div>}
        {plans && plans.length === 0 && <div className="card empty-detail">{t('plans.empty')}</div>}
        {plans && plans.length > 0 && (
          <ul className="plan-grid">
            {plans.map((p) => <PlanCard key={p.id} plan={p} onEdit={() => setModal({ type: 'edit', plan: p })} />)}
          </ul>
        )}
      </div>

      <Dialog open={modal !== null} onClose={close} wide
        title={modal?.type === 'edit' ? t('plans.edit') : t('plans.new')}>
        {modal && <PlanForm plan={modal.type === 'edit' ? modal.plan : undefined} onSaved={close} onCancel={close} />}
      </Dialog>
    </div>
  )
}

function PlanCard({ plan, onEdit }: { plan: Plan; onEdit: () => void }) {
  const { t } = useTranslation()
  return (
    <li className={plan.active ? 'card plan-card' : 'card plan-card inactive'}>
      <header className="plan-card-head">
        <div className="plan-card-title">
          <h3>{plan.name}</h3>
          <span className="badges">
            {plan.isDefault && <span className="pill pill-soft">{t('plans.badge.default')}</span>}
            {plan.isKidsDefault && <span className="pill pill-soft">{t('plans.badge.kidsDefault')}</span>}
            {!plan.active && <span className="pill pill-muted">{t('plans.badge.inactive')}</span>}
          </span>
        </div>
        <button type="button" className="btn btn-sm btn-icon" onClick={onEdit}
          aria-label={t('plans.editNamed', { name: plan.name })} title={t('common.edit')}>
          <Pencil size={15} aria-hidden />
        </button>
      </header>
      <p className="plan-price">
        <strong>{formatMoney(plan.priceCents)}</strong>
        <span>{t(`plans.interval.${plan.intervalMonths}`)}</span>
      </p>
      {plan.description && <p className="hint">{plan.description}</p>}
      <p className="row-sub">
        {t('plans.dayOf', { day: plan.dueDay })} · {t('plans.subscribers', { count: plan.subscribers })}
      </p>
    </li>
  )
}
