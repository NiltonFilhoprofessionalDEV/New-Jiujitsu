export type Period = { from: string; to: string }

type Bucket = { count: number; cents: number }

export type ClassRow = { classId: string; title: string; instructor: string | null; sessions: number; checkIns: number }

export type InstructorRow = { membershipId: string; name: string; classes: number; sessions: number; checkIns: number }

export type StudentRow = { studentId: string; name: string; belt: string | null; degrees: number; checkIns: number }

export type AbsentRow = { studentId: string; name: string; lastCheckIn: string | null }

export type Report = {
  period: Period
  students: { active: number; inactive: number; newInPeriod: number }
  attendance: { checkIns: number; sessions: number; studentsTrained: number }
  classes: ClassRow[]
  instructors: InstructorRow[]
  topStudents: StudentRow[]
  absent: { since: string; total: number; students: AbsentRow[] }
  /** Só para gestores. */
  finance: { billed: Bucket; received: Bucket; overdue: { students: number; cents: number } } | null
}
