"use client";

import React from 'react';
import { useProjects, useUpdateProjectStatus } from '@/features/projects/hooks/useProjects';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Plus, Play, Pause, CheckCircle } from 'lucide-react';
import { Project } from '@/features/projects/services/projects.service';

export default function ProjectsPage() {
    const { data: projects, isLoading } = useProjects();
    const updateStatus = useUpdateProjectStatus();

    if (isLoading) {
        return <div className="p-8 text-center text-gray-500">Cargando proyectos...</div>;
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold tracking-tight">Proyectos y Centros de Costo</h2>
                    <p className="text-muted-foreground">Mide la rentabilidad (P&L) individual de cada proyecto.</p>
                </div>
                <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    Nuevo Proyecto
                </Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {projects?.map((project: Project) => (
                    <Card key={project.id} className="flex flex-col">
                        <CardHeader className="pb-3 border-b">
                            <div className="flex justify-between items-start">
                                <div>
                                    <CardTitle className="text-lg">{project.name}</CardTitle>
                                    <p className="text-xs text-gray-500 mt-1">{project.id.split('-')[0].toUpperCase()}</p>
                                </div>
                                <span className={`px-2 py-1 rounded-full text-xs font-medium
                                    ${project.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                                      project.status === 'ON_HOLD' ? 'bg-yellow-100 text-yellow-800' :
                                      project.status === 'COMPLETED' ? 'bg-blue-100 text-blue-800' :
                                      'bg-gray-100 text-gray-800'}`}>
                                    {project.status}
                                </span>
                            </div>
                        </CardHeader>
                        <CardContent className="pt-4 flex-1 flex flex-col">
                            <p className="text-sm text-gray-600 mb-4 flex-1">{project.description}</p>
                            
                            <div className="space-y-2 mb-4 text-sm">
                                <div className="flex justify-between">
                                    <span className="text-gray-500">Presupuesto</span>
                                    <span className="font-medium">${project.budget.toLocaleString('es-CL')}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-500">Gastos a la fecha</span>
                                    <span className="font-medium text-red-600">$0</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-500">Ingresos</span>
                                    <span className="font-medium text-green-600">$0</span>
                                </div>
                            </div>

                            <div className="flex justify-end space-x-2 pt-2 border-t">
                                {project.status !== 'ACTIVE' && (
                                    <Button variant="outline" size="sm" onClick={() => updateStatus.mutate({ id: project.id, status: 'ACTIVE' })}>
                                        <Play className="h-4 w-4 text-green-600" />
                                    </Button>
                                )}
                                {project.status === 'ACTIVE' && (
                                    <Button variant="outline" size="sm" onClick={() => updateStatus.mutate({ id: project.id, status: 'ON_HOLD' })}>
                                        <Pause className="h-4 w-4 text-yellow-600" />
                                    </Button>
                                )}
                                {project.status !== 'COMPLETED' && (
                                    <Button variant="outline" size="sm" onClick={() => updateStatus.mutate({ id: project.id, status: 'COMPLETED' })}>
                                        <CheckCircle className="h-4 w-4 text-blue-600" />
                                    </Button>
                                )}
                            </div>
                        </CardContent>
                    </Card>
                ))}
                
                {(!projects || projects.length === 0) && (
                    <div className="col-span-full text-center py-12 text-muted-foreground bg-gray-50 rounded-lg border border-dashed">
                        No has creado ningún centro de costo todavía.
                    </div>
                )}
            </div>
        </div>
    );
}
