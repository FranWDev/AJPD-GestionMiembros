package org.dubini.gestion.controller;

import org.dubini.gestion.config.AccessKeyProperties;
import org.dubini.gestion.model.Cargo;
import org.dubini.gestion.model.Centro;
import org.dubini.gestion.model.HistorialCargo;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MiembroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessKeyProperties accessKeyProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MiembroRepository miembroRepository;

    @Autowired
    private CentroRepository centroRepository;

    @Autowired
    private CargoRepository cargoRepository;

    private String authHeader;
    private Long centroId;
    private Long cargoId1;
    private Long cargoId2;

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

        Centro centro = centroRepository.save(new Centro(null, "Centro Test"));
        centroId = centro.getId();

        Cargo cargo1 = cargoRepository.save(new Cargo(null, "Presidente"));
        cargoId1 = cargo1.getId();

        Cargo cargo2 = cargoRepository.save(new Cargo(null, "Secretario"));
        cargoId2 = cargo2.getId();
    }

    @Test
    public void testFullMiembroFlowAndCargoHistory() throws Exception {
        mockMvc.perform(get("/api/miembros")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        String createMiembroBody = new JSONObject()
                .put("nombreRazonSocial", "Pedro Rodriguez")
                .put("centroId", centroId)
                .put("telefono", "987654321")
                .put("correo", "pedro@test.com")
                .put("cargoId", cargoId1)
                .put("fechaCargo", LocalDate.now().toString())
                .put("enlaceWhatsapp", "wlink")
                .toString();

        String createResponse = mockMvc.perform(post("/api/miembros")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createMiembroBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombreRazonSocial").value("Pedro Rodriguez"))
                .andExpect(jsonPath("$.historialCargos").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long miembroId = new JSONObject(createResponse).getLong("id");

        Miembro createdEntity = miembroRepository.findById(miembroId).orElseThrow();
        assertEquals(1, createdEntity.getHistorialCargos().size());
        HistorialCargo hc = createdEntity.getHistorialCargos().iterator().next();
        assertEquals(cargoId1, hc.getCargoId());
        assertNull(hc.getFechaFin());

        String updateMiembroBody = new JSONObject()
                .put("nombreRazonSocial", "Pedro Rodriguez Modificado")
                .put("centroId", centroId)
                .put("telefono", "987654321")
                .put("correo", "pedro@test.com")
                .put("cargoId", cargoId2)
                .put("fechaCargo", LocalDate.now().toString())
                .put("enlaceWhatsapp", "wlink")
                .toString();

        mockMvc.perform(put("/api/miembros/" + miembroId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateMiembroBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreRazonSocial").value("Pedro Rodriguez Modificado"));

        createdEntity = miembroRepository.findById(miembroId).orElseThrow();
        assertEquals(2, createdEntity.getHistorialCargos().size());

        HistorialCargo oldHc = createdEntity.getHistorialCargos().stream()
                .filter(h -> h.getCargoId().equals(cargoId1))
                .findFirst().orElseThrow();
        assertNotNull(oldHc.getFechaFin());

        HistorialCargo newHc = createdEntity.getHistorialCargos().stream()
                .filter(h -> h.getCargoId().equals(cargoId2))
                .findFirst().orElseThrow();
        assertNull(newHc.getFechaFin());

        Long historyId = newHc.getId();
        String updateHistoryBody = new JSONObject()
                .put("fechaInicio", LocalDate.now().minusDays(1).toString())
                .put("fechaFin", LocalDate.now().plusDays(5).toString())
                .put("cargo", new JSONObject().put("id", cargoId1))
                .toString();

        mockMvc.perform(put("/api/miembros/" + miembroId + "/historial/" + historyId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateHistoryBody))
                .andExpect(status().isOk());

        createdEntity = miembroRepository.findById(miembroId).orElseThrow();
        HistorialCargo updatedHc = createdEntity.getHistorialCargos().stream()
                .filter(h -> h.getId().equals(historyId))
                .findFirst().orElseThrow();
        assertEquals(LocalDate.now().minusDays(1), updatedHc.getFechaInicio());
        assertEquals(LocalDate.now().plusDays(5), updatedHc.getFechaFin());
        assertEquals(cargoId1, updatedHc.getCargoId());

        mockMvc.perform(delete("/api/miembros/" + miembroId + "/historial/" + historyId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        createdEntity = miembroRepository.findById(miembroId).orElseThrow();
        assertEquals(1, createdEntity.getHistorialCargos().size());

        mockMvc.perform(delete("/api/miembros/" + miembroId)
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/miembros/" + miembroId)
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Miembro no encontrado"));
    }
}
