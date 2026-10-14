import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'

export function AuthLayout({ title, subtitle, children }: { title: string; subtitle: string; children: ReactNode }) {
  const { t } = useTranslation()
  return (
    <div className="auth-screen">
      <div className="card auth-card">
        <div className="auth-brand">
          <span className="brand-mark" aria-hidden>
            J
          </span>
          {t('app.tagline')}
        </div>
        <div>
          <h1>{title}</h1>
          <p className="empty">{subtitle}</p>
        </div>
        {children}
      </div>
    </div>
  )
}
