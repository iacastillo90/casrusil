package com.casrusil.siierpai.modules.b2b.infrastructure.persistence.adapter;

import com.casrusil.siierpai.modules.b2b.domain.model.CustomerOrder;
import com.casrusil.siierpai.modules.b2b.domain.model.CustomerOrderItem;
import com.casrusil.siierpai.modules.b2b.domain.model.OrderStatus;
import com.casrusil.siierpai.modules.b2b.domain.port.out.CustomerOrderRepositoryPort;
import com.casrusil.siierpai.modules.b2b.infrastructure.persistence.entity.CustomerOrderEntity;
import com.casrusil.siierpai.modules.b2b.infrastructure.persistence.entity.CustomerOrderItemEntity;
import com.casrusil.siierpai.modules.b2b.infrastructure.persistence.repository.CustomerOrderJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerOrderRepositoryAdapter implements CustomerOrderRepositoryPort {

    private final CustomerOrderJpaRepository jpaRepository;

    @Override
    public CustomerOrder save(CustomerOrder order) {
        CustomerOrderEntity entity = jpaRepository.findById(order.getId()).orElse(null);

        if (entity == null) {
            entity = CustomerOrderEntity.builder()
                    .id(order.getId())
                    .customerId(order.getCustomerId().value())
                    .status(order.getStatus().name())
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .build();
            for (CustomerOrderItem item : order.getItems()) {
                entity.addItem(toItemEntity(item));
            }
        } else {
            entity.setStatus(order.getStatus().name());
        }

        return mapToDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<CustomerOrder> findByIdAndCustomerId(UUID id, UserId customerId) {
        return jpaRepository.findByIdAndCustomerId(id, customerId.value()).map(this::mapToDomain);
    }

    @Override
    public List<CustomerOrder> findByCustomerId(UserId customerId) {
        return jpaRepository.findByCustomerIdOrderByCreatedAtDesc(customerId.value()).stream()
                .map(this::mapToDomain)
                .toList();
    }

    private CustomerOrderItemEntity toItemEntity(CustomerOrderItem item) {
        return CustomerOrderItemEntity.builder()
                .productName(item.productName())
                .quantity(item.quantity())
                .unitPrice(item.unitPrice())
                .build();
    }

    private CustomerOrder mapToDomain(CustomerOrderEntity entity) {
        List<CustomerOrderItem> items = entity.getItems().stream()
                .map(i -> new CustomerOrderItem(i.getProductName(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        return CustomerOrder.restore(
                entity.getId(),
                UserId.of(entity.getCustomerId()),
                items,
                entity.getTotalAmount(),
                entity.getCreatedAt(),
                OrderStatus.valueOf(entity.getStatus()));
    }
}
