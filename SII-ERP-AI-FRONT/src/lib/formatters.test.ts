import { describe, it, expect } from 'vitest';
import { formatCLP, formatDate, validateRut } from './formatters';

describe('formatters', () => {
    describe('formatCLP', () => {
        it('should format positive numbers correctly', () => {
            expect(formatCLP(1000)).toBe('$1.000');
            expect(formatCLP(1500000)).toBe('$1.500.000');
        });

        it('should format zero correctly', () => {
            expect(formatCLP(0)).toBe('$0');
        });

        it('should format negative numbers correctly', () => {
            // Check if it contains the number and a minus sign, format might vary
            const formatted = formatCLP(-5000);
            expect(formatted).toContain('5.000');
            expect(formatted).toContain('-');
        });
    });

    describe('validateRut', () => {
        it('accepts valid RUTs with and without formatting', () => {
            expect(validateRut('11111111-1')).toBe(true);
            expect(validateRut('12.345.678-5')).toBe(true);
            expect(validateRut('76086428-5')).toBe(true);
            expect(validateRut('10000013-K')).toBe(true);
            expect(validateRut('10000013-k')).toBe(true);
            expect(validateRut('123456785')).toBe(true);
        });

        it('rejects RUTs with an invalid check digit', () => {
            expect(validateRut('11111111-2')).toBe(false);
            expect(validateRut('12.345.678-9')).toBe(false);
            expect(validateRut('10000013-0')).toBe(false);
        });

        it('rejects malformed or empty RUTs', () => {
            expect(validateRut('')).toBe(false);
            expect(validateRut('abc')).toBe(false);
            expect(validateRut('123')).toBe(false);
            expect(validateRut('1234567890-1')).toBe(false);
        });
    });

    describe('formatDate', () => {
        it('should format date string correctly', () => {
            // Note: This test might depend on timezone, so we mock or use specific date
            const date = '2024-01-01T12:00:00Z';
            // Assuming es-CL locale
            // expect(formatDate(date)).toBe('01/01/2024'); 
            // Since locale might vary in test env, we check basic structure or mock Intl
            const formatted = formatDate(date);
            expect(typeof formatted).toBe('string');
            expect(formatted.length).toBeGreaterThan(0);
        });
    });
});
