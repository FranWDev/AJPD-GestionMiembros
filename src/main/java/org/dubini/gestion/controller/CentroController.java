package org.dubini.gestion.controller;

import org.dubini.gestion.dto.CentroDto;
import org.dubini.gestion.service.CentroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/centros")
@Tag(name = "Centros", description = "Endpoints para la gestión de centros de la asociación")
public class CentroController {

    private final CentroService service;

    public CentroController(CentroService service) {
        this.service = service;
    }

    @Operation(summary = "Listar centros", description = "Obtiene una lista paginada de centros, opcionalmente filtrada por nombre")
    @GetMapping
    public ResponseEntity<Page<CentroDto>> getCentros(
            @Parameter(description = "Nombre del centro para filtrar") @RequestParam(required = false) String nombre,
            Pageable pageable) {
        return ResponseEntity.ok(service.getCentros(nombre, pageable));
    }

    @Operation(summary = "Obtener centro por ID")
    @ApiResponse(responseCode = "200", description = "Centro encontrado")
    @ApiResponse(responseCode = "404", description = "Centro no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<CentroDto> getCentroById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCentroById(id));
    }

    @Operation(summary = "Crear nuevo centro")
    @PostMapping
    public ResponseEntity<CentroDto> createCentro(@Valid @RequestBody CentroDto dto) {
        return ResponseEntity.ok(service.createCentro(dto));
    }

    @Operation(summary = "Actualizar centro existente")
    @PutMapping("/{id}")
    public ResponseEntity<CentroDto> updateCentro(@PathVariable Long id, @Valid @RequestBody CentroDto dto) {
        return ResponseEntity.ok(service.updateCentro(id, dto));
    }

    @Operation(summary = "Eliminar un centro")
    @ApiResponse(responseCode = "204", description = "Centro eliminado exitosamente")
    @ApiResponse(responseCode = "409", description = "No se puede eliminar porque tiene miembros asociados")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCentro(@PathVariable Long id) {
        service.deleteCentro(id);
        return ResponseEntity.noContent().build();
    }
}
