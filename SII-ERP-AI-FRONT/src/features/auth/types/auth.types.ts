export type UserRole =
    | 'ADMIN'
    | 'ACCOUNTANT'
    | 'TREASURER'
    | 'HR'
    | 'SALES'
    | 'OPERATIONS'
    | 'AUDITOR'
    | 'USER';

export interface User {
    id: string;
    email: string;
    name: string;
    role: UserRole;
}

export interface Company {
    id: string;
    rut: string;
    razonSocial: string;
}

export interface LoginCredentials {
    email: string;
    password: string;
}

export interface RegisterData {
    rut: string;
    razonSocial: string;
    adminEmail: string;
    adminPassword: string;
}

export interface AuthResponse {
    token: string;
    userId: string;
    userName: string;
    companyId: string;
    companyName: string;
    role: UserRole;
}
