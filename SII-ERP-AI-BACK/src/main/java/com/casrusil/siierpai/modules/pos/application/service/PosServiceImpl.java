package com.casrusil.siierpai.modules.pos.application.service;

import com.casrusil.siierpai.modules.pos.domain.model.PosTransaction;
import com.casrusil.siierpai.modules.pos.domain.model.PosTransactionItem;
import com.casrusil.siierpai.modules.pos.domain.port.in.PosUseCase;
import com.casrusil.siierpai.modules.pos.domain.port.out.PosTransactionRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PosServiceImpl implements PosUseCase {

    private final PosTransactionRepository transactionRepository;

    public PosServiceImpl(PosTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public PosTransaction registerSale(CompanyId companyId, String posTerminalId, String cashierName, String paymentMethod, List<PosTransactionItem> items) {
        PosTransaction transaction = PosTransaction.create(companyId, posTerminalId, cashierName, paymentMethod, items);
        return transactionRepository.save(transaction);
    }

    @Override
    public List<PosTransaction> getTransactionsByCompany(CompanyId companyId) {
        return transactionRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional
    public void syncPendingTransactions(CompanyId companyId) {
        List<PosTransaction> pending = transactionRepository.findPendingSync(companyId);
        for (PosTransaction tx : pending) {
            // TODO: In a real system, this would call the SII service to generate an electronic receipt (Boleta Electrónica)
            // and maybe decrease inventory.
            
            tx.markAsCompleted();
            transactionRepository.save(tx);
        }
    }
}
