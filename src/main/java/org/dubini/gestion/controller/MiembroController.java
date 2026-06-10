package org.dubini.gestion.controller;

import jakarta.validation.Valid;
import org.dubini.gestion.dto.HistorialCargoDto;
import org.dubini.gestion.dto.MiembroRequestDto;
import org.dubini.gestion.dto.MiembroResponseDto;
import org.dubini.gestion.service.MiembroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/miembros")
public class MiembroController {

    private final MiembroService service;

    public MiembroController(MiembroService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<MiembroResponseDto>> getMiembros(
            @RequestParam(required = false) String filtroBaja,
            @RequestParam(required = false) Long centroId,
            @RequestParam(required = false) Long cargoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAltaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaAltaHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaBajaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaBajaHasta,
            @RequestParam(required = false) String nacionalidad,
            @RequestParam(required = false) String buscar,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getMiembros(
                filtroBaja, centroId, cargoId,
                fechaAltaDesde, fechaAltaHasta,
                fechaBajaDesde, fechaBajaHasta,
                nacionalidad, buscar,
                pageable
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MiembroResponseDto> getMiembroById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMiembroById(id));
    }

    @PostMapping
    public ResponseEntity<MiembroResponseDto> createMiembro(@Valid @RequestBody MiembroRequestDto dto) {
        return ResponseEntity.ok(service.createMiembro(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MiembroResponseDto> updateMiembro(@PathVariable Long id, @Valid @RequestBody MiembroRequestDto dto) {
        return ResponseEntity.ok(service.updateMiembro(id, dto));
    }

    @PutMapping("/{miembroId}/historial/{historialId}")
    public ResponseEntity<MiembroResponseDto> updateHistorialCargo(
            @PathVariable Long miembroId,
            @PathVariable Long historialId,
            @RequestBody HistorialCargoDto dto) {
        return ResponseEntity.ok(service.updateHistorialCargo(miembroId, historialId, dto));
    }

    @DeleteMapping("/{miembroId}/historial/{historialId}")
    public ResponseEntity<MiembroResponseDto> deleteHistorialCargo(
            @PathVariable Long miembroId,
            @PathVariable Long historialId) {
        return ResponseEntity.ok(service.deleteHistorialCargo(miembroId, historialId));
    }

    @PutMapping("/{id}/baja")
    public ResponseEntity<MiembroResponseDto> darDeBaja(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        return ResponseEntity.ok(service.darDeBaja(id, body));
    }

    @DeleteMapping("/{id}/baja")
    public ResponseEntity<MiembroResponseDto> reactivarMiembro(@PathVariable Long id) {
        return ResponseEntity.ok(service.reactivarMiembro(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMiembro(@PathVariable Long id) {
        service.deleteMiembro(id);
        return ResponseEntity.noContent().build();
    }
}
