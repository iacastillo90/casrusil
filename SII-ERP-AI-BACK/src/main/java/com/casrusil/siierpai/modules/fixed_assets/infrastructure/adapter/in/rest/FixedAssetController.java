package com.casrusil.siierpai.modules.fixed_assets.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;

import com.casrusil.siierpai.modules.fixed_assets.application.service.FixedAssetServiceImpl;
import com.casrusil.siierpai.modules.fixed_assets.domain.model.FixedAsset;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fixed-assets")
public class FixedAssetController {

    private final FixedAssetServiceImpl service;

    public FixedAssetController(FixedAssetServiceImpl service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FixedAsset> registerAsset(@Valid @RequestBody RegisterAssetReq req) {
        return ResponseEntity.ok(service.registerAsset(CompanyContext.requireCompanyId(), req.name(), req.category(), req.purchaseValue(), req.purchaseDate()));
    }

    @GetMapping
    public ResponseEntity<List<FixedAsset>> getAssets() {
        return ResponseEntity.ok(service.getAssets(CompanyContext.requireCompanyId()));
    }

    @PostMapping("/run-depreciation")
    public ResponseEntity<Void> runDepreciation() {
        service.runMonthlyDepreciation(CompanyContext.requireCompanyId());
        return ResponseEntity.ok().build();
    }

    public record RegisterAssetReq(String name, String category, BigDecimal purchaseValue, LocalDate purchaseDate) {}
}