package org.dubini.gestion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de inicio de sesión con clave de acceso")
public class LoginRequest {
    @Schema(description = "Clave de acceso para autenticación", example = "mi-clave-secreta")
    private String accessKey;
}
