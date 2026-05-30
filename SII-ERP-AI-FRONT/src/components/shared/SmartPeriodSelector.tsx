"use client";

import { useState, useEffect } from "react";
import { format, startOfMonth, endOfMonth, parseISO } from "date-fns";
import { es } from "date-fns/locale";
import { Calendar as CalendarIcon, ArrowRightLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { DateRange } from "react-day-picker";

export interface PeriodRange {
    from: Date;
    to: Date;
    mode: 'MONTH' | 'RANGE';
}

interface Props {
    value: PeriodRange;
    onChange: (range: PeriodRange) => void;
    className?: string;
}

export function SmartPeriodSelector({ value, onChange, className }: Props) {
    const [mode, setMode] = useState<'MONTH' | 'RANGE'>(value.mode);

    // Generar últimos 12 meses para el select rápido
    const last12Months = Array.from({ length: 12 }, (_, i) => {
        const d = new Date();
        d.setMonth(d.getMonth() - i);
        return d;
    });

    const handleMonthSelect = (monthStr: string) => {
        const date = parseISO(monthStr);
        onChange({
            from: startOfMonth(date),
            to: endOfMonth(date),
            mode: 'MONTH'
        });
    };

    const handleRangeSelect = (range: DateRange | undefined) => {
        if (range?.from) {
            onChange({
                from: range.from,
                to: range.to || range.from, // Si no hay 'to', usa 'from' temporalmente
                mode: 'RANGE'
            });
        }
    };

    return (
        <div className={`flex items-center gap-2 ${className}`}>
            {/* Toggle Mode */}
            <div className="bg-muted p-1 rounded-lg flex text-xs font-medium">
                <button
                    onClick={() => setMode('MONTH')}
                    className={`px-3 py-1 rounded-md transition-all ${mode === 'MONTH' ? 'bg-white shadow text-primary' : 'text-muted-foreground hover:text-foreground'}`}
                >
                    Mensual
                </button>
                <button
                    onClick={() => setMode('RANGE')}
                    className={`px-3 py-1 rounded-md transition-all ${mode === 'RANGE' ? 'bg-white shadow text-primary' : 'text-muted-foreground hover:text-foreground'}`}
                >
                    Rango
                </button>
            </div>

            {mode === 'MONTH' ? (
                <Select
                    value={format(value.from, "yyyy-MM")}
                    onValueChange={handleMonthSelect}
                >
                    <SelectTrigger className="w-[200px] bg-white">
                        <CalendarIcon className="mr-2 h-4 w-4" />
                        <SelectValue placeholder="Seleccionar mes" />
                    </SelectTrigger>
                    <SelectContent>
                        {last12Months.map((date) => (
                            <SelectItem key={format(date, "yyyy-MM")} value={format(date, "yyyy-MM")}>
                                {format(date, "MMMM yyyy", { locale: es })}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            ) : (
                <Popover>
                    <PopoverTrigger asChild>
                        <Button variant="outline" className="w-[260px] justify-start text-left font-normal bg-white">
                            <CalendarIcon className="mr-2 h-4 w-4" />
                            {value.from ? (
                                value.to ? (
                                    <>
                                        {format(value.from, "dd/MM/yy")} - {format(value.to, "dd/MM/yy")}
                                    </>
                                ) : (
                                    format(value.from, "dd/MM/yy")
                                )
                            ) : (
                                <span>Seleccionar fechas</span>
                            )}
                        </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0" align="end">
                        <Calendar
                            initialFocus
                            mode="range"
                            defaultMonth={value.from}
                            selected={{ from: value.from, to: value.to }}
                            onSelect={handleRangeSelect}
                            numberOfMonths={2}
                            locale={es}
                        />
                    </PopoverContent>
                </Popover>
            )}
        </div>
    );
}
