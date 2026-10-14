import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import { GRADUATIONS } from '@/graduations/graduationsApi'
import { api } from '@/lib/api'

import type {
  AttendanceSession,
  ClassRequest,
  ClassStudent,
  DayClass,
  SaveSessionInput,
  StaffMember,
  TrainingClass,
} from './types'

const CLASSES = 'classes'
const ATTENDANCE = 'attendance'
const STAFF = 'staff'

export function useClasses() {
  return useQuery({
    queryKey: [CLASSES, 'list'],
    queryFn: async () => (await api.get<TrainingClass[]>('/classes')).data,
  })
}

export function useClassStudents(classId: string) {
  return useQuery({
    queryKey: [CLASSES, 'students', classId],
    queryFn: async () => (await api.get<ClassStudent[]>(`/classes/${classId}/students`)).data,
  })
}

export function useStaff() {
  return useQuery({
    queryKey: [STAFF],
    queryFn: async () => (await api.get<StaffMember[]>('/staff')).data,
  })
}

/** Aulas, inscrições e chamada aparecem umas nas outras (contagens, título, professor): invalida as duas áreas. */
function useClassMutation<TVars, TResult>(fn: (vars: TVars) => Promise<TResult>) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: [CLASSES] }),
        queryClient.invalidateQueries({ queryKey: [ATTENDANCE] }),
      ])
    },
  })
}

export function useSaveClass() {
  return useClassMutation(async ({ id, body }: { id?: string; body: ClassRequest }) =>
    id
      ? (await api.put<TrainingClass>(`/classes/${id}`, body)).data
      : (await api.post<TrainingClass>('/classes', body)).data,
  )
}

export function useArchiveClass() {
  return useClassMutation(async (id: string) => {
    await api.delete(`/classes/${id}`)
  })
}

export function useEnrollment(classId: string) {
  return useClassMutation(async ({ studentId, enroll }: { studentId: string; enroll: boolean }) =>
    enroll
      ? (await api.post<ClassStudent[]>(`/classes/${classId}/students`, { studentId })).data
      : (await api.delete<ClassStudent[]>(`/classes/${classId}/students/${studentId}`)).data,
  )
}

export function useDayClasses(date: string) {
  return useQuery({
    queryKey: [ATTENDANCE, 'day', date],
    queryFn: async () => (await api.get<DayClass[]>('/attendance/day', { params: { date } })).data,
  })
}

const sessionKey = (classId: string, date: string) => [ATTENDANCE, 'session', classId, date]

export function useAttendanceSession(classId: string, date: string) {
  return useQuery({
    queryKey: sessionKey(classId, date),
    queryFn: async () =>
      (await api.get<AttendanceSession>(`/attendance/classes/${classId}/sessions/${date}`)).data,
  })
}

export function useSaveAttendance(classId: string, date: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: async (body: SaveSessionInput) =>
      (await api.put<AttendanceSession>(`/attendance/classes/${classId}/sessions/${date}`, body)).data,
    onSuccess: async (session) => {
      queryClient.setQueryData(sessionKey(classId, date), session)
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: [ATTENDANCE, 'day', date] }),
        queryClient.invalidateQueries({ queryKey: [GRADUATIONS] }),
      ])
    },
  })
}
