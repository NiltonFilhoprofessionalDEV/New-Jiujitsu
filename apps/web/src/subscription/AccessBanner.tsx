import { useTranslation } from 'react-i18next'
import { Link, useLocation } from 'react-router'

import { SUBSCRIPTION_PATH, TRIAL_WARNING_DAYS, type AcademyAccess } from './types'

/** Aviso para o gestor nos últimos dias do teste e durante a tolerância; some na própria tela de assinatura. */
export function AccessBanner({ access }: { access: AcademyAccess }) {
  const { t } = useTranslation()
  const { pathname } = useLocation()
  const trialEnding = access.status === 'TRIAL' && access.daysLeft <= TRIAL_WARNING_DAYS
  if (pathname === SUBSCRIPTION_PATH || (!trialEnding && access.status !== 'GRACE')) return null

  const grace = access.status === 'GRACE'
  return (
    <div className="access-banner" data-tone={grace ? 'danger' : 'warning'} role="status">
      <p>
        {grace
          ? t('subscription.banner.grace', { count: access.daysLeft })
          : t('subscription.banner.trial', { count: access.daysLeft })}
      </p>
      <Link to={SUBSCRIPTION_PATH} className="btn btn-sm btn-primary">
        {grace ? t('subscription.banner.regularize') : t('subscription.banner.subscribe')}
      </Link>
    </div>
  )
}
