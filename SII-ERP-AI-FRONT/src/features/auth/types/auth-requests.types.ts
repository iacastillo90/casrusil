/**
 * Authentication Request Types
 * Strict type definitions for auth-related API requests
 */

export interface UpdateCompanyRequest {
    name?: string;
    rut?: string;
    address?: string;
    phone?: string;
    email?: string;
    logo?: string;
    taxRegime?: string;
    businessActivity?: string;
}

export interface CreateUserRequest {
    email: string;
    password: string;
    name: string;
    role: 'ADMIN' | 'ACCOUNTANT' | 'VIEWER';
    companyId: string;
}

export interface UpdateUserRequest {
    name?: string;
    email?: string;
    role?: 'ADMIN' | 'ACCOUNTANT' | 'VIEWER';
    isActive?: boolean;
}
