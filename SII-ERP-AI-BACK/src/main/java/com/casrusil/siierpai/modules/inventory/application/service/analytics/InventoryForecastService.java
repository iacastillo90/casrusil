package com.casrusil.siierpai.modules.inventory.application.service.analytics;

import com.casrusil.siierpai.modules.inventory.domain.model.MovementType;
import com.casrusil.siierpai.modules.inventory.infrastructure.persistence.entity.ProductEntity;
import com.casrusil.siierpai.modules.inventory.infrastructure.persistence.entity.StockMovementEntity;
import com.casrusil.siierpai.modules.inventory.infrastructure.persistence.repository.ProductJpaRepository;
import com.casrusil.siierpai.modules.inventory.infrastructure.persistence.repository.StockMovementJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryForecastService {

    private final ProductJpaRepository productRepo;
    private final StockMovementJpaRepository movementRepo;

    public InventoryForecastService(ProductJpaRepository productRepo, StockMovementJpaRepository movementRepo) {
        this.productRepo = productRepo;
        this.movementRepo = movementRepo;
    }

    public record StockForecast(String productId, String name, BigDecimal currentStock,
                                BigDecimal averageDailySales, int daysUntilStockout) {}

    public List<StockForecast> predictStockouts(CompanyId companyId) {
        String companyIdStr = companyId.value().toString();
        List<ProductEntity> products = productRepo.findAllByCompanyId(companyIdStr);
        List<StockForecast> forecasts = new ArrayList<>();

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        for (ProductEntity product : products) {
            // Find all OUT movements in the last 30 days using correct repository method
            List<StockMovementEntity> recentMovements = movementRepo
                .findByProductIdAndCompanyIdOrderByMovementDateDesc(product.getId(), companyIdStr)
                .stream()
                .filter(m -> m.getType() == MovementType.OUT
                          && m.getMovementDate().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());

            BigDecimal totalSold = recentMovements.stream()
                .map(StockMovementEntity::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalSold.compareTo(BigDecimal.ZERO) == 0) continue;

            BigDecimal averageDailySales = totalSold.divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
            BigDecimal currentStock = product.getCurrentStock();

            int daysUntilStockout = 999;
            if (averageDailySales.compareTo(BigDecimal.ZERO) > 0) {
                daysUntilStockout = currentStock.divide(averageDailySales, 0, RoundingMode.DOWN).intValue();
            }

            // Only report items running out in < 45 days
            if (daysUntilStockout <= 45) {
                forecasts.add(new StockForecast(
                        product.getId().toString(), product.getName(),
                        currentStock, averageDailySales, daysUntilStockout));
            }
        }

        // Sort by most urgent
        forecasts.sort((a, b) -> Integer.compare(a.daysUntilStockout(), b.daysUntilStockout()));
        return forecasts;
    }
}