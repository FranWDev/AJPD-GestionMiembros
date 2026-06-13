package org.dubini.gestion.dto;

import java.time.LocalDate;

public record MiembroFiltro(
    String filtroBaja,
    Long centroId,
    Long cargoId,
    LocalDate fechaAltaDesde,
    LocalDate fechaAltaHasta,
    LocalDate fechaBajaDesde,
    LocalDate fechaBajaHasta,
    String nacionalidad,
    String buscar
) {}
