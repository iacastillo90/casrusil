package com.casrusil.siierpai.modules.bankingsync.application.service;

import com.casrusil.siierpai.shared.infrastructure.context.TenantGuard;
import com.casrusil.siierpai.modules.bankingsync.domain.model.BankConnection;
import com.casrusil.siierpai.modules.bankingsync.domain.model.BankTransaction;
import com.casrusil.siierpai.modules.bankingsync.domain.port.in.BankingSyncUseCase;
import com.casrusil.siierpai.modules.bankingsync.domain.port.out.BankConnectionRepository;
import com.casrusil.siierpai.modules.bankingsync.domain.port.out.BankTransactionRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BankingSyncServiceImpl implements BankingSyncUseCase {

    private final BankConnectionRepository connectionRepository;
    private final BankTransactionRepository transactionRepository;

    public BankingSyncServiceImpl(BankConnectionRepository connectionRepository, BankTransactionRepository transactionRepository) {
        this.connectionRepository = connectionRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional
    public BankConnection connectBank(CompanyId companyId, String bankName, String accountNumber) {
        BankConnection connection = BankConnection.create(companyId, bankName, accountNumber);
        return connectionRepository.save(connection);
    }

    @Override
    public List<BankConnection> getConnections(CompanyId companyId) {
        return connectionRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional
    public void syncTransactions(CompanyId companyId) {
        List<BankConnection> connections = connectionRepository.findByCompanyId(companyId);
        
        for (BankConnection conn : connections) {
            // Mock API Sync from Open Banking provider
            BankTransaction tx1 = BankTransaction.create(companyId, conn.getBankName(), conn.getAccountNumber(), UUID.randomUUID().toString(), LocalDateTime.now().minusDays(1), "Pago Proveedores", new BigDecimal("-150000"));
            BankTransaction tx2 = BankTransaction.create(companyId, conn.getBankName(), conn.getAccountNumber(), UUID.randomUUID().toString(), LocalDateTime.now(), "Abono Cliente", new BigDecimal("500000"));
            
            transactionRepository.save(tx1);
            transactionRepository.save(tx2);
            
            conn.setLastSync(LocalDateTime.now().toString());
            connectionRepository.save(conn);
        }
    }

    @Override
    public List<BankTransaction> getTransactions(CompanyId companyId) {
        return transactionRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional
    public void reconcileTransaction(UUID transactionId) {
        BankTransaction tx = TenantGuard.requireOwned(transactionRepository.findById(transactionId),
                BankTransaction::getCompanyId, "Movimiento bancario");
        tx.markAsReconciled();
        transactionRepository.save(tx);
    }

    @Override
    @Transactional
    public void ignoreTransaction(UUID transactionId) {
        BankTransaction tx = TenantGuard.requireOwned(transactionRepository.findById(transactionId),
                BankTransaction::getCompanyId, "Movimiento bancario");
        tx.markAsIgnored();
        transactionRepository.save(tx);
    }
}
