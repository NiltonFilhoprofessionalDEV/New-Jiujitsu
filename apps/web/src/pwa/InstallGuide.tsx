import { CircleCheck, EllipsisVertical, Share, SquarePlus, type LucideIcon } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { Dialog } from '@/components/Dialog'

import type { GuidePlatform } from './installPrompt'

const STEP_ICONS: Record<GuidePlatform, LucideIcon[]> = {
  ios: [Share, SquarePlus, CircleCheck],
  android: [EllipsisVertical, SquarePlus, CircleCheck],
}

type Props = { platform: GuidePlatform | null; onClose: () => void }

/** Passo a passo para quando o navegador não instala por botão (Safari no iPhone, navegador dentro de outro app). */
export function InstallGuide({ platform, onClose }: Props) {
  const { t } = useTranslation()
  const shown = platform ?? 'android'
  return (
    <Dialog open={platform !== null} onClose={onClose} title={t('install.guide.title')}>
      <div className="install-guide">
        <p className="dialog-text">{t('install.guide.intro')}</p>
        <ol className="install-steps">
          {STEP_ICONS[shown].map((Icon, index) => (
            <li key={index}>
              <span className="install-step-icon" aria-hidden>
                <Icon size={20} />
              </span>
              <span>
                <strong>{t(`install.guide.${shown}.step${index + 1}`)}</strong>
                <span className="install-step-hint">{t(`install.guide.${shown}.hint${index + 1}`)}</span>
              </span>
            </li>
          ))}
        </ol>
        <p className="install-note">{t(`install.guide.${shown}.inApp`)}</p>
        {shown === 'ios' && <p className="install-note">{t('install.guide.ios.login')}</p>}
      </div>
    </Dialog>
  )
}
