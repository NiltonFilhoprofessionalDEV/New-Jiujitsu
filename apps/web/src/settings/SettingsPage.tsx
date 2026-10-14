import { useTranslation } from 'react-i18next'

import { useIsManager } from '@/auth/useAuth'
import { PageHeader } from '@/components/PageHeader'
import { EnrollmentLinkCard } from '@/invites/EnrollmentLinkCard'

import { AcademyProfileForm } from './AcademyProfileForm'
import { PasswordForm } from './PasswordForm'

/** Dados da academia e link de matrícula só para gestores; a senha é de cada um. */
export function SettingsPage() {
  const { t } = useTranslation()
  const isManager = useIsManager()
  return (
    <div className="page">
      <PageHeader eyebrow={t('common.yourAcademy')} title={t('settings.title')}
        description={isManager ? t('pages.settings') : undefined} />
      <div className="stack">
        {isManager && <AcademyProfileForm />}
        {isManager && <EnrollmentLinkCard />}
        <PasswordForm />
      </div>
    </div>
  )
}
