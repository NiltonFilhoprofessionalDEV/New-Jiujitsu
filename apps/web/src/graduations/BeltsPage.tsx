import { useTranslation } from 'react-i18next'

import { MANAGER_ROLES } from '@/auth/types'
import { useAuth } from '@/auth/useAuth'
import { ClassesHeader } from '@/classes/ClassesHeader'

import { BeltRulesForm } from './BeltRulesForm'
import { useBeltRules, useSaveBeltRules } from './graduationsApi'
import { ReadyList } from './ReadyList'

/** Aba "Faixas" da área Aulas: quem está pronto para graduar e as regras de cada faixa. */
export function BeltsPage() {
  const { t } = useTranslation()
  const { activeMembership } = useAuth()
  const canEdit = Boolean(activeMembership && MANAGER_ROLES.includes(activeMembership.role))
  const { data: rules, dataUpdatedAt, isLoading, isError } = useBeltRules()
  const save = useSaveBeltRules()

  return (
    <div className="page page-wide">
      <ClassesHeader />
      <div className="stack">
        <section className="stack" aria-labelledby="ready-title">
          <div>
            <h2 id="ready-title" className="section-title">{t('graduation.readyList.title')}</h2>
            <p className="hint">{t('graduation.readyList.intro')}</p>
          </div>
          <ReadyList />
        </section>

        <section className="stack" aria-labelledby="rules-title">
          <div>
            <h2 id="rules-title" className="section-title">{t('beltRules.title')}</h2>
            <p className="hint">{t(canEdit ? 'beltRules.intro' : 'beltRules.introReadOnly')}</p>
          </div>
          {isLoading && <p className="empty">{t('common.loading')}</p>}
          {isError && <div className="alert alert-error">{t('beltRules.loadError')}</div>}
          {rules && <BeltRulesForm key={dataUpdatedAt} rules={rules} canEdit={canEdit} save={save} />}
        </section>
      </div>
    </div>
  )
}
