package mx.ine.gestiona_t.modules.cv.dto.response;

public record CvUploadResponse(
    String fileId,
    String fileName,
    long fileSize,
    String status,
    String message
) {}