package org.dubini.gestion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CentroDto {
    private Long id;
    @NotBlank(message = "El nombre del centro es obligatorio")
    private String nombre;
}
