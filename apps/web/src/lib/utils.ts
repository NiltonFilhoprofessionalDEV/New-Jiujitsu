import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

/** Junta classes condicionais; em conflito de utilities do Tailwind, a última vence. */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
