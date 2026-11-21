import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { dmsService } from '../services/dms.service';

export const useDocuments = () => useQuery({ queryKey: ['dms', 'docs'], queryFn: dmsService.getDocuments });

export const useCreateDocument = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: dmsService.createDocument,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dms', 'docs'] }),
    });
};

export const useUploadDocumentFile = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ({
            file,
            title,
            documentType,
        }: {
            file: File;
            title?: string;
            documentType: string;
        }) => dmsService.uploadDocumentFile(file, { title, documentType }),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dms', 'docs'] }),
    });
};

export const useAnalyzeDocument = () => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: dmsService.analyzeDocument,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['dms', 'docs'] })
    });
};