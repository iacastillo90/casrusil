package com.casrusil.siierpai.modules.b2b.domain.port.out;

import com.casrusil.siierpai.modules.b2b.domain.model.CustomerOrder;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerOrderRepositoryPort {

    CustomerOrder save(CustomerOrder order);

    Optional<CustomerOrder> findByIdAndCustomerId(UUID id, UserId customerId);

    List<CustomerOrder> findByCustomerId(UserId customerId);
}
