import { useState, type SyntheticEvent } from 'react'

import { cn } from '@/lib/utils'

import { MISSING_THUMB_WIDTH, thumbnailUrl } from './youtube'

type Props = { youtubeId: string; className?: string }

/**
 * Capa grande (destaque): mostra na hora a capa padrão e sobrepõe a HD quando ela existe. O pai precisa
 * estar posicionado ({@code relative}/{@code absolute}).
 */
export function HiResThumb({ youtubeId, className }: Props) {
  const [hiResReady, setHiResReady] = useState<string | null>(null)
  const onLoad = (event: SyntheticEvent<HTMLImageElement>) => {
    if (event.currentTarget.naturalWidth > MISSING_THUMB_WIDTH) setHiResReady(youtubeId)
  }
  return (
    <>
      <img src={thumbnailUrl(youtubeId)} alt="" className={className} />
      <img src={thumbnailUrl(youtubeId, 'maxres')} alt="" onLoad={onLoad}
        className={cn(className, 'absolute inset-0 transition-[opacity,transform]', hiResReady === youtubeId ? 'opacity-100' : 'opacity-0')} />
    </>
  )
}
