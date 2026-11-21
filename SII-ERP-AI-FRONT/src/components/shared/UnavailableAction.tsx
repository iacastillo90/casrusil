"use client";

import type { ReactNode } from "react";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";

interface UnavailableActionProps {
    children: ReactNode;
    reason: string;
}

/**
 * Envuelve una acción deshabilitada para explicar por qué no está disponible.
 * Evita botones que no hacen nada sin avisar.
 */
export function UnavailableAction({ children, reason }: UnavailableActionProps) {
    return (
        <TooltipProvider>
            <Tooltip>
                <TooltipTrigger asChild>
                    <span tabIndex={0} className="inline-flex">
                        {children}
                    </span>
                </TooltipTrigger>
                <TooltipContent>{reason}</TooltipContent>
            </Tooltip>
        </TooltipProvider>
    );
}
