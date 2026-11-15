package com.casrusil.siierpai.modules.b2b.application.service;

import com.casrusil.siierpai.modules.b2b.domain.model.CustomerOrder;
import com.casrusil.siierpai.modules.b2b.domain.model.CustomerOrderItem;
import com.casrusil.siierpai.modules.b2b.domain.port.in.CustomerOrderUseCase;
import com.casrusil.siierpai.modules.b2b.domain.port.out.CustomerOrderRepositoryPort;
import com.casrusil.siierpai.shared.domain.exception.ResourceNotFoundException;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerOrderService implements CustomerOrderUseCase {

    private final CustomerOrderRepositoryPort orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CustomerOrder> listOrders(UserId customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional
    public CustomerOrder createOrder(UserId customerId, List<CustomerOrderItem> items) {
        return orderRepository.save(CustomerOrder.create(customerId, items));
    }

    @Override
    @Transactional
    public CustomerOrder payOrder(UUID orderId, UserId customerId) {
        CustomerOrder order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        order.pay();
        return orderRepository.save(order);
    }
}
