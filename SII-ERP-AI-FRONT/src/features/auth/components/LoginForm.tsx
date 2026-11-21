"use client";

import { useEffect } from "react";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";

import { authService } from "../services/auth.service";
import Link from "next/link";
import { useAuthStore } from '@/features/auth/stores/auth.store';

const loginSchema = z.object({
    email: z.string().email("Email inválido"),
    password: z.string().min(6, "La contraseña debe tener al menos 6 caracteres"),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export function LoginForm() {
    const sessionExpired = useAuthStore((state) => state.sessionExpired);
    const clearSessionExpired = useAuthStore((state) => state.clearSessionExpired);

    useEffect(() => {
        if (!sessionExpired) return;
        toast.error("Tu sesión expiró. Vuelve a iniciar sesión.");
        clearSessionExpired();
    }, [sessionExpired, clearSessionExpired]);

    const router = useRouter();
    const [isLoading, setIsLoading] = useState(false);
    // Sin JS el navegador enviaría el formulario de forma nativa. El botón no
    // se habilita hasta que React esté montado.
    const [hydrated, setHydrated] = useState(false);

    useEffect(() => setHydrated(true), []);

    const form = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
        defaultValues: {
            email: "",
            password: "",
        },
    });

    async function onSubmit(data: LoginFormValues) {
        setIsLoading(true);
        try {
            // 1. Llamada al Backend Real (el servicio ya actualiza el store via handleAuthSuccess)
            const response = await authService.login({
                email: data.email,
                password: data.password,
            });

            // 2. Notificación
            toast.success("¡Bienvenido!", {
                description: `Has iniciado sesión como ${response.userName}`,
            });

            // 3. Redirección
            router.push('/dashboard');
            router.refresh();

        } catch (error: unknown) {
            // Manejo de errores con type guard
            const errorMessage = error instanceof Error
                ? error.message
                : (error && typeof error === 'object' && 'response' in error && error.response && typeof error.response === 'object' && 'data' in error.response && error.response.data && typeof error.response.data === 'object' && 'message' in error.response.data)
                    ? String(error.response.data.message)
                    : 'Error al iniciar sesión';

            toast.error("Error de autenticación", {
                description: errorMessage,
            });

            // Resetear password si falla
            form.setValue('password', '');
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <Card className="w-full max-w-sm border-border/60 shadow-sm">
            <CardHeader className="space-y-1">
                <CardTitle className="text-2xl font-semibold tracking-tight">Iniciar Sesión</CardTitle>
                <CardDescription>
                    Ingresa tu email para acceder a tu cuenta
                </CardDescription>
            </CardHeader>
            <CardContent>
                <Form {...form}>
                    {/* method="post" evita que un submit nativo (sin JS) mande la
                        contraseña en la query string. action="" no expone datos. */}
                    <form method="post" action="" onSubmit={form.handleSubmit(onSubmit)} className="space-y-5">
                        <FormField
                            control={form.control}
                            name="email"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Correo Electrónico</FormLabel>
                                    <FormControl>
                                        <Input
                                            placeholder="nombre@empresa.com"
                                            type="email"
                                            autoComplete="email"
                                            disabled={isLoading}
                                            {...field}
                                        />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />

                        <FormField
                            control={form.control}
                            name="password"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Contraseña</FormLabel>
                                    <FormControl>
                                        <Input
                                            placeholder="••••••••"
                                            type="password"
                                            autoComplete="current-password"
                                            disabled={isLoading}
                                            {...field}
                                        />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />

                        <Button
                            type="submit"
                            className="w-full"
                            loading={isLoading}
                            disabled={!hydrated}
                        >
                            Iniciar Sesión
                        </Button>
                    </form>
                </Form>
            </CardContent>
            <CardFooter className="flex justify-center">
                <div className="text-sm text-center text-muted-foreground">
                    ¿No tienes cuenta?{" "}
                    <Link href="/register" className="font-medium text-primary underline-offset-4 hover:underline">
                        Regístrate
                    </Link>
                </div>
            </CardFooter>
        </Card>
    );
}
