import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { projectsService, CreateProjectRequest } from '../services/projects.service';

export const useProjects = () => {
    return useQuery({
        queryKey: ['projects'],
        queryFn: projectsService.getProjects
    });
};

export const useProject = (id: string) => {
    return useQuery({
        queryKey: ['projects', id],
        queryFn: () => projectsService.getProject(id),
        enabled: !!id
    });
};

export const useCreateProject = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (data: CreateProjectRequest) => projectsService.createProject(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['projects'] });
        }
    });
};

export const useUpdateProjectStatus = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({ id, status }: { id: string, status: string }) => projectsService.updateProjectStatus(id, status),
        onSuccess: (_, { id }) => {
            queryClient.invalidateQueries({ queryKey: ['projects'] });
            queryClient.invalidateQueries({ queryKey: ['projects', id] });
        }
    });
};
