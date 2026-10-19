import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'

import { DataItem } from '@/components/DataItem'

import { formatPhone } from '@/lib/masks'

import { ageFrom } from './belts'
import { formatCpf, formatDate } from './format'
import type { StudentDetail } from './types'

type Fact = { label: string; value: ReactNode }
type Group = { title: string; facts: Fact[] }

/** Só mostra o que foi preenchido: campo vazio não vira travessão, e grupo vazio some. */
function useFactGroups(student: StudentDetail): Group[] {
  const { t } = useTranslation()
  const age = ageFrom(student.birthDate)
  const training = student.kind === 'TRAINING'

  const groups: Group[] = [
    {
      title: t('students.facts.contact'),
      facts: [
        { label: t('students.form.email'), value: student.email },
        { label: t('students.form.phone'), value: formatPhone(student.phone) },
        { label: t('students.form.emergencyContact'), value: student.emergencyContact },
        { label: t('students.form.emergencyPhone'), value: formatPhone(student.emergencyPhone) },
      ],
    },
    {
      title: t('students.facts.personal'),
      facts: [
        { label: t('students.form.birthDate'),
          value: student.birthDate && `${formatDate(student.birthDate)} · ${t('students.years', { count: age ?? 0 })}` },
        { label: t('students.form.cpf'), value: student.cpf && formatCpf(student.cpf) },
        { label: t('students.form.gender'), value: student.gender && t(`students.gender.${student.gender}`) },
      ],
    },
    {
      title: t('students.facts.training'),
      facts: training
        ? [
            { label: t('students.form.degreeAwardedAt'), value: student.degreeAwardedAt && formatDate(student.degreeAwardedAt) },
            { label: t('students.form.beltRuler'),
              value: student.beltRuler !== 'AUTO' && t(`students.ruler.${student.beltRuler}`) },
          ]
        : [{ label: t('students.form.enrollmentDate', { context: student.kind }), value: formatDate(student.enrollmentDate) }],
    },
  ]
  return groups
    .map((g) => ({ ...g, facts: g.facts.filter((f) => Boolean(f.value)) }))
    .filter((g) => g.facts.length > 0)
}

export function StudentFacts({ student }: { student: StudentDetail }) {
  const groups = useFactGroups(student)
  if (groups.length === 0) return null

  return (
    <div className="facts">
      {groups.map((group) => (
        <section key={group.title} className="fact-group" aria-label={group.title}>
          <h3 className="fact-title">{group.title}</h3>
          <dl className="data-list">
            {group.facts.map((f) => <DataItem key={f.label} label={f.label} value={f.value} />)}
          </dl>
        </section>
      ))}
    </div>
  )
}
