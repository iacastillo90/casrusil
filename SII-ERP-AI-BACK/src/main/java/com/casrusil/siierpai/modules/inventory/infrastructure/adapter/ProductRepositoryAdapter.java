package com.casrusil.siierpai.modules.inventory.infrastructure.adapter;

import com.casrusil.siierpai.modules.inventory.domain.model.Product;
import com.casrusil.siierpai.modules.inventory.domain.port.ProductRepositoryPort;
import com.casrusil.siierpai.modules.inventory.infrastructure.persistence.entity.ProductEntity;
import com.casrusil.siierpai.modules.inventory.infrastructure.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository jpaRepository;

    @Override
    public Product save(Product product) {
        ProductEntity entity = mapToEntity(product);
        ProductEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Product> findByIdAndCompanyId(Long id, String companyId) {
        return jpaRepository.findByIdAndCompanyId(id, companyId).map(this::mapToDomain);
    }

    @Override
    public Optional<Product> findBySkuAndCompanyId(String sku, String companyId) {
        return jpaRepository.findBySkuAndCompanyId(sku, companyId).map(this::mapToDomain);
    }

    @Override
    public List<Product> findAllByCompanyId(String companyId) {
        return jpaRepository.findAllByCompanyId(companyId).stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByIdAndCompanyId(Long id, String companyId) {
        jpaRepository.deleteByIdAndCompanyId(id, companyId);
    }

    private ProductEntity mapToEntity(Product domain) {
        ProductEntity entity = new ProductEntity();
        entity.setId(domain.getId());
        entity.setCompanyId(domain.getCompanyId());
        entity.setSku(domain.getSku());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setCurrentStock(domain.getCurrentStock());
        entity.setMinStock(domain.getMinStock());
        entity.setAverageCost(domain.getAverageCost());
        entity.setSalePrice(domain.getSalePrice());
        entity.setCategory(domain.getCategory());
        return entity;
    }

    private Product mapToDomain(ProductEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .sku(entity.getSku())
                .name(entity.getName())
                .description(entity.getDescription())
                .currentStock(entity.getCurrentStock())
                .minStock(entity.getMinStock())
                .averageCost(entity.getAverageCost())
                .salePrice(entity.getSalePrice())
                .category(entity.getCategory())
                .build();
    }
}
