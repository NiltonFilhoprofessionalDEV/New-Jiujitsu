import type { ReactNode } from 'react'

import { PageTitle } from './PageTitle'

type Props = {
  eyebrow: string
  title: string
  /** Uma frase dizendo para que serve a tela. */
  description?: string
  actions?: ReactNode
}

export function PageHeader({ eyebrow, title, description, actions }: Props) {
  return (
    <header className="page-header">
      <div className="page-heading">
        <div className="eyebrow">{eyebrow}</div>
        <PageTitle>{title}</PageTitle>
        {description && <p className="page-sub">{description}</p>}
      </div>
      {actions && <div className="page-actions">{actions}</div>}
    </header>
  )
}
