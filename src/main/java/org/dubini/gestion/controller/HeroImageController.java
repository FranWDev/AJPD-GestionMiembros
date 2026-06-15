package org.dubini.gestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dubini.gestion.dto.response.ImageResponseDTO;
import org.dubini.gestion.service.HeroImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/hero-images")
@Tag(name = "Imágenes Hero", description = "Endpoints para la gestión de imágenes de portada (hero)")
public class HeroImageController {

    private final HeroImageService heroImageService;

    public HeroImageController(HeroImageService heroImageService) {
        this.heroImageService = heroImageService;
    }

    @Operation(summary = "Obtener todas las imágenes hero")
    @GetMapping
    public ResponseEntity<java.util.List<HeroImageUrlResponse>> getAllHeroImages() throws IOException {
        return ResponseEntity.ok(heroImageService.getAllHeroImages());
    }

    @Operation(summary = "Actualizar una imagen hero específica")
    @PutMapping(value = "/{heroName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasAccessToWeb()")
    public ResponseEntity<ImageResponseDTO> updateHeroImage(
            @PathVariable String heroName,
            @RequestParam("image") MultipartFile file) throws IOException {
        
        ImageResponseDTO response = heroImageService.updateHeroImage(heroName, file);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener la URL pública de una imagen hero")
    @GetMapping("/{heroName}/url")
    public ResponseEntity<HeroImageUrlResponse> getHeroImageUrl(@PathVariable String heroName) throws IOException {
        String url = heroImageService.getHeroImageUrl(heroName);
        return ResponseEntity.ok(new HeroImageUrlResponse(heroName, url));
    }

    @Operation(summary = "Eliminar una imagen hero específica")
    @DeleteMapping("/{heroName}")
    @PreAuthorize("@securityService.hasAccessToWeb()")
    public ResponseEntity<Void> deleteHeroImage(@PathVariable String heroName) throws IOException {
        heroImageService.deleteHeroImage(heroName);
        return ResponseEntity.noContent().build();
    }

    public static class HeroImageUrlResponse {
        private String heroName;
        private String url;

        public HeroImageUrlResponse() {}

        public HeroImageUrlResponse(String heroName, String url) {
            this.heroName = heroName;
            this.url = url;
        }

        public String getHeroName() {
            return heroName;
        }

        public void setHeroName(String heroName) {
            this.heroName = heroName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
