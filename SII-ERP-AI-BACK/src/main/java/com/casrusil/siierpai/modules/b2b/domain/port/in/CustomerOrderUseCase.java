package com.casrusil.siierpai.modules.b2b.domain.port.in;

import com.casrusil.siierpai.modules.b2b.domain.model.CustomerOrder;
import com.casrusil.siierpai.modules.b2b.domain.model.CustomerOrderItem;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;

import java.util.List;
import java.util.UUID;

public interface CustomerOrderUseCase {

    List<CustomerOrder> listOrders(UserId customerId);

    CustomerOrder createOrder(UserId customerId, List<CustomerOrderItem> items);

    CustomerOrder payOrder(UUID orderId, UserId customerId);
}
