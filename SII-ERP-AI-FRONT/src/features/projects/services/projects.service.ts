import { api } from '@/lib/api';

export interface Project {
    id: string;
    companyId: string;
    name: string;
    description: string;
    startDate: string;
    endDate?: string;
    status: 'ACTIVE' | 'COMPLETED' | 'ON_HOLD';
    budget: number;
}

export interface CreateProjectRequest {
    name: string;
    description: string;
    budget: number;
}

export const projectsService = {
    getProjects: async (): Promise<Project[]> => {
        const response = await api.get('/projects');
        return response.data;
    },
    
    getProject: async (id: string): Promise<Project> => {
        const response = await api.get(`/projects/${id}`);
        return response.data;
    },
    
    createProject: async (data: CreateProjectRequest): Promise<Project> => {
        const response = await api.post('/projects', data);
        return response.data;
    },
    
    updateProjectStatus: async (id: string, status: string): Promise<Project> => {
        const response = await api.put(`/projects/${id}/status`, { status });
        return response.data;
    }
};
