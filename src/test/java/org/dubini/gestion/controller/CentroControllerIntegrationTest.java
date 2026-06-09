package org.dubini.gestion.controller;

import org.dubini.gestion.config.AccessKeyProperties;
import org.dubini.gestion.model.Centro;
import org.dubini.gestion.model.Miembro;
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
public class CentroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessKeyProperties accessKeyProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public void testFullCentroFlow() throws Exception {
        mockMvc.perform(get("/api/centros")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        String newCentroBody = new JSONObject()
                .put("nombre", "Centro Integracion")
                .toString();

        String createResponse = mockMvc.perform(post("/api/centros")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCentroBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Centro Integracion"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long centroId = new JSONObject(createResponse).getLong("id");

        String updateCentroBody = new JSONObject()
                .put("nombre", "Centro Integracion Modificado")
                .toString();

        mockMvc.perform(put("/api/centros/" + centroId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateCentroBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Centro Integracion Modificado"));

        mockMvc.perform(get("/api/centros/" + centroId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Centro Integracion Modificado"));

        mockMvc.perform(delete("/api/centros/" + centroId)
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/centros/" + centroId)
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Centro no encontrado"));
    }

    @Test
    public void testDeleteCentro_AssignedToMember() throws Exception {
        Centro centro = centroRepository.save(new Centro(null, "Centro Ocupado"));
        Miembro miembro = new Miembro(null, "Juan", centro.getId(), "123", "juan@test.com", null, LocalDate.now(), "link", new HashSet<>());
        miembroRepository.save(miembro);

        mockMvc.perform(delete("/api/centros/" + centro.getId())
                        .header("Authorization", authHeader))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No se puede eliminar el centro porque está asignado a uno o más miembros"));
    }
}
