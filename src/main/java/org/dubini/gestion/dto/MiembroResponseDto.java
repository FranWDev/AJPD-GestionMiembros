package org.dubini.gestion.dto;

import java.time.LocalDate;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta con los datos de un miembro")
public class MiembroResponseDto {
    @Schema(description = "ID único del miembro", example = "1")
    private Long id;
    
    @Schema(description = "Nombre o Razón Social", example = "Juan Pérez")
    private String nombreRazonSocial;
    
    @Schema(description = "Centro al que pertenece")
    private CentroDto centro;
    
    @Schema(description = "Teléfono de contacto", example = "123456789")
    private String telefono;
    
    @Schema(description = "Correo electrónico", example = "juan.perez@example.com")
    private String correo;
    
    @Schema(description = "Cargo actual del miembro")
    private CargoDto cargo;
    
    @Schema(description = "Fecha desde la que ostenta el cargo actual", example = "2023-01-01")
    private LocalDate fechaCargo;
    
    @Schema(description = "Enlace directo a WhatsApp", example = "https://wa.me/123456789")
    private String enlaceWhatsapp;
    
    @Schema(description = "NIF o CIF", example = "12345678A")
    private String nifCif;
    
    @Schema(description = "Nacionalidad del miembro", example = "Española")
    private String nacionalidad;
    
    @Schema(description = "Domicilio", example = "Calle Falsa 123")
    private String domicilio;
    
    @Schema(description = "Fecha de nacimiento", example = "1990-05-15")
    private LocalDate fechaNacimiento;
    
    @Schema(description = "Fecha de alta en la asociación", example = "2022-10-20")
    private LocalDate fechaAlta;
    
    @Schema(description = "Observaciones", example = "Miembro honorario")
    private String observaciones;
    
    @Schema(description = "Fecha de baja (si aplica)", example = "null")
    private LocalDate fechaBaja;
    
    @Schema(description = "Historial de cargos ocupados")
    private Set<HistorialCargoDto> historialCargos;
}
