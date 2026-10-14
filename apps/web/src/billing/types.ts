import type { Page, StudentStatus } from '@/students/types'

export type Plan = {
  id: string
  name: string
  description: string | null
  priceCents: number
  intervalMonths: number
  dueDay: number
  active: boolean
  isDefault: boolean
  isKidsDefault: boolean
  subscribers: number
}

/** Campos numéricos vazios vão como {@code null} e a API devolve o erro do campo. */
export type PlanRequest = Omit<Plan, 'id' | 'subscribers' | 'priceCents' | 'dueDay'> & {
  priceCents: number | null
  dueDay: number | null
}

export type DiscountType = 'PERCENT' | 'FIXED'

export type Subscription = {
  id: string
  student: { id: string; name: string; nickname: string | null; status: StudentStatus }
  plan: { id: string; name: string; priceCents: number; intervalMonths: number }
  dueDay: number
  discountType: DiscountType | null
  discountValue: number | null
  discountReason: string | null
  /** Valor da próxima fatura, já com desconto. */
  amountCents: number
  nextDue: string
  startedOn: string
  overdueSince: string | null
  overdueCents: number
}

/** {@code dueDay} nulo usa o dia do plano. */
export type TermsRequest = {
  planId: string
  dueDay: number | null
  discountType: DiscountType | null
  discountValue: number | null
  discountReason: string | null
}

export type LinkRequest = { studentId: string; terms: TermsRequest; firstDueDate: string | null }

/** {@code OVERDUE}: em aberto e com vencimento antes de hoje. */
export type InvoiceSituation = 'OPEN' | 'OVERDUE' | 'PAID' | 'CANCELED'
export type SituationFilter = 'all' | 'open' | 'overdue' | 'paid' | 'canceled'
export type PaymentMethod = 'CASH' | 'PIX' | 'CARD' | 'TRANSFER' | 'OTHER'

export type Invoice = {
  id: string
  studentId: string
  studentName: string
  description: string
  amountCents: number
  dueDate: string
  situation: InvoiceSituation
  paidOn: string | null
  paymentMethod: PaymentMethod | null
}

export type Bucket = { count: number; cents: number }

export type InvoiceList = {
  page: Page<Invoice>
  /** Do mês e da busca escolhidos, sem o filtro de situação. */
  totals: { open: Bucket; overdue: Bucket; paid: Bucket }
  /** Da academia inteira, independente dos filtros. */
  overdue: { students: number; cents: number }
}

/** {@code month}: {@code yyyy-MM}, ou vazio para todos os meses. */
export type InvoiceQuery = { situation: SituationFilter; month: string; q: string; page: number }

export type InvoiceAction =
  | { type: 'pay'; id: string; paidOn: string; method: PaymentMethod }
  | { type: 'cancel'; id: string }
  | { type: 'reverse'; id: string }
