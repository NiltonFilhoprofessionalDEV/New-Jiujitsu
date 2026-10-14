import { ChevronRight } from 'lucide-react'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import { Dialog } from '@/components/Dialog'
import { BeltIcon } from '@/students/BeltIcon'
import { beltInfo, type BeltGroup } from '@/students/belts'

import { BeltRuleForm } from './BeltRuleForm'
import type { BeltRule } from './types'

const GROUPS: readonly BeltGroup[] = ['adult', 'kids']
const ROW_ICON_SIZE = 40

type Props = { rules: BeltRule[]; canEdit: boolean }

/** "24 meses · 100 treinos · grau a cada 30 treinos": a regra inteira numa linha. */
function useRuleSummary() {
  const { t } = useTranslation()
  return (rule: BeltRule) => {
    const parts = [
      rule.minMonths !== null && t('graduation.months', { count: rule.minMonths }),
      rule.minTrainings !== null && t('beltRules.summary.trainings', { count: rule.minTrainings }),
      rule.trainingsPerDegree !== null && t('beltRules.summary.perDegree', { count: rule.trainingsPerDegree }),
    ].filter(Boolean)
    return parts.length > 0 ? parts.join(' · ') : t('beltRules.summary.none')
  }
}

/** Sem {@code onEdit} (professor) a linha só informa a regra. */
function RuleRow({ rule, onEdit }: { rule: BeltRule; onEdit?: () => void }) {
  const { t } = useTranslation()
  const summary = useRuleSummary()
  const content = (
    <>
      <BeltIcon belt={rule.belt} size={ROW_ICON_SIZE} />
      <span className="person-main">
        <span className="person-name">
          <span className="row-title">{t(`belts.${rule.belt}`)}</span>
          {rule.custom && <span className="pill pill-outline">{t('beltRules.custom')}</span>}
        </span>
        <span className="row-sub">{summary(rule)}</span>
      </span>
    </>
  )
  if (!onEdit) return <div className="person-row static">{content}</div>
  return (
    <button type="button" className="person-row" onClick={onEdit}>
      {content}
      <ChevronRight size={18} className="row-chevron" aria-hidden />
    </button>
  )
}

/** Faixas de um grupo por vez; quem pode editar abre a faixa num painel com os três campos. */
export function BeltRulesList({ rules, canEdit }: Props) {
  const { t } = useTranslation()
  const [group, setGroup] = useState<BeltGroup>('adult')
  const [editing, setEditing] = useState<BeltRule | null>(null)
  const [savedBelt, setSavedBelt] = useState<string | null>(null)
  const visible = rules.filter((r) => beltInfo(r.belt)?.group === group)

  function pickGroup(next: BeltGroup) {
    setGroup(next)
    setSavedBelt(null)
  }

  return (
    <div className="stack">
      <fieldset className="segmented rules-groups">
        <legend className="sr-only">{t('beltRules.groupAria')}</legend>
        {GROUPS.map((g) => (
          <label key={g} className={group === g ? 'segment active' : 'segment'}>
            <input type="radio" name="belt-group" value={g} checked={group === g} onChange={() => pickGroup(g)} />
            {t(`belts.group.${g}`)}
          </label>
        ))}
      </fieldset>
      {savedBelt && (
        <div className="alert alert-success" role="status">
          {t('beltRules.savedNamed', { belt: t(`belts.${savedBelt}`) })}
        </div>
      )}
      <div className="card list-card">
        <ul className="people-list">
          {visible.map((rule) => (
            <li key={rule.belt}>
              <RuleRow rule={rule} onEdit={canEdit ? () => setEditing(rule) : undefined} />
            </li>
          ))}
        </ul>
      </div>
      <Dialog open={editing !== null} onClose={() => setEditing(null)}
        title={editing ? t('beltRules.editNamed', { belt: t(`belts.${editing.belt}`) }) : ''}>
        {editing && (
          <BeltRuleForm key={editing.belt} rule={editing} onCancel={() => setEditing(null)}
            onSaved={(belt) => {
              setSavedBelt(belt)
              setEditing(null)
            }} />
        )}
      </Dialog>
    </div>
  )
}
