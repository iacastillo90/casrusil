package com.casrusil.siierpai.shared.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 🚀 FERRARI ENGINE: Configuración de procesamiento asíncrono con Virtual
 * Threads.
 * 
 * <p>
 * Habilita el procesamiento paralelo de eventos de dominio utilizando
 * Virtual Threads de Java 21+, permitiendo manejar millones de tareas
 * concurrentes sin el overhead de threads nativos.
 * 
 * <h2>Beneficios de Virtual Threads:</h2>
 * <ul>
 * <li>Escalabilidad masiva: Millones de threads virtuales vs miles de threads
 * nativos</li>
 * <li>Menor consumo de memoria: ~1KB por virtual thread vs ~1MB por thread
 * nativo</li>
 * <li>Mejor throughput: Ideal para I/O-bound operations (DB, HTTP)</li>
 * <li>Código más simple: No necesita CompletableFuture ni reactive streams</li>
 * </ul>
 * 
 * <h2>Listeners Afectados:</h2>
 * <ul>
 * <li>{@code InvoiceAccountingListener} - Asientos contables</li>
 * <li>{@code PartnerLedgerListener} - Cuentas corrientes</li>
 * <li>{@code SustainabilityListener} - Métricas verdes (Fase 2)</li>
 * <li>{@code F29ProjectionListener} - Proyección F29 (Fase 1)</li>
 * </ul>
 * 
 * @see java.util.concurrent.Executors#newVirtualThreadPerTaskExecutor()
 * @since 2.0 - Ferrari Engine
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Configura el executor para usar Virtual Threads.
     * 
     * <p>
     * Cada tarea asíncrona (@Async) se ejecutará en su propio Virtual Thread,
     * permitiendo procesamiento paralelo masivo sin bloquear el thread principal.
     * 
     * @return Executor basado en Virtual Threads
     */
    @Override
    public Executor getAsyncExecutor() {
        logger.info("🚀 [FERRARI ENGINE] Inicializando Virtual Thread Executor");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Maneja excepciones no capturadas en métodos @Async.
     * 
     * <p>
     * Esto evita que errores en listeners asíncronos pasen desapercibidos,
     * lo cual era un problema común en la arquitectura anterior.
     * 
     * @return Handler que registra excepciones en logs
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            logger.error("❌ [ASYNC ERROR] Excepción en método asíncrono: {} | Método: {} | Parámetros: {}",
                    ex.getMessage(),
                    method.getName(),
                    params,
                    ex);
        };
    }
}
