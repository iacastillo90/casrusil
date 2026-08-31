package com.casrusil.siierpai.shared.infrastructure.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${minio.url:http://localhost:9000}")
    private String url;

    @Value("${minio.access-key:admin}")
    private String accessKey;

    @Value("${minio.secret-key:password123}")
    private String secretKey;

    @Value("${minio.bucket-name:sii-erp-documents}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(url)
                    .credentials(accessKey, secretKey)
                    .build();
            
            // Crear el bucket si no existe (muy útil para inicialización local)
            boolean isExist = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("🚀 [MinIO] Bucket '{}' creado exitosamente.", bucketName);
            } else {
                log.info("✅ [MinIO] Conectado exitosamente al bucket '{}'", bucketName);
            }
            return client;
        } catch (Exception e) {
            log.warn("⚠️ [MinIO] Error inicializando cliente MinIO (puede que no esté corriendo): {}", e.getMessage());
            // No bloqueamos la app, retornamos un builder básico si falla
            return MinioClient.builder().endpoint("http://localhost:9000").credentials("none", "none").build();
        }
    }
}
