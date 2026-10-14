export type GraduationKind = 'BELT' | 'DEGREE'

export type Graduation = {
  id: string
  kind: GraduationKind
  belt: string
  degrees: number
  graduatedOn: string
  priorTrainings: number | null
  exception: boolean
  note: string | null
  recordedBy: string | null
  /** Só a mais recente: removê-la devolve o aluno à faixa/grau anterior. */
  removable: boolean
}

export type RuleTarget = {
  minMonths: number | null
  minTrainings: number | null
  trainingsPerDegree: number | null
}

export type Progress = {
  beltSince: string
  monthsAtBelt: number
  trainingsSinceBelt: number
  lastSince: string
  trainingsSinceLast: number
  ready: GraduationKind | null
  /** {@code null} na faixa preta e acima: segue a régua fixa da IBJJF. */
  rule: RuleTarget | null
}

export type StudentGraduations = {
  history: Graduation[]
  /** {@code null} para aluno inativo: não há progresso a acompanhar. */
  progress: Progress | null
}

export type ReadyStudent = {
  studentId: string
  name: string
  nickname: string | null
  belt: string
  degrees: number
  kind: GraduationKind
  since: string
  months: number
  trainings: number
}

export type BeltRule = RuleTarget & {
  belt: string
  /** {@code false}: valor padrão, a academia ainda não alterou esta faixa. */
  custom: boolean
}

export type GraduationRequest = {
  kind: GraduationKind
  belt: string | null
  degrees: number
  graduatedOn: string
  priorTrainings: number | null
  exception: boolean
  note: string | null
}
