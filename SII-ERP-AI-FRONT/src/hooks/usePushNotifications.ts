import { useEffect, useState } from 'react';
import { toast } from 'sonner';
import { useAuthStore } from '@/features/auth/stores/auth.store';

export const usePushNotifications = () => {
    const [isConnected, setIsConnected] = useState(false);
    const userId = useAuthStore((state) => state.user?.id);

    useEffect(() => {
        let eventSource: EventSource | null = null;

        const connect = () => {
            if (!userId) return;
            try {
                // Same-origin: el proxy de Next reenvía /api/v1 al backend.
                eventSource = new EventSource(`/api/v1/notifications/subscribe?userId=${encodeURIComponent(userId)}`);

                eventSource.onopen = () => {
                    setIsConnected(true);
                };

                eventSource.onmessage = (event) => {
                    try {
                        const data = JSON.parse(event.data);
                        toast(data.title || 'Notificación', {
                            description: data.message || data.content,
                        });
                    } catch (e) {
                        console.error('Error parsing notification', e);
                    }
                };

                eventSource.onerror = () => {
                    setIsConnected(false);
                    eventSource?.close();
                };
            } catch (err) {
                console.error("Failed to connect to SSE", err);
            }
        };

        connect();

        return () => {
            if (eventSource) {
                eventSource.close();
            }
            setIsConnected(false);
        };
    }, [userId]);

    return { isConnected };
};
