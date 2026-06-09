package org.dubini.gestion.controller;

import org.dubini.gestion.config.AccessKeyProperties;
import org.dubini.gestion.model.Cargo;
import org.dubini.gestion.model.Centro;
import org.dubini.gestion.model.Miembro;
import org.dubini.gestion.repository.CargoRepository;
import org.dubini.gestion.repository.CentroRepository;
import org.dubini.gestion.repository.MiembroRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CargoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessKeyProperties accessKeyProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private CentroRepository centroRepository;

    @Autowired
    private MiembroRepository miembroRepository;

    private String authHeader;

    @BeforeEach
    public void setup() throws Exception {
        accessKeyProperties.setAccessKey(passwordEncoder.encode("testkey"));

        String loginBody = new JSONObject()
                .put("accessKey", "testkey")
                .toString();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new JSONObject(loginResponse).getString("token");
        authHeader = "Bearer " + token;
    }

    @Test
    public void testFullCargoFlow() throws Exception {
        mockMvc.perform(get("/api/cargos")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        String newCargoBody = new JSONObject()
                .put("nombre", "Cargo Integracion")
                .toString();

        String createResponse = mockMvc.perform(post("/api/cargos")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCargoBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Cargo Integracion"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long cargoId = new JSONObject(createResponse).getLong("id");

        String updateCargoBody = new JSONObject()
                .put("nombre", "Cargo Integracion Modificado")
                .toString();

        mockMvc.perform(put("/api/cargos/" + cargoId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateCargoBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cargo Integracion Modificado"));

        mockMvc.perform(get("/api/cargos/" + cargoId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cargo Integracion Modificado"));

        mockMvc.perform(delete("/api/cargos/" + cargoId)
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cargos/" + cargoId)
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cargo no encontrado"));
    }

    @Test
    public void testDeleteCargo_AssignedToMember() throws Exception {
        Cargo cargo = cargoRepository.save(new Cargo(null, "Cargo Ocupado"));
        Centro centro = centroRepository.save(new Centro(null, "Centro Test"));
        Miembro miembro = new Miembro(null, "Juan", centro.getId(), "123", "juan@test.com", cargo.getId(), LocalDate.now(), "link", new HashSet<>());
        miembroRepository.save(miembro);

        mockMvc.perform(delete("/api/cargos/" + cargo.getId())
                        .header("Authorization", authHeader))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No se puede eliminar el cargo porque está asignado a uno o más miembros"));
    }
}
