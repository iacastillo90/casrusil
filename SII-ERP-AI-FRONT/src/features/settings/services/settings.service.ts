import api from "@/lib/axios";
import { CompanyProfile, User, UserRole, NotificationPreferences } from "../types/settings.types";

interface CompanyApiDto {
    rut: string;
    razonSocial: string;
    email: string;
    commercialAddress: string;
    website?: string;
    phoneNumber: string;
    logoUrl?: string;
    isProfileComplete?: boolean;
}

interface UserApiDto {
    id: string | { value: string };
    fullName: string;
    email: string;
    role: string;
    active?: boolean;
    createdAt?: string;
}

const mapCompany = (dto: CompanyApiDto): CompanyProfile => ({
    name: dto.razonSocial ?? '',
    rut: dto.rut ?? '',
    address: dto.commercialAddress ?? '',
    email: dto.email ?? '',
    phone: dto.phoneNumber ?? '',
    website: dto.website ?? '',
    logoUrl: dto.logoUrl,
    isProfileComplete: dto.isProfileComplete ?? false,
});

const mapUser = (dto: UserApiDto): User => ({
    id: typeof dto.id === 'string' ? dto.id : dto.id.value,
    name: dto.fullName || dto.email,
    email: dto.email,
    role: dto.role as UserRole,
    status: dto.active === false ? 'INVITED' : 'ACTIVE',
});

const mapPreferences = (dto: Record<string, string> | null | undefined): NotificationPreferences => ({
    email: dto?.email !== 'false',
    push: dto?.push !== 'false',
    marketing: dto?.marketing === 'true',
});

/** El backend no tiene el rol VIEWER: se aproxima a USER. */
const toBackendRole = (role: string): string => (role === 'VIEWER' ? 'USER' : role);

export const settingsService = {
    // Company Profile
    updateCompanyProfile: async (data: FormData): Promise<CompanyProfile> => {
        const payload = {
            razonSocial: String(data.get('name') ?? ''),
            email: String(data.get('email') ?? ''),
            commercialAddress: String(data.get('address') ?? ''),
            website: String(data.get('website') ?? ''),
            phoneNumber: String(data.get('phone') ?? ''),
            logoUrl: data.get('logoUrl') ? String(data.get('logoUrl')) : undefined,
        };

        const { data: response } = await api.put<CompanyApiDto>('/company/profile', payload);
        return mapCompany(response);
    },

    getCompanyProfile: async (): Promise<CompanyProfile> => {
        const { data } = await api.get<CompanyApiDto>('/company/profile');
        return mapCompany(data);
    },

    // User Management
    getUsersList: async (): Promise<User[]> => {
        const { data } = await api.get<UserApiDto[]>('/company/users');
        return (data ?? []).map(mapUser);
    },

    inviteUser: async (email: string, role: string, name?: string): Promise<User> => {
        const backendRole = toBackendRole(role);
        await api.post('/sso/users/invite', { email, role: backendRole });

        return {
            id: `invited-${email}`,
            name: name || email,
            email,
            role: backendRole as UserRole,
            status: 'INVITED',
        };
    },

    removeUser: async (userId: string): Promise<void> => {
        await api.delete(`/sso/users/${userId}`);
    },

    // Notifications
    updateNotificationPreferences: async (prefs: NotificationPreferences): Promise<NotificationPreferences> => {
        const payload: Record<string, string> = {
            email: String(prefs.email),
            push: String(prefs.push),
            marketing: String(prefs.marketing),
        };
        const { data } = await api.put<Record<string, string>>('/notifications/preferences', payload);
        return mapPreferences(data);
    },

    getNotificationPreferences: async (): Promise<NotificationPreferences> => {
        const { data } = await api.get<Record<string, string>>('/notifications/preferences');
        return mapPreferences(data);
    },

};
