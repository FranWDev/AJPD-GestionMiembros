package org.dubini.gestion.dto;

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
@Schema(description = "DTO para la representación de un Cargo")
public class CargoDto {
    @Schema(description = "ID único del cargo", example = "1")
    private Long id;
    
    @NotBlank(message = "El nombre del cargo es obligatorio")
    @Schema(description = "Nombre del cargo", example = "Director General")
    private String nombre;
}
