import { useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

import { MaskedInput } from '@/components/MaskedInput'
import { problemOf } from '@/lib/api'
import { maskCnpj, maskPhone } from '@/lib/masks'
import { apiErrorMessage } from '@/lib/apiErrors'
import { formText } from '@/lib/formText'

import { useAcademyProfile, useUpdateAcademyProfile, type AcademyProfile } from './settingsApi'

type FieldName = Exclude<keyof AcademyProfile, 'name'>
type FieldSpec = { name: FieldName; type?: string; maxLength: number; span?: boolean; pattern?: string }

const FIELDS: readonly FieldSpec[] = [
  { name: 'phone', type: 'tel', maxLength: 30 },
  { name: 'email', type: 'email', maxLength: 254 },
  { name: 'cnpj', maxLength: 18 },
  { name: 'address', maxLength: 255, span: true },
  { name: 'city', maxLength: 120 },
  { name: 'state', maxLength: 2, pattern: '[A-Za-z]{2}' },
]

function toProfile(form: FormData): AcademyProfile {
  return {
    name: formText(form, 'name') ?? '',
    cnpj: formText(form, 'cnpj'),
    email: formText(form, 'email'),
    phone: formText(form, 'phone'),
    address: formText(form, 'address'),
    city: formText(form, 'city'),
    state: formText(form, 'state')?.toUpperCase() ?? null,
  }
}

export function AcademyProfileForm() {
  const { t } = useTranslation()
  const { data: profile, isLoading, isError } = useAcademyProfile()
  const update = useUpdateAcademyProfile()
  const [saved, setSaved] = useState(false)
  const fieldErrors = update.error ? (problemOf(update.error).fields ?? {}) : {}

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaved(false)
    update.mutate(toProfile(new FormData(event.currentTarget)), { onSuccess: () => setSaved(true) })
  }

  return (
    <section className="card stack" aria-labelledby="academy-profile-title">
      <div>
        <h3 id="academy-profile-title">{t('settings.academy.title')}</h3>
        <p className="hint">{t('settings.academy.hint')}</p>
      </div>
      {isLoading && <p className="empty">{t('common.loading')}</p>}
      {isError && <div className="alert alert-error">{t('settings.academy.loadError')}</div>}
      {update.error && <div className="alert alert-error" role="alert">{apiErrorMessage(t, update.error)}</div>}
      {saved && <div className="notice" role="status">{t('settings.saved')}</div>}
      {profile && (
        <form className="form-grid" onSubmit={onSubmit}>
          <div className="field span-2">
            <label htmlFor="ap-name">{t('settings.academy.name')}</label>
            <input id="ap-name" name="name" className="input" required maxLength={120} defaultValue={profile.name}
              aria-invalid={Boolean(fieldErrors.name)} />
          </div>
          {FIELDS.map((f) => (
            <div key={f.name} className={f.span ? 'field span-2' : 'field'}>
              <label htmlFor={`ap-${f.name}`}>{t(`settings.academy.${f.name}`)}</label>
              {f.name === 'phone' || f.name === 'cnpj' ? (
                <MaskedInput id={`ap-${f.name}`} name={f.name} type={f.type ?? 'text'} className="input"
                  mask={f.name === 'phone' ? maskPhone : maskCnpj} maxLength={f.name === 'phone' ? 15 : 18}
                  placeholder={f.name === 'phone' ? '(11) 99999-9999' : '00.000.000/0000-00'}
                  defaultValue={profile[f.name]} aria-invalid={Boolean(fieldErrors[f.name])} />
              ) : (
                <input id={`ap-${f.name}`} name={f.name} type={f.type ?? 'text'} className="input"
                  maxLength={f.maxLength} pattern={f.pattern} defaultValue={profile[f.name] ?? ''}
                  aria-invalid={Boolean(fieldErrors[f.name])} />
              )}
              {fieldErrors[f.name] && <span className="field-error">{fieldErrors[f.name]}</span>}
            </div>
          ))}
          <div className="form-actions span-2">
            <button type="submit" className="btn btn-primary" disabled={update.isPending}>
              {update.isPending ? t('common.saving') : t('common.save')}
            </button>
          </div>
        </form>
      )}
    </section>
  )
}
