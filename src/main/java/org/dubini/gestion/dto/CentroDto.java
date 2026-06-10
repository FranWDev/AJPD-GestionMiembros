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
@Schema(description = "DTO para la representación de un Centro")
public class CentroDto {
    @Schema(description = "ID único del centro", example = "1")
    private Long id;
    
    @NotBlank(message = "El nombre del centro es obligatorio")
    @Schema(description = "Nombre del centro", example = "Centro Principal")
    private String nombre;
}
