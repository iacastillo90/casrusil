package com.casrusil.siierpai.modules.invoicing.domain.port.in;

import com.casrusil.siierpai.modules.invoicing.domain.dto.UpdateInvoiceCommand;
import com.casrusil.siierpai.modules.invoicing.domain.model.Invoice;

import java.util.UUID;

public interface UpdateInvoiceUseCase {

    Invoice updateInvoice(UUID id, UpdateInvoiceCommand command);
}
