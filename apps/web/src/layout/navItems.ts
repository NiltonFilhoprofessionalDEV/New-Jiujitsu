import {
  Award,
  BarChart3,
  Bell,
  CalendarDays,
  ClipboardList,
  CreditCard,
  DollarSign,
  Dumbbell,
  FileText,
  Home,
  MessageCircle,
  Receipt,
  Settings,
  ShieldCheck,
  UserCog,
  Users,
  type LucideIcon,
} from 'lucide-react'

/** {@code managersOnly}: some do menu do professor (a API também nega o acesso). */
export type NavItem = { to: string; labelKey: string; icon: LucideIcon; end?: boolean; managersOnly?: boolean }
export type NavGroup = { titleKey: string; items: NavItem[] }

export const MANAGER_NAV: NavGroup[] = [
  {
    titleKey: 'nav.groups.academia',
    items: [
      { to: '/', labelKey: 'nav.inicio', icon: Home, end: true },
      { to: '/alunos', labelKey: 'nav.alunos', icon: Users },
      { to: '/aulas', labelKey: 'nav.aulas', icon: Dumbbell },
      { to: '/equipe', labelKey: 'nav.equipe', icon: ShieldCheck, managersOnly: true },
      { to: '/mensalidades', labelKey: 'nav.planosDeMensalidade', icon: CreditCard, managersOnly: true },
      { to: '/certificados', labelKey: 'nav.certificados', icon: Award },
    ],
  },
  {
    titleKey: 'nav.groups.financeiro',
    items: [
      { to: '/financeiro', labelKey: 'nav.movimentacoes', icon: DollarSign, end: true, managersOnly: true },
      { to: '/financeiro/alunos', labelKey: 'nav.mensalidade', icon: Users, managersOnly: true },
      { to: '/financeiro/faturas', labelKey: 'nav.faturas', icon: Receipt, managersOnly: true },
      { to: '/financeiro/cobranca', labelKey: 'nav.cobranca', icon: Settings, managersOnly: true },
    ],
  },
  {
    titleKey: 'nav.groups.relatorios',
    items: [
      { to: '/relatorios', labelKey: 'nav.relatorios', icon: BarChart3, end: true },
      { to: '/relatorios/professores', labelKey: 'nav.professores', icon: UserCog },
    ],
  },
  {
    titleKey: 'nav.groups.comunicacao',
    items: [
      { to: '/mensagens', labelKey: 'nav.mensagens', icon: MessageCircle },
      { to: '/requests', labelKey: 'nav.solicitacoes', icon: ClipboardList },
      { to: '/notificacoes', labelKey: 'nav.notificacoes', icon: Bell },
    ],
  },
  {
    titleKey: 'nav.groups.conta',
    items: [
      { to: '/calendario', labelKey: 'nav.calendario', icon: CalendarDays },
      { to: '/configuracoes', labelKey: 'nav.configuracoes', icon: FileText },
    ],
  },
]

/** Menu visível para o papel; grupos que ficam vazios somem. */
export function navFor(isManager: boolean): NavGroup[] {
  if (isManager) return MANAGER_NAV
  return MANAGER_NAV.map((g) => ({ ...g, items: g.items.filter((item) => !item.managersOnly) }))
    .filter((g) => g.items.length > 0)
}
