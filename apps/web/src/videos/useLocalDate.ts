import { useTranslation } from 'react-i18next'

/** Formata um instante da API (UTC) no fuso e idioma de quem está vendo. */
export function useLocalDate(options: Intl.DateTimeFormatOptions) {
  const { i18n } = useTranslation()
  const format = new Intl.DateTimeFormat(i18n.language, options)
  return (iso: string) => format.format(new Date(iso))
}
