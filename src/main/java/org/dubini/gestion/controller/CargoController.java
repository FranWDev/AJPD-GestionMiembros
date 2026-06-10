package org.dubini.gestion.controller;

import jakarta.validation.Valid;
import org.dubini.gestion.dto.CargoDto;
import org.dubini.gestion.service.CargoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cargos")
public class CargoController {

    private final CargoService service;

    public CargoController(CargoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<CargoDto>> getCargos(
            @RequestParam(required = false) String nombre,
            Pageable pageable) {
        return ResponseEntity.ok(service.getCargos(nombre, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargoDto> getCargoById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCargoById(id));
    }

    @PostMapping
    public ResponseEntity<CargoDto> createCargo(@Valid @RequestBody CargoDto dto) {
        return ResponseEntity.ok(service.createCargo(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CargoDto> updateCargo(@PathVariable Long id, @Valid @RequestBody CargoDto dto) {
        return ResponseEntity.ok(service.updateCargo(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCargo(@PathVariable Long id) {
        service.deleteCargo(id);
        return ResponseEntity.noContent().build();
    }
}
