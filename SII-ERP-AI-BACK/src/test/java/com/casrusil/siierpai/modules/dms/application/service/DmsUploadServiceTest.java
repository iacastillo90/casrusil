package com.casrusil.siierpai.modules.dms.application.service;

import com.casrusil.siierpai.modules.dms.domain.model.CorporateDocument;
import com.casrusil.siierpai.modules.dms.infrastructure.adapter.out.ai.GeminiApiClient;
import com.casrusil.siierpai.modules.dms.infrastructure.persistence.entity.CorporateDocumentEntity;
import com.casrusil.siierpai.modules.dms.infrastructure.persistence.repository.CorporateDocumentJpaRepository;
import com.casrusil.siierpai.shared.application.port.out.DocumentStoragePort;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Subida de archivos del gestor documental: valida tamaño, tipo MIME y que el
 * archivo se envía al almacenamiento antes de persistir los metadatos.
 */
class DmsUploadServiceTest {

    private CorporateDocumentJpaRepository repo;
    private DocumentStoragePort storage;
    private DmsServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(CorporateDocumentJpaRepository.class);
        storage = mock(DocumentStoragePort.class);
        service = new DmsServiceImpl(repo, storage, mock(GeminiApiClient.class),
                10L * 1024 * 1024, "application/pdf,image/png");
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void uploadsPdfAndPersistsStorageKey() throws Exception {
        when(storage.uploadDocument(anyString(), any(InputStream.class), eq("application/pdf")))
                .thenReturn("key");

        MockMultipartFile file = new MockMultipartFile(
                "file", "Contrato 2026.pdf", "application/pdf", "%PDF-1.4 demo".getBytes());
        CompanyId companyId = CompanyId.random();

        CorporateDocument document = service.uploadDocumentWithFile(
                companyId, null, "CONTRATO", file);

        assertEquals("Contrato 2026", document.getTitle());
        assertEquals("CONTRATO", document.getDocumentType());
        assertTrue(document.getStorageUrl().startsWith(companyId.value().toString() + "/"));
        assertTrue(document.getStorageUrl().endsWith("Contrato_2026.pdf"));
        verify(repo).save(any(CorporateDocumentEntity.class));
    }

    @Test
    void rejectsDisallowedContentTypeWithoutTouchingStorage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.sh", "application/x-sh", "echo hola".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.uploadDocumentWithFile(
                CompanyId.random(), "Script", "OTRO", file));

        verify(storage, never()).uploadDocument(anyString(), any(), anyString());
        verify(repo, never()).save(any());
    }

    @Test
    void rejectsFileOverLimit() throws Exception {
        byte[] big = new byte[(int) (10L * 1024 * 1024 + 1)];
        MockMultipartFile file = new MockMultipartFile("file", "grande.pdf", "application/pdf", big);

        assertThrows(IllegalArgumentException.class, () -> service.uploadDocumentWithFile(
                CompanyId.random(), "Grande", "CONTRATO", file));

        verify(storage, never()).uploadDocument(anyString(), any(), anyString());
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "vacio.pdf", "application/pdf", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> service.uploadDocumentWithFile(
                CompanyId.random(), "Vacío", "CONTRATO", file));
    }
}
