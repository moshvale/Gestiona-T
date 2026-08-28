package mx.ine.gestiona_t.modules.cv.controller;

import mx.ine.gestiona_t.modules.auth.service.JwtService;
import mx.ine.gestiona_t.modules.cv.dto.response.CvUploadResponse;
import mx.ine.gestiona_t.modules.cv.service.CvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cv")
public class CvUploadController {
    
    private static final Logger log = LoggerFactory.getLogger(CvUploadController.class);
    
    private final CvService cvService;
    private final JwtService jwtService;
    
    public CvUploadController(CvService cvService, JwtService jwtService) {
        this.cvService = cvService;
        this.jwtService = jwtService;
    }
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Mono<CvUploadResponse>> subirCv(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        
        UUID aspiranteId = jwtService.extractAspiranteId(extractToken(authHeader));
        
        log.info("POST /api/v1/cv/upload - Aspirante: {}, Archivo: {}", 
                 aspiranteId, file.getOriginalFilename());
        
        return ResponseEntity.ok(cvService.subirCvNoEstructurado(file, aspiranteId));
    }
    
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Token no proporcionado");
    }
}