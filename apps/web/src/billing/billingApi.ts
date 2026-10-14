import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { api } from '@/lib/api'

import type {
  Invoice,
  InvoiceAction,
  InvoiceList,
  InvoiceQuery,
  LinkRequest,
  Plan,
  PlanRequest,
  Subscription,
  TermsRequest,
} from './types'

/** Planos, mensalidades e faturas se refletem uns nos outros (contagens, valores, saldos): uma chave só. */
export const BILLING = 'billing'

export function usePlans() {
  return useQuery({
    queryKey: [BILLING, 'plans'],
    queryFn: async () => (await api.get<Plan[]>('/plans')).data,
  })
}

export function useSubscriptions() {
  return useQuery({
    queryKey: [BILLING, 'subscriptions'],
    queryFn: async () => (await api.get<Subscription[]>('/subscriptions')).data,
  })
}

export function useInvoices(query: InvoiceQuery) {
  return useQuery({
    queryKey: [BILLING, 'invoices', query],
    queryFn: async () =>
      (await api.get<InvoiceList>('/invoices', { params: { ...query, month: query.month || undefined } })).data,
    placeholderData: keepPreviousData,
  })
}

function useBillingMutation<TVars, TResult>(fn: (vars: TVars) => Promise<TResult>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: [BILLING] }),
  })
}

export function useSavePlan() {
  return useBillingMutation(async ({ id, body }: { id?: string; body: PlanRequest }) =>
    id ? (await api.put<Plan>(`/plans/${id}`, body)).data : (await api.post<Plan>('/plans', body)).data,
  )
}

export function useLinkSubscription() {
  return useBillingMutation(async (body: LinkRequest) => (await api.post<Subscription>('/subscriptions', body)).data)
}

export function useUpdateSubscription() {
  return useBillingMutation(async ({ id, terms }: { id: string; terms: TermsRequest }) =>
    (await api.put<Subscription>(`/subscriptions/${id}`, terms)).data,
  )
}

export function useCancelSubscription() {
  return useBillingMutation(async (id: string) => {
    await api.delete(`/subscriptions/${id}`)
  })
}

export function useInvoiceAction() {
  return useBillingMutation(async (action: InvoiceAction) => {
    const base = `/invoices/${action.id}`
    switch (action.type) {
      case 'pay':
        return (await api.post<Invoice>(`${base}/pay`, { paidOn: action.paidOn, method: action.method })).data
      case 'cancel':
        return (await api.post<Invoice>(`${base}/cancel`)).data
      case 'reverse':
        return (await api.post<Invoice>(`${base}/reverse`)).data
    }
  })
}
