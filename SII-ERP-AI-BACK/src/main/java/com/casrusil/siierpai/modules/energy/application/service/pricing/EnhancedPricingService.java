package com.casrusil.siierpai.modules.energy.application.service.pricing;

import com.casrusil.siierpai.modules.energy.domain.model.StationId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.*;
import com.casrusil.siierpai.modules.energy.domain.port.out.DemandPricingRepository;
import com.casrusil.siierpai.modules.energy.domain.port.out.PromotionalCampaignRepository;
import com.casrusil.siierpai.modules.energy.domain.port.out.SeasonalPricingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Enhanced pricing service with advanced pricing strategies.
 * Supports demand-based, seasonal, and promotional pricing.
 */
@Service
@Transactional
public class EnhancedPricingService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedPricingService.class);

    private final DynamicPricingService basePricingService;
    private final DemandPricingRepository demandPricingRepository;
    private final SeasonalPricingRepository seasonalPricingRepository;
    private final PromotionalCampaignRepository campaignRepository;

    public EnhancedPricingService(
            DynamicPricingService basePricingService,
            DemandPricingRepository demandPricingRepository,
            SeasonalPricingRepository seasonalPricingRepository,
            PromotionalCampaignRepository campaignRepository) {
        this.basePricingService = basePricingService;
        this.demandPricingRepository = demandPricingRepository;
        this.seasonalPricingRepository = seasonalPricingRepository;
        this.campaignRepository = campaignRepository;
    }

    /**
     * Calculates a pricing preview before charging starts.
     */
    public PricingPreview calculatePricingPreview(
            StationId stationId,
            BigDecimal estimatedKwh,
            LocalDateTime startTime,
            Optional<String> promoCode,
            Optional<UUID> userId,
            Optional<Integer> currentDemandKw) {

        logger.info("Calculating pricing preview for station {} at {}", stationId, startTime);

        List<PricingPreview.PriceAdjustment> breakdown = new ArrayList<>();

        // Step 1: Get base price from existing pricing service
        BigDecimal basePrice = basePricingService.getPriceForTimeSlot(startTime)
                .multiply(estimatedKwh);
        logger.debug("Base price: ${}", basePrice);

        // Step 2: Apply demand-based pricing
        BigDecimal demandAdjustedPrice = applyDemandPricing(
                basePrice,
                startTime,
                currentDemandKw.orElse(0),
                breakdown);

        // Step 3: Apply seasonal adjustments
        BigDecimal seasonalPrice = applySeasonalPricing(
                demandAdjustedPrice,
                startTime.toLocalDate(),
                breakdown);

        // Step 4: Apply promotional discount
        BigDecimal finalPrice = promoCode
                .flatMap(code -> applyPromotionalDiscount(
                        seasonalPrice,
                        code,
                        userId.orElse(null),
                        startTime,
                        breakdown))
                .orElse(seasonalPrice);

        logger.info("Pricing preview calculated: base=${}, final=${}, discount=${}",
                basePrice, finalPrice, basePrice.subtract(finalPrice));

        return PricingPreview.builder()
                .basePrice(basePrice)
                .demandAdjustedPrice(demandAdjustedPrice)
                .seasonalPrice(seasonalPrice)
                .finalPrice(finalPrice)
                .breakdown(breakdown)
                .build();
    }

    /**
     * Applies demand-based pricing adjustments.
     */
    private BigDecimal applyDemandPricing(
            BigDecimal basePrice,
            LocalDateTime timestamp,
            Integer currentDemandKw,
            List<PricingPreview.PriceAdjustment> breakdown) {

        List<DemandBasedPricing> applicableRules = demandPricingRepository.findAllActive()
                .stream()
                .filter(rule -> rule.isApplicable(timestamp))
                .toList();

        if (applicableRules.isEmpty()) {
            logger.debug("No demand-based pricing rules applicable");
            return basePrice;
        }

        // Use the first applicable rule (could be enhanced with priority logic)
        DemandBasedPricing rule = applicableRules.get(0);
        BigDecimal adjustedPrice = rule.calculatePrice(basePrice, currentDemandKw, timestamp);

        String details = String.format("Demand-based pricing: %s (demand: %d kW)",
                rule.getName(), currentDemandKw);
        breakdown.add(PricingPreview.PriceAdjustment.demandBased(
                basePrice, adjustedPrice, details));

        logger.debug("Applied demand pricing '{}': ${} -> ${}", rule.getName(), basePrice, adjustedPrice);
        return adjustedPrice;
    }

    /**
     * Applies seasonal pricing adjustments.
     */
    private BigDecimal applySeasonalPricing(
            BigDecimal currentPrice,
            java.time.LocalDate date,
            List<PricingPreview.PriceAdjustment> breakdown) {

        List<SeasonalPricing> activeSeasons = seasonalPricingRepository.findActiveOn(date);

        if (activeSeasons.isEmpty()) {
            logger.debug("No seasonal pricing active for date {}", date);
            return currentPrice;
        }

        // Apply the first active seasonal pricing (could be enhanced with priority)
        SeasonalPricing seasonal = activeSeasons.get(0);
        BigDecimal adjustedPrice = seasonal.applyTo(currentPrice);

        breakdown.add(PricingPreview.PriceAdjustment.seasonal(
                currentPrice, adjustedPrice, seasonal.season().name()));

        logger.debug("Applied seasonal pricing '{}': ${} -> ${}",
                seasonal.season(), currentPrice, adjustedPrice);
        return adjustedPrice;
    }

    /**
     * Applies promotional discount.
     */
    private Optional<BigDecimal> applyPromotionalDiscount(
            BigDecimal currentPrice,
            String code,
            UUID userId,
            LocalDateTime timestamp,
            List<PricingPreview.PriceAdjustment> breakdown) {

        Optional<PromotionalCampaign> campaignOpt = campaignRepository.findByCode(code);

        if (campaignOpt.isEmpty()) {
            logger.warn("Promotional code '{}' not found", code);
            return Optional.empty();
        }

        PromotionalCampaign campaign = campaignOpt.get();

        if (!campaign.isValid(timestamp, userId)) {
            logger.warn("Promotional code '{}' is not valid for user {} at {}",
                    code, userId, timestamp);
            return Optional.empty();
        }

        BigDecimal discountedPrice = campaign.applyDiscount(currentPrice);

        breakdown.add(PricingPreview.PriceAdjustment.promotional(
                currentPrice, discountedPrice, code));

        logger.info("Applied promotional code '{}': ${} -> ${}", code, currentPrice, discountedPrice);
        return Optional.of(discountedPrice);
    }

    /**
     * Records the usage of a promotional code.
     */
    public void recordPromotionalUsage(String code) {
        campaignRepository.findByCode(code).ifPresent(campaign -> {
            campaign.recordUsage();
            campaignRepository.save(campaign);
            logger.info("Recorded usage of promotional code '{}'", code);
        });
    }

    /**
     * Gets the current active price for a station.
     */
    public BigDecimal getCurrentPrice(StationId stationId, Optional<Integer> currentDemandKw) {
        LocalDateTime now = LocalDateTime.now();
        PricingPreview preview = calculatePricingPreview(
                stationId,
                BigDecimal.ONE, // 1 kWh for price per kWh
                now,
                Optional.empty(),
                Optional.empty(),
                currentDemandKw);
        return preview.finalPrice();
    }

    // Demand Pricing Management

    public DemandBasedPricing createDemandPricing(DemandBasedPricing pricing) {
        logger.info("Creating demand-based pricing rule: {}", pricing.getName());
        return demandPricingRepository.save(pricing);
    }

    public List<DemandBasedPricing> getAllActiveDemandPricing() {
        return demandPricingRepository.findAllActive();
    }

    // Seasonal Pricing Management

    public SeasonalPricing createSeasonalPricing(SeasonalPricing pricing) {
        logger.info("Creating seasonal pricing for {}", pricing.season());
        return seasonalPricingRepository.save(pricing);
    }

    public List<SeasonalPricing> getAllSeasonalPricing() {
        return seasonalPricingRepository.findAll();
    }

    // Promotional Campaign Management

    public PromotionalCampaign createCampaign(PromotionalCampaign campaign) {
        if (campaignRepository.existsByCode(campaign.getCode())) {
            throw new IllegalArgumentException("Campaign code already exists: " + campaign.getCode());
        }
        logger.info("Creating promotional campaign: {}", campaign.getCode());
        return campaignRepository.save(campaign);
    }

    public Optional<PromotionalCampaign> findCampaignByCode(String code) {
        return campaignRepository.findByCode(code);
    }

    public List<PromotionalCampaign> getActiveCampaigns() {
        return campaignRepository.findActiveAt(LocalDateTime.now());
    }

    public PromotionalCampaign updateCampaign(CampaignId id, boolean active) {
        PromotionalCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + id));

        if (active) {
            campaign.activate();
        } else {
            campaign.deactivate();
        }

        return campaignRepository.save(campaign);
    }
}
