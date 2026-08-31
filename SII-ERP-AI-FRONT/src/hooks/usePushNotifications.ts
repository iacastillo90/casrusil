import { useEffect, useState } from 'react';
import { toast } from 'sonner';

export const usePushNotifications = () => {
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        let eventSource: EventSource | null = null;

        const connect = () => {
            try {
                // Connect to real SSE endpoint. In a real app, pass the userId or token.
                // For demo purposes, we pass userId=1
                eventSource = new EventSource('http://localhost:8080/api/v1/notifications/subscribe?userId=1');

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
    }, []);

    return { isConnected };
};
