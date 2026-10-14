import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { todayIso } from '@/classes/dates'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'
import { StudentPicker } from '@/students/StudentPicker'

import { useLinkSubscription, useUpdateSubscription } from './billingApi'
import { toTerms } from './terms'
import { TermsFields } from './TermsFields'
import type { Plan, Subscription } from './types'

type Props = {
  plans: Plan[]
  /** Editar; sem ela, o formulário vincula um aluno novo. */
  subscription?: Subscription
  /** Alunos que já têm mensalidade e não aparecem na busca. */
  subscribedIds: readonly string[]
  onSaved: () => void
  onCancel: () => void
}

type PickedStudent = { id: string; name: string }

export function SubscriptionForm({ plans, subscription, subscribedIds, onSaved, onCancel }: Props) {
  const { t } = useTranslation()
  const link = useLinkSubscription()
  const update = useUpdateSubscription()
  const [student, setStudent] = useState<PickedStudent | null>(subscription?.student ?? null)
  const [error, setError] = useState<string | null>(null)
  const pending = link.isPending || update.isPending

  if (!student) {
    return (
      <div className="stack">
        <p className="hint">{t('subscriptions.form.pickHint')}</p>
        <StudentPicker scope="billing" exclude={subscribedIds} actionLabel={() => t('subscriptions.form.pick')}
          onPick={(s) => setStudent({ id: s.id, name: s.name })} />
      </div>
    )
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!student) return
    setError(null)
    const form = new FormData(event.currentTarget)
    const terms = toTerms(form)
    try {
      if (subscription) {
        await update.mutateAsync({ id: subscription.id, terms })
      } else {
        await link.mutateAsync({ studentId: student.id, terms, firstDueDate: formText(form, 'firstDueDate') })
      }
      onSaved()
    } catch (err) {
      setError(apiErrorMessage(t, err))
    }
  }

  return (
    <form onSubmit={onSubmit} className="form-grid" noValidate>
      {error && <div className="alert alert-error span-2" role="alert">{error}</div>}
      <div className="span-2 picked-student">
        <strong>{student.name}</strong>
        {!subscription && (
          <button type="button" className="link-btn" onClick={() => setStudent(null)}>
            {t('subscriptions.form.changeStudent')}
          </button>
        )}
      </div>
      <TermsFields plans={plans} subscription={subscription} />
      {!subscription && (
        <div className="field">
          <label htmlFor="sf-first-due">{t('subscriptions.form.firstDue')}</label>
          <input id="sf-first-due" name="firstDueDate" type="date" className="input" min={todayIso()} />
        </div>
      )}
      <p className="hint span-2">
        {subscription ? t('subscriptions.form.changesHint') : t('subscriptions.form.issueHint')}
      </p>
      <div className="form-actions span-2">
        <button type="button" className="btn" onClick={onCancel}>{t('common.cancel')}</button>
        <button type="submit" className="btn btn-primary" disabled={pending}>
          {pending ? t('common.saving') : subscription ? t('common.save') : t('subscriptions.form.submitLink')}
        </button>
      </div>
    </form>
  )
}
