package com.casrusil.siierpai.modules.pos.domain.port.in;

import com.casrusil.siierpai.modules.pos.domain.model.PosTransaction;
import com.casrusil.siierpai.modules.pos.domain.model.PosTransactionItem;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;

import java.util.List;
import java.util.UUID;

public interface PosUseCase {
    PosTransaction registerSale(CompanyId companyId, String posTerminalId, String cashierName, String paymentMethod, List<PosTransactionItem> items);
    List<PosTransaction> getTransactionsByCompany(CompanyId companyId);
    void syncPendingTransactions(CompanyId companyId);
}
