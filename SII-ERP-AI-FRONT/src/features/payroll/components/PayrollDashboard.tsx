"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";

interface PayslipResponse {
  payslipId: number;
  employeeId: number;
  employeeName: string;
  baseSalary: number;
  netSalary: number;
}

export function PayrollDashboard() {
  const [payslips, setPayslips] = useState<PayslipResponse[]>([]);
  const [loading, setLoading] = useState(false);

  const calculatePayroll = async () => {
    try {
      setLoading(true);
      // Ajustar con la ruta real del Controller
      const response = await api.post('/payroll/calculate', { periodId: 1 });
      setPayslips(response.data);
    } catch (error) {
      console.error("Error calculating payroll:", error);
    } finally {
      setLoading(false);
    }
  };

  const downloadPdf = async (payslipId: number) => {
    try {
      const response = await api.get(`/payroll/payslip/${payslipId}/pdf`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `liquidacion_${payslipId}.pdf`);
      document.body.appendChild(link);
      link.click();
    } catch (error) {
      console.error("Error downloading PDF", error);
    }
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">Remuneraciones</h1>
        <Button onClick={calculatePayroll} disabled={loading}>
          {loading ? "Calculando..." : "Cerrar Remuneraciones del Mes"}
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Liquidaciones Generadas</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Empleado ID</TableHead>
                <TableHead>Nombre</TableHead>
                <TableHead className="text-right">Sueldo Base</TableHead>
                <TableHead className="text-right">A Pagar (Líquido)</TableHead>
                <TableHead className="text-right">Acciones</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {payslips.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} className="text-center">No hay liquidaciones en el periodo actual.</TableCell>
                </TableRow>
              ) : (
                payslips.map((p) => (
                  <TableRow key={p.payslipId}>
                    <TableCell className="font-medium">{p.employeeId}</TableCell>
                    <TableCell>{p.employeeName || "Empleado " + p.employeeId}</TableCell>
                    <TableCell className="text-right">${p.baseSalary?.toFixed(0)}</TableCell>
                    <TableCell className="text-right font-bold text-green-600">${p.netSalary?.toFixed(0)}</TableCell>
                    <TableCell className="text-right">
                      <Button variant="outline" size="sm" onClick={() => downloadPdf(p.payslipId)}>
                        Descargar PDF
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
