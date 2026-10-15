import type { ReactNode } from 'react'

import { PageTitle } from '@/components/PageTitle'
import { BrandLogo } from '@/layout/BrandLogo'

type Props = { title: string; subtitle: string; children: ReactNode; after?: ReactNode }

/** {@code after} fica abaixo do cartão, fora do formulário. */
export function AuthLayout({ title, subtitle, children, after }: Props) {
  return (
    <div className="auth-screen">
      <div className="auth-panel">
        <header className="auth-head">
          <BrandLogo />
          <span className="auth-rule" aria-hidden />
          <PageTitle className="auth-title">{title}</PageTitle>
          <p className="auth-sub">{subtitle}</p>
        </header>
        <div className="card auth-card">{children}</div>
        {after}
      </div>
    </div>
  )
}
