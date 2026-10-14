import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { EnrollmentLinkCard } from './EnrollmentLinkCard'
import { InviteList } from './InviteList'
import { NewInviteForm } from './NewInviteForm'

const SECTIONS = ['link', 'person', 'sent'] as const
type Section = (typeof SECTIONS)[number]

/** Painel "Convidar alunos": uma forma de convidar por vez, em vez de três blocos empilhados. */
export function InvitesPanel() {
  const { t } = useTranslation()
  const [section, setSection] = useState<Section>('link')

  return (
    <div className="stack invites-panel">
      <div className="segmented" role="group" aria-label={t('invites.sectionsAria')}>
        {SECTIONS.map((s) => (
          <button key={s} type="button" aria-pressed={section === s}
            className={section === s ? 'segment active' : 'segment'} onClick={() => setSection(s)}>
            {t(`invites.section.${s}`)}
          </button>
        ))}
      </div>
      {section === 'link' && <EnrollmentLinkCard />}
      {section === 'person' && <NewInviteForm />}
      {section === 'sent' && <InviteList />}
    </div>
  )
}
