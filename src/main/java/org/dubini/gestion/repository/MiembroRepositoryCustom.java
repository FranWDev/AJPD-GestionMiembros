package org.dubini.gestion.repository;

import org.dubini.gestion.model.Miembro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

public interface MiembroRepositoryCustom {
    Page<Miembro> findByFilters(
            String filtroBaja,
            Long centroId,
            Long cargoId,
            LocalDate fechaAltaDesde,
            LocalDate fechaAltaHasta,
            LocalDate fechaBajaDesde,
            LocalDate fechaBajaHasta,
            String nacionalidad,
            String buscar,
            Pageable pageable
    );
}
