package com.casrusil.siierpai.modules.dms.infrastructure.web;

import com.casrusil.siierpai.modules.dms.application.service.DmsServiceImpl;
import com.casrusil.siierpai.modules.dms.infrastructure.adapter.in.rest.DmsController;
import com.casrusil.siierpai.modules.dms.domain.model.CorporateDocument;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato multipart de POST /dms: archivo + metadatos en la misma petición.
 */
class DmsMultipartEndpointTest {

    @Test
    void acceptsMultipartUploadWithFileAndMetadata() throws Exception {
        DmsServiceImpl service = mock(DmsServiceImpl.class);
        DmsController controller = new DmsController(service);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        CompanyId companyId = CompanyId.random();
        CorporateDocument document = CorporateDocument.create(
                companyId, "Contrato", "CONTRATO", companyId.value() + "/file.pdf");
        when(service.uploadDocumentWithFile(any(CompanyId.class), eq("Contrato"), eq("CONTRATO"), any()))
                .thenReturn(document);

        MockMultipartFile file = new MockMultipartFile(
                "file", "Contrato.pdf", "application/pdf", "%PDF-1.4".getBytes());

        var result = ScopedValue.where(CompanyContext.COMPANY_ID, companyId).call(() ->
                mvc.perform(multipart("/api/v1/dms")
                                .file(file)
                                .param("documentType", "CONTRATO")
                                .param("title", "Contrato"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.title").value("Contrato"))
                        .andExpect(jsonPath("$.documentType").value("CONTRATO"))
                        .andReturn());

        verify(service).uploadDocumentWithFile(any(CompanyId.class), eq("Contrato"), eq("CONTRATO"), any());
        org.junit.jupiter.api.Assertions.assertEquals(200, result.getResponse().getStatus());
    }
}
