import {
  Award,
  BarChart3,
  CalendarDays,
  ClipboardCheck,
  CreditCard,
  Home,
  Inbox,
  Receipt,
  Settings,
  ShieldCheck,
  UserCog,
  Users,
  Wallet,
  type LucideIcon,
} from 'lucide-react'

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
      { to: '/configuracoes', labelKey: 'nav.configuracoes', icon: Settings },
    ],
  },
]

/** Menu visível para o papel; grupos que ficam vazios somem. */
export function navFor(isManager: boolean): NavGroup[] {
  if (isManager) return MANAGER_NAV
  return MANAGER_NAV.map((g) => ({ ...g, items: g.items.filter((item) => !item.managersOnly) }))
    .filter((g) => g.items.length > 0)
}
