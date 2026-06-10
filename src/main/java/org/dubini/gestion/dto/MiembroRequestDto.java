package org.dubini.gestion.dto;

import java.time.LocalDate;

import org.dubini.gestion.validation.ValidNifCif;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para crear o actualizar un miembro")
public class MiembroRequestDto {
    @NotBlank(message = "El nombre o razón social del miembro es obligatorio")
    @Schema(description = "Nombre o Razón Social", example = "Juan Pérez", required = true)
    private String nombreRazonSocial;

    @Schema(description = "ID del centro al que pertenece", example = "1")
    private Long centroId;

    @Schema(description = "Teléfono de contacto", example = "123456789")
    private String telefono;

    @Schema(description = "Correo electrónico", example = "juan.perez@example.com")
    private String correo;

    @Schema(description = "ID del cargo actual", example = "2")
    private Long cargoId;

    @Schema(description = "Fecha desde la que ostenta el cargo", example = "2023-01-01")
    private LocalDate fechaCargo;

    @Schema(description = "Enlace directo a WhatsApp", example = "https://wa.me/123456789")
    private String enlaceWhatsapp;

    @ValidNifCif
    @Schema(description = "NIF o CIF validado", example = "12345678A")
    private String nifCif;

    @Schema(description = "Nacionalidad", example = "Española")
    private String nacionalidad;

    @Schema(description = "Domicilio", example = "Calle Falsa 123")
    private String domicilio;

    @Schema(description = "Fecha de nacimiento", example = "1990-05-15")
    private LocalDate fechaNacimiento;

    @Schema(description = "Fecha de alta", example = "2022-10-20")
    private LocalDate fechaAlta;

    @Schema(description = "Observaciones adicionales", example = "Sin observaciones")
    private String observaciones;

    @Schema(description = "Fecha de baja", example = "null")
    private LocalDate fechaBaja;
}
