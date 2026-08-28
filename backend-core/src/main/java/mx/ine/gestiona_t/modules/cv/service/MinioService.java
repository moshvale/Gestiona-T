package mx.ine.gestiona_t.modules.cv.service;

import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

@Service
public class MinioService {
    
    private static final Logger log = LoggerFactory.getLogger(MinioService.class);
    
    private final MinioClient minioClient;
    
    @Value("${minio.bucket.cv}")
    private String bucketName;
    
    public MinioService(
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
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Bucket {} creado exitosamente", bucketName);
            }
        } catch (Exception e) {
            log.error("Error al inicializar bucket: {}", e.getMessage());
        }
    }
    
    public String uploadFile(MultipartFile file, String folder, String fileName) {
        try {
            String objectName = folder + "/" + fileName;
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            
            log.info("Archivo subido exitosamente: {}", objectName);
            return objectName;
            
        } catch (Exception e) {
            log.error("Error al subir archivo: {}", e.getMessage());
            throw new RuntimeException("Error al subir archivo a MinIO", e);
        }
    }
    
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error al descargar archivo: {}", e.getMessage());
            throw new RuntimeException("Error al descargar archivo de MinIO", e);
        }
    }
    
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            log.info("Archivo eliminado: {}", objectName);
        } catch (Exception e) {
            log.error("Error al eliminar archivo: {}", e.getMessage());
        }
    }
}