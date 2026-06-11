package org.dubini.gestion.service;

import org.dubini.gestion.dto.CentroDto;
import org.dubini.gestion.dto.DtoMapper;
import org.dubini.gestion.exception.BusinessRuleException;
import org.dubini.gestion.exception.ResourceNotFoundException;
import org.dubini.gestion.model.Centro;
import org.dubini.gestion.repository.CentroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CentroService {

    private final CentroRepository repo;

    public CentroService(CentroRepository repo) {
        this.repo = repo;
    }

    @Cacheable(value = "centros")
    public Page<CentroDto> getCentros(String nombre, Pageable pageable) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            return repo.findByNombreContainingIgnoreCase(nombre.trim(), pageable).map(DtoMapper::toDto);
        }
        return repo.findAll(pageable).map(DtoMapper::toDto);
    }

    @Cacheable(value = "centro", key = "#id")
    public CentroDto getCentroById(Long id) {
        Centro c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Centro no encontrado"));
        return DtoMapper.toDto(c);
    }

    @Transactional
    @CacheEvict(value = "centros", allEntries = true)
    public CentroDto createCentro(CentroDto dto) {
        Centro c = new Centro(null, dto.getNombre());
        c = repo.save(c);
        return DtoMapper.toDto(c);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "centros", allEntries = true),
            @CacheEvict(value = "centro", key = "#id"),
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "miembro", allEntries = true)
    })
    public CentroDto updateCentro(Long id, CentroDto dto) {
        Centro c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Centro no encontrado"));
        c.setNombre(dto.getNombre());
        c = repo.save(c);
        return DtoMapper.toDto(c);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "centros", allEntries = true),
            @CacheEvict(value = "centro", key = "#id"),
            @CacheEvict(value = "miembros", allEntries = true),
            @CacheEvict(value = "miembro", allEntries = true)
    })
    public void deleteCentro(Long id) {
        Centro c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Centro no encontrado"));
        if (repo.countMiembrosByCentroId(id) > 0) {
            throw new BusinessRuleException("No se puede eliminar el centro porque está asignado a uno o más miembros");
        }
        repo.delete(c);
    }
}
