import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router'

import { useAuth } from '@/auth/useAuth'
import { apiErrorMessage } from '@/lib/apiErrors'

/** Faixa visível só enquanto o admin da plataforma está dentro de uma academia que não é dele. */
export function SupportBanner() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { activeMembership, session, switchAcademy } = useAuth()
  const [error, setError] = useState<string | null>(null)
  const [pending, setPending] = useState(false)
  if (!activeMembership?.platformSupport) return null

  const home = session?.memberships.find((membership) => !membership.platformSupport)

  async function leave() {
    if (!home) return
    setPending(true)
    setError(null)
    try {
      await switchAcademy(home.id)
      navigate('/')
    } catch (err) {
      setError(apiErrorMessage(t, err))
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="access-banner" data-tone="warning" role="status">
      <p>{error ?? t('admin.support.banner', { academy: activeMembership.academy.name })}</p>
      {home && (
        <button type="button" className="btn btn-sm" onClick={() => void leave()} disabled={pending}>
          {t('admin.support.leave')}
        </button>
      )}
    </div>
  )
}
