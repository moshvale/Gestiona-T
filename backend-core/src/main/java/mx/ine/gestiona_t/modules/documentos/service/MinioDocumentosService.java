package mx.ine.gestiona_t.modules.documentos.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class MinioDocumentosService {
    
    private static final Logger log = LoggerFactory.getLogger(MinioDocumentosService.class);
    private final MinioClient minioClient;
    
    @Value("${minio.bucket.documentos}")
    private String bucketName;
    
    public MinioDocumentosService(
        @Value("${minio.endpoint}") String endpoint,
        @Value("${minio.access.key}") String accessKey,
        @Value("${minio.secret.key}") String secretKey
    ) {
        this.minioClient = MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build();
    }

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket {} creado", bucketName);
            }
        } catch (Exception e) {
            log.error("Error al inicializar bucket: {}", e.getMessage());
        }
    }
    
    public String uploadDocumento(MultipartFile file, UUID aspiranteId) {
        try {
            // Leer bytes para calcular un hash determinístico
            byte[] bytes = file.getBytes();
            String hash = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(java.security.MessageDigest.getInstance("SHA-256").digest(bytes))
                .substring(0, 12);
            String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = aspiranteId + "_" + uniqueSuffix + "_" + hash + "_" + sanitizeFileName(file.getOriginalFilename());
            String objectName = "documentos/" + fecha + "/" + fileName;

            // Usar un stream nuevo a partir de los bytes ya leídos
            try (InputStream is = new java.io.ByteArrayInputStream(bytes)) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(is, bytes.length, -1)
                        .contentType(file.getContentType())
                        .build()
                );
            }

            log.info("Documento subido: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Error al subir documento: {}", e.getMessage());
            throw new RuntimeException("Error al subir documento", e);
        }
    }
    
    public byte[] downloadDocumento(String objectName) {
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(objectName).build())) {
            return is.readAllBytes();
        } catch (Exception e) {
            log.error("Error al descargar documento: {}", e.getMessage());
            throw new RuntimeException("Error al descargar documento", e);
        }
    }
    
    public void deleteDocumento(String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build()
            );
        } catch (Exception e) {
            log.error("Error al eliminar documento: {}", e.getMessage());
        }
    }
    
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "documento";
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}