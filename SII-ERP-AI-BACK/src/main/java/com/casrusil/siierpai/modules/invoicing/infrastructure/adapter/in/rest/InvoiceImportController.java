package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.casrusil.siierpai.modules.partners.domain.model.Partner;
import com.casrusil.siierpai.modules.partners.domain.model.PartnerType;
import com.casrusil.siierpai.modules.partners.domain.port.out.PartnerRepository;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;

@RestController
@RequestMapping("/api/v1/invoices/import")
@PreAuthorize("hasAnyRole('ADMIN','SALES','ACCOUNTANT')")
public class InvoiceImportController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceImportController.class);
    private final CreateInvoiceUseCase createInvoiceUseCase;
    private final com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository accountRepository;
    private final PartnerRepository partnerRepository;
    private final CompanyRepository companyRepository;

    // Formato fecha SII: dd/MM/yyyy (ej: 30/09/2025)
    private static final DateTimeFormatter SII_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public InvoiceImportController(CreateInvoiceUseCase createInvoiceUseCase,
            com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository accountRepository,
            PartnerRepository partnerRepository,
            CompanyRepository companyRepository) {
        this.createInvoiceUseCase = createInvoiceUseCase;
        this.accountRepository = accountRepository;
        this.partnerRepository = partnerRepository;
        this.companyRepository = companyRepository;
    }

    @PostMapping(value = { "", "/sii-csv" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importSiiCsv(@RequestParam("file") MultipartFile file,
            @RequestParam("bookType") String bookType) {

        // La empresa sale del JWT (CompanyContext); nunca de la petición (R1).
        final CompanyId companyId = CompanyContext.requireCompanyId();

        if (file.isEmpty())
            return ResponseEntity.badRequest().body(new ImportResult(0, 0, "Archivo vacío"));

        TransactionType transactionType;
        if ("COMPRA".equalsIgnoreCase(bookType) || "PURCHASE".equalsIgnoreCase(bookType)) {
            transactionType = TransactionType.PURCHASE;
        } else if ("VENTA".equalsIgnoreCase(bookType) || "SALE".equalsIgnoreCase(bookType)) {
            transactionType = TransactionType.SALE;
        } else {
            return ResponseEntity.badRequest()
                    .body(new ImportResult(0, 0, "Tipo de libro inválido. Use COMPRA o VENTA"));
        }

        // 🔥 "GOD MODE": Pre-crear cuentas críticas ANTES de procesar nada
        // Esto evita condiciones de carrera y asegura que el 100% de las facturas pasen
        // CRITICAL: Usa transacción independiente para que persistan incluso si falla
        // una factura
        // El RUT propio es el de la empresa autenticada (F08); sin él no se importa.
        final String ownRut = companyRepository.findById(companyId)
                .map(Company::getRut)
                .filter(rut -> !rut.isBlank())
                .orElse(null);
        if (ownRut == null) {
            return ResponseEntity.unprocessableEntity().body(new ImportResult(0, 0,
                    "La empresa no tiene RUT registrado; complétalo en el perfil de la empresa antes de importar"));
        }

        ensureCriticalAccountsExistIsolated(companyId);

        int successCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        // Configuración CSV para formato SII (delimitador punto y coma)
        // FIX: .setQuote(null) es vital para evitar que glosas con medidas (ej: 3")
        // rompan el parser
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(';')
                .setQuote(null)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .build();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.ISO_8859_1)); // SII suele usar ISO-8859-1
                                                                                            // (ANSI)
                CSVParser csvParser = new CSVParser(reader, format)) {

            for (CSVRecord record : csvParser) {
                try {
                    // Mapeo dinámico según tipo de libro (Compra vs Venta tienen nombres de columna
                    // distintos)
                    Integer typeCode = Integer.parseInt(record.get("Tipo Doc"));
                    Long folio = Long.parseLong(record.get("Folio"));

                    // SII usa "Fecha Docto" en ambos, pero a veces varía
                    String dateStr = record.isMapped("Fecha Docto") ? record.get("Fecha Docto")
                            : record.get("Fecha Emision");
                    LocalDate date = LocalDate.parse(dateStr, SII_DATE_FORMATTER);

                    String issuerRut;
                    String receiverRut;

                    if (transactionType == TransactionType.PURCHASE) {
                        issuerRut = record.get("RUT Proveedor");
                        receiverRut = ownRut;
                    } else {
                        issuerRut = ownRut;
                        receiverRut = record.get("Rut cliente");
                    }

                    // Montos (Manejo de nulos y nombres variados)
                    BigDecimal netAmount = parseAmount(record, "Monto Neto");
                    BigDecimal taxAmount = parseAmount(record, "Monto IVA", "Monto IVA Recuperable");
                    BigDecimal totalAmount = parseAmount(record, "Monto Total", "Monto total"); // Ojo
                                                                                                // mayúscula/minúscula

                    // Extraer Razón Social
                    String businessName = null;
                    if (record.isMapped("Razon Social")) {
                        businessName = record.get("Razon Social");
                    } else if (record.isMapped("Razón Social")) {
                        businessName = record.get("Razón Social");
                    }

                    // 🔥 EJECUTAR AUTO-ENROLLMENT
                    // CRITICAL: Usa transacción independiente para que el Partner persista
                    boolean isSale = (transactionType == TransactionType.SALE);
                    String partnerRut = (transactionType == TransactionType.PURCHASE) ? issuerRut : receiverRut;
                    ensurePartnerExistsIsolated(companyId, partnerRut, businessName, isSale);

                    Invoice invoice = Invoice.create(
                            companyId,
                            InvoiceType.fromCode(typeCode),
                            folio,
                            issuerRut,
                            receiverRut,
                            businessName, // ✅ Nuevo campo
                            date,
                            netAmount,
                            taxAmount,
                            totalAmount,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            Invoice.ORIGIN_MANUAL,
                            transactionType,
                            Collections.emptyList());

                    // En ventas, la razón social del receptor es el nombre del cliente
                    // (columna "Razon Social" del CSV). En compras el receptor es la empresa.
                    if (isSale && businessName != null && !businessName.isBlank()) {
                        invoice = invoice.withReceiverName(businessName.trim());
                    }

                    final Invoice invoiceToCreate = invoice;
                    createInvoiceUseCase.createInvoice(invoiceToCreate);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    String ref = java.util.UUID.randomUUID().toString().substring(0, 8);
                    logger.error("Error procesando fila {} [ref={}]: {}", record.getRecordNumber(), ref,
                            e.getMessage(), e);
                    errors.add("Fila " + record.getRecordNumber() + ": " + rowErrorMessage(e, ref));
                }
            }
        } catch (Exception e) {
            logger.error("Error reading CSV", e);
            return ResponseEntity.badRequest().body(new ImportResult(successCount, errorCount,
                    "No se pudo leer el archivo. Verifica que sea el CSV del SII (separado por ';')."));
        }

        String msg = String.format("Procesado: %d éxitos, %d errores. %s", successCount, errorCount,
                String.join(" | ", errors));
        return ResponseEntity.ok(new ImportResult(successCount, errorCount, msg));
    }

    /**
     * Mensaje de error por fila apto para el usuario: los errores de formato del CSV se
     * muestran tal cual; los internos (BD, NPE, etc.) se ocultan tras una referencia.
     */
    static String rowErrorMessage(Exception e, String ref) {
        if (e instanceof IllegalArgumentException
                || e instanceof java.time.format.DateTimeParseException
                || e instanceof com.casrusil.siierpai.shared.domain.exception.DomainException) {
            return e.getMessage();
        }
        return "error interno al guardar la fila (ref " + ref + ")";
    }

    BigDecimal parseAmount(CSVRecord record, String... possibleHeaders) {
        for (String header : possibleHeaders) {
            // Check if mapped AND has value (case insensitive check logic is in parser
            // options)
            if (record.isMapped(header)) {
                String val = record.get(header);
                if (val != null && !val.trim().isEmpty()) {
                    try {
                        // Remove thousands separators (.) and replace decimal separator (,) with (.)
                        String clean = val.replace(".", "").replace(",", ".");
                        return new BigDecimal(clean);
                    } catch (NumberFormatException e) {
                        // Antes se devolvía ZERO en silencio y la factura se importaba con
                        // montos en cero. Ahora la fila se reporta como error.
                        throw new IllegalArgumentException(
                                "Monto inválido en columna '" + header + "': '" + val + "'", e);
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public record ImportResult(int successCount, int errorCount, String message) {
    }

    /**
     * 🔥 CRITICAL FIX: Transacción INDEPENDIENTE para crear cuentas
     * 
     * REQUIRES_NEW = Crea una nueva transacción, independiente de la transacción
     * padre.
     * Si falla la creación de una factura después, esta transacción YA está
     * confirmada.
     * 
     * saveAndFlush = Fuerza escritura inmediata a la base de datos (no solo en
     * memoria).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureCriticalAccountsExistIsolated(CompanyId companyId) {
        String[] criticalCodes = {
                "4.1.01.001", // Ventas
                "2.1.01.001", // Proveedores
                "1.1.05.001", // Clientes Nacionales (ACTIVO) - CRÍTICO PARA VENTAS
                "1.1.03.001", // Existencias
                "1.1.03.999", // Existencias por Clasificar (CRÍTICA)
                "1.1.08.001", // IVA Crédito
                "2.1.04.001", // IVA Débito
                "5.1.03.025", // Gastos Generales
                "5.9.99.999" // Gastos por Clasificar
        };

        for (String code : criticalCodes) {
            try {
                if (accountRepository.findByCode(companyId, code).isEmpty()) {
                    logger.info("⚡ [PRE-FLIGHT] Creando cuenta crítica faltante: {}", code);
                    createDefaultAccountIsolated(companyId, code);
                }
            } catch (Exception e) {
                logger.error("Error creando cuenta {}: {}", code, e.getMessage(), e);
                // Continuar con las demás cuentas incluso si una falla
            }
        }
    }

    /**
     * 🔥 CRITICAL FIX: Usa saveAndFlush para persistencia inmediata
     * 
     * Esto garantiza que la cuenta se escriba a la BD AHORA, no al final de la
     * transacción.
     */
    private void createDefaultAccountIsolated(CompanyId companyId, String code) {
        String name = switch (code) {
            case "4.1.01.001" -> "Ventas Nacionales";
            case "2.1.04.001" -> "IVA Débito Fiscal";
            case "1.1.05.001" -> "Clientes Nacionales"; // <--- ESTO ES VITAL PARA VENTAS
            case "2.1.01.001" -> "Proveedores Nacionales";
            case "1.1.03.001" -> "Existencias";
            case "1.1.03.999" -> "Existencias por Clasificar";
            case "1.1.08.001" -> "IVA Crédito Fiscal";
            case "5.1.03.025" -> "Gastos Generales";
            case "5.9.99.999" -> "Gastos por Clasificar";
            default -> "Cuenta Auto-Generada " + code;
        };

        com.casrusil.siierpai.modules.accounting.domain.model.AccountType type = code.startsWith("2")
                ? com.casrusil.siierpai.modules.accounting.domain.model.AccountType.LIABILITY
                : code.startsWith("1") ? com.casrusil.siierpai.modules.accounting.domain.model.AccountType.ASSET
                        : code.startsWith("5")
                                ? com.casrusil.siierpai.modules.accounting.domain.model.AccountType.EXPENSE
                                : com.casrusil.siierpai.modules.accounting.domain.model.AccountType.REVENUE;

        com.casrusil.siierpai.modules.accounting.domain.model.Account newAccount = new com.casrusil.siierpai.modules.accounting.domain.model.Account(
                companyId, code, name, type, "Auto-generada por Importador (God Mode)");

        // 🔥 CRITICAL: saveAndFlush en lugar de save
        accountRepository.saveAndFlush(newAccount);
        logger.info("✅ Cuenta {} creada y confirmada en BD", code);
    }

    /**
     * 🧠 AUTO-ENROLLMENT: Registra al socio comercial automáticamente.
     * 
     * REQUIRES_NEW = Transacción independiente para que el Partner persista
     * incluso si falla la factura después.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensurePartnerExistsIsolated(CompanyId companyId, String rut, String name, boolean isSale) {
        if (rut == null || rut.isBlank())
            return;

        try {
            // 1. Determinar el Rol basado en el contexto de importación
            PartnerType type = isSale ? PartnerType.CUSTOMER : PartnerType.SUPPLIER;

            // 2. Buscar si ya existe (por RUT y Empresa)
            var existingPartner = partnerRepository.findByCompanyIdAndRut(companyId, rut);

            if (existingPartner.isEmpty()) {
                logger.info("🆕 Nuevo Socio Detectado: {} ({}) - Tipo: {}", name, rut, type);
                // Adapt to existing Partner model: create first, then add type
                Partner newPartner = Partner.create(companyId, rut, name != null ? name : "Sin Nombre");
                newPartner.addType(type);
                partnerRepository.save(newPartner);
                logger.info("✅ Partner {} creado y confirmado en BD", rut);
            } else {
                // Update existing partner if needed (e.g. add new role or update name)
                Partner partner = existingPartner.get();
                boolean updated = false;

                if (!partner.getTypes().contains(type)) {
                    partner.addType(type);
                    updated = true;
                }

                if (name != null && !name.isBlank()) {
                    partner.updateName(name);
                    updated = true;
                }

                if (updated) {
                    partnerRepository.save(partner);
                    logger.debug("✅ Partner {} actualizado", rut);
                }
            }
        } catch (Exception e) {
            logger.error("Error procesando partner {}: {}", rut, e.getMessage(), e);
            // No lanzar excepción para no romper la importación
        }
    }
}
