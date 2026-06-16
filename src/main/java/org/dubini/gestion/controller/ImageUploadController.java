package org.dubini.gestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.dubini.gestion.dto.response.EditorJSImageResponseDTO;
import org.dubini.gestion.dto.response.ImageResponseDTO;
import org.dubini.gestion.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Validated
@RestController
@RequestMapping("/api/images")
@Tag(name = "Imágenes", description = "Endpoints para la subida y gestión de archivos de imagen")
@PreAuthorize("@securityService.hasAccessToWeb()")
public class ImageUploadController {

    private static final int MAX_WIDTH = 4096;
    private static final int MAX_HEIGHT = 4096;
    private static final int MIN_DIMENSION = 50;

    private final ImageService service;

    public ImageUploadController(ImageService service) {
        this.service = service;
    }

    @Operation(summary = "Subir una imagen")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EditorJSImageResponseDTO> uploadImage(
            @RequestParam("image") MultipartFile file,
            @RequestParam(defaultValue = "800") @Min(value = MIN_DIMENSION, message = "El ancho mínimo es 50px") @Max(value = MAX_WIDTH, message = "El ancho máximo es 4096px") int width,
            @RequestParam(defaultValue = "600") @Min(value = MIN_DIMENSION, message = "La altura mínima es 50px") @Max(value = MAX_HEIGHT, message = "La altura máxima es 4096px") int height,
            @RequestParam(defaultValue = "0.8") @DecimalMin(value = "0.1", message = "La calidad mínima es 0.1") @DecimalMax(value = "1.0", message = "La calidad máxima es 1.0") float quality)
            throws IOException {

        validateFile(file);

        ImageResponseDTO response = service.saveImage(file);

        return ResponseEntity.ok(
                EditorJSImageResponseDTO.success(
                        response.getUrl(),
                        response.getFileName(),
                        response.getSize()));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("El archivo no debe superar 10MB");
        }
    }
}
