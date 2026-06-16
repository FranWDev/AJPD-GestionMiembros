package org.dubini.gestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dubini.gestion.service.GoogleDriveService;
import org.dubini.gestion.service.GoogleDriveService.DriveFileDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/miembros/{id}/documentos")
@Tag(name = "Documentos de Miembros", description = "Endpoints para la gestión de archivos DNI y FOTO de miembros en Google Drive")
public class MiembroDocumentoController {

    private final GoogleDriveService googleDriveService;

    public MiembroDocumentoController(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    @Operation(summary = "Listar documentos de un miembro por tipo", description = "Obtiene los archivos del miembro en la carpeta DNI o FOTO de Google Drive")
    @GetMapping("/{tipo}")
    public ResponseEntity<List<DriveFileDto>> listDocumentos(
            @PathVariable Long id,
            @PathVariable String tipo) {
        validateTipo(tipo);
        return ResponseEntity.ok(googleDriveService.listFiles(id, tipo.toUpperCase()));
    }

    @Operation(summary = "Obtener token de acceso temporal para Google Drive", description = "Obtiene el token de acceso OAuth del Service Account para descargas directas en el frontend")
    @GetMapping("/token/google")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<Map<String, String>> getAccessToken() {
        String token = googleDriveService.getAccessToken();
        if (token == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(Map.of("accessToken", token));
    }

    @Operation(summary = "Generar URL de subida para Google Drive", description = "Inicia una sesión de subida resumible en Google Drive y devuelve la URL")
    @PostMapping("/{tipo}/upload-url")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<Map<String, String>> generateUploadUrl(
            @PathVariable Long id,
            @PathVariable String tipo,
            @RequestParam String fileName,
            @RequestParam String contentType) {
        
        validateTipo(tipo);
        validateFileName(tipo, fileName);

        String uploadUrl = googleDriveService.generateUploadUrl(id, tipo.toUpperCase(), fileName, contentType);
        return ResponseEntity.ok(Map.of("uploadUrl", uploadUrl));
    }

    @Operation(summary = "Eliminar un documento en Google Drive")
    @DeleteMapping("/archivo/{fileId}")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<Void> deleteDocumento(
            @PathVariable Long id,
            @PathVariable String fileId) {
        googleDriveService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    private void validateTipo(String tipo) {
        if (!"DNI".equalsIgnoreCase(tipo) && !"FOTO".equalsIgnoreCase(tipo)) {
            throw new IllegalArgumentException("El tipo de documento debe ser DNI o FOTO");
        }
    }

    private void validateFileName(String tipo, String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        }

        String baseName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
        if ("DNI".equalsIgnoreCase(tipo)) {
            if (!"DNI-1".equalsIgnoreCase(baseName) && !"DNI-2".equalsIgnoreCase(baseName)) {
                throw new IllegalArgumentException("Los archivos en la sección DNI deben llamarse DNI-1 o DNI-2");
            }
        } else if ("FOTO".equalsIgnoreCase(tipo)) {
            if (!"FOTO".equalsIgnoreCase(baseName)) {
                throw new IllegalArgumentException("El archivo en la sección FOTO debe llamarse FOTO");
            }
        }
    }
}
