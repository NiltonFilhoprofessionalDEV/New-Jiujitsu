import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { Dialog } from '@/components/Dialog'
import type { ApproveRank, EnrollmentRequestItem } from '@/invites/types'
import { formText } from '@/lib/formText'
import { BeltFields } from '@/students/BeltFields'

type Props = {
  request: EnrollmentRequestItem
  pending: boolean
  error: string | null
  onClose: () => void
  onConfirm: (rank: ApproveRank) => void
}

/** O gestor confirma a faixa e a data antes de liberar o aluno. */
export function ApproveRequestDialog({ request, pending, error, onClose, onConfirm }: Props) {
  const { t } = useTranslation()
  const [belt, setBelt] = useState(request.belt ?? 'branca')
  const [degrees, setDegrees] = useState(request.degrees)
  const beginner = belt === 'branca' && degrees === 0

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    onConfirm({
      belt,
      degrees,
      beltAwardedAt: beginner ? null : formText(form, 'beltAwardedAt'),
      degreeAwardedAt: degrees > 0 ? formText(form, 'degreeAwardedAt') : null,
    })
  }

  return (
    <Dialog open title={t('requests.approveTitle', { name: request.name })} onClose={onClose}>
      <form className="form-grid" onSubmit={onSubmit}>
        <p className="hint span-2">{t('requests.approveHint')}</p>
        {error && <div className="alert alert-error span-2" role="alert">{error}</div>}
        <BeltFields idPrefix="ap" initialBelt={request.belt} initialDegrees={request.degrees}
          onChange={(nextBelt, nextDegrees) => { setBelt(nextBelt); setDegrees(nextDegrees) }} />
        {beginner ? (
          <p className="hint span-2">{t('students.form.whiteBeltClock')}</p>
        ) : (
          <div className="field-pair">
            <div className="field">
              <label htmlFor="ap-belt-at">{t('students.form.beltAwardedAt')}</label>
              <input id="ap-belt-at" name="beltAwardedAt" type="date" className="input" required />
              <span className="hint">{t('students.form.beltAwardedAtHint')}</span>
            </div>
            {degrees > 0 && (
              <div className="field">
                <label htmlFor="ap-degree-at">{t('students.form.degreeAwardedAt')}</label>
                <input id="ap-degree-at" name="degreeAwardedAt" type="date" className="input" required />
                <span className="hint">{t('students.form.degreeAwardedAtHint')}</span>
              </div>
            )}
          </div>
        )}
        <div className="form-actions span-2">
          <button type="button" className="btn" onClick={onClose}>{t('common.cancel')}</button>
          <button type="submit" className="btn btn-primary" disabled={pending}>
            {pending ? t('common.saving') : t('requests.approveSubmit')}
          </button>
        </div>
      </form>
    </Dialog>
  )
}
