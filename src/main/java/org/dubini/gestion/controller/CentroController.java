package org.dubini.gestion.controller;

import jakarta.validation.Valid;
import org.dubini.gestion.dto.CentroDto;
import org.dubini.gestion.service.CentroService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/centros")
public class CentroController {

    private final CentroService service;

    public CentroController(CentroService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<CentroDto>> getCentros(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        return ResponseEntity.ok(service.getCentros(nombre, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentroDto> getCentroById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCentroById(id));
    }

    @PostMapping
    public ResponseEntity<CentroDto> createCentro(@Valid @RequestBody CentroDto dto) {
        return ResponseEntity.ok(service.createCentro(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CentroDto> updateCentro(@PathVariable Long id, @Valid @RequestBody CentroDto dto) {
        return ResponseEntity.ok(service.updateCentro(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCentro(@PathVariable Long id) {
        service.deleteCentro(id);
        return ResponseEntity.noContent().build();
    }
}
