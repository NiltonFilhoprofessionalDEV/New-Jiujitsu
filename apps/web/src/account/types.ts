import type { InvoiceSituation } from '@/billing/types'
import type { GraduationKind } from '@/graduations/types'
import type { StudentKind, StudentStatus } from '@/students/types'

export type Profile = {
  id: string
  name: string
  nickname: string | null
  kind: StudentKind
  belt: string | null
  degrees: number
  status: StudentStatus
  /** O próprio cadastro da conta logada; os demais são dependentes. */
  self: boolean
}

type StaffRef = { membershipId: string; name: string }

export type ScheduledClass = {
  classId: string
  title: string
  weekdays: number[]
  startTime: string
  endTime: string | null
  location: string | null
  instructor: StaffRef | null
}

export type TodayClass = Omit<ScheduledClass, 'weekdays'> & {
  enrolled: boolean
  checkedIn: boolean
  /** A presença foi marcada pela própria conta; a do professor só ele desfaz. */
  canUndo: boolean
  opensAt: string
  closesAt: string
  open: boolean
}

export type Overview = {
  student: Profile
  classes: ScheduledClass[]
  attendanceTotal: number
  recentAttendance: { date: string; title: string }[]
  graduations: { kind: GraduationKind; belt: string; degrees: number; graduatedOn: string }[]
  progress: { beltSince: string; monthsAtBelt: number; trainingsSinceBelt: number } | null
}

export type PortalInvoice = {
  id: string
  description: string
  amountCents: number
  dueDate: string
  situation: InvoiceSituation
  paidOn: string | null
}
