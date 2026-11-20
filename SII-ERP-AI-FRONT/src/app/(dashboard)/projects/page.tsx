"use client";

import { Info, Pause, Play, CheckCircle } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { PageHeader } from "@/features/accounting/components/PageHeader";
import { EmptyState, ErrorState } from "@/features/accounting/components/DataStates";
import { useProjects, useUpdateProjectStatus } from "@/features/projects/hooks/useProjects";
import { formatCLP } from "@/lib/formatters";

const STATUS_LABEL: Record<string, string> = {
    ACTIVE: "Activo",
    ON_HOLD: "En pausa",
    COMPLETED: "Completado",
};

export default function ProjectsPage() {
    const { data: projects, isLoading, error, refetch, isFetching } = useProjects();
    const updateStatus = useUpdateProjectStatus();

    return (
        <div className="space-y-6">
            <PageHeader
                title="Proyectos y centros de costo"
                description="Presupuesto y estado de cada proyecto. El P&L por proyecto todavía no está expuesto por el backend."
            />

            {error ? (
                <ErrorState
                    message={error instanceof Error ? error.message : "Error al cargar los proyectos"}
                    onRetry={() => refetch()}
                    retrying={isFetching && !isLoading}
                />
            ) : isLoading ? (
                <p className="text-sm text-muted-foreground">Cargando proyectos…</p>
            ) : !projects || projects.length === 0 ? (
                <EmptyState
                    title="Sin centros de costo"
                    description="Todavía no hay proyectos registrados en esta empresa."
                />
            ) : (
                <>
                    <div className="flex items-start gap-2 rounded-lg border border-info-subtle-foreground/25 bg-info-subtle px-4 py-3 text-sm text-info-subtle-foreground">
                        <Info className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
                        <p>
                            Gastos e ingresos por proyecto no se muestran: el backend no expone un endpoint de P&amp;L
                            por centro de costo. Se habilitará cuando exista.
                        </p>
                    </div>

                    <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
                        {projects.map((project) => (
                            <Card key={project.id} className="flex flex-col">
                                <CardHeader className="border-b pb-3">
                                    <div className="flex items-start justify-between gap-2">
                                        <div>
                                            <CardTitle className="text-lg">{project.name}</CardTitle>
                                            <p className="mt-1 text-xs text-muted-foreground">
                                                {project.id.split("-")[0].toUpperCase()}
                                            </p>
                                        </div>
                                        <Badge variant="outline">{STATUS_LABEL[project.status] ?? project.status}</Badge>
                                    </div>
                                </CardHeader>
                                <CardContent className="flex flex-1 flex-col pt-4">
                                    <p className="mb-4 flex-1 text-sm text-muted-foreground">
                                        {project.description || "Sin descripción"}
                                    </p>
                                    <div className="mb-4 flex justify-between text-sm">
                                        <span className="text-muted-foreground">Presupuesto</span>
                                        <span className="font-medium">{formatCLP(project.budget)}</span>
                                    </div>
                                    <div className="flex justify-end gap-2 border-t pt-2">
                                        {project.status !== "ACTIVE" && (
                                            <Button
                                                variant="outline"
                                                size="sm"
                                                aria-label="Activar proyecto"
                                                loading={updateStatus.isPending}
                                                onClick={() => updateStatus.mutate({ id: project.id, status: "ACTIVE" })}
                                            >
                                                <Play className="h-4 w-4" />
                                            </Button>
                                        )}
                                        {project.status === "ACTIVE" && (
                                            <Button
                                                variant="outline"
                                                size="sm"
                                                aria-label="Pausar proyecto"
                                                loading={updateStatus.isPending}
                                                onClick={() => updateStatus.mutate({ id: project.id, status: "ON_HOLD" })}
                                            >
                                                <Pause className="h-4 w-4" />
                                            </Button>
                                        )}
                                        {project.status !== "COMPLETED" && (
                                            <Button
                                                variant="outline"
                                                size="sm"
                                                aria-label="Completar proyecto"
                                                loading={updateStatus.isPending}
                                                onClick={() => updateStatus.mutate({ id: project.id, status: "COMPLETED" })}
                                            >
                                                <CheckCircle className="h-4 w-4" />
                                            </Button>
                                        )}
                                    </div>
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
}
