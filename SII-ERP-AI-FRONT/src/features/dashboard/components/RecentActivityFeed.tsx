'use client';

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { useRecentActivity } from "../hooks/useRecentActivity";
import { Loader2 } from "lucide-react";
import { formatCLP } from "@/lib/formatters";

export function RecentActivityFeed() {
    const { data: activities, isLoading } = useRecentActivity();

    return (
        <Card className="col-span-3">
            <CardHeader>
                <CardTitle>Actividad Reciente</CardTitle>
                <CardDescription>Últimos movimientos registrados en el sistema.</CardDescription>
            </CardHeader>
            <CardContent>
                {isLoading ? (
                    <div className="flex justify-center p-8"><Loader2 className="animate-spin" /></div>
                ) : (
                    <div className="space-y-8">
                        {activities?.map((item) => (
                            <div key={item.id} className="flex items-center">
                                <Avatar className="h-9 w-9">
                                    <AvatarFallback>{item.user.slice(0, 2).toUpperCase()}</AvatarFallback>
                                </Avatar>
                                <div className="ml-4 space-y-1">
                                    <p className="text-sm font-medium leading-none">{item.description}</p>
                                    <p className="text-sm text-muted-foreground">{item.user}</p>
                                </div>
                                <div className="ml-auto font-medium">{formatCLP(item.amount)}</div>
                            </div>
                        ))}
                    </div>
                )}
            </CardContent>
        </Card>
    );
}
