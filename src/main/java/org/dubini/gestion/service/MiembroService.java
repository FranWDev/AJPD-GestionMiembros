package org.dubini.gestion.service;

import org.dubini.gestion.dto.*;
import org.dubini.gestion.exception.ResourceNotFoundException;
import org.dubini.gestion.model.Cargo;
import org.dubini.gestion.model.Centro;
import org.dubini.gestion.model.HistorialCargo;
import org.dubini.gestion.model.Miembro;
import org.dubini.gestion.repository.CargoRepository;
import org.dubini.gestion.repository.CentroRepository;
import org.dubini.gestion.repository.MiembroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MiembroService {

    private static final String MIEMBRO_NO_ENCONTRADO = "Miembro no encontrado";
    private static final String FECHA_BAJA = "fechaBaja";

    private final MiembroRepository miembroRepo;
    private final CentroRepository centroRepo;
    private final CargoRepository cargoRepo;

    public MiembroService(MiembroRepository miembroRepo, CentroRepository centroRepo, CargoRepository cargoRepo) {
        this.miembroRepo = miembroRepo;
        this.centroRepo = centroRepo;
        this.cargoRepo = cargoRepo;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "miembros")
    public Page<MiembroResponseDto> getMiembros(
            MiembroFiltro filtro,
            Pageable pageable
    ) {
        Page<Miembro> pg = miembroRepo.findByFilters(filtro, pageable);
        List<Miembro> members = pg.getContent();

        if (members.isEmpty()) {
            return Page.empty(pageable);
        }

        Set<Long> centroIds = members.stream()
                .map(Miembro::getCentroId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> cargoIds = members.stream()
                .map(Miembro::getCargoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> historyCargoIds = members.stream()
                .flatMap(m -> m.getHistorialCargos() == null ? Stream.empty() : m.getHistorialCargos().stream())
                .map(HistorialCargo::getCargoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Centro> centroMap = new HashMap<>();
        if (!centroIds.isEmpty()) {
            centroRepo.findAllById(centroIds).forEach(c -> centroMap.put(c.getId(), c));
        }

        Map<Long, Cargo> cargoMap = new HashMap<>();
        if (!cargoIds.isEmpty()) {
            cargoRepo.findAllById(cargoIds).forEach(c -> cargoMap.put(c.getId(), c));
        }

        Map<Long, Cargo> historyCargoMap = new HashMap<>();
        if (!historyCargoIds.isEmpty()) {
            cargoRepo.findAllById(historyCargoIds).forEach(c -> historyCargoMap.put(c.getId(), c));
        }

        return pg.map(m -> DtoMapper.toResponseDto(m, centroMap, cargoMap, historyCargoMap));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "miembro", key = "#id")
    public MiembroResponseDto getMiembroById(Long id) {
        return getMiembroResponseDtoById(id);
    }

    private MiembroResponseDto getMiembroResponseDtoById(Long id) {
        Miembro m = miembroRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(MIEMBRO_NO_ENCONTRADO));
        Map<Long, Centro> centroMap = new HashMap<>();
        if (m.getCentroId() != null) {
            centroRepo.findById(m.getCentroId()).ifPresent(c -> centroMap.put(c.getId(), c));
        }
        Map<Long, Cargo> cargoMap = new HashMap<>();
        if (m.getCargoId() != null) {
            cargoRepo.findById(m.getCargoId()).ifPresent(c -> cargoMap.put(c.getId(), c));
        }
        Set<Long> historyCargoIds = m.getHistorialCargos() != null ? m.getHistorialCargos().stream()
                .map(HistorialCargo::getCargoId)
                .collect(Collectors.toSet()) : Collections.emptySet();
        Map<Long, Cargo> historyCargoMap = new HashMap<>();
        if (!historyCargoIds.isEmpty()) {
            cargoRepo.findAllById(historyCargoIds).forEach(c -> historyCargoMap.put(c.getId(), c));
        }
        return DtoMapper.toResponseDto(m, centroMap, cargoMap, historyCargoMap);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "cargoHistorial", allEntries = true)
    })
    public MiembroResponseDto createMiembro(MiembroRequestDto dto) {
        Miembro m = new Miembro();
        DtoMapper.updateEntity(m, dto);
        m.setFechaCargo(dto.getFechaCargo() != null ? dto.getFechaCargo() : LocalDate.now(ZoneId.systemDefault()));
        m.setFechaAlta(dto.getFechaAlta() != null ? dto.getFechaAlta() : LocalDate.now(ZoneId.systemDefault()));

        if (dto.getCargoId() != null) {
            HistorialCargo hc = new HistorialCargo(null, m.getFechaCargo(), null, dto.getCargoId());
            m.getHistorialCargos().add(hc);
        }

        m.alignCurrentCargoWithHistory();

        m = miembroRepo.save(m);
        return getMiembroResponseDtoById(m.getId());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "miembro", key = "#id"),
            @CacheEvict(value = "cargoHistorial", allEntries = true)
    })
    public MiembroResponseDto updateMiembro(Long id, MiembroRequestDto dto) {
        Miembro m = miembroRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(MIEMBRO_NO_ENCONTRADO));
        
        Long oldCargoId = m.getCargoId();
        Long newCargoId = dto.getCargoId();

        DtoMapper.updateEntity(m, dto);

        if (!Objects.equals(oldCargoId, newCargoId)) {
            LocalDate changeDate = dto.getFechaCargo() != null ? dto.getFechaCargo() : LocalDate.now(ZoneId.systemDefault());
            m.setFechaCargo(changeDate);

            if (oldCargoId != null) {
                m.getHistorialCargos().stream()
                        .filter(hc -> Objects.equals(hc.getCargoId(), oldCargoId) && hc.getFechaFin() == null)
                        .forEach(hc -> hc.setFechaFin(changeDate));
            }

            if (newCargoId != null) {
                HistorialCargo newHistory = new HistorialCargo(null, changeDate, null, newCargoId);
                m.getHistorialCargos().add(newHistory);
            }
        }

        m.alignCurrentCargoWithHistory();

        m = miembroRepo.save(m);
        return getMiembroResponseDtoById(m.getId());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "miembro", key = "#miembroId"),
            @CacheEvict(value = "cargoHistorial", allEntries = true)
    })
    public MiembroResponseDto updateHistorialCargo(Long miembroId, Long historialId, HistorialCargoDto dto) {
        Miembro m = miembroRepo.findById(miembroId).orElseThrow(() -> new ResourceNotFoundException(MIEMBRO_NO_ENCONTRADO));
        HistorialCargo hc = m.getHistorialCargos().stream()
                .filter(h -> Objects.equals(h.getId(), historialId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Registro de historial no encontrado"));

        hc.setFechaInicio(dto.getFechaInicio());
        hc.setFechaFin(dto.getFechaFin());
        if (dto.getCargo() != null) {
            hc.setCargoId(dto.getCargo().getId());
        }

        m.alignCurrentCargoWithHistory();

        m = miembroRepo.save(m);
        return getMiembroResponseDtoById(m.getId());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "miembro", key = "#miembroId"),
            @CacheEvict(value = "cargoHistorial", allEntries = true)
    })
    public MiembroResponseDto deleteHistorialCargo(Long miembroId, Long historialId) {
        Miembro m = miembroRepo.findById(miembroId).orElseThrow(() -> new ResourceNotFoundException(MIEMBRO_NO_ENCONTRADO));
        HistorialCargo toDelete = m.getHistorialCargos().stream()
                .filter(h -> Objects.equals(h.getId(), historialId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Registro de historial no encontrado"));

        boolean wasActive = toDelete.getFechaFin() == null;

        m.getHistorialCargos().remove(toDelete);

        if (wasActive && !m.getHistorialCargos().isEmpty()) {
            m.getHistorialCargos().stream()
                    .filter(h -> h.getFechaFin() != null)
                    .max(Comparator.comparing(HistorialCargo::getFechaInicio))
                    .ifPresent(prev -> prev.setFechaFin(null));
        }

        m.alignCurrentCargoWithHistory();

        m = miembroRepo.save(m);
        return getMiembroResponseDtoById(m.getId());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "miembro", key = "#id"),
            @CacheEvict(value = "cargoHistorial", allEntries = true)
    })
    public void deleteMiembro(Long id) {
        Miembro m = miembroRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(MIEMBRO_NO_ENCONTRADO));
        miembroRepo.delete(m);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "miembro", key = "#id"),
            @CacheEvict(value = "cargoHistorial", allEntries = true)
    })
    public MiembroResponseDto darDeBaja(Long id, Map<String, String> body) {
        LocalDate fechaBaja = null;
        if (body != null && body.containsKey(FECHA_BAJA) && body.get(FECHA_BAJA) != null && !body.get(FECHA_BAJA).isEmpty()) {
            fechaBaja = LocalDate.parse(body.get(FECHA_BAJA));
        }
        LocalDate finalFechaBaja = fechaBaja != null ? fechaBaja : LocalDate.now(ZoneId.systemDefault());
        Miembro m = miembroRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(MIEMBRO_NO_ENCONTRADO));
        m.setFechaBaja(finalFechaBaja);

        if (m.getHistorialCargos() != null) {
            m.getHistorialCargos().stream()
                    .filter(hc -> hc.getFechaFin() == null)
                    .forEach(hc -> hc.setFechaFin(finalFechaBaja));
        }
        m.alignCurrentCargoWithHistory();

        m = miembroRepo.save(m);
        return getMiembroResponseDtoById(m.getId());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "miembro", key = "#id"),
            @CacheEvict(value = "cargoHistorial", allEntries = true)
    })
    public MiembroResponseDto reactivarMiembro(Long id) {
        Miembro m = miembroRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException(MIEMBRO_NO_ENCONTRADO));
        LocalDate oldFechaBaja = m.getFechaBaja();
        m.setFechaBaja(null);
        if (oldFechaBaja != null && m.getHistorialCargos() != null) {
            m.getHistorialCargos().stream()
                    .filter(hc -> oldFechaBaja.equals(hc.getFechaFin()))
                    .forEach(hc -> hc.setFechaFin(null));
        }
        m.alignCurrentCargoWithHistory();
        m = miembroRepo.save(m);
        return getMiembroResponseDtoById(m.getId());
    }
}
