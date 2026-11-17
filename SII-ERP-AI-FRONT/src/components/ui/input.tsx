import * as React from "react"
import { cn } from "@/lib/utils"

export interface InputProps
    extends React.InputHTMLAttributes<HTMLInputElement> {
    /** Mensaje de error. Si se informa, marca el campo como inválido y lo asocia con `aria-describedby`. */
    error?: string
    /** Texto de ayuda debajo del campo (siempre visible). */
    helpText?: string
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
    (
        {
            className,
            type,
            id,
            error,
            helpText,
            "aria-describedby": ariaDescribedBy,
            ...props
        },
        ref
    ) => {
        const generatedId = React.useId()
        const inputId = id ?? generatedId
        const helpId = helpText ? `${inputId}-help` : undefined
        const errorId = error ? `${inputId}-error` : undefined
        const describedBy = [ariaDescribedBy, helpId, errorId].filter(Boolean).join(" ") || undefined

        const input = (
            <input
                id={inputId}
                type={type}
                aria-invalid={error ? true : undefined}
                aria-describedby={describedBy}
                className={cn(
                    "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
                    error && "border-destructive focus-visible:ring-destructive",
                    className
                )}
                ref={ref}
                {...props}
            />
        )

        if (!error && !helpText) {
            return input
        }

        return (
            <div className="w-full">
                {input}
                {helpText && (
                    <p id={helpId} className="mt-1 text-xs text-muted-foreground">
                        {helpText}
                    </p>
                )}
                {error && (
                    <p id={errorId} role="alert" className="mt-1 text-xs font-medium text-destructive">
                        {error}
                    </p>
                )}
            </div>
        )
    }
)
Input.displayName = "Input"

export { Input }
