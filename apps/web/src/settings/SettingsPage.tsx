import { useTranslation } from 'react-i18next'

import { useIsManager } from '@/auth/useAuth'
import { EnrollmentLinkCard } from '@/invites/EnrollmentLinkCard'

import { AcademyProfileForm } from './AcademyProfileForm'
import { PasswordForm } from './PasswordForm'

/** Dados da academia e link de matrícula só para gestores; a senha é de cada um. */
export function SettingsPage() {
  const { t } = useTranslation()
  const isManager = useIsManager()
  return (
    <div className="page">
      <div className="page-header">
        <div>
          <div className="eyebrow">{t('common.yourAcademy')}</div>
          <h1>{t('settings.title')}</h1>
        </div>
      </div>
      <div className="stack">
        {isManager && <AcademyProfileForm />}
        {isManager && <EnrollmentLinkCard />}
        <PasswordForm />
      </div>
    </div>
  )
}
