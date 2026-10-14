import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { AuthLayout } from '@/auth/pages/AuthLayout'

/** Estado final das telas públicas (link inválido, pausado, convite usado...): só resta ir para o login. */
export function PublicNotice({ title, body }: { title: string; body: string }) {
  const { t } = useTranslation()
  return (
    <AuthLayout title={title} subtitle={body}>
      <Link className="btn btn-block" to="/login">{t('auth.submitLogin')}</Link>
    </AuthLayout>
  )
}
