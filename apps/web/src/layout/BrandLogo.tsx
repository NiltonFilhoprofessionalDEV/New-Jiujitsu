import { useTranslation } from 'react-i18next'

/** Logo em texto: "B" vazado e "J" vermelho. {@code stacked} quebra em BJJ / PULSE. */
export function BrandLogo({ stacked = false }: { stacked?: boolean }) {
  const { t } = useTranslation()
  return (
    <span className={stacked ? 'brand-logo stacked' : 'brand-logo'} role="img" aria-label={t('app.name')}>
      <span aria-hidden>
        <span className="logo-b">B</span>
        <span className="logo-j">J</span>J
      </span>
      <span aria-hidden>PULSE</span>
    </span>
  )
}
