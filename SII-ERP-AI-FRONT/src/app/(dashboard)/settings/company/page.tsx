"use client";

import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { settingsService } from "@/features/settings/services/settings.service";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Loader2 } from "lucide-react";
import { useState } from "react";

type CompanyFormValues = {
    name: string;
    rut: string;
    address: string;
    email: string;
    phone: string;
};

export default function CompanySettingsPage() {
    const [loading, setLoading] = useState(false);
    const form = useForm<CompanyFormValues>({
        defaultValues: {
            name: "",
            rut: "",
            address: "",
            email: "",
            phone: "",
        }
    });

    const onSubmit = async (values: CompanyFormValues) => {
        setLoading(true);
        try {
            const formData = new FormData();
            Object.entries(values).forEach(([key, value]) => formData.append(key, value as string));
            await settingsService.updateCompanyProfile(formData);
            toast.success("Empresa actualizada correctamente");
        } catch (e) {
            toast.error("Error al guardar");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-6">
            <h1 className="text-3xl font-bold tracking-tight">Configuración de Empresa</h1>
            <Card>
                <CardHeader><CardTitle>Datos Fiscales</CardTitle></CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                            <FormField control={form.control} name="name" render={({field}) => (
                                <FormItem><FormLabel>Nombre/Razón Social</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage /></FormItem>
                            )} />
                            <FormField control={form.control} name="rut" render={({field}) => (
                                <FormItem><FormLabel>RUT</FormLabel><FormControl><Input {...field} /></FormControl><FormMessage /></FormItem>
                            )} />
                            <Button type="submit" disabled={loading}>{loading ? <Loader2 className="animate-spin" /> : "Guardar"}</Button>
                        </form>
                    </Form>
                </CardContent>
            </Card>
        </div>
    );
}
