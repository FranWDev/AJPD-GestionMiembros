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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CargoControllerIntegrationTest {

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
    void setup() throws Exception {
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
    void testFullCargoFlow() throws Exception {
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
    void testDeleteCargo_AssignedToMember() throws Exception {
        Cargo cargo = cargoRepository.save(new Cargo(null, "Cargo Ocupado"));
        Centro centro = centroRepository.save(new Centro(null, "Centro Test"));
        Miembro miembro = new Miembro();
        miembro.setNombreRazonSocial("Juan");
        miembro.setCentroId(centro.getId());
        miembro.setTelefono("123");
        miembro.setCorreo("juan@test.com");
        miembro.setCargoId(cargo.getId());
        miembro.setFechaCargo(LocalDate.now(ZoneId.systemDefault()));
        miembro.setEnlaceWhatsapp("link");
        miembro.setHistorialCargos(new HashSet<>());
        miembroRepository.save(miembro);

        mockMvc.perform(delete("/api/cargos/" + cargo.getId())
                        .header("Authorization", authHeader))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No se puede eliminar el cargo porque está asignado a uno o más miembros"));
    }

    @Test
    void testGetCargosWithNameFilter() throws Exception {
        cargoRepository.save(new Cargo(null, "Secretario Ejecutivo"));
        cargoRepository.save(new Cargo(null, "Vocal de Distrito"));

        mockMvc.perform(get("/api/cargos?nombre=Secretario")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Secretario Ejecutivo"));

        mockMvc.perform(get("/api/cargos?nombre=ejecutivo")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Secretario Ejecutivo"));

        String newCargoEmptyBody = new JSONObject()
                .put("nombre", "   ")
                .toString();

        mockMvc.perform(post("/api/cargos")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newCargoEmptyBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.nombre").value("El nombre del cargo es obligatorio"));
    }

    @Test
    void testCargoHistorialFlows() throws Exception {
        Centro centro = centroRepository.save(new Centro(null, "Centro Historial"));
        Cargo cargo = cargoRepository.save(new Cargo(null, "Vocal Especial"));
        
        Miembro m = new Miembro();
        m.setNombreRazonSocial("Lionel Messi");
        m.setCentroId(centro.getId());
        m.setCargoId(cargo.getId());
        m.setNacionalidad("Argentina");
        m.setNifCif("12345678Z");
        m.setFechaAlta(LocalDate.of(2026, 6, 1));
        m.setFechaCargo(LocalDate.of(2026, 6, 2));
        
        HistorialCargo hc = new HistorialCargo(null, LocalDate.of(2026, 6, 2), null, cargo.getId());
        m.setHistorialCargos(new HashSet<>(List.of(hc)));
        
        m = miembroRepository.save(m);
        Long historyId = m.getHistorialCargos().iterator().next().getId();

        mockMvc.perform(get("/api/cargos/historial?cargoId=" + cargo.getId())
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].miembroNombre").value("Lionel Messi"))
                .andExpect(jsonPath("$.content[0].cargoNombre").value("Vocal Especial"));

        mockMvc.perform(get("/api/cargos/historial?buscar=Lionel")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].miembroNombre").value("Lionel Messi"));

        Cargo newCargo = cargoRepository.save(new Cargo(null, "Vocal Modificado"));
        String editBody = new JSONObject()
                .put("fechaInicio", "2026-06-03")
                .put("fechaFin", "2026-06-09")
                .put("cargoId", newCargo.getId())
                .toString();

        mockMvc.perform(put("/api/cargos/historial/" + historyId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaInicio").value("2026-06-03"))
                .andExpect(jsonPath("$.fechaFin").value("2026-06-09"))
                .andExpect(jsonPath("$.cargoNombre").value("Vocal Modificado"));

        // Verificar que el miembro ya no tiene cargo activo porque se cerró el historial (fechaFin no nulo)
        Miembro updatedMiembro = miembroRepository.findById(m.getId()).orElseThrow();
        assertNull(updatedMiembro.getCargoId());
        assertNull(updatedMiembro.getFechaCargo());

        String badEditBody = new JSONObject()
                .put("fechaFin", "2026-06-09")
                .toString();

        mockMvc.perform(put("/api/cargos/historial/" + historyId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badEditBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.fechaInicio").value("La fecha de inicio es obligatoria"))
                .andExpect(jsonPath("$.validationErrors.cargoId").value("El id del cargo es obligatorio"));
    }

    @Test
    void testHistorialCargoAutoClosureAndAlignment() throws Exception {
        Centro centro = centroRepository.save(new Centro(null, "Centro Auto Closure"));
        Cargo cargoPresidente = cargoRepository.save(new Cargo(null, "Presidente"));
        Cargo cargoSecretario = cargoRepository.save(new Cargo(null, "Secretario"));

        Miembro m = new Miembro();
        m.setNombreRazonSocial("Andres Auto");
        m.setCentroId(centro.getId());
        m.setNacionalidad("Española");
        m.setNifCif("12345678Z");
        m.setFechaAlta(LocalDate.of(2026, 6, 1));
        
        // Creamos el miembro con el primer cargo (Presidente) activo desde el 2026-06-01
        m.setCargoId(cargoPresidente.getId());
        m.setFechaCargo(LocalDate.of(2026, 6, 1));
        HistorialCargo hc1 = new HistorialCargo(null, LocalDate.of(2026, 6, 1), null, cargoPresidente.getId());
        
        // Añadimos también un segundo cargo (Secretario) pero que estuvo cerrado del 2026-06-05 al 2026-06-10
        HistorialCargo hc2 = new HistorialCargo(null, LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 10), cargoSecretario.getId());
        
        m.setHistorialCargos(new HashSet<>(List.of(hc1, hc2)));
        m = miembroRepository.save(m);

        final Long mid = m.getId();
        HistorialCargo dbHc1 = m.getHistorialCargos().stream().filter(h -> h.getCargoId().equals(cargoPresidente.getId())).findFirst().orElseThrow();
        HistorialCargo dbHc2 = m.getHistorialCargos().stream().filter(h -> h.getCargoId().equals(cargoSecretario.getId())).findFirst().orElseThrow();
        Long hc1Id = dbHc1.getId();
        Long hc2Id = dbHc2.getId();

        // Verificar estado inicial:
        // - hc1 (Presidente, 2026-06-01) está ACTIVO (fechaFin = null).
        // - hc2 (Secretario, 2026-06-05) está CERRADO (fechaFin = 2026-06-10).
        // El miembro tiene como cargo activo actual Presidente.
        Miembro dbMiembro = miembroRepository.findById(mid).orElseThrow();
        assertEquals(cargoPresidente.getId(), dbMiembro.getCargoId());
        assertEquals(LocalDate.of(2026, 6, 1), dbMiembro.getFechaCargo());
        assertNull(dbHc1.getFechaFin());

        // Editamos el segundo historial (Secretario) para poner fechaFin = null (volverlo activo).
        // Al alinearse, debe poner a Secretario como el cargo actual del miembro y cerrar
        // automáticamente el cargo anterior (Presidente) con fechaFin = fechaInicio del Secretario (2026-06-05).
        String editBody = new JSONObject()
                .put("fechaInicio", "2026-06-05")
                .put("fechaFin", JSONObject.NULL)
                .put("cargoId", cargoSecretario.getId())
                .toString();

        mockMvc.perform(put("/api/cargos/historial/" + hc2Id)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editBody))
                .andExpect(status().isOk());

        // Validamos el estado en base de datos
        dbMiembro = miembroRepository.findById(mid).orElseThrow();
        
        // 1. El miembro tiene ahora el cargo Secretario y fechaCargo = 2026-06-05
        assertEquals(cargoSecretario.getId(), dbMiembro.getCargoId());
        assertEquals(LocalDate.of(2026, 6, 5), dbMiembro.getFechaCargo());

        // 2. El historial de Secretario está activo
        HistorialCargo finalHc2 = dbMiembro.getHistorialCargos().stream().filter(h -> h.getId().equals(hc2Id)).findFirst().orElseThrow();
        assertNull(finalHc2.getFechaFin());

        // 3. El historial de Presidente se ha cerrado automáticamente con fechaFin = 2026-06-05
        HistorialCargo finalHc1 = dbMiembro.getHistorialCargos().stream().filter(h -> h.getId().equals(hc1Id)).findFirst().orElseThrow();
        assertEquals(LocalDate.of(2026, 6, 5), finalHc1.getFechaFin());
    }
}
