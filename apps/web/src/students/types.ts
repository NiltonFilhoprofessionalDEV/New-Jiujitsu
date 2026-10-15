export type StudentKind = 'TRAINING' | 'RESPONSIBLE'
export type StudentStatus = 'ACTIVE' | 'INACTIVE'
export type BeltRuler = 'AUTO' | 'KIDS' | 'ADULT'
export type Gender = 'MALE' | 'FEMALE'

export type StatusFilter = 'active' | 'inactive' | 'all'
export type KindFilter = 'all' | 'training' | 'responsible'
export type AgeFilter = 'all' | 'kids' | 'adult' | 'under18' | 'adult18'

export type HouseholdRef = { id: string; name: string }

export type StudentListItem = {
  id: string
  name: string
  nickname: string | null
  email: string | null
  phone: string | null
  emergencyPhone: string | null
  kind: StudentKind
  status: StudentStatus
  belt: string | null
  degrees: number
  birthDate: string | null
  enrollmentDate: string
  household: HouseholdRef | null
  householdResponsible: boolean
  hasLogin: boolean
}

export type StudentDetail = StudentListItem & {
  cpf: string | null
  gender: Gender | null
  degreeAwardedAt: string | null
  beltRuler: BeltRuler
  giSize: string | null
  beltSize: string | null
  createdAt: string
}

export type StudentRequest = {
  kind: StudentKind
  name: string
  nickname: string | null
  email: string | null
  cpf: string | null
  phone: string | null
  emergencyPhone: string | null
  birthDate: string | null
  gender: Gender | null
  belt: string | null
  degrees: number | null
  degreeAwardedAt: string | null
  beltRuler: BeltRuler
  enrollmentDate: string | null
  giSize: string | null
  beltSize: string | null
}

export type StudentCounts = { all: number; training: number; responsible: number; hiddenInactive: number }

export type StudentQuery = {
  q: string
  status: StatusFilter
  kind: KindFilter
  age: AgeFilter
  household?: 'none'
  page: number
  size?: number
}

export type Page<T> = { content: T[]; totalElements: number; totalPages: number; number: number; size: number }

export type HouseholdSummary = {
  id: string
  name: string
  responsible: { id: string; name: string } | null
  memberCount: number
}

export type HouseholdDetail = {
  id: string
  name: string
  responsibleStudentId: string | null
  members: StudentListItem[]
}
