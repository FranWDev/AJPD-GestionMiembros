package org.dubini.gestion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Table("miembros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Miembro {
    @Id
    private Long id;
    private String nombreRazonSocial;
    private Long centroId;
    private String telefono;
    private String correo;
    private Long cargoId;
    private LocalDate fechaCargo;
    private String enlaceWhatsapp;
    private String nifCif;
    private String nacionalidad;
    private String domicilio;
    private LocalDate fechaNacimiento;
    private LocalDate fechaAlta;
    private String observaciones;
    private LocalDate fechaBaja;

    @MappedCollection(idColumn = "miembro_id")
    private Set<HistorialCargo> historialCargos = new HashSet<>();

    public void alignCurrentCargoWithHistory() {
        if (this.historialCargos == null || this.historialCargos.isEmpty()) {
            this.cargoId = null;
            this.fechaCargo = null;
            return;
        }

        List<HistorialCargo> activeEntries = this.historialCargos.stream()
                .filter(h -> h.getFechaFin() == null)
                .sorted((h1, h2) -> h2.getFechaInicio().compareTo(h1.getFechaInicio()))
                .toList();

        if (!activeEntries.isEmpty()) {
            HistorialCargo active = activeEntries.get(0);
            this.cargoId = active.getCargoId();
            this.fechaCargo = active.getFechaInicio();

            for (int i = 1; i < activeEntries.size(); i++) {
                activeEntries.get(i).setFechaFin(active.getFechaInicio());
            }
        } else {
            this.cargoId = null;
            this.fechaCargo = null;
        }
    }
}
