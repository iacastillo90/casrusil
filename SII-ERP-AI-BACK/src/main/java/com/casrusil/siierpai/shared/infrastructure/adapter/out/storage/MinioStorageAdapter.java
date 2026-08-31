package com.casrusil.siierpai.shared.infrastructure.adapter.out.storage;

import com.casrusil.siierpai.shared.application.port.out.DocumentStoragePort;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
public class MinioStorageAdapter implements DocumentStoragePort {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name:sii-erp-documents}")
    private String bucketName;

    public MinioStorageAdapter(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public String uploadDocument(String fileName, InputStream stream, String contentType) throws Exception {
        // En Java 8+, stream.available() a veces no es exacto, pero MinIO necesita el size.
        // Si no sabemos el tamaño, pasamos -1 y dejamos que defina un partSize.
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(stream, -1, 10485760) // 10MB part size
                        .contentType(contentType)
                        .build()
        );
        return fileName; // En una app real, podrías retornar la URL pública
    }

    @Override
    public InputStream downloadDocument(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

    @Override
    public String getPresignedUrl(String fileName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(fileName)
                        .expiry(1, TimeUnit.HOURS)
                        .build()
        );
    }
}
