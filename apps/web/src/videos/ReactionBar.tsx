import { Heart } from 'lucide-react'
import { useTranslation } from 'react-i18next'

import { REACTIONS } from './reactions'
import type { Video } from './types'
import { useFavorite, useReact } from './videosApi'

/** Uma reação por pessoa: tocar na mesma tira, tocar em outra troca. Favoritar fica ao lado. */
export function ReactionBar({ video }: { video: Video }) {
  const { t } = useTranslation()
  const react = useReact(video.id)
  const favorite = useFavorite(video.id)

  return (
    <div className="reaction-bar">
      <div className="reactions" role="group" aria-label={t('videos.reactAria')}>
        {REACTIONS.map(({ id, emoji }) => {
          const mine = video.myReaction === id
          const count = video.reactions[id] ?? 0
          return (
            <button key={id} type="button" className={mine ? 'reaction active' : 'reaction'} aria-pressed={mine}
              disabled={react.isPending} title={t(`videos.reaction.${id}`)}
              aria-label={t('videos.reactionLabel', { name: t(`videos.reaction.${id}`), count })}
              onClick={() => react.mutate(mine ? null : id)}>
              <span aria-hidden>{emoji}</span>
              {count > 0 && <span className="reaction-count">{count}</span>}
            </button>
          )
        })}
      </div>
      <button type="button" className={video.favorite ? 'btn btn-sm favorite-btn active' : 'btn btn-sm favorite-btn'}
        aria-pressed={video.favorite} disabled={favorite.isPending} onClick={() => favorite.mutate(!video.favorite)}>
        <Heart size={16} aria-hidden />
        {video.favorite ? t('videos.favorited') : t('videos.addFavorite')}
      </button>
    </div>
  )
}
