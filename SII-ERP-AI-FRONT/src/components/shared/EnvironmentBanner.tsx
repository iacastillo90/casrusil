"use client";

import { useEffect, useState } from "react";
import { FlaskConical } from "lucide-react";

import { api } from "@/lib/api";

function normalize(value: string | undefined): "certificacion" | "produccion" {
    const normalized = (value ?? "").trim().toLowerCase();
    return normalized === "produccion" || normalized === "production" ? "produccion" : "certificacion";
}

/**
 * Avisa que el ambiente SII activo es certificación (maullin) y que los DTE
 * emitidos no tienen validez tributaria.
 *
 * <p>
 * El ambiente se toma de {@code GET /api/v1/sii/status} cuando el backend lo
 * expone (campo {@code ambiente}); mientras no exista, se usa
 * {@code NEXT_PUBLIC_SII_ENV}. En producción el banner no se renderiza.
 * </p>
 */
export function EnvironmentBanner() {
    const configured = normalize(process.env.NEXT_PUBLIC_SII_ENV);
    const [ambiente, setAmbiente] = useState<"certificacion" | "produccion">(configured);

    useEffect(() => {
        api.get<{ environment?: string; ambiente?: string }>("/sii/status")
            .then(({ data }) => {
                const backendValue = data?.environment ?? data?.ambiente;
                if (backendValue) {
                    setAmbiente(normalize(backendValue));
                }
            })
            .catch(() => {
                // Sin permisos o backend caído: se mantiene el valor configurado.
            });
    }, []);

    if (ambiente === "produccion") {
        return null;
    }

    return (
        <div
            role="status"
            className="flex items-center gap-2 border-b border-warning-subtle-foreground/20 bg-warning-subtle px-4 py-2 text-xs font-medium text-warning-subtle-foreground"
        >
            <FlaskConical className="h-4 w-4 shrink-0" aria-hidden="true" />
            <span>
                Ambiente de certificación SII (maullin): los documentos emitidos son de prueba y no tienen validez
                tributaria.
            </span>
        </div>
    );
}

export default EnvironmentBanner;
