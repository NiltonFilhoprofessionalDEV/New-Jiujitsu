import type { InvoiceSituation } from './types'

export const SITUATION_PILL: Record<InvoiceSituation, string> = {
  OPEN: 'pill pill-outline',
  OVERDUE: 'pill pill-danger',
  PAID: 'pill pill-success',
  CANCELED: 'pill pill-muted',
}
