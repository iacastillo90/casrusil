package com.casrusil.siierpai.modules.inventory.domain.port;

import com.casrusil.siierpai.modules.inventory.domain.model.StockMovement;
import java.util.List;

public interface StockMovementRepositoryPort {
    StockMovement save(StockMovement movement);
    List<StockMovement> findByProductIdAndCompanyId(Long productId, String companyId);
}
