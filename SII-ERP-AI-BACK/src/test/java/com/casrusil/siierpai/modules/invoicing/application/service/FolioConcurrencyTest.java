package com.casrusil.siierpai.modules.invoicing.application.service;

import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.entity.CafEntity;
import com.casrusil.siierpai.modules.integration_sii.infrastructure.adapter.out.persistence.repository.CafJpaRepository;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que la asignación de folios sea atómica bajo concurrencia real
 * contra la base de datos de test.
 *
 * <p>
 * Cada hilo ejecuta su propia transacción. Si el bloqueo pesimista o el cálculo
 * del siguiente folio fueran incorrectos, dos hilos obtendrían el mismo folio.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:folio_concurrency;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;LOCK_TIMEOUT=20000",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "langchain4j.google-ai-gemini.chat-model.api-key=demo",
        "langchain4j.google-ai-gemini.chat-model.model-name=gemini-1.5-flash",
        "langchain4j.pgvector.host=localhost",
        "langchain4j.pgvector.port=5432",
        "langchain4j.pgvector.user=test",
        "langchain4j.pgvector.password=test",
        "langchain4j.pgvector.database=test",
        "langchain4j.pgvector.dimension=1536",
        "spring.mail.host=localhost",
        "spring.mail.port=2525",
        "spring.mail.username=test",
        "spring.mail.password=test"
})
class FolioConcurrencyTest {

    private static final int THREADS = 8;
    private static final String TIPO_DTE = "33";

    @Autowired
    private FolioAllocationService folioAllocationService;

    @Autowired
    private CafJpaRepository cafJpaRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private CompanyId companyId;

    @BeforeEach
    void setUp() {
        companyId = CompanyId.random();
    }

    @Test
    void concurrentReservationsMustReturnDistinctFolios() throws Exception {
        CafEntity caf = saveCaf(1L, 1000L, 1L);

        List<Long> folios = reserveConcurrently();

        assertEquals(THREADS, folios.size());
        Set<Long> distinct = new HashSet<>(folios);
        assertEquals(THREADS, distinct.size(), "Se asignó un folio duplicado: " + folios);
        folios.forEach(folio -> assertTrue(folio >= 1L && folio <= 1000L, "Folio fuera de rango: " + folio));

        CafEntity reloaded = cafJpaRepository.findById(caf.getId()).orElseThrow();
        assertEquals(1L + THREADS, reloaded.getCurrentFolio());
    }

    @Test
    void exhaustedCafReturnsEmptyInsteadOfReusingFolios() {
        saveCaf(1L, 2L, 1L);

        assertEquals(1L, reserveOne());
        assertEquals(2L, reserveOne());
        assertTrue(folioAllocationService.reserveNextFolio(companyId, Integer.parseInt(TIPO_DTE)).isEmpty());
    }

    private List<Long> reserveConcurrently() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Long>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < THREADS; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    start.await(10, TimeUnit.SECONDS);
                    return transactionTemplate.execute(status -> folioAllocationService
                            .reserveNextFolio(companyId, Integer.parseInt(TIPO_DTE))
                            .orElseThrow(() -> new IllegalStateException("Sin folios disponibles")));
                }));
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS), "Los hilos no arrancaron a tiempo");
            start.countDown();

            List<Long> folios = new ArrayList<>();
            for (Future<Long> future : futures) {
                folios.add(future.get(30, TimeUnit.SECONDS));
            }
            return folios;
        } finally {
            pool.shutdownNow();
        }
    }

    private Long reserveOne() {
        return transactionTemplate.execute(status -> folioAllocationService
                .reserveNextFolio(companyId, Integer.parseInt(TIPO_DTE))
                .orElseThrow(() -> new IllegalStateException("Sin folios disponibles")));
    }

    private CafEntity saveCaf(long initialFolio, long finalFolio, long currentFolio) {
        CafEntity caf = new CafEntity();
        caf.setId(UUID.randomUUID());
        caf.setCompanyId(companyId.value().toString());
        caf.setTipoDte(TIPO_DTE);
        caf.setInitialFolio(initialFolio);
        caf.setFinalFolio(finalFolio);
        caf.setCurrentFolio(currentFolio);
        caf.setFechaAutorizacion(LocalDate.now());
        caf.setFechaVencimiento(LocalDate.now().plusYears(1));
        caf.setXmlContent("<CAF/>");
        return cafJpaRepository.save(caf);
    }
}
