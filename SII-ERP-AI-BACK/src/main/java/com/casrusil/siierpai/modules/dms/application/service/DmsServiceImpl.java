package com.casrusil.siierpai.modules.dms.application.service;

import com.casrusil.siierpai.modules.dms.domain.model.CorporateDocument;
import com.casrusil.siierpai.modules.dms.infrastructure.persistence.entity.CorporateDocumentEntity;
import com.casrusil.siierpai.modules.dms.infrastructure.persistence.repository.CorporateDocumentJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DmsServiceImpl {
    private final CorporateDocumentJpaRepository repo;

    public DmsServiceImpl(CorporateDocumentJpaRepository repo) {
        this.repo = repo;
    }

    public CorporateDocument uploadDocument(CompanyId companyId, String title, String documentType, String storageUrl) {
        CorporateDocumentEntity e = new CorporateDocumentEntity();
        e.setId(UUID.randomUUID());
        e.setCompanyId(companyId.value().toString());
        e.setTitle(title);
        e.setDocumentType(documentType);
        e.setStorageUrl(storageUrl);
        repo.save(e);
        return new CorporateDocument(e.getId(), companyId, title, documentType, storageUrl, null, null);
    }

    public List<CorporateDocument> getDocuments(CompanyId companyId) {
        return repo.findByCompanyId(companyId.value().toString()).stream()
            .map(e -> new CorporateDocument(e.getId(), companyId, e.getTitle(), e.getDocumentType(), e.getStorageUrl(), e.getExpirationDate(), e.getAiSummary()))
            .collect(Collectors.toList());
    }

    public CorporateDocument analyzeDocumentWithAi(CompanyId companyId, UUID documentId) {
        CorporateDocumentEntity e = repo.findById(documentId)
            .filter(d -> d.getCompanyId().equals(companyId.value().toString()))
            .orElseThrow(() -> new RuntimeException("Document not found"));

        // Mock AI Extraction Logic but we save it to DB
        e.setExpirationDate(LocalDate.now().plusMonths(6));
        e.setAiSummary("Este contrato es de arrendamiento. Las partes involucradas son Inmobiliaria SUR y Acme Corp. El canon de arriendo es de 50 UF.");
        repo.save(e);

        return new CorporateDocument(e.getId(), companyId, e.getTitle(), e.getDocumentType(), e.getStorageUrl(), e.getExpirationDate(), e.getAiSummary());
    }
}