package org.dubini.gestion.service;

import org.dubini.gestion.dto.CargoDto;
import org.dubini.gestion.dto.DtoMapper;
import org.dubini.gestion.exception.BusinessRuleException;
import org.dubini.gestion.exception.ResourceNotFoundException;
import org.dubini.gestion.model.Cargo;
import org.dubini.gestion.repository.CargoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CargoService {

    private final CargoRepository repo;

    public CargoService(CargoRepository repo) {
        this.repo = repo;
    }

    public Page<CargoDto> getCargos(String nombre, Pageable pageable) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            return repo.findByNombreContainingIgnoreCase(nombre.trim(), pageable).map(DtoMapper::toDto);
        }
        return repo.findAll(pageable).map(DtoMapper::toDto);
    }

    public CargoDto getCargoById(Long id) {
        Cargo c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado"));
        return DtoMapper.toDto(c);
    }

    @Transactional
    public CargoDto createCargo(CargoDto dto) {
        Cargo c = new Cargo(null, dto.getNombre());
        c = repo.save(c);
        return DtoMapper.toDto(c);
    }

    @Transactional
    public CargoDto updateCargo(Long id, CargoDto dto) {
        Cargo c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado"));
        c.setNombre(dto.getNombre());
        c = repo.save(c);
        return DtoMapper.toDto(c);
    }

    @Transactional
    public void deleteCargo(Long id) {
        Cargo c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cargo no encontrado"));
        if (repo.countMiembrosByCargoId(id) > 0) {
            throw new BusinessRuleException("No se puede eliminar el cargo porque está asignado a uno o más miembros");
        }
        if (repo.countHistorialByCargoId(id) > 0) {
            throw new BusinessRuleException("No se puede eliminar el cargo porque está registrado en el historial de cargos");
        }
        repo.delete(c);
    }
}
