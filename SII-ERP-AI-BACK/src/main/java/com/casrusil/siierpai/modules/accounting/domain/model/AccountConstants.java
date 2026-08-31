package com.casrusil.siierpai.modules.accounting.domain.model;

/**
 * 🏛️ SINGLE SOURCE OF TRUTH para códigos de cuentas contables.
 * 
 * Centraliza todos los códigos de cuentas para evitar discrepancias entre
 * el Seeder, el Importador y el Listener.
 */
public class AccountConstants {

    // ========== ACTIVOS (1.x.xx.xxx) ==========
    public static final String CAJA = "1.1.01.001";
    public static final String BANCO = "1.1.02.001"; // Reservado para Banco (no usado actualmente)
    public static final String EXISTENCIAS = "1.1.03.001";
    public static final String EXISTENCIAS_POR_CLASIFICAR = "1.1.03.999";
    public static final String CLIENTES_NACIONALES = "1.1.05.001"; // ✅ ÚNICA FUENTE DE VERDAD
    public static final String IVA_CREDITO = "1.1.08.001";
    public static final String ACTIVO_FIJO = "1.5.01.001"; // Maquinaria, Equipos, etc.

    // ========== PASIVOS (2.x.xx.xxx) ==========
    public static final String PROVEEDORES_NACIONALES = "2.1.01.001";
    public static final String PROVEEDORES_ENERGIA = "2.1.01.005";
    public static final String IVA_DEBITO = "2.1.04.001";

    // ========== PATRIMONIO (3.x.xx.xxx) ==========
    // (Agregar cuando sea necesario)

    // ========== INGRESOS (4.x.xx.xxx) ==========
    public static final String VENTAS_NACIONALES = "4.1.01.001";

    // ========== GASTOS (5.x.xx.xxx) ==========
    public static final String GASTOS_GENERALES = "5.1.03.025";
    public static final String GASTOS_POR_CLASIFICAR = "5.9.99.999";

    private AccountConstants() {
        // Clase de constantes, no instanciable
    }
}
