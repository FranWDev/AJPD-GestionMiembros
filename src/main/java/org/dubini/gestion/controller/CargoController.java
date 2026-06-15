package org.dubini.gestion.controller;

import java.time.LocalDate;

import org.dubini.gestion.dto.CargoDto;
import org.dubini.gestion.dto.CargoHistorialDto;
import org.dubini.gestion.dto.CargoHistorialEditDto;
import org.dubini.gestion.service.CargoService;
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
@RequestMapping("/api/cargos")
@Tag(name = "Cargos", description = "Endpoints para la gestión de cargos y su historial")
public class CargoController {

    private final CargoService service;

    public CargoController(CargoService service) {
        this.service = service;
    }

    @Operation(summary = "Listar cargos", description = "Obtiene una lista paginada de todos los cargos, opcionalmente filtrada por nombre")
    @GetMapping
    public ResponseEntity<Page<CargoDto>> getCargos(
            @Parameter(description = "Nombre del cargo a filtrar") @RequestParam(required = false) String nombre,
            Pageable pageable) {
        return ResponseEntity.ok(service.getCargos(nombre, pageable));
    }

    @Operation(summary = "Obtener cargo por ID")
    @ApiResponse(responseCode = "200", description = "Cargo encontrado")
    @ApiResponse(responseCode = "404", description = "Cargo no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<CargoDto> getCargoById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCargoById(id));
    }

    @Operation(summary = "Crear nuevo cargo")
    @PostMapping
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<CargoDto> createCargo(@Valid @RequestBody CargoDto dto) {
        return ResponseEntity.ok(service.createCargo(dto));
    }

    @Operation(summary = "Actualizar cargo existente")
    @PutMapping("/{id}")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<CargoDto> updateCargo(@PathVariable Long id, @Valid @RequestBody CargoDto dto) {
        return ResponseEntity.ok(service.updateCargo(id, dto));
    }

    @Operation(summary = "Eliminar un cargo")
    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<Void> deleteCargo(@PathVariable Long id) {
        service.deleteCargo(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener historial de cargos", description = "Obtiene el historial de cambios de cargos con diversos filtros de fecha")
    @GetMapping("/historial")
    public ResponseEntity<Page<CargoHistorialDto>> getCargoHistorial(
            @RequestParam(required = false) Long cargoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinHasta,
            @RequestParam(required = false) String buscar,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getCargoHistorial(
                cargoId,
                fechaInicioDesde,
                fechaInicioHasta,
                fechaFinDesde,
                fechaFinHasta,
                buscar,
                pageable
        ));
    }

    @Operation(summary = "Actualizar registro de historial de cargo")
    @PutMapping("/historial/{id}")
    @PreAuthorize("@securityService.hasAccessToOrganization()")
    public ResponseEntity<CargoHistorialDto> updateCargoHistorial(
            @PathVariable Long id,
            @Valid @RequestBody CargoHistorialEditDto dto
    ) {
        return ResponseEntity.ok(service.updateCargoHistorial(id, dto));
    }
}
