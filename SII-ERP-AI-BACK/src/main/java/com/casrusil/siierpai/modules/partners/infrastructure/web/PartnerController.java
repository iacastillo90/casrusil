package com.casrusil.siierpai.modules.partners.infrastructure.web;

import com.casrusil.siierpai.modules.partners.domain.model.Partner;
import com.casrusil.siierpai.modules.partners.domain.port.out.PartnerRepository;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Datos de contacto de los partners (clientes/proveedores).
 *
 * <p>
 * El email del receptor es el que usa B-12 para despachar la factura: si está
 * registrado, el envío sólo puede ir a esa dirección.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/partners")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT','SALES')")
public class PartnerController {

    private final PartnerRepository partnerRepository;

    public PartnerController(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    @PutMapping("/{rut}/email")
    public ResponseEntity<Partner> updateEmail(@PathVariable String rut,
            @RequestBody UpdatePartnerEmailRequest request) {
        Partner partner = partnerRepository
                .findByCompanyIdAndRut(CompanyContext.requireCompanyId(), rut)
                .orElseThrow(() -> new IllegalArgumentException("Partner no encontrado: " + rut));
        partner.updateEmail(request.email().trim());
        return ResponseEntity.ok(partnerRepository.save(partner));
    }

    public record UpdatePartnerEmailRequest(
            @NotBlank @Email String email) {
    }
}
