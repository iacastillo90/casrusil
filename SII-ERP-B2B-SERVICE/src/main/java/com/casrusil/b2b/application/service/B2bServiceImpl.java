package com.casrusil.b2b.application.service;

import com.casrusil.b2b.domain.model.B2bOrder;
import com.casrusil.b2b.infrastructure.persistence.entity.B2bOrderEntity;
import com.casrusil.b2b.infrastructure.persistence.entity.B2bOrderItemEntity;
import com.casrusil.b2b.infrastructure.persistence.repository.B2bOrderJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class B2bServiceImpl {
    private final B2bOrderJpaRepository repo;

    public B2bServiceImpl(B2bOrderJpaRepository repo) {
        this.repo = repo;
    }

    public B2bOrder placeOrder(String companyId, String customerId, List<B2bOrder.B2bOrderItem> items) {
        BigDecimal total = items.stream().map(i -> i.unitPrice().multiply(new BigDecimal(i.quantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        B2bOrderEntity e = new B2bOrderEntity();
        e.setId(UUID.randomUUID());
        e.setCompanyId(companyId);
        e.setCustomerId(customerId);
        e.setTotalAmount(total);
        e.setStatus("PENDING");
        e.setCreatedAt(LocalDate.now());

        List<B2bOrderItemEntity> ents = items.stream().map(i -> {
            B2bOrderItemEntity ie = new B2bOrderItemEntity();
            ie.setProductId(i.productId());
            ie.setQuantity(i.quantity());
            ie.setUnitPrice(i.unitPrice());
            return ie;
        }).collect(Collectors.toList());
        e.setItems(ents);

        repo.save(e);
        return new B2bOrder(e.getId(), companyId, customerId, items, total, "PENDING", e.getCreatedAt());
    }

    public List<B2bOrder> getCustomerOrders(String companyId, String customerId) {
        return repo.findByCompanyIdAndCustomerId(companyId, customerId).stream().map(e -> {
            List<B2bOrder.B2bOrderItem> items = e.getItems().stream()
                .map(i -> new B2bOrder.B2bOrderItem(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                .collect(Collectors.toList());
            return new B2bOrder(e.getId(), companyId, e.getCustomerId(), items, e.getTotalAmount(), e.getStatus(), e.getCreatedAt());
        }).collect(Collectors.toList());
    }
}
