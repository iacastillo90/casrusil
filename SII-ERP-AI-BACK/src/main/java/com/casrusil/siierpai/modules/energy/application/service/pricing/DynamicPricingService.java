package com.casrusil.siierpai.modules.energy.application.service.pricing;

import com.casrusil.siierpai.modules.energy.domain.model.SessionId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.PricingRule;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.PricingRuleId;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.PricingType;
import com.casrusil.siierpai.modules.energy.domain.model.pricing.TimeSlot;
import com.casrusil.siierpai.modules.energy.domain.port.out.PricingRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Service for dynamic pricing calculations.
 * 
 * Calculates energy prices based on time-of-use, peak/off-peak,
 * and user-specific pricing rules.
 */
@Service
@Transactional
public class DynamicPricingService {

    private static final Logger logger = LoggerFactory.getLogger(DynamicPricingService.class);

    private final PricingRuleRepository pricingRuleRepository;

    @Value("${ocpp.pricing.price-per-kwh:150.0}")
    private BigDecimal defaultPricePerKwh;

    public DynamicPricingService(PricingRuleRepository pricingRuleRepository) {
        this.pricingRuleRepository = pricingRuleRepository;
    }

    /**
     * Calculates the price for a charging session.
     * 
     * @param sessionId      Session ID
     * @param energyConsumed Energy consumed in kWh
     * @param timestamp      Timestamp of the session
     * @param userId         User ID (for user-specific pricing)
     * @return Total price
     */
    public BigDecimal calculatePrice(
            SessionId sessionId,
            BigDecimal energyConsumed,
            LocalDateTime timestamp,
            UUID userId) {

        logger.debug("Calculating price for session {} at {}", sessionId, timestamp);

        // Get applicable pricing rules
        List<PricingRule> activeRules = pricingRuleRepository.findAllActive();

        // Find the best applicable rule (highest priority)
        PricingRule applicableRule = activeRules.stream()
                .filter(rule -> rule.appliesAt(timestamp))
                .max(Comparator.comparing(this::getRulePriority))
                .orElse(null);

        BigDecimal pricePerKwh;

        if (applicableRule != null) {
            pricePerKwh = applicableRule.calculatePrice(timestamp);
            logger.info("Using pricing rule '{}' for session {}: ${}/kWh",
                    applicableRule.getName(), sessionId, pricePerKwh);
        } else {
            pricePerKwh = defaultPricePerKwh;
            logger.debug("Using default price for session {}: ${}/kWh", sessionId, pricePerKwh);
        }

        return energyConsumed.multiply(pricePerKwh);
    }

    /**
     * Gets the price per kWh for a specific timestamp.
     */
    public BigDecimal getPriceForTimeSlot(LocalDateTime timestamp) {
        List<PricingRule> activeRules = pricingRuleRepository.findAllActive();

        return activeRules.stream()
                .filter(rule -> rule.appliesAt(timestamp))
                .max(Comparator.comparing(this::getRulePriority))
                .map(rule -> rule.calculatePrice(timestamp))
                .orElse(defaultPricePerKwh);
    }

    /**
     * Creates a new pricing rule.
     */
    public PricingRule createPricingRule(
            String name,
            PricingType type,
            BigDecimal basePrice,
            TimeSlot timeSlot,
            BigDecimal multiplier) {

        logger.info("Creating pricing rule: {} ({})", name, type);

        PricingRule rule = PricingRule.create(name, type, basePrice, timeSlot, multiplier);
        return pricingRuleRepository.save(rule);
    }

    /**
     * Updates a pricing rule.
     */
    public PricingRule updatePricingRule(PricingRuleId id, boolean active) {
        logger.info("Updating pricing rule {}: active={}", id, active);

        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pricing rule not found: " + id));

        if (active) {
            rule.activate();
        } else {
            rule.deactivate();
        }

        return pricingRuleRepository.save(rule);
    }

    /**
     * Deletes a pricing rule.
     */
    public void deletePricingRule(PricingRuleId id) {
        logger.info("Deleting pricing rule {}", id);
        pricingRuleRepository.delete(id);
    }

    /**
     * Gets all pricing rules.
     */
    public List<PricingRule> getPricingRules() {
        return pricingRuleRepository.findAllActive();
    }

    /**
     * Gets pricing rules by type.
     */
    public List<PricingRule> getPricingRulesByType(PricingType type) {
        return pricingRuleRepository.findActiveByType(type);
    }

    /**
     * Determines rule priority (higher is better).
     * USER_SPECIFIC > PROMOTIONAL > TIME_OF_USE > PEAK_OFF_PEAK > FIXED
     */
    private int getRulePriority(PricingRule rule) {
        return switch (rule.getType()) {
            case USER_SPECIFIC -> 5;
            case PROMOTIONAL -> 4;
            case TIME_OF_USE -> 3;
            case PEAK_OFF_PEAK -> 2;
            case FIXED -> 1;
        };
    }
}
