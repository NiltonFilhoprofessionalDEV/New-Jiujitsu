import { Lock } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link } from 'react-router'

import { SUBSCRIPTION_PATH } from './types'

/** Toma o lugar das telas da equipe enquanto a academia está suspensa; os alunos seguem usando o app. */
export function SuspendedScreen({ isManager }: { isManager: boolean }) {
  const { t } = useTranslation()
  return (
    <section className="suspended card" aria-labelledby="suspended-title">
      <span className="suspended-icon" aria-hidden><Lock size={22} /></span>
      <h1 id="suspended-title">{t('subscription.suspended.title')}</h1>
      <p>{t('subscription.suspended.body')}</p>
      {isManager ? (
        <Link to={SUBSCRIPTION_PATH} className="btn btn-primary">{t('subscription.suspended.cta')}</Link>
      ) : (
        <p className="hint">{t('subscription.suspended.askManager')}</p>
      )}
    </section>
  )
}
