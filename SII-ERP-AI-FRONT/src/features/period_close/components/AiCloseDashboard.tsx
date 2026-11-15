"use client";

import { useState } from "react";
import { usePeriodClose, type PeriodAuditResponse } from "../hooks/usePeriodClose";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

export function AiCloseDashboard() {
  const { auditPeriod } = usePeriodClose();
  const [analysis, setAnalysis] = useState<PeriodAuditResponse | null>(null);

  const runAiAudit = () => {
    const now = new Date();
    auditPeriod.mutate(
      { year: now.getFullYear(), month: now.getMonth() + 1 },
      {
        onSuccess: (data) => setAnalysis(data),
        onError: () =>
          setAnalysis(null),
      }
    );
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">Cierre de Mes con IA</h1>
        <Button onClick={runAiAudit} disabled={auditPeriod.isPending} className="bg-purple-600 hover:bg-purple-700">
          {auditPeriod.isPending ? "Analizando asientos..." : "Ejecutar Auditoría IA"}
        </Button>
      </div>

      {auditPeriod.isError && (
        <p className="text-sm text-destructive">
          Error al conectar con la Inteligencia Artificial. Revisá que el backend esté arriba y que las credenciales
          del modelo sean válidas.
        </p>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Análisis del Periodo {analysis ? `(${analysis.period})` : ""}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="min-h-[200px] space-y-4 p-4 bg-muted rounded-md whitespace-pre-wrap font-mono text-sm">
            {analysis ? (
              <>
                <div>
                  <span className="font-semibold">Listo para cerrar: </span>
                  {analysis.readyToClose ? "sí" : "no"}
                </div>
                {analysis.issuesFound.length > 0 && (
                  <ul className="list-disc pl-5">
                    {analysis.issuesFound.map((issue) => (
                      <li key={issue}>{issue}</li>
                    ))}
                  </ul>
                )}
                <p>{analysis.aiRecommendation}</p>
              </>
            ) : (
              <span className="text-muted-foreground">
                Presioná el botón superior para enviar el consolidado al modelo de lenguaje (LLM)...
              </span>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
