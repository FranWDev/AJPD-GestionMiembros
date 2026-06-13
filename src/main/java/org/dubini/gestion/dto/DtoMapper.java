package org.dubini.gestion.dto;

import org.dubini.gestion.model.Cargo;
import org.dubini.gestion.model.Centro;
import org.dubini.gestion.model.HistorialCargo;
import org.dubini.gestion.model.Miembro;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DtoMapper {

    private DtoMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static CentroDto toDto(Centro c) {
        if (c == null) return null;
        return new CentroDto(c.getId(), c.getNombre());
    }

    public static CargoDto toDto(Cargo c) {
        if (c == null) return null;
        return new CargoDto(c.getId(), c.getNombre());
    }

    public static void updateEntity(Miembro m, MiembroRequestDto dto) {
        m.setNombreRazonSocial(dto.getNombreRazonSocial());
        m.setCentroId(dto.getCentroId());
        m.setTelefono(dto.getTelefono());
        m.setCorreo(dto.getCorreo());
        m.setCargoId(dto.getCargoId());
        m.setEnlaceWhatsapp(dto.getEnlaceWhatsapp());
        m.setNifCif(dto.getNifCif());
        m.setNacionalidad(dto.getNacionalidad());
        m.setDomicilio(dto.getDomicilio());
        m.setFechaNacimiento(dto.getFechaNacimiento());
        m.setFechaAlta(dto.getFechaAlta());
        m.setObservaciones(dto.getObservaciones());
        m.setFechaBaja(dto.getFechaBaja());
    }

    public static MiembroResponseDto toResponseDto(Miembro m, Map<Long, Centro> centroMap, Map<Long, Cargo> cargoMap, Map<Long, Cargo> historyCargoMap) {
        MiembroResponseDto dto = new MiembroResponseDto();
        dto.setId(m.getId());
        dto.setNombreRazonSocial(m.getNombreRazonSocial());
        dto.setTelefono(m.getTelefono());
        dto.setCorreo(m.getCorreo());
        dto.setFechaCargo(m.getFechaCargo());
        dto.setEnlaceWhatsapp(m.getEnlaceWhatsapp());
        dto.setNifCif(m.getNifCif());
        dto.setNacionalidad(m.getNacionalidad());
        dto.setDomicilio(m.getDomicilio());
        dto.setFechaNacimiento(m.getFechaNacimiento());
        dto.setFechaAlta(m.getFechaAlta());
        dto.setObservaciones(m.getObservaciones());
        dto.setFechaBaja(m.getFechaBaja());

        if (m.getCentroId() != null && centroMap != null && centroMap.containsKey(m.getCentroId())) {
            dto.setCentro(toDto(centroMap.get(m.getCentroId())));
        }

        if (m.getCargoId() != null && cargoMap != null && cargoMap.containsKey(m.getCargoId())) {
            dto.setCargo(toDto(cargoMap.get(m.getCargoId())));
        }

        if (m.getHistorialCargos() != null) {
            Set<HistorialCargoDto> historyDtos = m.getHistorialCargos().stream()
                    .map(hc -> {
                        CargoDto cDto = null;
                        if (historyCargoMap != null && historyCargoMap.containsKey(hc.getCargoId())) {
                            cDto = toDto(historyCargoMap.get(hc.getCargoId()));
                        }
                        return new HistorialCargoDto(hc.getId(), hc.getFechaInicio(), hc.getFechaFin(), cDto);
                    })
                    .collect(Collectors.toSet());
            dto.setHistorialCargos(historyDtos);
        }
        return dto;
    }
}
