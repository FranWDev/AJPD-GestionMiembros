package org.dubini.gestion.repository;

import org.dubini.gestion.model.Miembro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

import org.dubini.gestion.dto.MiembroFiltro;

public interface MiembroRepositoryCustom {
    Page<Miembro> findByFilters(
            MiembroFiltro filtro,
            Pageable pageable
    );
}
