package com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceLine;
import com.casrusil.siierpai.modules.invoicing.domain.model.InvoiceType;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.CreateInvoiceUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.GetInvoiceAuditTrailUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.ManageInvoiceUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.SearchInvoicesUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.port.in.UpdateInvoiceUseCase;
import com.casrusil.siierpai.modules.invoicing.domain.service.InvoiceDispatchService;
import com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest.dto.InvoiceAuditTrailEntryDto;
import com.casrusil.siierpai.modules.invoicing.infrastructure.adapter.in.rest.dto.UpdateInvoiceRequest;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para la gestión de facturas electrónicas.
 * 
 * <p>
 * Permite crear, listar y enviar facturas por correo electrónico.
 * Es el punto de entrada principal para la facturación.
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 * <li>{@code POST /api/v1/invoices}: Crear nueva factura.</li>
 * <li>{@code GET /api/v1/invoices}: Listar facturas de la empresa.</li>
 * <li>{@code POST /api/v1/invoices/{id}/send}: Enviar factura por email.</li>
 * </ul>
 * 
 * @see CreateInvoiceUseCase
 * @see SearchInvoicesUseCase
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/invoices")
@PreAuthorize("hasAnyRole('ADMIN','SALES','ACCOUNTANT')")
public class InvoiceController {

        private final CreateInvoiceUseCase createInvoiceUseCase;
        private final SearchInvoicesUseCase searchInvoicesUseCase;
        private final InvoiceDispatchService invoiceDispatchService;
        private final ManageInvoiceUseCase manageInvoiceUseCase;
        private final UpdateInvoiceUseCase updateInvoiceUseCase;
        private final GetInvoiceAuditTrailUseCase getInvoiceAuditTrailUseCase;

        public InvoiceController(CreateInvoiceUseCase createInvoiceUseCase,
                        SearchInvoicesUseCase searchInvoicesUseCase,
                        InvoiceDispatchService invoiceDispatchService,
                        ManageInvoiceUseCase manageInvoiceUseCase,
                        UpdateInvoiceUseCase updateInvoiceUseCase,
                        GetInvoiceAuditTrailUseCase getInvoiceAuditTrailUseCase) {
                this.createInvoiceUseCase = createInvoiceUseCase;
                this.searchInvoicesUseCase = searchInvoicesUseCase;
                this.invoiceDispatchService = invoiceDispatchService;
                this.manageInvoiceUseCase = manageInvoiceUseCase;
                this.updateInvoiceUseCase = updateInvoiceUseCase;
                this.getInvoiceAuditTrailUseCase = getInvoiceAuditTrailUseCase;
        }

        @PostMapping
        public ResponseEntity<Invoice> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
                CompanyId companyId = CompanyContext.requireCompanyId();
                Invoice invoice = Invoice.create(
                                companyId,
                                InvoiceType.fromCode(request.tipoDte()),
                                request.folio(),
                                request.rutEmisor(),
                                request.rutReceptor() != null ? request.rutReceptor() : "66.666.666-6", // Fallback or
                                                                                                        // require it
                                request.razonSocialEmisor(),
                                request.fechaEmision(),
                                request.montoNeto(),
                                request.montoIva(),
                                request.montoTotal(),
                                BigDecimal.ZERO, // Fixed Asset
                                BigDecimal.ZERO, // Common Use VAT
                                Invoice.ORIGIN_MANUAL,
                                com.casrusil.siierpai.modules.invoicing.domain.model.TransactionType.SALE, // Default to
                                                                                                           // Sale?
                                mapLines(request.lines()));

                return ResponseEntity.ok(createInvoiceUseCase.createInvoice(invoice));
        }

        @GetMapping
        public ResponseEntity<List<Invoice>> getInvoices() {
                CompanyId companyId = CompanyContext.requireCompanyId();
                return ResponseEntity.ok(searchInvoicesUseCase.getInvoicesByCompany(companyId));
        }

        @GetMapping("/{id}")
        public ResponseEntity<Invoice> getInvoice(@PathVariable UUID id) {
                return ResponseEntity.ok(manageInvoiceUseCase.getInvoice(id));
        }

        @PutMapping("/{id}")
        public ResponseEntity<Invoice> updateInvoice(@PathVariable UUID id,
                        @RequestBody UpdateInvoiceRequest request) {
                return ResponseEntity.ok(updateInvoiceUseCase.updateInvoice(id, request.toCommand()));
        }

        @GetMapping("/{id}/audit-trail")
        public ResponseEntity<List<InvoiceAuditTrailEntryDto>> getAuditTrail(@PathVariable UUID id) {
                manageInvoiceUseCase.getInvoice(id);
                List<InvoiceAuditTrailEntryDto> entries = getInvoiceAuditTrailUseCase.getAuditTrail(id).stream()
                                .map(InvoiceAuditTrailEntryDto::from)
                                .toList();
                return ResponseEntity.ok(entries);
        }

        @PostMapping("/{id}/send")
        public ResponseEntity<Void> sendInvoice(@PathVariable UUID id, @RequestParam String email) {
                invoiceDispatchService.dispatchInvoice(id, email);
                return ResponseEntity.ok().build();
        }

        private InvoiceLine mapLine(InvoiceLineRequest line) {
                return new InvoiceLine(1, line.description(), null, line.quantity(), line.unitPrice(),
                                line.totalAmount(), "UN");
        }

        /**
         * Mapea el request del frontend al dominio, asignando lineNumber por índice
         * (no hardcodea 1) y calculando amount = quantity * unitPrice cuando el request
         * no provee totalAmount explícito.
         */
        private List<InvoiceLine> mapLines(List<InvoiceLineRequest> lines) {
                if (lines == null) {
                        return List.of();
                }
                int index = 0;
                List<InvoiceLine> result = new ArrayList<>(lines.size());
                for (InvoiceLineRequest line : lines) {
                        BigDecimal amount = line.totalAmount() != null
                                        ? line.totalAmount()
                                        : line.quantity().multiply(line.unitPrice());
                        result.add(new InvoiceLine(index + 1, line.description(), null,
                                        line.quantity(), line.unitPrice(), amount, "UN"));
                        index++;
                }
                return result;
        }

        /**
         * DTO de creación de factura con validaciones.
         * Todos los campos obligatorios generan errores 400 claros si se omiten.
         */
        public record CreateInvoiceRequest(
                        @NotNull(message = "El tipo de DTE es obligatorio")
                        @Min(value = 33, message = "Tipo DTE mínimo es 33 (Factura Afecta)")
                        @Max(value = 112, message = "Tipo DTE máximo es 112")
                        Integer tipoDte,

                        @NotNull(message = "El folio es obligatorio")
                        @Positive(message = "El folio debe ser un número positivo")
                        Long folio,

                        @NotBlank(message = "El RUT del emisor es obligatorio")
                        @Pattern(regexp = "^\\d{1,8}-[\\dkK]$", message = "Formato de RUT inválido (ej: 12345678-9)")
                        String rutEmisor,

                        @Pattern(regexp = "^\\d{1,8}-[\\dkK]$", message = "Formato de RUT receptor inválido")
                        String rutReceptor,

                        @NotBlank(message = "La razón social del emisor es obligatoria")
                        @Size(max = 100, message = "Razón social máxima de 100 caracteres")
                        String razonSocialEmisor,

                        @NotNull(message = "La fecha de emisión es obligatoria")
                        LocalDate fechaEmision,

                        @NotNull(message = "El monto total es obligatorio")
                        @DecimalMin(value = "0.0", inclusive = false, message = "El monto total debe ser mayor a 0")
                        BigDecimal montoTotal,

                        @NotNull(message = "El monto neto es obligatorio")
                        @DecimalMin(value = "0.0", inclusive = false, message = "El monto neto debe ser mayor a 0")
                        BigDecimal montoNeto,

                        @NotNull(message = "El monto IVA es obligatorio")
                        @DecimalMin(value = "0.0", message = "El monto IVA no puede ser negativo")
                        BigDecimal montoIva,

                        @NotNull(message = "Las líneas de la factura son obligatorias")
                        @Size(min = 1, message = "Debe incluir al menos una línea de detalle")
                        List<InvoiceLineRequest> lines) {
        }

        public record InvoiceLineRequest(
                        @NotBlank(message = "La descripción del ítem es obligatoria")
                        @Size(max = 80, message = "Descripción máxima de 80 caracteres")
                        String description,

                        @NotNull @DecimalMin(value = "0.0", inclusive = false)
                        BigDecimal quantity,

                        @NotNull @DecimalMin(value = "0.0", inclusive = false)
                        BigDecimal unitPrice,

                        @NotNull @DecimalMin(value = "0.0", inclusive = false)
                        BigDecimal totalAmount) {
        }
}
