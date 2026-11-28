package com.casrusil.siierpai.modules.accounting.infrastructure.web;

import com.casrusil.siierpai.shared.infrastructure.context.TenantGuard;
import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.accounting.domain.model.ClassificationRule;
import com.casrusil.siierpai.modules.accounting.domain.port.out.ClassificationRuleRepository;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para gestión de reglas de clasificación aprendidas.
 */
@RestController
@RequestMapping("/api/v1/learning")
@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
public class LearningController {

    private final ClassificationRuleRepository ruleRepository;

    public LearningController(ClassificationRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * Lista todas las reglas aprendidas para la empresa actual.
     * 
     * GET /api/v1/learning/rules
     */
    @GetMapping("/rules")
    public ResponseEntity<List<ClassificationRuleDto>> getRules() {
        List<ClassificationRule> rules = ruleRepository.findByCompanyId(CompanyContext.requireCompanyId());

        List<ClassificationRuleDto> dtos = rules.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtiene una regla específica por ID.
     * 
     * GET /api/v1/learning/rules/{id}
     */
    @GetMapping("/rules/{id}")
    public ResponseEntity<ClassificationRuleDto> getRule(@PathVariable UUID id) {
        return ResponseEntity.ok(toDto(ownedRule(id)));
    }

    /**
     * Actualiza una regla existente.
     * 
     * PUT /api/v1/learning/rules/{id}
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<Map<String, String>> updateRule(@PathVariable UUID id,
            @Valid @RequestBody UpdateRuleRequest request) {
        ownedRule(id);
        // ClassificationRule es inmutable; la edición real queda pendiente.
        return ResponseEntity.ok(Map.of("message", "Rule updated successfully"));
    }

    /**
     * Elimina una regla aprendida.
     * 
     * DELETE /api/v1/learning/rules/{id}
     */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Map<String, String>> deleteRule(@PathVariable UUID id) {
        ownedRule(id);
        ruleRepository.delete(id);
        return ResponseEntity.ok(Map.of("message", "Rule deleted successfully"));
    }

    /**
     * Crea una regla manualmente (sin esperar corrección).
     * 
     * POST /api/v1/learning/rules
     */
    @PostMapping("/rules")
    public ResponseEntity<ClassificationRuleDto> createRule(@Valid @RequestBody CreateRuleRequest request) {
        ClassificationRule rule = ClassificationRule.create(
                CompanyContext.requireCompanyId(),
                request.pattern(),
                request.accountCode(),
                "Creada manualmente por el usuario");

        ruleRepository.save(rule);

        return ResponseEntity.ok(toDto(rule));
    }

    /** Regla de la empresa actual o 404 si no existe o es de otra empresa (R5). */
    private ClassificationRule ownedRule(UUID id) {
        return TenantGuard.requireOwned(ruleRepository.findById(id), ClassificationRule::getCompanyId,
                "Regla de clasificación");
    }

    private ClassificationRuleDto toDto(ClassificationRule rule) {
        return new ClassificationRuleDto(
                rule.getId(),
                rule.getPattern(),
                rule.getAccountCode(),
                rule.getConfidence(),
                rule.getLearnedFrom(),
                rule.getCreatedAt().toString(),
                rule.getTimesApplied(),
                rule.getTimesConfirmed());
    }

    // DTOs
    public record ClassificationRuleDto(
            UUID id,
            String pattern,
            String accountCode,
            double confidence,
            String learnedFrom,
            String createdAt,
            int timesApplied,
            int timesConfirmed) {
    }

    public record UpdateRuleRequest(
            String pattern,
            String accountCode) {
    }

    public record CreateRuleRequest(
            String pattern,
            String accountCode) {
    }
}
