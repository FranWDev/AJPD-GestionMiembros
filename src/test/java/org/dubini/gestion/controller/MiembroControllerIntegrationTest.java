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

    @Test
    public void testMiembroNuevosCamposYBajaReactivacion() throws Exception {
        String body = new JSONObject()
                .put("nombreRazonSocial", "Juan Perez")
                .put("centroId", centroId)
                .put("telefono", "123456789")
                .put("correo", "juan@test.com")
                .put("nifCif", "12345678Z")
                .put("nacionalidad", "Española")
                .put("domicilio", "Calle Principal 123")
                .put("fechaNacimiento", "1990-01-01")
                .put("fechaAlta", "2026-06-01")
                .put("observaciones", "Ninguna observacion")
                .toString();

        String createResponse = mockMvc.perform(post("/api/miembros")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nifCif").value("12345678Z"))
                .andExpect(jsonPath("$.nacionalidad").value("Española"))
                .andExpect(jsonPath("$.domicilio").value("Calle Principal 123"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1990-01-01"))
                .andExpect(jsonPath("$.fechaAlta").value("2026-06-01"))
                .andExpect(jsonPath("$.observaciones").value("Ninguna observacion"))
                .andExpect(jsonPath("$.fechaBaja").isEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long miembroId = new JSONObject(createResponse).getLong("id");

        String bajaBody = new JSONObject()
                .put("fechaBaja", "2026-06-05")
                .toString();

        mockMvc.perform(put("/api/miembros/" + miembroId + "/baja")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bajaBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaBaja").value("2026-06-05"));

        mockMvc.perform(delete("/api/miembros/" + miembroId + "/baja")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaBaja").isEmpty());
    }

    @Test
    public void testMiembroValidationAndFilters() throws Exception {
        String invalidNameBody = new JSONObject()
                .put("nombreRazonSocial", "   ")
                .put("centroId", centroId)
                .put("nifCif", "12345678Z")
                .toString();

        mockMvc.perform(post("/api/miembros")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidNameBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.nombreRazonSocial").value("El nombre o razón social del miembro es obligatorio"));

        String invalidNifBody = new JSONObject()
                .put("nombreRazonSocial", "Valid Name")
                .put("centroId", centroId)
                .put("nifCif", "12345678A")
                .toString();

        mockMvc.perform(post("/api/miembros")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidNifBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.nifCif").value("DNI/NIF/NIE no es válido"));

        Miembro m1 = new Miembro();
        m1.setNombreRazonSocial("Andres Iniesta");
        m1.setCentroId(centroId);
        m1.setCargoId(cargoId1);
        m1.setNacionalidad("Española");
        m1.setNifCif("12345678Z");
        m1.setTelefono("666111222");
        m1.setCorreo("andres@iniesta.com");
        m1.setFechaAlta(LocalDate.of(2026, 6, 1));
        m1.setFechaCargo(LocalDate.now());
        m1.setHistorialCargos(new HashSet<>());
        miembroRepository.save(m1);

        Miembro m2 = new Miembro();
        m2.setNombreRazonSocial("Zinedine Zidane");
        m2.setCentroId(centroId);
        m2.setCargoId(cargoId2);
        m2.setNacionalidad("Francesa");
        m2.setNifCif("44444444T");
        m2.setTelefono("777111222");
        m2.setCorreo("zizou@zidane.com");
        m2.setFechaAlta(LocalDate.of(2026, 6, 2));
        m2.setFechaBaja(LocalDate.of(2026, 6, 8));
        m2.setFechaCargo(LocalDate.now());
        m2.setHistorialCargos(new HashSet<>());
        miembroRepository.save(m2);

        mockMvc.perform(get("/api/miembros?filtroBaja=ACTIVOS")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombreRazonSocial").value("Andres Iniesta"));

        mockMvc.perform(get("/api/miembros?filtroBaja=BAJAS")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombreRazonSocial").value("Zinedine Zidane"));

        mockMvc.perform(get("/api/miembros")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get("/api/miembros?centroId=" + centroId + "&cargoId=" + cargoId1)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombreRazonSocial").value("Andres Iniesta"));

        mockMvc.perform(get("/api/miembros?nacionalidad=francesa")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombreRazonSocial").value("Zinedine Zidane"));

        mockMvc.perform(get("/api/miembros?fechaAltaDesde=2026-06-01&fechaAltaHasta=2026-06-01")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombreRazonSocial").value("Andres Iniesta"));

        mockMvc.perform(get("/api/miembros?buscar=iniesta")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombreRazonSocial").value("Andres Iniesta"));

        mockMvc.perform(get("/api/miembros?buscar=zizou@")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombreRazonSocial").value("Zinedine Zidane"));

        mockMvc.perform(get("/api/miembros?buscar=666111")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombreRazonSocial").value("Andres Iniesta"));

        mockMvc.perform(get("/api/miembros?buscar=44444444T")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombreRazonSocial").value("Zinedine Zidane"));

        mockMvc.perform(get("/api/miembros?buscar=And")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombreRazonSocial").value("Andres Iniesta"));

        mockMvc.perform(get("/api/miembros?buscar=Zid")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombreRazonSocial").value("Zinedine Zidane"));
     }

    @Test
    public void testDeleteRecentCargoHistoryRestoresPreviousCargo() throws Exception {

        String createMiembroBody = new JSONObject()
                .put("nombreRazonSocial", "Test Restores Cargo")
                .put("centroId", centroId)
                .put("telefono", "123123123")
                .put("correo", "testrestore@test.com")
                .put("cargoId", cargoId1)
                .put("fechaCargo", LocalDate.now().toString())
                .put("enlaceWhatsapp", "wlink")
                .toString();

        String createResponse = mockMvc.perform(post("/api/miembros")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createMiembroBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargo.id").value(cargoId1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long miembroId = new JSONObject(createResponse).getLong("id");

        String updateMiembroBody = new JSONObject()
                .put("nombreRazonSocial", "Test Restores Cargo")
                .put("centroId", centroId)
                .put("telefono", "123123123")
                .put("correo", "testrestore@test.com")
                .put("cargoId", cargoId2)
                .put("fechaCargo", LocalDate.now().toString())
                .put("enlaceWhatsapp", "wlink")
                .toString();

        mockMvc.perform(put("/api/miembros/" + miembroId)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateMiembroBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargo.id").value(cargoId2));

        Miembro miembro = miembroRepository.findById(miembroId).orElseThrow();
        assertEquals(2, miembro.getHistorialCargos().size());
        
        HistorialCargo activeHc = miembro.getHistorialCargos().stream()
                .filter(hc -> hc.getCargoId().equals(cargoId2))
                .findFirst().orElseThrow();
        Long activeHistoryId = activeHc.getId();

        mockMvc.perform(delete("/api/miembros/" + miembroId + "/historial/" + activeHistoryId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargo.id").value(cargoId1));

        miembro = miembroRepository.findById(miembroId).orElseThrow();
        assertEquals(1, miembro.getHistorialCargos().size());
        assertEquals(cargoId1, miembro.getCargoId());
        
        HistorialCargo remainingHc = miembro.getHistorialCargos().iterator().next();
        assertEquals(cargoId1, remainingHc.getCargoId());
        assertNull(remainingHc.getFechaFin());
    }

    @Test
    public void testMiembroBajaFinalizaCargo() throws Exception {
        String body = new JSONObject()
                .put("nombreRazonSocial", "Test Baja Cargo")
                .put("centroId", centroId)
                .put("telefono", "123456789")
                .put("correo", "baja_cargo@test.com")
                .put("cargoId", cargoId1)
                .put("fechaCargo", "2026-06-01")
                .toString();

        String createResponse = mockMvc.perform(post("/api/miembros")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargo.id").value(cargoId1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long miembroId = new JSONObject(createResponse).getLong("id");

        Miembro miembro = miembroRepository.findById(miembroId).orElseThrow();
        assertEquals(1, miembro.getHistorialCargos().size());
        HistorialCargo hc = miembro.getHistorialCargos().iterator().next();
        assertNull(hc.getFechaFin());
        assertEquals(cargoId1, miembro.getCargoId());

        String bajaBody = new JSONObject()
                .put("fechaBaja", "2026-06-05")
                .toString();

        mockMvc.perform(put("/api/miembros/" + miembroId + "/baja")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bajaBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaBaja").value("2026-06-05"))
                .andExpect(jsonPath("$.cargo").isEmpty());

        miembro = miembroRepository.findById(miembroId).orElseThrow();
        assertNull(miembro.getCargoId());
        assertEquals(1, miembro.getHistorialCargos().size());
        HistorialCargo hcFinalized = miembro.getHistorialCargos().iterator().next();
        assertEquals(LocalDate.of(2026, 6, 5), hcFinalized.getFechaFin());
    }

    @Test
    public void testMiembroReactivacionRestauraCargo() throws Exception {
        String body = new JSONObject()
                .put("nombreRazonSocial", "Test Reactivacion Cargo")
                .put("centroId", centroId)
                .put("telefono", "123456789")
                .put("correo", "reactivacion_cargo@test.com")
                .put("cargoId", cargoId1)
                .put("fechaCargo", "2026-06-01")
                .toString();

        String createResponse = mockMvc.perform(post("/api/miembros")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargo.id").value(cargoId1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long miembroId = new JSONObject(createResponse).getLong("id");

        // Put on baja
        String bajaBody = new JSONObject()
                .put("fechaBaja", "2026-06-05")
                .toString();

        mockMvc.perform(put("/api/miembros/" + miembroId + "/baja")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bajaBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaBaja").value("2026-06-05"))
                .andExpect(jsonPath("$.cargo").isEmpty());

        // Revert baja (reactivate)
        mockMvc.perform(delete("/api/miembros/" + miembroId + "/baja")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaBaja").isEmpty())
                .andExpect(jsonPath("$.cargo.id").value(cargoId1));

        Miembro miembro = miembroRepository.findById(miembroId).orElseThrow();
        assertEquals(cargoId1, miembro.getCargoId());
        assertEquals(1, miembro.getHistorialCargos().size());
        HistorialCargo hcRestored = miembro.getHistorialCargos().iterator().next();
        assertNull(hcRestored.getFechaFin());
    }
}
