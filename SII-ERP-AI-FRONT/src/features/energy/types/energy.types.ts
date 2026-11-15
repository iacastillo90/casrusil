export interface EnergyStation {
    id: string;
    name: string;
    type: 'CHARGING' | 'GRID' | 'SOLAR' | 'BATTERY';
    status: 'ACTIVE' | 'MAINTENANCE' | 'OFFLINE';
    connected: boolean;
    maxCapacityKW: number;
    currentLoadKW?: number;
    connectorCount?: number;
}

export interface EnergyMetrics {
    totalConsumptionKW: number;
    estimatedDailyCost: number;
    peakConsumptionKW: number;
    totalStations: number;
    activeStations: number;
    averageLoadPercent: number;
}

export interface EnergyAlert {
    id: string;
    stationId: string;
    type: 'VOLTAGE_DROP' | 'OVERLOAD' | 'TEMPERATURE' | 'CONNECTIVITY';
    severity: 'LOW' | 'MEDIUM' | 'HIGH';
    message: string;
    timestamp: string;
    resolved: boolean;
}

export interface ChargeSession {
    id: string;
    stationId: string;
    connectorId: string;
    vehicleId?: string;
    startTime: string;
    endTime?: string;
    energyConsumedKWH: number;
    cost: number;
    status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
}