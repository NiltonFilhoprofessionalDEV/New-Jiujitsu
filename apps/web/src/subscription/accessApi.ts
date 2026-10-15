import { useMutation, useQuery } from '@tanstack/react-query'

import { api } from '@/lib/api'

import type { AcademyAccess } from './types'

export const ACADEMY_ACCESS_KEY = ['academy-access']

/** Código da API quando a equipe de uma academia suspensa tenta usar o sistema. */
export const SUSPENDED_CODE = 'ACADEMY_SUSPENDED'

export function useAcademyAccess() {
  return useQuery({
    queryKey: ACADEMY_ACCESS_KEY,
    queryFn: async () => (await api.get<AcademyAccess>('/academy-access')).data,
  })
}

/** Assina na primeira vez (com CPF/CNPJ) e devolve a página de pagamento da fatura em aberto. */
export function useCheckout() {
  return useMutation({
    mutationFn: async (cpfCnpj: string | null) =>
      (await api.post<{ invoiceUrl: string }>('/academy-access/checkout', cpfCnpj ? { cpfCnpj } : {})).data,
  })
}
