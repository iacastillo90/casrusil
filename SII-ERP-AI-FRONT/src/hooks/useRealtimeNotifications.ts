import { usePushNotifications } from './usePushNotifications';

export function useRealtimeNotifications() {
    // We delegate to usePushNotifications which now handles real SSE connection
    // and toast displaying. This avoids duplicate connections/toasts.
    usePushNotifications();
}
