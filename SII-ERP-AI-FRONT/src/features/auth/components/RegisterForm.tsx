"use client";

import { useEffect } from "react";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { toast } from "sonner";
import Link from "next/link";
import { validateRut, formatRut } from "@/lib/formatters";
import { authService } from "../services/auth.service";

const registerSchema = z.object({
    rut: z.string().refine(validateRut, "RUT inválido"),
    razonSocial: z.string().min(1, "Razón Social requerida"),
    email: z.string().email("Email inválido"),
    password: z.string().min(6, "Contraseña debe tener al menos 6 caracteres"),
    confirmPassword: z.string()
}).refine((data) => data.password === data.confirmPassword, {
    message: "Las contraseñas no coinciden",
    path: ["confirmPassword"],
});

type RegisterFormData = z.infer<typeof registerSchema>;

export function RegisterForm() {
    const router = useRouter();
    const [isLoading, setIsLoading] = useState(false);
    // Evita el submit nativo antes de hidratar (no debe haber datos en la URL).
    const [hydrated, setHydrated] = useState(false);

    useEffect(() => setHydrated(true), []);

    const { register, handleSubmit, setValue, formState: { errors } } = useForm<RegisterFormData>({
        resolver: zodResolver(registerSchema),
    });

    const onSubmit = async (data: RegisterFormData) => {
        setIsLoading(true);
        try {
            // Register creates the company and user, and might return token/login right away
            // Or just success. Assuming it returns AuthResponse similar to login or we redirect to login.
            // The prompt/plan implied redirect to login, but if register returns token we could auto-login.
            // Let's stick to plan: redirect to login.
            // Wait, if it auto-logs in, that's better UX.
            // The service returns AuthResponse in my implementation plan.

            await authService.register({
                rut: data.rut,
                razonSocial: data.razonSocial,
                adminEmail: data.email,
                adminPassword: data.password
            });

            toast.success("Cuenta creada exitosamente. Por favor inicia sesión.");
            router.push("/login");
        } catch (error: unknown) {
            const errorMessage = error instanceof Error
                ? error.message
                : 'Error al crear cuenta';

            toast.error(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Card className="w-full max-w-md border-border/60 shadow-sm">
            <CardHeader className="space-y-1">
                <CardTitle className="text-2xl font-semibold tracking-tight">Crear Cuenta</CardTitle>
                <CardDescription>
                    Registra tu empresa para comenzar
                </CardDescription>
            </CardHeader>
            <form method="post" action="" onSubmit={handleSubmit(onSubmit)}>
                <CardContent className="grid gap-4">
                    <div className="grid gap-2">
                        <Label htmlFor="rut">RUT Empresa</Label>
                        <Input
                            id="rut"
                            autoComplete="off"
                            {...register("rut")}
                            onBlur={(e) => setValue("rut", formatRut(e.target.value))}
                            placeholder="76.543.210-K"
                        />
                        {errors.rut && <span className="text-xs text-destructive">{errors.rut.message}</span>}
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="razonSocial">Razón Social</Label>
                        <Input id="razonSocial" autoComplete="organization" {...register("razonSocial")} />
                        {errors.razonSocial && <span className="text-xs text-destructive">{errors.razonSocial.message}</span>}
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="email">Email Admin</Label>
                        <Input id="email" type="email" autoComplete="email" {...register("email")} />
                        {errors.email && <span className="text-xs text-destructive">{errors.email.message}</span>}
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="password">Contraseña</Label>
                        <Input id="password" type="password" autoComplete="new-password" {...register("password")} />
                        {errors.password && <span className="text-xs text-destructive">{errors.password.message}</span>}
                    </div>
                    <div className="grid gap-2">
                        <Label htmlFor="confirmPassword">Confirmar Contraseña</Label>
                        <Input id="confirmPassword" type="password" autoComplete="new-password" {...register("confirmPassword")} />
                        {errors.confirmPassword && <span className="text-xs text-destructive">{errors.confirmPassword.message}</span>}
                    </div>
                </CardContent>
                <CardFooter className="flex flex-col gap-4">
                    <Button
                        className="w-full"
                        type="submit"
                        loading={isLoading}
                        disabled={!hydrated}
                    >
                        Registrarse
                    </Button>
                    <div className="text-sm text-center text-muted-foreground">
                        ¿Ya tienes cuenta?{" "}
                        <Link href="/login" className="font-medium text-primary underline-offset-4 hover:underline">
                            Inicia Sesión
                        </Link>
                    </div>
                </CardFooter>
            </form>
        </Card>
    );
}
