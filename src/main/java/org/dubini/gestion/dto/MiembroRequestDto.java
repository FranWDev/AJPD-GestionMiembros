package org.dubini.gestion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dubini.gestion.validation.ValidNifCif;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MiembroRequestDto {
    @NotBlank(message = "El nombre o razón social del miembro es obligatorio")
    private String nombreRazonSocial;
    private Long centroId;
    private String telefono;
    private String correo;
    private Long cargoId;
    private LocalDate fechaCargo;
    private String enlaceWhatsapp;
    @ValidNifCif
    private String nifCif;
    private String nacionalidad;
    private String domicilio;
    private LocalDate fechaNacimiento;
    private LocalDate fechaAlta;
    private String observaciones;
    private LocalDate fechaBaja;
}
