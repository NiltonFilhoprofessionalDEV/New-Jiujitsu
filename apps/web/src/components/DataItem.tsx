import type { ReactNode } from 'react'

/** Par rótulo/valor dentro de um {@code <dl className="data-list">}; valor vazio vira travessão. */
export function DataItem({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div>
      <dt>{label}</dt>
      <dd>{value || '—'}</dd>
    </div>
  )
}
