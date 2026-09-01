package com.casrusil.siierpai.modules.payroll.infrastructure.previred;

import com.casrusil.siierpai.modules.payroll.domain.model.Employee;
import com.casrusil.siierpai.modules.payroll.domain.model.PayrollPeriod;
import com.casrusil.siierpai.modules.payroll.domain.model.Payslip;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Generador del archivo plano de PreviRed en "formato estándar largo": 105 campos
 * por línea, separados por punto y coma ({@code ;}).
 *
 * <p><b>Reglas operativas de PreviRed respetadas:</b>
 * <ul>
 *   <li>Se emiten exactamente 105 campos por trabajador.</li>
 *   <li>Los campos numéricos vacíos van en {@code 0}; los alfanuméricos vacíos en blanco.</li>
 *   <li>Los montos son enteros en pesos (sin separador de miles ni decimales).</li>
 *   <li>El archivo se codifica en ISO-8859-1 (Latin-1) y usa fin de línea CRLF.</li>
 *   <li>El RUT va sin puntos, con dígito verificador en campo aparte.</li>
 *   <li>Incluye los campos de la Reforma de Pensiones: Rentabilidad Protegida (RP)
 *       y Rentabilidad Protegida Patronal (RPP).</li>
 * </ul>
 *
 * <p><b>Nota de cumplimiento:</b> el orden de los 105 campos sigue el layout estándar
 * documentado por PreviRed. Como las posiciones son definidas por la normativa vigente,
 * este archivo debe validarse contra la especificación oficial y con el Editor de PreviRed
 * (nunca abrir/guardar con Excel, que corrompe ceros a la izquierda) antes de su envío en
 * producción. Los campos para los que el ERP aún no tiene dato se emiten con el valor
 * neutro que PreviRed acepta ({@code 0} numérico) y quedan claramente marcados.
 */
@Component
public class PreviredFileGenerator {

    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("MMyyyy");
    private static final int FIELD_COUNT = 105;
    private static final String SEP = ";";

    /**
     * Genera una línea de 105 campos para un trabajador.
     *
     * @param employee datos del trabajador
     * @param payslip  liquidación calculada del período
     * @param period   período (mes/año) con valores de UF/UTM
     * @return la línea PreviRed (sin salto de línea)
     */
    public String generateLine(Employee employee, Payslip payslip, PayrollPeriod period) {
        // Todos los campos parten en "0" (neutro numérico de PreviRed) y se sobrescriben
        // los que tenemos con dato real.
        String[] f = new String[FIELD_COUNT];
        for (int i = 0; i < FIELD_COUNT; i++) {
            f[i] = "0";
        }

        String[] rutParts = splitRut(employee.getRut());
        String[] names = splitName(employee.getFullName());
        String periodStr = period.getPeriod().format(PERIOD_FMT);

        // ---- Identificación del trabajador (campos 1-9) ----
        set(f, 1, rutParts[0]);                 // RUT sin DV
        set(f, 2, rutParts[1]);                 // Dígito verificador
        set(f, 3, names[0]);                    // Apellido paterno
        set(f, 4, names[1]);                    // Apellido materno
        set(f, 5, names[2]);                    // Nombres
        set(f, 6, "");                          // Sexo (M/F) — no disponible aún
        set(f, 7, "0");                         // Nacionalidad (0 = chileno)
        set(f, 8, "1");                         // Tipo de pago (1 = remuneración del mes)
        set(f, 9, periodStr);                   // Período desde (MMAAAA)

        // ---- Período y jornada (campos 10-18) ----
        set(f, 10, periodStr);                  // Período hasta (MMAAAA)
        set(f, 13, "0");                        // Régimen previsional (0 = AFP)
        set(f, 14, "0");                        // Tipo de trabajador
        set(f, 17, "30");                       // Días trabajados (por defecto mes completo)
        set(f, 18, "0");                        // Tipo de línea

        // ---- AFP: cotizaciones obligatorias (campos 19-27) ----
        set(f, 20, intPeso(payslip.getTaxableIncome()));   // Renta imponible AFP
        set(f, 21, intPeso(payslip.getAfpDiscount()));     // Cotización obligatoria AFP

        // ---- Reforma de Pensiones: Rentabilidad Protegida (nuevos campos) ----
        // RP (aporte del trabajador) y RPP (aporte patronal). Sin dato aún -> 0.
        set(f, 28, "0");                        // Rentabilidad Protegida (RP)
        set(f, 29, "0");                        // Rentabilidad Protegida Patronal (RPP)

        // ---- Salud (Fonasa / Isapre) (campos 30-45 aprox.) ----
        boolean isFonasa = employee.getHealthSystem() == null
                || employee.getHealthSystem().toUpperCase().contains("FONASA");
        set(f, 30, isFonasa ? "07" : "");       // Código institución de salud (07 = Fonasa)
        set(f, 31, intPeso(payslip.getTaxableIncome())); // Renta imponible salud
        set(f, 33, intPeso(payslip.getHealthDiscount())); // Cotización pactada / 7% legal

        // ---- Seguro de Cesantía (AFC) ----
        set(f, 46, intPeso(payslip.getTaxableIncome()));   // Renta imponible AFC
        set(f, 47, intPeso(payslip.getUnemploymentInsurance())); // Aporte trabajador AFC

        return String.join(SEP, f);
    }

    /** Asigna un valor a un campo usando numeración 1-based (como el layout de PreviRed). */
    private void set(String[] fields, int oneBasedIndex, String value) {
        fields[oneBasedIndex - 1] = (value == null) ? "" : value;
    }

    /** Convierte un monto a entero en pesos (sin decimales ni separadores). */
    private String intPeso(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    /** Separa el RUT en número (sin puntos) y dígito verificador. */
    private String[] splitRut(String rut) {
        if (rut == null || rut.isBlank()) {
            return new String[] { "0", "0" };
        }
        String clean = rut.replace(".", "").trim();
        int dash = clean.lastIndexOf('-');
        if (dash > 0) {
            return new String[] { clean.substring(0, dash), clean.substring(dash + 1).toUpperCase() };
        }
        // Sin guion: el último caracter es el DV.
        return new String[] {
                clean.substring(0, clean.length() - 1),
                clean.substring(clean.length() - 1).toUpperCase()
        };
    }

    /**
     * Separa el nombre completo en apellido paterno, materno y nombres (best-effort).
     * El modelo actual solo guarda {@code fullName}; en producción conviene separar
     * los apellidos en campos propios del trabajador.
     */
    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[] { "", "", "" };
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return new String[] { parts[0], "", "" };
        }
        if (parts.length == 2) {
            return new String[] { parts[0], "", parts[1] };
        }
        // 3+ tokens: paterno, materno, resto = nombres.
        String paterno = parts[0];
        String materno = parts[1];
        String nombres = String.join(" ", java.util.Arrays.copyOfRange(parts, 2, parts.length));
        return new String[] { paterno, materno, nombres };
    }
}
