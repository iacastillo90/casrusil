package com.casrusil;

import com.casrusil.siierpai.modules.accounting.domain.model.Account;
import com.casrusil.siierpai.modules.accounting.domain.model.AccountType;
import com.casrusil.siierpai.modules.accounting.domain.port.out.AccountRepository;
import com.casrusil.siierpai.modules.ai_assistant.infrastructure.config.EmbeddingConfig;
import com.casrusil.siierpai.modules.sso.domain.model.Company;
import com.casrusil.siierpai.modules.sso.domain.port.out.CompanyRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
public class SiiErpAiApplication {

	private static final Logger logger = LoggerFactory.getLogger(SiiErpAiApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SiiErpAiApplication.class, args);
	}

	/**
	 * 🧠 GENESIS SEEDER: Garantiza que la estructura contable base exista siempre.
	 * Se ejecuta automáticamente al levantar el Backend.
	 */
	@Bean
	CommandLineRunner ensureAccountingBasics(CompanyRepository companyRepository, AccountRepository accountRepository) {
		return args -> {
			logger.info("🚀 [GENESIS] Verificando integridad contable para todas las empresas...");

			List<Company> companies = companyRepository.findAll();

			for (Company company : companies) {
				logger.info("🔧 Auditando Plan de Cuentas para: {}", company.getRazonSocial());

				// Lista Maestra de Cuentas "God Mode"
				ensureAccount(accountRepository, company, "1.1.01.001", "Caja", AccountType.ASSET);
				ensureAccount(accountRepository, company, "1.1.03.001", "Existencias", AccountType.ASSET);
				ensureAccount(accountRepository, company, "1.1.03.999", "Existencias por Clasificar",
						AccountType.ASSET); // <--- LA QUE FALTABA
				ensureAccount(accountRepository, company, "1.1.05.001", "Clientes Nacionales", AccountType.ASSET);
				ensureAccount(accountRepository, company, "1.1.08.001", "IVA Crédito Fiscal", AccountType.ASSET);

				ensureAccount(accountRepository, company, "2.1.01.001", "Proveedores Nacionales",
						AccountType.LIABILITY);
				ensureAccount(accountRepository, company, "2.1.04.001", "IVA Débito Fiscal", AccountType.LIABILITY);

				ensureAccount(accountRepository, company, "4.1.01.001", "Ventas Nacionales", AccountType.REVENUE);

				ensureAccount(accountRepository, company, "5.1.03.025", "Gastos Generales", AccountType.EXPENSE);
				ensureAccount(accountRepository, company, "5.9.99.999", "Gastos por Clasificar", AccountType.EXPENSE);
			}

			logger.info("✅ [GENESIS] Integridad contable restaurada. Listo para importar.");
		};
	}

	/**
	 * 🤖 EMBEDDING MODEL TEST: Verifica que el modelo ONNX all-MiniLM-L6-v2
	 * se descarga correctamente y genera embeddings de 384 dimensiones.
	 * Solo se ejecuta cuando langchain4j.rag.enabled=true
	 */
	@Bean
	@Lazy
	@ConditionalOnProperty(name = "langchain4j.rag.enabled", havingValue = "true", matchIfMissing = false)
	CommandLineRunner testEmbeddingModel(@Lazy EmbeddingModel embeddingModel, @Lazy EmbeddingStore<?> embeddingStore) {
		return args -> {
			logger.info("🤖 [RAG TEST] Probando modelo de embeddings ONNX all-MiniLM-L6-v2...");
			long startTime = System.currentTimeMillis();
			
			// Test 1: Generate embedding for Spanish text
			String testText = "Esta es una prueba de embeddings en español para RAG";
			Embedding embedding = embeddingModel.embed(testText).content();
			
			long embedTime = System.currentTimeMillis() - startTime;
			
			// Verify dimension
			int dimension = embedding.vector().length;
			assert dimension == 384 : "Expected 384 dimensions, got " + dimension;
			
			// Verify non-zero values
		 float sum = 0;
		 for (float v : embedding.vector()) {
		 sum += Math.abs(v);
		 }
		 assert sum > 0 : "Embedding should have non-zero values";
			
		 logger.info("✅ [RAG TEST] Modelo ONNX cargado correctamente en {}ms", embedTime);
		 logger.info("✅ [RAG TEST] Dimensión de embeddings: {}", dimension);
		 logger.info("✅ [RAG TEST] Muestra (primeros 5): {}, {}, {}, {}, {}",
				 embedding.vector()[0], embedding.vector()[1], embedding.vector()[2],
				 embedding.vector()[3], embedding.vector()[4]);
		 logger.info("✅ [RAG TEST] Magnitud (suma abs): {}", sum);
		 logger.info("✅ [RAG TEST] EmbeddingStore disponible: {}", embeddingStore.getClass().getSimpleName());
			
		 // Test 2: Deterministic embeddings
		 Embedding emb1 = embeddingModel.embed("Factura #123").content();
		 Embedding emb2 = embeddingModel.embed("Factura #123").content();
		 boolean identical = true;
		 for (int i = 0; i < emb1.vector().length; i++) {
			 if (emb1.vector()[i] != emb2.vector()[i]) {
				 identical = false;
				 break;
			 }
		 }
		 logger.info("✅ [RAG TEST] Embeddings determinísticos: {}", identical);
			
		 logger.info("🎉 [RAG TEST] RAG está listo para indexar y buscar documentos!");
		};
	}

	private void ensureAccount(AccountRepository repo, Company company, String code, String name, AccountType type) {
		if (repo.findByCode(company.getId(), code).isEmpty()) {
			logger.info("   ➕ Creando cuenta faltante: {} - {}", code, name);
			Account account = new Account(company.getId(), code, name, type, "Auto-generated by Genesis");
			repo.save(account);
		}
	}
}
