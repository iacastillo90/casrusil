package com.casrusil.siierpai.modules.inventory.domain.port;

import com.casrusil.siierpai.modules.inventory.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findByIdAndCompanyId(Long id, String companyId);
    Optional<Product> findBySkuAndCompanyId(String sku, String companyId);
    List<Product> findAllByCompanyId(String companyId);
    void deleteByIdAndCompanyId(Long id, String companyId);
}
