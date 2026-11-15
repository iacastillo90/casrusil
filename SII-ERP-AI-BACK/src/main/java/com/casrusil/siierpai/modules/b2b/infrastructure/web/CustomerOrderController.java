package com.casrusil.siierpai.modules.b2b.infrastructure.web;

import com.casrusil.siierpai.modules.b2b.domain.model.CustomerOrder;
import com.casrusil.siierpai.modules.b2b.domain.model.CustomerOrderItem;
import com.casrusil.siierpai.modules.b2b.domain.port.in.CustomerOrderUseCase;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/b2b/orders")
@RequiredArgsConstructor
public class CustomerOrderController {

    private final CustomerOrderUseCase customerOrderUseCase;

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> myOrders() {
        UserId userId = currentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(customerOrderUseCase.listOrders(userId).stream()
                .map(OrderResponse::from)
                .toList());
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        UserId userId = currentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<CustomerOrderItem> items = request.items().stream()
                .map(i -> new CustomerOrderItem(i.productName(), i.quantity(), i.unitPrice()))
                .toList();

        CustomerOrder order = customerOrderUseCase.createOrder(userId, items);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> payOrder(@PathVariable UUID id) {
        UserId userId = currentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(OrderResponse.from(customerOrderUseCase.payOrder(id, userId)));
    }

    private UserId currentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserId userId)) {
            return null;
        }
        return userId;
    }

    public record CreateOrderRequest(@NotNull @NotEmpty List<@Valid ItemRequest> items) {
    }

    public record ItemRequest(
            @NotNull String productName,
            @Positive int quantity,
            @NotNull BigDecimal unitPrice) {
    }

    public record OrderItemResponse(String productName, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }

    public record OrderResponse(
            UUID id,
            String status,
            BigDecimal totalAmount,
            List<OrderItemResponse> items,
            String createdAt) {

        static OrderResponse from(CustomerOrder order) {
            List<OrderItemResponse> items = order.getItems().stream()
                    .map(i -> new OrderItemResponse(i.productName(), i.quantity(), i.unitPrice(), i.subtotal()))
                    .toList();

            return new OrderResponse(
                    order.getId(),
                    order.getStatus().name(),
                    order.getTotalAmount(),
                    items,
                    order.getCreatedAt().toString());
        }
    }
}
