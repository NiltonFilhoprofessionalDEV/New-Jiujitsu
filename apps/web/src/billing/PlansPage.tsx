import { Pencil } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { Dialog } from '@/components/Dialog'
import { PageTitle } from '@/components/PageTitle'

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
      <div className="page-header">
        <div>
          <div className="eyebrow">{t('common.yourAcademy')}</div>
          <PageTitle>{t('plans.title')}</PageTitle>
        </div>
        <div className="page-actions">
          <button type="button" className="btn btn-primary" onClick={() => setModal({ type: 'new' })}>
            {t('plans.new')}
          </button>
        </div>
      </div>

      <div className="stack">
        <p className="hint">{t('plans.intro')}</p>
        {isLoading && <p className="empty">{t('common.loading')}</p>}
        {isError && <div className="alert alert-error">{t('plans.loadError')}</div>}
        {plans && plans.length === 0 && <div className="card empty-detail">{t('plans.empty')}</div>}
        {plans && plans.length > 0 && <PlansTable plans={plans} onEdit={(plan) => setModal({ type: 'edit', plan })} />}
      </div>

      <Dialog open={modal !== null} onClose={close} wide
        title={modal?.type === 'edit' ? t('plans.edit') : t('plans.new')}>
        {modal && <PlanForm plan={modal.type === 'edit' ? modal.plan : undefined} onSaved={close} onCancel={close} />}
      </Dialog>
    </div>
  )
}

function PlansTable({ plans, onEdit }: { plans: Plan[]; onEdit: (plan: Plan) => void }) {
  const { t } = useTranslation()
  return (
    <div className="card table-card">
      <table className="table">
        <thead>
          <tr>
            <th scope="col">{t('plans.col.name')}</th>
            <th scope="col">{t('plans.col.price')}</th>
            <th scope="col" className="hide-narrow">{t('plans.col.dueDay')}</th>
            <th scope="col">{t('plans.col.subscribers')}</th>
            <th scope="col" className="col-actions">{t('plans.col.actions')}</th>
          </tr>
        </thead>
        <tbody>
          {plans.map((p) => (
            <tr key={p.id}>
              <td>
                <div className="row-title">{p.name}</div>
                {p.description && <div className="row-sub">{p.description}</div>}
                <span className="badges">
                  {p.isDefault && <span className="pill pill-soft">{t('plans.badge.default')}</span>}
                  {p.isKidsDefault && <span className="pill pill-soft">{t('plans.badge.kidsDefault')}</span>}
                  {!p.active && <span className="pill pill-muted">{t('plans.badge.inactive')}</span>}
                </span>
              </td>
              <td className="nowrap">
                <div className="row-title">{formatMoney(p.priceCents)}</div>
                <div className="row-sub">{t(`plans.interval.${p.intervalMonths}`)}</div>
              </td>
              <td className="hide-narrow nowrap">{t('plans.dayOf', { day: p.dueDay })}</td>
              <td>{p.subscribers}</td>
              <td className="col-actions">
                <button type="button" className="icon-btn" onClick={() => onEdit(p)}
                  aria-label={t('plans.editNamed', { name: p.name })} title={t('common.edit')}>
                  <Pencil size={16} aria-hidden />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
