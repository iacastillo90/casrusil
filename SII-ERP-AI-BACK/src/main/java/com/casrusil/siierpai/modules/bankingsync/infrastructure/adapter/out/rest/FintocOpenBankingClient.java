package com.casrusil.siierpai.modules.bankingsync.infrastructure.adapter.out.rest;

import com.casrusil.siierpai.modules.bankingsync.domain.model.BankTransaction;
import com.casrusil.siierpai.modules.bankingsync.domain.port.out.OpenBankingClient;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class FintocOpenBankingClient implements OpenBankingClient {

    private static final Logger logger = LoggerFactory.getLogger(FintocOpenBankingClient.class);
    
    @Value("${fintoc.api.key:default_key}")
    private String fintocApiKey;

    private final org.springframework.web.client.RestTemplate restTemplate;

    public FintocOpenBankingClient(org.springframework.web.client.RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<BankTransaction> fetchTransactions(CompanyId companyId, String bankName, String accountNumber) {
        logger.info("Fetching real transactions from Fintoc for account {} at {}", accountNumber, bankName);
        
        if ("default_key".equals(fintocApiKey)) {
            logger.warn("Fintoc API Key not configured. Integration would fail in production.");
            throw new RuntimeException("Fintoc API key not configured for real integration");
        }
        
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", fintocApiKey);
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            // Fintoc Link Token would ideally be retrieved from a secure vault based on CompanyId and BankName.
            // Assuming accountNumber represents the Fintoc Account ID in this context.
            String url = "https://api.fintoc.com/v1/accounts/" + accountNumber + "/movements";
            
            org.springframework.http.ResponseEntity<FintocMovementResponse[]> response = restTemplate.exchange(
                url, 
                org.springframework.http.HttpMethod.GET, 
                entity, 
                FintocMovementResponse[].class
            );
            
            if (response.getBody() == null) {
                return Collections.emptyList();
            }
            
            return java.util.Arrays.stream(response.getBody())
                .map(mov -> BankTransaction.create(
                    companyId, 
                    bankName, 
                    accountNumber, 
                    mov.id(),
                    LocalDateTime.parse(mov.post_date().substring(0, 19)),
                    mov.description(),
                    mov.amount()
                ))
                .toList();
                
        } catch (Exception e) {
            logger.error("Failed to fetch Fintoc transactions", e);
            throw new RuntimeException("Error communicating with Fintoc API", e);
        }
    }
    
    // Internal DTO for Fintoc Movement
    private record FintocMovementResponse(String id, BigDecimal amount, String description, String post_date) {}
}
