package com.casrusil.siierpai.modules.b2b.domain.model;

import com.casrusil.siierpai.shared.domain.valueobject.UserId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CustomerOrder {

    private final UUID id;
    private final UserId customerId;
    private final List<CustomerOrderItem> items;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private OrderStatus status;

    private CustomerOrder(UUID id, UserId customerId, List<CustomerOrderItem> items, BigDecimal totalAmount,
            LocalDateTime createdAt, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.status = status;
    }

    public static CustomerOrder create(UserId customerId, List<CustomerOrderItem> items) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("order must have at least one item");
        }

        List<CustomerOrderItem> itemCopy = List.copyOf(items);
        BigDecimal total = itemCopy.stream()
                .map(CustomerOrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CustomerOrder(UUID.randomUUID(), customerId, itemCopy, total, LocalDateTime.now(),
                OrderStatus.PENDING);
    }

    public static CustomerOrder restore(UUID id, UserId customerId, List<CustomerOrderItem> items,
            BigDecimal totalAmount, LocalDateTime createdAt, OrderStatus status) {
        return new CustomerOrder(id, customerId, List.copyOf(items), totalAmount, createdAt, status);
    }

    public void pay() {
        if (status == OrderStatus.PAID) {
            throw new IllegalStateException("Order " + id + " is already paid");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order " + id + " is cancelled and cannot be paid");
        }
        this.status = OrderStatus.PAID;
    }

    public UUID getId() {
        return id;
    }

    public UserId getCustomerId() {
        return customerId;
    }

    public List<CustomerOrderItem> getItems() {
        return items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
