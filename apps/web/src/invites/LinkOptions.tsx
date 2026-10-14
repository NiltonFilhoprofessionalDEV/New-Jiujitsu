import { RefreshCw } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { ConfirmDialog } from '@/components/ConfirmDialog'

import type { useLinkAction } from './invitesApi'
import type { EnrollmentLink } from './types'

type Props = { link: EnrollmentLink; action: ReturnType<typeof useLinkAction> }

/** Ajustes raros do link de matrícula, recolhidos para não competir com "copiar" e "enviar". */
export function LinkOptions({ link, action }: Props) {
  const { t } = useTranslation()
  const [confirmRotate, setConfirmRotate] = useState(false)
  const configure = (enabled: boolean, expiresOn: string | null) =>
    action.mutate({ type: 'configure', enabled, expiresOn })

  return (
    <>
      <details className="link-options">
        <summary>{t('invites.link.options')}</summary>
        <div className="link-settings">
          <label className="checkbox-inline">
            <input type="checkbox" checked={link.enabled} disabled={action.isPending}
              onChange={(e) => configure(e.target.checked, link.expiresOn)} />
            {t('invites.link.acceptSignups')}
          </label>
          <label className="field">
            <span>{t('invites.link.expiresAt')}</span>
            <input type="date" className="input" value={link.expiresOn ?? ''} disabled={action.isPending}
              onChange={(e) => configure(link.enabled, e.target.value || null)} />
          </label>
          <button type="button" className="btn btn-sm btn-ghost" onClick={() => setConfirmRotate(true)}>
            <RefreshCw size={15} aria-hidden /> {t('invites.link.rotate')}
          </button>
        </div>
      </details>
      <ConfirmDialog
        open={confirmRotate}
        title={t('invites.link.rotate')}
        body={t('invites.link.rotateConfirm')}
        confirmLabel={t('invites.link.rotate')}
        pending={action.isPending}
        error={null}
        onConfirm={() => action.mutate({ type: 'rotate' }, { onSettled: () => setConfirmRotate(false) })}
        onClose={() => setConfirmRotate(false)}
      />
    </>
  )
}
