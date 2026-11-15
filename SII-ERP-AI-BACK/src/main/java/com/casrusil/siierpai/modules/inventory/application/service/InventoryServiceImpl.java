package com.casrusil.siierpai.modules.inventory.application.service;

import com.casrusil.siierpai.modules.inventory.domain.dto.ProductRequest;
import com.casrusil.siierpai.modules.inventory.domain.dto.ProductResponse;
import com.casrusil.siierpai.modules.inventory.domain.dto.StockMovementRequest;
import com.casrusil.siierpai.modules.inventory.domain.dto.StockMovementResponse;
import com.casrusil.siierpai.modules.inventory.domain.model.MovementType;
import com.casrusil.siierpai.modules.inventory.domain.model.Product;
import com.casrusil.siierpai.modules.inventory.domain.model.StockMovement;
import com.casrusil.siierpai.modules.inventory.domain.port.ProductRepositoryPort;
import com.casrusil.siierpai.modules.inventory.domain.port.StockMovementRepositoryPort;
import com.casrusil.siierpai.shared.domain.event.StockMovedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryUseCase {

    private final ProductRepositoryPort productRepository;
    private final StockMovementRepositoryPort stockMovementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, String companyId) {
        Product product = Product.builder()
                .companyId(companyId)
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .salePrice(request.getSalePrice())
                .category(request.getCategory())
                .minStock(request.getMinStock() != null ? request.getMinStock() : BigDecimal.valueOf(5))
                .currentStock(BigDecimal.ZERO)
                .averageCost(BigDecimal.ZERO)
                .build();
                
        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String companyId) {
        Product product = productRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
                
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSalePrice(request.getSalePrice());
        product.setCategory(request.getCategory());
        if (request.getMinStock() != null) {
            product.setMinStock(request.getMinStock());
        }
        
        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, String companyId) {
        productRepository.deleteByIdAndCompanyId(id, companyId);
    }

    @Override
    public ProductResponse getProduct(Long id, String companyId) {
        return productRepository.findByIdAndCompanyId(id, companyId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public List<ProductResponse> getAllProducts(String companyId) {
        return productRepository.findAllByCompanyId(companyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StockMovementResponse registerMovement(Long productId, StockMovementRequest request, String companyId) {
        Product product = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
                
        BigDecimal totalMovementCost = request.getQuantity().multiply(request.getUnitCost());
        
        StockMovement movement = StockMovement.builder()
                .companyId(companyId)
                .productId(productId)
                .type(request.getType())
                .quantity(request.getQuantity())
                .unitCost(request.getUnitCost())
                .totalCost(totalMovementCost)
                .movementDate(request.getMovementDate())
                .referenceDocument(request.getReferenceDocument())
                .description(request.getDescription())
                .build();
                
        // PMP (Precio Medio Ponderado) Logic
        if (request.getType() == MovementType.IN) {
            BigDecimal currentTotalValue = product.getCurrentStock().multiply(product.getAverageCost());
            BigDecimal newTotalStock = product.getCurrentStock().add(request.getQuantity());
            BigDecimal newTotalValue = currentTotalValue.add(totalMovementCost);
            
            BigDecimal newAverageCost = newTotalStock.compareTo(BigDecimal.ZERO) == 0 ? 
                    BigDecimal.ZERO : newTotalValue.divide(newTotalStock, 4, RoundingMode.HALF_UP);
                    
            product.setCurrentStock(newTotalStock);
            product.setAverageCost(newAverageCost);
        } else if (request.getType() == MovementType.OUT) {
            if (product.getCurrentStock().compareTo(request.getQuantity()) < 0) {
                throw new RuntimeException("Insufficient stock");
            }
            product.setCurrentStock(product.getCurrentStock().subtract(request.getQuantity()));
            // Average cost remains the same on OUT movements for PMP
        }
        
        productRepository.save(product);
        StockMovement savedMovement = stockMovementRepository.save(movement);
        
        eventPublisher.publishEvent(new StockMovedEvent(
                savedMovement.getCompanyId(),
                savedMovement.getProductId(),
                savedMovement.getType().name(),
                savedMovement.getQuantity(),
                savedMovement.getUnitCost(),
                savedMovement.getTotalCost(),
                savedMovement.getMovementDate()
        ));
        
        return mapToMovementResponse(savedMovement);
    }

    @Override
    public List<StockMovementResponse> getProductMovements(Long productId, String companyId) {
        return stockMovementRepository.findByProductIdAndCompanyId(productId, companyId).stream()
                .map(this::mapToMovementResponse)
                .collect(Collectors.toList());
    }
    
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .currentStock(product.getCurrentStock())
                .minStock(product.getMinStock())
                .averageCost(product.getAverageCost())
                .salePrice(product.getSalePrice())
                .category(product.getCategory())
                .build();
    }
    
    private StockMovementResponse mapToMovementResponse(StockMovement movement) {
        return StockMovementResponse.builder()
                .id(movement.getId())
                .productId(movement.getProductId())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .unitCost(movement.getUnitCost())
                .totalCost(movement.getTotalCost())
                .movementDate(movement.getMovementDate())
                .referenceDocument(movement.getReferenceDocument())
                .description(movement.getDescription())
                .build();
    }
}