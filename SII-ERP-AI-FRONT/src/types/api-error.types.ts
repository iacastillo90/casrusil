/**
 * API Error Types
 * Strict type definitions and utilities for API error handling
 */

export interface ApiError {
    message: string;
    status?: number;
    code?: string;
    details?: Record<string, any>;
}

export interface ApiErrorResponse {
    response?: {
        data?: {
            message?: string;
            error?: string;
        };
        status?: number;
    };
    message?: string;
}

/**
 * Type guard to check if an error is an ApiError
 */
export function isApiError(error: unknown): error is ApiError {
    return typeof error === 'object' && error !== null && 'message' in error;
}

/**
 * Type guard to check if an error has a response property
 */
export function isApiErrorResponse(error: unknown): error is ApiErrorResponse {
    return typeof error === 'object' && error !== null && 'response' in error;
}

/**
 * Extract a user-friendly error message from any error type
 */
export function getErrorMessage(error: unknown): string {
    // Check for API error response format (Axios)
    if (isApiErrorResponse(error)) {
        return error.response?.data?.message ||
            error.response?.data?.error ||
            error.message ||
            'An error occurred';
    }

    // Check for standard Error
    if (error instanceof Error) {
        return error.message;
    }

    // Check for ApiError interface
    if (isApiError(error)) {
        return error.message;
    }

    // Fallback for unknown error types
    if (typeof error === 'string') {
        return error;
    }

    return 'An unknown error occurred';
}
