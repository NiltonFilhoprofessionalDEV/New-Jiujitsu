import type { MembershipRole } from '@/auth/types'
import type { StudentStatus } from '@/students/types'

/** ISO-8601: 1 = segunda … 7 = domingo. */
export type Weekday = 1 | 2 | 3 | 4 | 5 | 6 | 7

export type StaffRef = { membershipId: string; name: string }
export type StaffMember = StaffRef & { role: MembershipRole }

export type TrainingClass = {
  id: string
  title: string
  description: string | null
  weekdays: Weekday[]
  /** {@code HH:mm} ou {@code HH:mm:ss}. */
  startTime: string
  endTime: string | null
  location: string | null
  instructor: StaffRef | null
  capacity: number | null
  enrolledCount: number
}

export type ClassRequest = {
  title: string
  description: string | null
  weekdays: Weekday[]
  startTime: string
  endTime: string | null
  location: string | null
  instructorMembershipId: string | null
  capacity: number | null
}

export type ClassStudent = {
  id: string
  name: string
  nickname: string | null
  belt: string | null
  degrees: number
  status: StudentStatus
}

export type DayClass = {
  classId: string
  title: string
  startTime: string
  endTime: string | null
  location: string | null
  instructor: StaffRef | null
  enrolled: number
  present: number
  /** Fora da grade semanal: aberta pelo professor neste dia. */
  extra: boolean
}

export type RosterEntry = { student: ClassStudent; enrolled: boolean; present: boolean }

export type AttendanceSession = {
  classId: string
  title: string
  date: string
  startTime: string
  endTime: string | null
  instructor: StaffRef | null
  notes: string | null
  canMark: boolean
  extra: boolean
  roster: RosterEntry[]
}

export type SaveSessionInput = { presentStudentIds: string[]; notes: string | null }
