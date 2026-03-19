import type { AxiosError } from 'axios'
import type { ErrorResponse } from '../types'

export function getApiError(error: unknown, fallback = 'Something went wrong'): string {
  const axiosError = error as AxiosError<ErrorResponse>
  return axiosError?.response?.data?.message ?? fallback
}
