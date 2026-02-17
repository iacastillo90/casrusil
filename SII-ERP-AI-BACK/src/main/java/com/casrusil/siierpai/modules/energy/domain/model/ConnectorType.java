package com.casrusil.siierpai.modules.energy.domain.model;

/**
 * Enum: Tipos de conectores según estándares internacionales.
 */
public enum ConnectorType {
    
    // Tipo 1 (SAE J1772) - Común en América del Norte y Asia
    TYPE_1("Type 1 (SAE J1772)", "AC", 7.4),
    
    // Tipo 2 (IEC 62196) - Estándar europeo
    TYPE_2("Type 2 (Mennekes)", "AC", 22.0),
    
    // CCS Tipo 1 (Combined Charging System)
    CCS_TYPE_1("CCS Type 1", "DC", 350.0),
    
    // CCS Tipo 2 (Combined Charging System)
    CCS_TYPE_2("CCS Type 2", "DC", 350.0),
    
    // CHAdeMO - Estándar japonés
    CHADEMO("CHAdeMO", "DC", 62.5),
    
    // Tesla Supercharger
    TESLA("Tesla Supercharger", "DC", 250.0),
    
    // GB/T - Estándar chino
    GB_T_AC("GB/T AC", "AC", 7.0),
    GB_T_DC("GB/T DC", "DC", 237.0);
    
    private final String displayName;
    private final String currentType; // AC o DC
    private final double maxPowerKw;
    
    ConnectorType(String displayName, String currentType, double maxPowerKw) {
        this.displayName = displayName;
        this.currentType = currentType;
        this.maxPowerKw = maxPowerKw;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCurrentType() {
        return currentType;
    }
    
    public double getMaxPowerKw() {
        return maxPowerKw;
    }
    
    public boolean isDC() {
        return "DC".equals(currentType);
    }
    
    public boolean isAC() {
        return "AC".equals(currentType);
    }
    
    public boolean isFastCharging() {
        return maxPowerKw > 50.0;
    }
}
