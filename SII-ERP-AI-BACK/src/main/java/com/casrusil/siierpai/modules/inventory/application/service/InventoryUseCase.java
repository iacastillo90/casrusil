package com.casrusil.siierpai.modules.inventory.application.service;

import com.casrusil.siierpai.modules.inventory.domain.dto.ProductRequest;
import com.casrusil.siierpai.modules.inventory.domain.dto.ProductResponse;
import com.casrusil.siierpai.modules.inventory.domain.dto.StockMovementRequest;
import com.casrusil.siierpai.modules.inventory.domain.dto.StockMovementResponse;

import java.util.List;

public interface InventoryUseCase {
    ProductResponse createProduct(ProductRequest request, String companyId);
    ProductResponse updateProduct(Long id, ProductRequest request, String companyId);
    void deleteProduct(Long id, String companyId);
    ProductResponse getProduct(Long id, String companyId);
    List<ProductResponse> getAllProducts(String companyId);
    
    StockMovementResponse registerMovement(Long productId, StockMovementRequest request, String companyId);
    List<StockMovementResponse> getProductMovements(Long productId, String companyId);
}
