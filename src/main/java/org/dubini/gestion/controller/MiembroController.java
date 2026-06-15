package org.dubini.gestion.controller;

import java.time.LocalDate;
import java.util.Map;

import org.dubini.gestion.dto.MiembroFiltro;
import org.dubini.gestion.dto.HistorialCargoDto;
import org.dubini.gestion.dto.MiembroRequestDto;
import org.dubini.gestion.dto.MiembroResponseDto;
import org.dubini.gestion.service.MiembroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/miembros")
@Tag(name = "Miembros", description = "Endpoints para la gestión de miembros y sus datos asociados")
public class MiembroController {

    private final MiembroService service;

    public MiembroController(MiembroService service) {
        this.service = service;
    }

    @Operation(summary = "Listar miembros con filtros", description = "Obtiene una lista paginada de miembros permitiendo múltiples criterios de filtrado")
    @GetMapping
    public ResponseEntity<Page<MiembroResponseDto>> getMiembros(
            @Parameter(description = "Filtro de estado (ACTIVO, BAJA, TODOS)") @RequestParam(required = false) String filtroBaja,
            @Parameter(description = "Filtrar por ID de centro") @RequestParam(required = false) Long centroId,
            @Parameter(description = "Filtrar por ID de cargo") @RequestParam(required = false) Long cargoId,
            @Parameter(description = "Fecha alta desde (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAltaDesde,
            @Parameter(description = "Fecha alta hasta (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAltaHasta,
            @Parameter(description = "Fecha baja desde (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaBajaDesde,
            @Parameter(description = "Fecha baja hasta (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaBajaHasta,
            @Parameter(description = "Filtrar por nacionalidad") @RequestParam(required = false) String nacionalidad,
            @Parameter(description = "Búsqueda textual (nombre, correo, NIF...)") @RequestParam(required = false) String buscar,
            Pageable pageable
    ) {
        MiembroFiltro filtro = new MiembroFiltro(
                filtroBaja, centroId, cargoId,
                fechaAltaDesde, fechaAltaHasta,
                fechaBajaDesde, fechaBajaHasta,
                nacionalidad, buscar
        );
        return ResponseEntity.ok(service.getMiembros(filtro, pageable));
    }

    @Operation(summary = "Obtener miembro por ID")
    @ApiResponse(responseCode = "200", description = "Miembro encontrado")
    @ApiResponse(responseCode = "404", description = "Miembro no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<MiembroResponseDto> getMiembroById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMiembroById(id));
    }

    @Operation(summary = "Crear nuevo miembro")
    @PostMapping
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<MiembroResponseDto> createMiembro(@Valid @RequestBody MiembroRequestDto dto) {
        return ResponseEntity.ok(service.createMiembro(dto));
    }

    @Operation(summary = "Actualizar miembro existente")
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<MiembroResponseDto> updateMiembro(@PathVariable Long id, @Valid @RequestBody MiembroRequestDto dto) {
        return ResponseEntity.ok(service.updateMiembro(id, dto));
    }

    @Operation(summary = "Actualizar entrada de historial de cargos del miembro")
    @PutMapping("/{miembroId}/historial/{historialId}")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<MiembroResponseDto> updateHistorialCargo(
            @PathVariable Long miembroId,
            @PathVariable Long historialId,
            @RequestBody HistorialCargoDto dto) {
        return ResponseEntity.ok(service.updateHistorialCargo(miembroId, historialId, dto));
    }

    @Operation(summary = "Eliminar entrada de historial de cargos del miembro")
    @DeleteMapping("/{miembroId}/historial/{historialId}")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<MiembroResponseDto> deleteHistorialCargo(
            @PathVariable Long miembroId,
            @PathVariable Long historialId) {
        return ResponseEntity.ok(service.deleteHistorialCargo(miembroId, historialId));
    }

    @Operation(summary = "Dar de baja a un miembro")
    @PutMapping("/{id}/baja")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<MiembroResponseDto> darDeBaja(
            @PathVariable Long id,
            @Parameter(description = "Cuerpo con la fecha de baja (opcional)") @RequestBody(required = false) Map<String, String> body) {
        return ResponseEntity.ok(service.darDeBaja(id, body));
    }

    @Operation(summary = "Reactivar un miembro dado de baja")
    @DeleteMapping("/{id}/baja")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<MiembroResponseDto> reactivarMiembro(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivarMiembro(id));
    }

    @Operation(summary = "Eliminar un miembro")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<Void> deleteMiembro(@PathVariable Long id) {
        service.deleteMiembro(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Sincronizar y generar directorios de Google Drive para todos los miembros")
    @PostMapping("/documentos/sync-folders")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<Void> syncAllFolders() {
        service.syncAllMembersFolders();
        return ResponseEntity.ok().build();
    }
}
