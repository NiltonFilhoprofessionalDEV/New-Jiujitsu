export type AccessStatus = 'TRIAL' | 'ACTIVE' | 'GRACE' | 'SUSPENDED'

/**
 * Assinatura da academia na plataforma (não confundir com as mensalidades dos alunos).
 * {@code enforced} falso: app livre, sem aviso, bloqueio nem tela de assinatura.
 */
export type AcademyAccess = {
  enforced: boolean
  status: AccessStatus
  trialEndsAt: string
  paidUntil: string | null
  accessUntil: string
  suspendsAt: string
  daysLeft: number
  monthlyPrice: number
  onlinePayment: boolean
  subscribed: boolean
}

/** Faixa de aviso para gestores: últimos dias do teste ou tolerância. */
export const TRIAL_WARNING_DAYS = 7

export const SUBSCRIPTION_PATH = '/assinatura'
export const ADMIN_PATH = '/admin'
