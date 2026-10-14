import { Check, Copy } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'

const FEEDBACK_MS = 2000

type Props = { text: string; label?: string; className?: string }

/** Copia para a área de transferência; sem permissão (HTTP, navegador antigo), mostra o texto para copiar à mão. */
export function CopyButton({ text, label, className = 'btn btn-sm' }: Props) {
  const { t } = useTranslation()
  const [copied, setCopied] = useState(false)

  useEffect(() => {
    if (!copied) return
    const timer = window.setTimeout(() => setCopied(false), FEEDBACK_MS)
    return () => window.clearTimeout(timer)
  }, [copied])

  async function copy() {
    if (!navigator.clipboard) {
      window.prompt(t('common.copyPrompt'), text)
      return
    }
    await navigator.clipboard.writeText(text)
    setCopied(true)
  }

  return (
    <button type="button" className={className} onClick={() => void copy().catch(() => window.prompt(t('common.copyPrompt'), text))}>
      {copied ? <Check size={15} aria-hidden /> : <Copy size={15} aria-hidden />}
      {copied ? t('common.copied') : (label ?? t('common.copy'))}
    </button>
  )
}
