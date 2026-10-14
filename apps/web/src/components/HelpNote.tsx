import { CircleHelp } from 'lucide-react'
import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'

/** Explicação longa recolhida: fica à mão sem poluir a tela de quem já conhece o fluxo. */
export function HelpNote({ children }: { children: ReactNode }) {
  const { t } = useTranslation()
  return (
    <details className="help-note">
      <summary>
        <CircleHelp size={16} aria-hidden />
        {t('common.howItWorks')}
      </summary>
      <div className="help-note-body">{children}</div>
    </details>
  )
}
