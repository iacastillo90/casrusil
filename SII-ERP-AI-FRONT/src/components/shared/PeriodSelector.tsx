"use client";

import * as React from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { format, addMonths, subMonths, parseISO } from "date-fns";
import { es } from "date-fns/locale";

interface PeriodSelectorProps {
    value: string; // YYYY-MM
    onChange: (value: string) => void;
}

export default function PeriodSelector({ value, onChange }: PeriodSelectorProps) {
    const date = parseISO(`${value}-01`);

    const handlePrevious = () => {
        onChange(format(subMonths(date, 1), "yyyy-MM"));
    };

    const handleNext = () => {
        onChange(format(addMonths(date, 1), "yyyy-MM"));
    };

    return (
        <div className="flex items-center space-x-2 bg-background border rounded-md p-1">
            <Button variant="ghost" size="icon" onClick={handlePrevious} className="h-8 w-8">
                <ChevronLeft className="h-4 w-4" />
            </Button>
            <div className="font-medium min-w-[120px] text-center capitalize">
                {format(date, "MMMM yyyy", { locale: es })}
            </div>
            <Button variant="ghost" size="icon" onClick={handleNext} className="h-8 w-8">
                <ChevronRight className="h-4 w-4" />
            </Button>
        </div>
    );
}
