/**
 * @file api.ts
 * @description Re-export del cliente HTTP unificado.
 *
 * FIX #22: Anteriormente existían dos instancias de axios separadas:
 * - `api.ts`   → instancia básica sin interceptor de companyId ni logout automático
 * - `axios.ts` → instancia completa con todos los interceptors
 *
 * Ahora `api.ts` es un alias de `axios.ts` para mantener compatibilidad con
 * imports existentes (`import { api } from '@/lib/api'`) sin duplicar código.
 *
 * @example
 * // Ambos imports usan el mismo cliente HTTP ahora:
 * import api from '@/lib/axios';
 * import { api } from '@/lib/api';
 */
export { default, default as api, apiClient } from './axios';
export type { ApiResponse, ApiError } from './axios';
