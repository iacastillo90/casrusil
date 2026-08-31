package com.casrusil.siierpai.shared.application.port.out;

import java.io.InputStream;

public interface DocumentStoragePort {
    /**
     * Sube un documento al almacenamiento (MinIO/S3).
     * @param fileName Nombre o ruta del archivo en el bucket
     * @param stream Stream con los datos
     * @param contentType Tipo MIME (ej. application/pdf)
     * @return URL o Identificador único guardado
     */
    String uploadDocument(String fileName, InputStream stream, String contentType) throws Exception;

    /**
     * Descarga un documento.
     * @param fileName Nombre del archivo en el bucket
     * @return Stream con los datos del archivo
     */
    InputStream downloadDocument(String fileName) throws Exception;

    /**
     * Genera una URL prefirmada (Presigned URL) para descargar o ver el archivo desde el frontend.
     * @param fileName Nombre del archivo
     * @return URL temporal
     */
    String getPresignedUrl(String fileName) throws Exception;
}
