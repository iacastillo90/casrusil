"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";

export function AiCloseDashboard() {
  const [analysis, setAnalysis] = useState<string>("");
  const [loading, setLoading] = useState(false);

  const runAiAudit = async () => {
    try {
      setLoading(true);
      // Ajustar con la ruta real del Controller
      const response = await api.post('/period-close/audit/ai', { periodId: 1 });
      setAnalysis(response.data.analysis);
    } catch (error) {
      console.error("Error running AI audit:", error);
      setAnalysis("Error al conectar con la Inteligencia Artificial. Revisa que el backend esté arriba y que las credenciales (Gemini/OpenAI) sean válidas.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">Cierre de Mes con IA</h1>
        <Button onClick={runAiAudit} disabled={loading} className="bg-purple-600 hover:bg-purple-700">
          {loading ? "Analizando asientos..." : "Ejecutar Auditoría IA"}
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Análisis del Periodo</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="min-h-[200px] p-4 bg-muted rounded-md whitespace-pre-wrap font-mono text-sm">
            {analysis ? (
              analysis
            ) : (
              <span className="text-muted-foreground">Presiona el botón superior para enviar el consolidado al modelo de lenguaje (LLM)...</span>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
