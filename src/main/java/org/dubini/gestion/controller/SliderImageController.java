package org.dubini.gestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dubini.gestion.dto.response.ImageResponseDTO;
import org.dubini.gestion.service.SliderImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/slider-images")
@Tag(name = "Carrusel de Imágenes", description = "Endpoints para la gestión de imágenes del slider y descripciones")
public class SliderImageController {

    private final SliderImageService sliderImageService;

    public SliderImageController(SliderImageService sliderImageService) {
        this.sliderImageService = sliderImageService;
    }

    @Operation(summary = "Obtener información completa de todos los slides")
    @GetMapping
    public ResponseEntity<java.util.List<SliderInfoResponse>> getAllSliderInfo() throws IOException {
        return ResponseEntity.ok(sliderImageService.getAllSliderInfo());
    }

    @Operation(summary = "Actualizar una imagen de slide específica")
    @PutMapping(value = "/{slideName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasAccessToWeb()")
    public ResponseEntity<ImageResponseDTO> updateSliderImage(
            @PathVariable String slideName,
            @RequestParam("image") MultipartFile file) throws IOException {
        ImageResponseDTO response = sliderImageService.updateSliderImage(slideName, file);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar el caption (pie de foto) de un slide")
    @PutMapping("/{slideName}/caption")
    @PreAuthorize("@securityService.hasAccessToWeb()")
    public ResponseEntity<SliderCaptionUpdateResponse> updateSliderCaption(
            @PathVariable String slideName,
            @RequestBody SliderCaptionRequest request) throws IOException {
        sliderImageService.updateSliderCaption(slideName, request.getCaption());
        return ResponseEntity.ok(new SliderCaptionUpdateResponse(slideName, "Caption actualizado exitosamente"));
    }

    @Operation(summary = "Obtener información completa de un slide (imagen y caption)")
    @GetMapping("/{slideName}")
    public ResponseEntity<SliderInfoResponse> getSliderInfo(@PathVariable String slideName) throws IOException {
        String imageUrl = sliderImageService.getSliderImageUrl(slideName);
        String caption = sliderImageService.getSliderCaption(slideName);
        return ResponseEntity.ok(new SliderInfoResponse(slideName, imageUrl, caption));
    }

    @Operation(summary = "Obtener solo la URL de la imagen de un slide")
    @GetMapping("/{slideName}/url")
    public ResponseEntity<SliderImageUrlResponse> getSliderImageUrl(@PathVariable String slideName) throws IOException {
        String imageUrl = sliderImageService.getSliderImageUrl(slideName);
        return ResponseEntity.ok(new SliderImageUrlResponse(slideName, imageUrl));
    }

    @Operation(summary = "Obtener solo el caption de un slide")
    @GetMapping("/{slideName}/caption")
    public ResponseEntity<SliderCaptionResponse> getSliderCaption(@PathVariable String slideName) throws IOException {
        String caption = sliderImageService.getSliderCaption(slideName);
        return ResponseEntity.ok(new SliderCaptionResponse(slideName, caption));
    }

    @Operation(summary = "Eliminar una imagen de slide")
    @DeleteMapping("/{slideName}")
    @PreAuthorize("@securityService.hasAccessToWeb()")
    public ResponseEntity<Void> deleteSliderImage(@PathVariable String slideName) throws IOException {
        sliderImageService.deleteSliderImage(slideName);
        return ResponseEntity.noContent().build();
    }

    // ============ DTOs ============

    public static class SliderCaptionRequest {
        private String caption;

        public SliderCaptionRequest() {}

        public SliderCaptionRequest(String caption) {
            this.caption = caption;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }
    }

    public static class SliderCaptionUpdateResponse {
        private String slideName;
        private String message;

        public SliderCaptionUpdateResponse() {}

        public SliderCaptionUpdateResponse(String slideName, String message) {
            this.slideName = slideName;
            this.message = message;
        }

        public String getSlideName() {
            return slideName;
        }

        public void setSlideName(String slideName) {
            this.slideName = slideName;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class SliderInfoResponse {
        private String slideName;
        private String imageUrl;
        private String caption;

        public SliderInfoResponse() {}

        public SliderInfoResponse(String slideName, String imageUrl, String caption) {
            this.slideName = slideName;
            this.imageUrl = imageUrl;
            this.caption = caption;
        }

        public String getSlideName() {
            return slideName;
        }

        public void setSlideName(String slideName) {
            this.slideName = slideName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }
    }

    public static class SliderImageUrlResponse {
        private String slideName;
        private String url;

        public SliderImageUrlResponse() {}

        public SliderImageUrlResponse(String slideName, String url) {
            this.slideName = slideName;
            this.url = url;
        }

        public String getSlideName() {
            return slideName;
        }

        public void setSlideName(String slideName) {
            this.slideName = slideName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class SliderCaptionResponse {
        private String slideName;
        private String caption;

        public SliderCaptionResponse() {}

        public SliderCaptionResponse(String slideName, String caption) {
            this.slideName = slideName;
            this.caption = caption;
        }

        public String getSlideName() {
            return slideName;
        }

        public void setSlideName(String slideName) {
            this.slideName = slideName;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }
    }
}
