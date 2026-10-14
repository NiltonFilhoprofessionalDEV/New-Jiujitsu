import { useTranslation } from 'react-i18next'

import { useClasses } from '@/classes/classesApi'
import { BeltIcon } from '@/students/BeltIcon'
import { BELTS } from '@/students/belts'

import type { Audience } from './types'

const AUDIENCES: readonly Audience[] = ['ALL', 'BELTS', 'CLASSES']
const CHIP_BELT_SIZE = 26

type Props = {
  audience: Audience
  onAudience: (audience: Audience) => void
  belts: string[]
  onBelts: (belts: string[]) => void
  classIds: string[]
  onClassIds: (classIds: string[]) => void
}

function toggle(list: string[], value: string): string[] {
  return list.includes(value) ? list.filter((v) => v !== value) : [...list, value]
}

/** Quem vê o vídeo: todos, algumas faixas ou algumas turmas. A equipe sempre vê tudo. */
export function AudienceFields({ audience, onAudience, belts, onBelts, classIds, onClassIds }: Props) {
  const { t } = useTranslation()
  const { data: classes = [] } = useClasses()

  return (
    <fieldset className="field span-2 audience-field">
      <legend>{t('videos.form.audience')}</legend>
      <div className="segmented">
        {AUDIENCES.map((a) => (
          <label key={a} className={audience === a ? 'segment active' : 'segment'}>
            <input type="radio" name="audience" value={a} checked={audience === a} onChange={() => onAudience(a)} />
            {t(`videos.audience.${a}`)}
          </label>
        ))}
      </div>
      {audience === 'BELTS' && (
        <div className="choice-chips">
          {BELTS.map((b) => (
            <label key={b.slug} className="choice-chip">
              <input type="checkbox" checked={belts.includes(b.slug)} onChange={() => onBelts(toggle(belts, b.slug))} />
              <BeltIcon belt={b.slug} size={CHIP_BELT_SIZE} />
              <span>{t(`belts.${b.slug}`)}</span>
            </label>
          ))}
        </div>
      )}
      {audience === 'CLASSES' && (
        <div className="choice-chips">
          {classes.length === 0 && <p className="empty">{t('videos.form.noClasses')}</p>}
          {classes.map((c) => (
            <label key={c.id} className="choice-chip">
              <input type="checkbox" checked={classIds.includes(c.id)} onChange={() => onClassIds(toggle(classIds, c.id))} />
              <span>{c.title}</span>
            </label>
          ))}
        </div>
      )}
      <p className="hint">{t('videos.form.audienceHint')}</p>
    </fieldset>
  )
}
