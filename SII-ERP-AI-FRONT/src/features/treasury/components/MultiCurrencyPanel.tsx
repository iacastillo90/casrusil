import React, { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { ArrowRightLeft, DollarSign } from 'lucide-react';
import { treasuryService } from '../services/treasury.service';

export const MultiCurrencyPanel = () => {
    const [amount, setAmount] = useState('1000');
    const [fromCurrency, setFromCurrency] = useState('USD');
    const [toCurrency, setToCurrency] = useState('CLP');

    const [rates, setRates] = useState<Record<string, number>>({});

    useEffect(() => {
        treasuryService.getExchangeRates().then(setRates);
    }, []);

    const convert = () => {
        const key = `${fromCurrency}_${toCurrency}`;
        const rate = rates[key] || 1;
        return (parseFloat(amount || '0') * rate).toLocaleString('es-CL');
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-lg flex items-center">
                    <ArrowRightLeft className="mr-2 h-5 w-5 text-green-600" />
                    Calculadora FX (Múltiples Divisas)
                </CardTitle>
            </CardHeader>
            <CardContent>
                <div className="flex items-end gap-2">
                    <div className="flex-1 space-y-1">
                        <label className="text-xs text-gray-500">Monto</label>
                        <Input type="number" value={amount} onChange={e => setAmount(e.target.value)} />
                    </div>
                    <div className="w-24 space-y-1">
                        <label className="text-xs text-gray-500">De</label>
                        <Select value={fromCurrency} onValueChange={setFromCurrency}>
                            <SelectTrigger><SelectValue/></SelectTrigger>
                            <SelectContent>
                                <SelectItem value="USD">USD</SelectItem>
                                <SelectItem value="EUR">EUR</SelectItem>
                                <SelectItem value="UF">UF</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    <div className="w-24 space-y-1">
                        <label className="text-xs text-gray-500">A</label>
                        <Select value={toCurrency} onValueChange={setToCurrency}>
                            <SelectTrigger><SelectValue/></SelectTrigger>
                            <SelectContent>
                                <SelectItem value="CLP">CLP</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                </div>

                <div className="mt-6 p-4 bg-gray-50 rounded-md border flex justify-between items-center">
                    <span className="text-sm font-medium text-gray-600">Valor Convertido:</span>
                    <span className="text-xl font-bold text-gray-900">${convert()}</span>
                </div>
            </CardContent>
        </Card>
    );
};
