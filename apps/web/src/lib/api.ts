import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'

import type { Session } from '@/auth/types'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api/v2'

const AUTH_PATHS = [
  '/auth/login',
  '/auth/register-academy',
  '/auth/refresh',
  '/auth/logout',
  '/public/academy-invites',
]

let accessToken: string | null = null
let refreshInFlight: Promise<Session | null> | null = null
let onSessionChange: (session: Session | null) => void = () => {}

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
  timeout: 30_000,
})

export function setAccessToken(token: string | null) {
  accessToken = token
}

export function subscribeSession(listener: (session: Session | null) => void) {
  onSessionChange = listener
}

/** Renova a sessão pelo cookie HttpOnly. Chamadas simultâneas compartilham a mesma requisição. */
export function refreshSession(): Promise<Session | null> {
  refreshInFlight ??= api
    .post<Session>('/auth/refresh')
    .then(({ data }) => {
      setAccessToken(data.accessToken)
      onSessionChange(data)
      return data
    })
    .catch(() => {
      setAccessToken(null)
      onSessionChange(null)
      return null
    })
    .finally(() => {
      refreshInFlight = null
    })
  return refreshInFlight
}

api.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`
  return config
})

type RetriableConfig = InternalAxiosRequestConfig & { _retried?: boolean }

api.interceptors.response.use(undefined, async (error: AxiosError) => {
  const config = error.config as RetriableConfig | undefined
  const isAuthCall = AUTH_PATHS.some((p) => config?.url?.startsWith(p))
  if (error.response?.status !== 401 || !config || config._retried || isAuthCall) {
    throw error
  }
  config._retried = true
  const session = await refreshSession()
  if (!session) throw error
  return api(config)
})

export type ApiProblem = { code?: string; detail?: string; fields?: Record<string, string> }

export function problemOf(error: unknown): ApiProblem {
  if (axios.isAxiosError<ApiProblem>(error) && error.response?.data) return error.response.data
  return {}
}
