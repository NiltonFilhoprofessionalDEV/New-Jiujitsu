import { useTranslation } from 'react-i18next'

export function ComingSoonPage({ titleKey }: { titleKey: string }) {
  const { t } = useTranslation()
  return (
    <div className="page">
      <div className="page-header">
        <div>
          <div className="eyebrow">{t('common.yourAcademy')}</div>
          <h1>{t(titleKey)}</h1>
        </div>
      </div>
      <div className="card">
        <p className="empty">{t('common.comingSoon')}</p>
      </div>
    </div>
  )
}
