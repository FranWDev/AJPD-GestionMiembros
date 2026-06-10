package org.dubini.gestion.repository;

import org.dubini.gestion.model.Miembro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class MiembroRepositoryImpl implements MiembroRepositoryCustom {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private MiembroRepository miembroRepository;

    public MiembroRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    public void setMiembroRepository(@Lazy MiembroRepository miembroRepository) {
        this.miembroRepository = miembroRepository;
    }

    @Override
    public Page<Miembro> findByFilters(
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
    ) {
        List<String> whereClauses = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource();

        if ("ACTIVOS".equalsIgnoreCase(filtroBaja)) {
            whereClauses.add("FECHA_BAJA IS NULL");
        } else if ("BAJAS".equalsIgnoreCase(filtroBaja)) {
            whereClauses.add("FECHA_BAJA IS NOT NULL");
        }

        if (centroId != null) {
            whereClauses.add("CENTRO_ID = :centroId");
            params.addValue("centroId", centroId);
        }

        if (cargoId != null) {
            whereClauses.add("CARGO_ID = :cargoId");
            params.addValue("cargoId", cargoId);
        }

        if (fechaAltaDesde != null) {
            whereClauses.add("FECHA_ALTA >= :fechaAltaDesde");
            params.addValue("fechaAltaDesde", fechaAltaDesde);
        }
        if (fechaAltaHasta != null) {
            whereClauses.add("FECHA_ALTA <= :fechaAltaHasta");
            params.addValue("fechaAltaHasta", fechaAltaHasta);
        }

        if (fechaBajaDesde != null) {
            whereClauses.add("FECHA_BAJA >= :fechaBajaDesde");
            params.addValue("fechaBajaDesde", fechaBajaDesde);
        }
        if (fechaBajaHasta != null) {
            whereClauses.add("FECHA_BAJA <= :fechaBajaHasta");
            params.addValue("fechaBajaHasta", fechaBajaHasta);
        }

        if (nacionalidad != null && !nacionalidad.trim().isEmpty()) {
            whereClauses.add("LOWER(NACIONALIDAD) LIKE LOWER(:nacionalidad)");
            params.addValue("nacionalidad", "%" + nacionalidad.trim() + "%");
        }

        if (buscar != null && !buscar.trim().isEmpty()) {
            String searchPattern = "%" + buscar.trim().toLowerCase() + "%";
            whereClauses.add("(LOWER(NOMBRE_RAZON_SOCIAL) LIKE :buscarVal OR LOWER(CORREO) LIKE :buscarVal OR TELEFONO LIKE :buscarVal OR LOWER(NIF_CIF) LIKE :buscarVal)");
            params.addValue("buscarVal", searchPattern);
        }

        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM miembros");
        StringBuilder querySql = new StringBuilder("SELECT ID FROM miembros");

        if (!whereClauses.isEmpty()) {
            String wherePart = " WHERE " + String.join(" AND ", whereClauses);
            countSql.append(wherePart);
            querySql.append(wherePart);
        }

        Integer total = jdbcTemplate.queryForObject(countSql.toString(), params, Integer.class);
        if (total == null || total == 0) {
            return Page.empty(pageable);
        }

        querySql.append(" ").append(getOrderByClause(pageable.getSort()));
        querySql.append(" LIMIT :limit OFFSET :offset");
        params.addValue("limit", pageable.getPageSize());
        params.addValue("offset", pageable.getOffset());

        List<Long> ids = jdbcTemplate.queryForList(querySql.toString(), params, Long.class);
        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Miembro> members = miembroRepository.findAllById(ids);
        Map<Long, Miembro> memberMap = members.stream()
                .collect(Collectors.toMap(Miembro::getId, m -> m));
        List<Miembro> sortedMembers = ids.stream()
                .map(memberMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(sortedMembers, pageable, total);
    }

    private String getOrderByClause(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return "ORDER BY ID DESC";
        }
        List<String> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            String column;
            switch (property) {
                case "nombreRazonSocial": column = "NOMBRE_RAZON_SOCIAL"; break;
                case "centroId": column = "CENTRO_ID"; break;
                case "telefono": column = "TELEFONO"; break;
                case "correo": column = "CORREO"; break;
                case "cargoId": column = "CARGO_ID"; break;
                case "fechaCargo": column = "FECHA_CARGO"; break;
                case "enlaceWhatsapp": column = "ENLACE_WHATSAPP"; break;
                case "nifCif": column = "NIF_CIF"; break;
                case "nacionalidad": column = "NACIONALIDAD"; break;
                case "domicilio": column = "DOMICILIO"; break;
                case "fechaNacimiento": column = "FECHA_NACIMIENTO"; break;
                case "fechaAlta": column = "FECHA_ALTA"; break;
                case "observaciones": column = "OBSERVACIONES"; break;
                case "fechaBaja": column = "FECHA_BAJA"; break;
                default: column = "ID"; break;
            }
            orders.add(column + " " + order.getDirection().name());
        }
        return "ORDER BY " + String.join(", ", orders);
    }
}
