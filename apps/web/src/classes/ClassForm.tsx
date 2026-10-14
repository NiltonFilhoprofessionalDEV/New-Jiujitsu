import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { problemOf } from '@/lib/api'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import { useSaveClass, useStaff } from './classesApi'
import { shortTime, WEEKDAYS } from './dates'
import type { ClassRequest, TrainingClass, Weekday } from './types'

type Props = {
  trainingClass?: TrainingClass
  onSaved: (saved: TrainingClass) => void
  onCancel: () => void
}

const MAX_CAPACITY = 1000

function toRequest(form: FormData): ClassRequest {
  const capacity = formText(form, 'capacity')
  return {
    title: formText(form, 'title') ?? '',
    description: formText(form, 'description'),
    weekdays: form.getAll('weekdays').map(Number) as Weekday[],
    startTime: formText(form, 'startTime') ?? '',
    endTime: formText(form, 'endTime'),
    location: formText(form, 'location'),
    instructorMembershipId: formText(form, 'instructor'),
    capacity: capacity === null ? null : Number(capacity),
  }
}

export function ClassForm({ trainingClass: c, onSaved, onCancel }: Props) {
  const { t } = useTranslation()
  const save = useSaveClass()
  const { data: staff = [], isLoading: staffLoading } = useStaff()
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  // O select de professor não é controlado: as opções precisam existir no mount para o defaultValue valer.
  if (staffLoading) return <p className="empty">{t('common.loading')}</p>

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setFieldErrors({})
    const body = toRequest(new FormData(event.currentTarget))
    if (body.weekdays.length === 0) {
      setFieldErrors({ weekdays: t('classes.form.weekdaysRequired') })
      return
    }
    try {
      onSaved(await save.mutateAsync({ id: c?.id, body }))
    } catch (err) {
      setError(apiErrorMessage(t, err))
      setFieldErrors(problemOf(err).fields ?? {})
    }
  }

  const fieldError = (name: string) =>
    fieldErrors[name] && <span className="field-error">{fieldErrors[name]}</span>

  return (
    <form onSubmit={onSubmit} className="form-grid" noValidate>
      {error && (
        <div className="alert alert-error span-2" role="alert">
          {error}
        </div>
      )}
      <div className="field span-2">
        <label htmlFor="cf-title">{t('classes.form.title')}</label>
        <input id="cf-title" name="title" className="input" required maxLength={80} defaultValue={c?.title}
          placeholder={t('classes.form.titlePlaceholder')} />
        {fieldError('title')}
      </div>
      <div className="field span-2">
        <label htmlFor="cf-description">{t('classes.form.description')}</label>
        <textarea id="cf-description" name="description" className="input" rows={2} maxLength={500}
          defaultValue={c?.description ?? ''} />
      </div>
      <fieldset className="field span-2 weekday-field">
        <legend>{t('classes.form.weekdays')}</legend>
        <div className="weekday-chips">
          {WEEKDAYS.map((d) => (
            <label key={d} className="weekday-chip">
              <input type="checkbox" name="weekdays" value={d} defaultChecked={c?.weekdays.includes(d)} />
              <span>{t(`weekdays.short.${d}`)}</span>
            </label>
          ))}
        </div>
        {fieldError('weekdays')}
      </fieldset>
      <div className="field">
        <label htmlFor="cf-start">{t('classes.form.startTime')}</label>
        <input id="cf-start" name="startTime" type="time" className="input" required
          defaultValue={shortTime(c?.startTime ?? null)} />
        {fieldError('startTime')}
      </div>
      <div className="field">
        <label htmlFor="cf-end">{t('classes.form.endTime')}</label>
        <input id="cf-end" name="endTime" type="time" className="input" defaultValue={shortTime(c?.endTime ?? null)} />
      </div>
      <div className="field">
        <label htmlFor="cf-location">{t('classes.form.location')}</label>
        <input id="cf-location" name="location" className="input" maxLength={80} defaultValue={c?.location ?? ''}
          placeholder={t('classes.form.locationPlaceholder')} />
      </div>
      <div className="field">
        <label htmlFor="cf-instructor">{t('classes.form.instructor')}</label>
        <select id="cf-instructor" name="instructor" className="input"
          defaultValue={c?.instructor?.membershipId ?? ''}>
          <option value="">{t('classes.form.noInstructor')}</option>
          {staff.map((s) => (
            <option key={s.membershipId} value={s.membershipId}>
              {s.name} · {t(`roles.${s.role}`)}
            </option>
          ))}
        </select>
      </div>
      <div className="field">
        <label htmlFor="cf-capacity">{t('classes.form.capacity')}</label>
        <input id="cf-capacity" name="capacity" type="number" className="input" min={1} max={MAX_CAPACITY}
          defaultValue={c?.capacity ?? ''} placeholder={t('classes.form.noLimit')} />
        {fieldError('capacity')}
      </div>
      <div className="form-actions span-2">
        <button type="button" className="btn" onClick={onCancel}>
          {t('common.cancel')}
        </button>
        <button type="submit" className="btn btn-primary" disabled={save.isPending}>
          {save.isPending ? t('common.saving') : c ? t('common.save') : t('classes.form.submitCreate')}
        </button>
      </div>
    </form>
  )
}
