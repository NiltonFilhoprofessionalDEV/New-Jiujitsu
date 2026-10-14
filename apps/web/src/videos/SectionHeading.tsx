import type { ReactNode } from 'react'

type Props = { id: string; title: string; aside?: ReactNode }

export function SectionHeading({ id, title, aside }: Props) {
  return (
    <div className="flex flex-wrap items-end justify-between gap-3">
      <h2 id={id} className="m-0 flex items-center gap-2.5 font-heading text-3xl leading-none font-normal tracking-wide uppercase">
        <span aria-hidden className="h-6 w-1 rounded-full bg-pulse" />
        {title}
      </h2>
      {aside}
    </div>
  )
}
