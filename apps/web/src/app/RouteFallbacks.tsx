import { useTranslation } from 'react-i18next'

export function PageLoading() {
  const { t } = useTranslation()
  return <p className="empty" role="status">{t('common.loading')}</p>
}

/** Erro de renderização ou chunk que sumiu após um deploy: recarregar resolve; detalhes não vão para a tela. */
export function RouteError() {
  const { t } = useTranslation()
  return (
    <div className="page">
      <div className="card stack" role="alert">
        <h1>{t('routeError.title')}</h1>
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
