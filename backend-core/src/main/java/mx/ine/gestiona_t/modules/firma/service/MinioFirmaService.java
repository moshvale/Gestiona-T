package mx.ine.gestiona_t.modules.firma.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class MinioFirmaService {
    
    private static final Logger log = LoggerFactory.getLogger(MinioFirmaService.class);
    private final MinioClient minioClient;
    
    @Value("${minio.bucket.firmas:documentos-firmados}")
    private String bucketName;
    
    public MinioFirmaService(
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
                log.info("Bucket {} creado con WORM habilitado", bucketName);
            }
        } catch (Exception e) {
            log.error("Error al inicializar bucket: {}", e.getMessage());
        }
    }
    
    public String guardarDocumentoOriginal(byte[] contenido, String folioDocumento, String fileName) {
        return guardarDocumento(contenido, "originales", folioDocumento, fileName);
    }
    
    public String guardarDocumentoFirmado(byte[] contenido, String folioDocumento, String fileName) {
        return guardarDocumento(contenido, "firmados", folioDocumento, fileName);
    }
    
    private String guardarDocumento(byte[] contenido, String carpeta, String folio, String fileName) {
        try {
            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectName = carpeta + "/" + fecha + "/" + folio + "_" + fileName;
            
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(contenido), contenido.length, -1)
                    .contentType("application/pdf")
                    .build()
            );
            
            log.info("Documento guardado: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Error al guardar documento: {}", e.getMessage());
            throw new RuntimeException("Error al guardar documento en MinIO", e);
        }
    }
    
    public byte[] descargarDocumento(String objectName) {
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(objectName).build())) {
            return is.readAllBytes();
        } catch (Exception e) {
            log.error("Error al descargar documento: {}", e.getMessage());
            throw new RuntimeException("Error al descargar documento", e);
        }
    }
}