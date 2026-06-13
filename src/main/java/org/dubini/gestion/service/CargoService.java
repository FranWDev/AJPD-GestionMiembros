package org.dubini.gestion.service;

import org.dubini.gestion.dto.CargoDto;
import org.dubini.gestion.dto.CargoHistorialDto;
import org.dubini.gestion.dto.CargoHistorialEditDto;
import org.dubini.gestion.dto.DtoMapper;
import org.dubini.gestion.exception.BusinessRuleException;
import org.dubini.gestion.exception.ResourceNotFoundException;
import org.dubini.gestion.model.Cargo;
import org.dubini.gestion.model.HistorialCargo;
import org.dubini.gestion.model.Miembro;
import org.dubini.gestion.repository.CargoRepository;
import org.dubini.gestion.repository.HistorialCargoRepository;
import org.dubini.gestion.repository.MiembroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CargoService {

    private static final String CARGO_NO_ENCONTRADO = "Cargo no encontrado";

    private final CargoRepository repo;
    private final HistorialCargoRepository historialRepo;
    private final MiembroRepository miembroRepo;

    public CargoService(CargoRepository repo, HistorialCargoRepository historialRepo, MiembroRepository miembroRepo) {
        this.repo = repo;
        this.historialRepo = historialRepo;
        this.miembroRepo = miembroRepo;
    }

    @Cacheable(value = "cargos")
    public Page<CargoDto> getCargos(String nombre, Pageable pageable) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            return repo.findByNombreContainingIgnoreCase(nombre.trim(), pageable).map(DtoMapper::toDto);
        }
        return repo.findAll(pageable).map(DtoMapper::toDto);
    }

    @Cacheable(value = "cargo", key = "#id")
    public CargoDto getCargoById(Long id) {
        Cargo c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException(CARGO_NO_ENCONTRADO));
        return DtoMapper.toDto(c);
    }

    @Transactional
    @CacheEvict(value = "cargos", allEntries = true)
    public CargoDto createCargo(CargoDto dto) {
        Cargo c = new Cargo(null, dto.getNombre());
        c = repo.save(c);
        return DtoMapper.toDto(c);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cargos", allEntries = true),
            @CacheEvict(value = "cargo", key = "#id"),
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "miembro", allEntries = true),
            @CacheEvict(value = "cargoHistorial", allEntries = true)
    })
    public CargoDto updateCargo(Long id, CargoDto dto) {
        Cargo c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException(CARGO_NO_ENCONTRADO));
        c.setNombre(dto.getNombre());
        c = repo.save(c);
        return DtoMapper.toDto(c);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cargos", allEntries = true),
            @CacheEvict(value = "cargo", key = "#id"),
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "miembro", allEntries = true),
            @CacheEvict(value = "cargoHistorial", allEntries = true)
    })
    public void deleteCargo(Long id) {
        Cargo c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException(CARGO_NO_ENCONTRADO));
        if (repo.countMiembrosByCargoId(id) > 0) {
            throw new BusinessRuleException("No se puede eliminar el cargo porque está asignado a uno o más miembros");
        }
        if (repo.countHistorialByCargoId(id) > 0) {
            throw new BusinessRuleException("No se puede eliminar el cargo porque está registrado en el historial de cargos");
        }
        repo.delete(c);
    }

    @Cacheable(value = "cargoHistorial")
    public Page<CargoHistorialDto> getCargoHistorial(
            Long cargoId,
            LocalDate fechaInicioDesde,
            LocalDate fechaInicioHasta,
            LocalDate fechaFinDesde,
            LocalDate fechaFinHasta,
            String buscar,
            Pageable pageable
    ) {
        return historialRepo.findCargoHistorial(
                cargoId,
                fechaInicioDesde,
                fechaInicioHasta,
                fechaFinDesde,
                fechaFinHasta,
                buscar,
                pageable
        );
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cargoHistorial", allEntries = true),
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "miembro", allEntries = true)
    })
    public CargoHistorialDto updateCargoHistorial(Long id, CargoHistorialEditDto dto) {
        Long miembroId = miembroRepo.findMiembroIdByHistorialId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de historial no encontrado"));

        Miembro m = miembroRepo.findById(miembroId)
                .orElseThrow(() -> new ResourceNotFoundException("Miembro no encontrado"));

        HistorialCargo hc = m.getHistorialCargos().stream()
                .filter(h -> Objects.equals(h.getId(), id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Registro de historial no encontrado"));

        hc.setFechaInicio(dto.getFechaInicio());
        hc.setFechaFin(dto.getFechaFin());
        hc.setCargoId(dto.getCargoId());

        m.alignCurrentCargoWithHistory();

        m = miembroRepo.save(m);

        Cargo cargo = repo.findById(dto.getCargoId())
                .orElseThrow(() -> new ResourceNotFoundException(CARGO_NO_ENCONTRADO));

        CargoHistorialDto result = new CargoHistorialDto();
        result.setId(hc.getId());
        result.setFechaInicio(hc.getFechaInicio());
        result.setFechaFin(hc.getFechaFin());
        result.setCargoId(cargo.getId());
        result.setCargoNombre(cargo.getNombre());
        result.setMiembroId(m.getId());
        result.setMiembroNombre(m.getNombreRazonSocial());
        result.setMiembroNif(m.getNifCif());
        return result;
    }
}
