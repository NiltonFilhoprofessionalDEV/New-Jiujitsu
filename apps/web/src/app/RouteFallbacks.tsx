import { useTranslation } from 'react-i18next'

import { PageTitle } from '@/components/PageTitle'

export function PageLoading() {
  const { t } = useTranslation()
  return (
    <div className="page-loading" role="status">
      <span className="live-dot" aria-hidden />
      <span className="sr-only">{t('common.loading')}</span>
    </div>
  )
}

/** Erro de renderização ou chunk que sumiu após um deploy: recarregar resolve; detalhes não vão para a tela. */
export function RouteError() {
  const { t } = useTranslation()
  return (
    <div className="page">
      <div className="card stack" role="alert">
        <PageTitle>{t('routeError.title')}</PageTitle>
        <p className="hint">{t('routeError.body')}</p>
        <div>
          <button type="button" className="btn btn-primary" onClick={() => window.location.reload()}>
            {t('routeError.reload')}
          </button>
        </div>
      </div>
    </div>
  )
}
