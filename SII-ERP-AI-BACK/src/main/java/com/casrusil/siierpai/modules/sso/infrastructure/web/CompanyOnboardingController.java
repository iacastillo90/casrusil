package com.casrusil.siierpai.modules.sso.infrastructure.web;

import com.casrusil.siierpai.modules.sso.application.service.CompanyCertificateService;
import com.casrusil.siierpai.modules.sso.application.service.CompanyCertificateService.CertificateInfo;
import com.casrusil.siierpai.modules.sso.application.service.CompanyManagementService;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Onboarding de la empresa: datos fiscales y certificado digital.
 *
 * <p>
 * Los datos incluyen giro, acteco y comuna, que {@code DteEmissionValidator}
 * exige para emitir. La respuesta indica qué falta para poder emitir.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/company/onboarding")
@PreAuthorize("hasRole('ADMIN')")
public class CompanyOnboardingController {

    private final CompanyManagementService companyService;
    private final CompanyCertificateService certificateService;

    public CompanyOnboardingController(CompanyManagementService companyService,
            CompanyCertificateService certificateService) {
        this.companyService = companyService;
        this.certificateService = certificateService;
    }

    @PostMapping
    public ResponseEntity<OnboardingStatus> saveCompanyData(@RequestBody OnboardingRequest request) {
        var companyId = CompanyContext.requireCompanyId();
        Company company = companyService.updateCompany(
                companyId,
                request.razonSocial(),
                request.email(),
                request.direccion(),
                null,
                null,
                null,
                request.giro(),
                request.acteco(),
                request.comuna());
        return ResponseEntity.ok(OnboardingStatus.from(company));
    }

    @PostMapping("/certificate")
    public ResponseEntity<CertificateInfo> uploadCertificate(@RequestParam("file") MultipartFile file,
            @RequestParam("password") String password) throws IOException {
        var companyId = CompanyContext.requireCompanyId();
        return ResponseEntity.ok(certificateService.upload(companyId, file.getBytes(), password));
    }

    public record OnboardingRequest(
            @NotBlank String razonSocial,
            @NotBlank @Email String email,
            String direccion,
            @NotBlank String giro,
            @NotBlank String acteco,
            @NotBlank String comuna) {
    }

    /**
     * Estado del onboarding: qué datos faltan para poder emitir un DTE.
     *
     * @param razonSocial    razón social registrada
     * @param missingFields  datos obligatorios pendientes
     * @param emissionReady  true si no falta nada
     */
    public record OnboardingStatus(String razonSocial, List<String> missingFields, boolean emissionReady) {

        static OnboardingStatus from(Company company) {
            List<String> missing = new ArrayList<>();
            if (blank(company.getRazonSocial())) {
                missing.add("razón social");
            }
            if (blank(company.getGiro())) {
                missing.add("giro");
            }
            if (blank(company.getActeco())) {
                missing.add("acteco");
            }
            if (blank(company.getComuna())) {
                missing.add("comuna");
            }
            if (blank(company.getCommercialAddress())) {
                missing.add("dirección");
            }
            return new OnboardingStatus(company.getRazonSocial(), missing, missing.isEmpty());
        }

        private static boolean blank(String value) {
            return value == null || value.isBlank();
        }
    }
}
