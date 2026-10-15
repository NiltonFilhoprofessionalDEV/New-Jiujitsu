import {
  Award,
  BadgeCheck,
  BarChart3,
  Building2,
  CalendarDays,
  ClipboardCheck,
  CreditCard,
  Home,
  Inbox,
  PlaySquare,
  Receipt,
  Settings,
  ShieldCheck,
  UserCog,
  UserRound,
  Users,
  Wallet,
  type LucideIcon,
} from 'lucide-react'

import { STUDENT_HOME } from '@/auth/types'
import { ADMIN_PATH, SUBSCRIPTION_PATH } from '@/subscription/types'

/**
 * {@code managersOnly}: some do menu do professor (a API também nega o acesso).
 * {@code badge}: contador exibido ao lado do item.
 */
export type NavItem = {
  to: string
  labelKey: string
  icon: LucideIcon
  end?: boolean
  managersOnly?: boolean
  badge?: 'pendingRequests'
}
export type NavGroup = { titleKey: string; items: NavItem[] }

export const MANAGER_NAV: NavGroup[] = [
  {
    titleKey: 'nav.groups.principal',
    items: [
      { to: '/', labelKey: 'nav.inicio', icon: Home, end: true },
      { to: '/aulas/presenca', labelKey: 'nav.chamada', icon: ClipboardCheck },
      { to: '/aulas', labelKey: 'nav.turmas', icon: CalendarDays, end: true },
      { to: '/alunos', labelKey: 'nav.alunos', icon: Users },
      { to: '/aulas/faixas', labelKey: 'nav.graduacoes', icon: Award },
      { to: '/videos', labelKey: 'nav.videos', icon: PlaySquare },
    ],
  },
  {
    titleKey: 'nav.groups.financeiro',
    items: [
      { to: '/financeiro/alunos', labelKey: 'nav.mensalidades', icon: Wallet, managersOnly: true },
      { to: '/financeiro/faturas', labelKey: 'nav.faturas', icon: Receipt, managersOnly: true },
      { to: '/mensalidades', labelKey: 'nav.planos', icon: CreditCard, managersOnly: true },
    ],
  },
  {
    titleKey: 'nav.groups.mais',
    items: [
      { to: '/requests', labelKey: 'nav.solicitacoes', icon: Inbox, badge: 'pendingRequests' },
      { to: '/relatorios', labelKey: 'nav.relatorios', icon: BarChart3, end: true },
      { to: '/relatorios/professores', labelKey: 'nav.professores', icon: UserCog },
      { to: '/equipe', labelKey: 'nav.equipe', icon: ShieldCheck, managersOnly: true },
      { to: SUBSCRIPTION_PATH, labelKey: 'nav.assinatura', icon: BadgeCheck, managersOnly: true },
      { to: '/configuracoes', labelKey: 'nav.configuracoes', icon: Settings },
    ],
  },
]

/** Só para o admin da plataforma, em qualquer papel na academia. */
const ADMIN_GROUP: NavGroup = {
  titleKey: 'nav.groups.plataforma',
  items: [{ to: ADMIN_PATH, labelKey: 'nav.admin', icon: Building2 }],
}

/** Atalhos da barra inferior da equipe no celular; o resto do menu fica em "Mais". */
const STAFF_TAB_PATHS = ['/', '/aulas/presenca', '/alunos', '/videos']

export const STAFF_TABS: NavItem[] = MANAGER_NAV.flatMap((g) => g.items)
  .filter((item) => STAFF_TAB_PATHS.includes(item.to))

/** Área do aluno: abas no topo (computador) e na barra inferior (celular). */
export const STUDENT_NAV: NavItem[] = [
  { to: STUDENT_HOME, labelKey: 'account.tabs.account', icon: UserRound, end: true },
  { to: `${STUDENT_HOME}/videos`, labelKey: 'account.tabs.videos', icon: PlaySquare },
  { to: `${STUDENT_HOME}/ajustes`, labelKey: 'account.tabs.settings', icon: Settings },
]

type NavOptions = { isPlatformAdmin?: boolean; subscriptionEnforced?: boolean }

/** Menu visível para o papel; grupos que ficam vazios somem. Sem assinatura ativa, o item dela some. */
export function navFor(
  isManager: boolean,
  { isPlatformAdmin = false, subscriptionEnforced = false }: NavOptions = {},
): NavGroup[] {
  const visible = (item: NavItem) =>
    (isManager || !item.managersOnly) && (subscriptionEnforced || item.to !== SUBSCRIPTION_PATH)
  const groups = MANAGER_NAV.map((g) => ({ ...g, items: g.items.filter(visible) }))
    .filter((g) => g.items.length > 0)
  return isPlatformAdmin ? [...groups, ADMIN_GROUP] : groups
}
