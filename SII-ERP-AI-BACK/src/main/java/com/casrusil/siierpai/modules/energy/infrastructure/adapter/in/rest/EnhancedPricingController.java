package com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest;

import com.casrusil.siierpai.modules.energy.application.service.pricing.EnhancedPricingService;
import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.*;
import com.casrusil.siierpai.modules.energy.infrastructure.adapter.in.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for enhanced pricing operations.
 */
@RestController
@RequestMapping("/api/v1/pricing")
@Tag(name = "Enhanced Pricing", description = "Advanced pricing with demand-based, seasonal, and promotional strategies")
@PreAuthorize("hasAnyRole('ADMIN','OPERATIONS')")
public class EnhancedPricingController {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedPricingController.class);

    private final EnhancedPricingService pricingService;

    public EnhancedPricingController(EnhancedPricingService pricingService) {
        this.pricingService = pricingService;
    }

    // ==================== Pricing Preview ====================

    @PostMapping("/preview")
    @Operation(summary = "Preview pricing before charging", description = "Calculate pricing with all applicable discounts and adjustments")
    public ResponseEntity<PricingPreviewDto> previewPricing(
            @RequestBody @Valid PricingPreviewRequest request) {

        logger.info("Pricing preview requested for station {}", request.stationId());

        try {
            PricingPreview preview = pricingService.calculatePricingPreview(
                    StationId.of(request.stationId()),
                    request.estimatedKwh(),
                    request.startTime() != null ? request.startTime() : LocalDateTime.now(),
                    Optional.ofNullable(request.promoCode()),
                    Optional.ofNullable(request.userId()),
                    Optional.ofNullable(request.currentDemandKw()));

            return ResponseEntity.ok(PricingPreviewDto.from(preview));

        } catch (Exception e) {
            logger.error("Error calculating pricing preview", e);
            throw new RuntimeException("Failed to calculate pricing preview: " + e.getMessage());
        }
    }

    @GetMapping("/current/{stationId}")
    @Operation(summary = "Get current price per kWh", description = "Get the current active price for a station")
    public ResponseEntity<CurrentPriceDto> getCurrentPrice(
            @PathVariable UUID stationId,
            @RequestParam(required = false) Integer currentDemandKw) {

        logger.info("Current price requested for station {}", stationId);

        BigDecimal pricePerKwh = pricingService.getCurrentPrice(
                StationId.of(stationId),
                Optional.ofNullable(currentDemandKw));

        return ResponseEntity.ok(new CurrentPriceDto(
                stationId,
                pricePerKwh,
                LocalDateTime.now()));
    }

    // ==================== Demand Pricing Management ====================

    @PostMapping("/demand-rules")
    @Operation(summary = "Create demand-based pricing rule")
    public ResponseEntity<DemandPricingDto> createDemandPricing(
            @RequestBody @Valid CreateDemandPricingRequest request) {

        logger.info("Creating demand-based pricing rule: {}", request.name());

        DemandBasedPricing pricing = DemandBasedPricing.builder()
                .name(request.name())
                .description(request.description())
                .baseRate(request.baseRate())
                .peakMultiplier(request.peakMultiplier())
                .offPeakMultiplier(request.offPeakMultiplier())
                .demandThresholdKw(request.demandThresholdKw())
                .peakStartTime(request.peakStartTime())
                .peakEndTime(request.peakEndTime())
                .applicableDays(request.applicableDays())
                .active(true)
                .build();

        DemandBasedPricing created = pricingService.createDemandPricing(pricing);

        return ResponseEntity.ok(DemandPricingDto.from(created));
    }

    @GetMapping("/demand-rules")
    @Operation(summary = "Get all active demand pricing rules")
    public ResponseEntity<List<DemandPricingDto>> getAllDemandPricing() {
        List<DemandBasedPricing> rules = pricingService.getAllActiveDemandPricing();
        return ResponseEntity.ok(rules.stream()
                .map(DemandPricingDto::from)
                .toList());
    }

    // ==================== Seasonal Pricing Management ====================

    @PostMapping("/seasonal-rates")
    @Operation(summary = "Create seasonal pricing rate")
    public ResponseEntity<SeasonalPricingDto> createSeasonalPricing(
            @RequestBody @Valid CreateSeasonalPricingRequest request) {

        logger.info("Creating seasonal pricing for {}", request.season());

        SeasonalPricing pricing = new SeasonalPricing(
                request.season(),
                request.multiplier(),
                request.startDate(),
                request.endDate(),
                request.description());

        SeasonalPricing created = pricingService.createSeasonalPricing(pricing);

        return ResponseEntity.ok(SeasonalPricingDto.from(created));
    }

    @GetMapping("/seasonal-rates")
    @Operation(summary = "Get all seasonal pricing rates")
    public ResponseEntity<List<SeasonalPricingDto>> getAllSeasonalPricing() {
        List<SeasonalPricing> rates = pricingService.getAllSeasonalPricing();
        return ResponseEntity.ok(rates.stream()
                .map(SeasonalPricingDto::from)
                .toList());
    }

    // ==================== Promotional Campaigns ====================

    @PostMapping("/campaigns")
    @Operation(summary = "Create promotional campaign")
    public ResponseEntity<PromotionalCampaignDto> createCampaign(
            @RequestBody @Valid CreateCampaignRequest request) {

        logger.info("Creating promotional campaign: {}", request.code());

        try {
            PromotionalCampaign campaign = PromotionalCampaign.builder()
                    .code(request.code())
                    .description(request.description())
                    .type(request.discountType())
                    .discountValue(request.discountValue())
                    .validFrom(request.validFrom())
                    .validUntil(request.validUntil())
                    .maxUsageCount(request.maxUsageCount())
                    .minimumPurchase(request.minimumPurchase())
                    .maximumDiscount(request.maximumDiscount())
                    .eligibleUserIds(request.eligibleUserIds())
                    .active(true)
                    .build();

            PromotionalCampaign created = pricingService.createCampaign(campaign);

            return ResponseEntity.ok(PromotionalCampaignDto.from(created));

        } catch (IllegalArgumentException e) {
            logger.error("Error creating campaign", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/campaigns")
    @Operation(summary = "Get all active campaigns")
    public ResponseEntity<List<PromotionalCampaignDto>> getActiveCampaigns() {
        List<PromotionalCampaign> campaigns = pricingService.getActiveCampaigns();
        return ResponseEntity.ok(campaigns.stream()
                .map(PromotionalCampaignDto::from)
                .toList());
    }

    @GetMapping("/campaigns/{code}")
    @Operation(summary = "Get campaign by code")
    public ResponseEntity<PromotionalCampaignDto> getCampaignByCode(@PathVariable String code) {
        return pricingService.findCampaignByCode(code)
                .map(PromotionalCampaignDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/campaigns/{id}/activate")
    @Operation(summary = "Activate campaign")
    public ResponseEntity<PromotionalCampaignDto> activateCampaign(@PathVariable UUID id) {
        try {
            PromotionalCampaign updated = pricingService.updateCampaign(
                    CampaignId.of(id),
                    true);
            return ResponseEntity.ok(PromotionalCampaignDto.from(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/campaigns/{id}/deactivate")
    @Operation(summary = "Deactivate campaign")
    public ResponseEntity<PromotionalCampaignDto> deactivateCampaign(@PathVariable UUID id) {
        try {
            PromotionalCampaign updated = pricingService.updateCampaign(
                    CampaignId.of(id),
                    false);
            return ResponseEntity.ok(PromotionalCampaignDto.from(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
