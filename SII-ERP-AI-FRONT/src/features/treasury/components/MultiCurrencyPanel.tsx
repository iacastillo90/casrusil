import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { ArrowRightLeft, Info } from 'lucide-react';

export const MultiCurrencyPanel = () => {
    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-lg flex items-center">
                    <ArrowRightLeft className="mr-2 h-5 w-5 text-green-600" />
                    Calculadora FX (Múltiples Divisas)
                </CardTitle>
            </CardHeader>
            <CardContent>
                <div className="flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">
                    <Info className="mt-0.5 h-4 w-4 shrink-0" />
                    <p>
                        Tipo de cambio pendiente de definición. La fuente de tipos de cambio
                        (USD, EUR, UF) aún no está definida por el negocio, por lo que no se
                        realiza conversión hasta que se acuerde el proveedor y el contrato.
                    </p>
                </div>
            </CardContent>
        </Card>
    );
};
