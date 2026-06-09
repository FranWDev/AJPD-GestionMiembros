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
import java.util.Set;

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

    @MappedCollection(idColumn = "MIEMBRO_ID")
    private Set<HistorialCargo> historialCargos = new HashSet<>();
}
