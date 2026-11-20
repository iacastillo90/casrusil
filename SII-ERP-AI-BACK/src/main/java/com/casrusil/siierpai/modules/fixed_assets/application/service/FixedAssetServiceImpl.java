package com.casrusil.siierpai.modules.fixed_assets.application.service;

import com.casrusil.siierpai.modules.fixed_assets.domain.model.FixedAsset;
import com.casrusil.siierpai.modules.fixed_assets.infrastructure.persistence.entity.FixedAssetEntity;
import com.casrusil.siierpai.modules.fixed_assets.infrastructure.persistence.repository.FixedAssetJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FixedAssetServiceImpl {

    private final FixedAssetJpaRepository faRepo;

    public FixedAssetServiceImpl(FixedAssetJpaRepository faRepo) {
        this.faRepo = faRepo;
    }

    public FixedAsset registerAsset(CompanyId companyId, String name, String category, BigDecimal purchaseValue, LocalDate purchaseDate) {
        FixedAssetEntity e = new FixedAssetEntity();
        e.setId(UUID.randomUUID());
        e.setCompanyId(companyId.value().toString());
        e.setName(name);
        e.setCategory(category);
        e.setPurchaseValue(purchaseValue);
        e.setPurchaseDate(purchaseDate);
        e.setCurrentResidualValue(purchaseValue);
        faRepo.save(e);
        return toDomain(e);
    }

    public FixedAsset getAsset(CompanyId companyId, UUID id) {
        return faRepo.findById(id)
                .filter(e -> companyId.value().toString().equals(e.getCompanyId()))
                .map(this::toDomain)
                .orElseThrow(() -> new com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException(
                        "Activo fijo no encontrado: " + id));
    }

    public List<FixedAsset> getAssets(CompanyId companyId) {
        return faRepo.findByCompanyId(companyId.value().toString()).stream().map(this::toDomain).collect(Collectors.toList());
    }

    public void runMonthlyDepreciation(CompanyId companyId) {
        List<FixedAssetEntity> assets = faRepo.findByCompanyId(companyId.value().toString());
        for (FixedAssetEntity asset : assets) {
            BigDecimal monthsUsefulLife = asset.getCategory().equals("VEHICLE") ? new BigDecimal("84") : new BigDecimal("120");
            BigDecimal monthlyDepreciation = asset.getPurchaseValue().divide(monthsUsefulLife, 2, RoundingMode.HALF_UP);
            
            BigDecimal current = asset.getCurrentResidualValue();
            BigDecimal newResidual = current.subtract(monthlyDepreciation);
            if (newResidual.compareTo(BigDecimal.ONE) < 0) {
                newResidual = BigDecimal.ONE;
            }
            asset.setCurrentResidualValue(newResidual);
        }
        faRepo.saveAll(assets);
    }

    private FixedAsset toDomain(FixedAssetEntity e) {
        FixedAsset fa = new FixedAsset(e.getId(), CompanyId.fromString(e.getCompanyId()), e.getName(), e.getCategory(), e.getPurchaseValue(), e.getPurchaseDate());
        fa.depreciate(e.getPurchaseValue().subtract(e.getCurrentResidualValue())); // Set current
        return fa;
    }
}