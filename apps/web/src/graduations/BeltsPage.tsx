import { useTranslation } from 'react-i18next'

import { MANAGER_ROLES } from '@/auth/types'
import { useAuth } from '@/auth/useAuth'
import { HelpNote } from '@/components/HelpNote'
import { PageHeader } from '@/components/PageHeader'

import { BeltRulesList } from './BeltRulesList'
import { useBeltRules, useReadyStudents } from './graduationsApi'
import { ReadyList } from './ReadyList'

/** Quem está pronto para graduar e o que cada faixa exige. */
export function BeltsPage() {
  const { t } = useTranslation()
  const { activeMembership } = useAuth()
  const canEdit = Boolean(activeMembership && MANAGER_ROLES.includes(activeMembership.role))
  const { data: rules, isLoading, isError } = useBeltRules()
  const { data: ready } = useReadyStudents()

  return (
    <div className="page page-wide">
      <PageHeader eyebrow={t('common.yourAcademy')} title={t('classes.tabs.belts')} description={t('pages.belts')} />
      <div className="belts-layout">
        <section className="stack" aria-labelledby="ready-title">
          <h2 id="ready-title" className="section-title">
            {t('graduation.readyList.title')}
            {ready && ready.length > 0 && <span className="section-count">{ready.length}</span>}
          </h2>
          <ReadyList />
        </section>

        <section className="stack" aria-labelledby="rules-title">
          <h2 id="rules-title" className="section-title">{t('beltRules.title')}</h2>
          <HelpNote>
            <p>{t(canEdit ? 'beltRules.intro' : 'beltRules.introReadOnly')}</p>
            <p>{t('beltRules.blackBelt')}</p>
            <p>{t('beltRules.defaultsNote')}</p>
          </HelpNote>
          {isLoading && <p className="empty">{t('common.loading')}</p>}
          {isError && <div className="alert alert-error">{t('beltRules.loadError')}</div>}
          {rules && <BeltRulesList rules={rules} canEdit={canEdit} />}
        </section>
      </div>
    </div>
  )
}
