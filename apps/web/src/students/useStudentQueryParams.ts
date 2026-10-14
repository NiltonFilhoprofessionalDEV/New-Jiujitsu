import { useCallback } from 'react'
import { useSearchParams } from 'react-router'

import type { AgeFilter, KindFilter, StatusFilter } from './types'

const STATUS: readonly StatusFilter[] = ['active', 'inactive', 'all']
const KIND: readonly KindFilter[] = ['all', 'training', 'responsible']
const AGE: readonly AgeFilter[] = ['all', 'kids', 'adult', 'under18', 'adult18']
const DEFAULTS: Record<string, string> = { status: 'active', kind: 'all', age: 'all', page: '0' }

function oneOf<T extends string>(value: string | null, allowed: readonly T[], fallback: T): T {
  return allowed.includes(value as T) ? (value as T) : fallback
}

/** Filtros, página e aluno selecionado ficam na URL: sobrevivem ao recarregar e ao botão voltar. */
export function useStudentQueryParams() {
  const [params, setParams] = useSearchParams()

  const state = {
    q: params.get('q') ?? '',
    status: oneOf(params.get('status'), STATUS, 'active'),
    kind: oneOf(params.get('kind'), KIND, 'all'),
    age: oneOf(params.get('age'), AGE, 'all'),
    page: Math.max(0, Number(params.get('page')) || 0),
    selectedId: params.get('id'),
    invitesOpen: params.get('convites') === '1',
  }

  const update = useCallback(
    (changes: Record<string, string | number | null>, resetPage = true) => {
      setParams(
        (current) => {
          const next = new URLSearchParams(current)
          for (const [key, value] of Object.entries(changes)) {
            if (value === null || value === '' || DEFAULTS[key] === String(value)) next.delete(key)
            else next.set(key, String(value))
          }
          if (resetPage && !('page' in changes)) next.delete('page')
          return next
        },
        { replace: true },
      )
    },
    [setParams],
  )

  return { ...state, update }
}
