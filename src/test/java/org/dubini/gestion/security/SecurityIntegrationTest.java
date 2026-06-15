package org.dubini.gestion.security;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String miembroBody;
    private String newsBody;

    @BeforeEach
    void setup() throws Exception {
        miembroBody = new JSONObject()
                .put("nombreRazonSocial", "Socio de Prueba")
                .put("telefono", "600111222")
                .put("correo", "socio.prueba@test.com")
                .put("nifCif", "12345678Z")
                .put("fechaAlta", LocalDate.now().toString())
                .toString();

        newsBody = new JSONObject()
                .put("title", "Titulo Noticia Prueba")
                .put("content", "{\"blocks\": []}")
                .toString();
    }

    @Test
    void testPresidenciaHasFullAccess() throws Exception {
        // Can write members
        mockMvc.perform(post("/api/miembros")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "presidencia@proyectodubini.org")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(miembroBody))
                .andExpect(status().isOk());

        // Can write news
        mockMvc.perform(post("/api/news")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "presidencia@proyectodubini.org")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody))
                .andExpect(status().isOk());

        // Can sync folders
        mockMvc.perform(post("/api/miembros/documentos/sync-folders")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "presidencia@proyectodubini.org"))))
                .andExpect(status().isOk());
    }

    @Test
    void testSecretariaCanManageOrganizationButNotWeb() throws Exception {
        // Can write members
        mockMvc.perform(post("/api/miembros")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "secretaria@proyectodubini.org")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(miembroBody))
                .andExpect(status().isOk());

        // CANNOT write news (403 Forbidden)
        mockMvc.perform(post("/api/news")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "secretaria@proyectodubini.org")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody))
                .andExpect(status().isForbidden());

        // Can sync folders
        mockMvc.perform(post("/api/miembros/documentos/sync-folders")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "secretaria@proyectodubini.org"))))
                .andExpect(status().isOk());
    }

    @Test
    void testComunicacionCanManageWebButNotOrganization() throws Exception {
        // CANNOT write members (403 Forbidden)
        mockMvc.perform(post("/api/miembros")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "comunicacion@proyectodubini.org")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(miembroBody))
                .andExpect(status().isForbidden());

        // Can write news
        mockMvc.perform(post("/api/news")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "comunicacion@proyectodubini.org")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody))
                .andExpect(status().isOk());
    }

    @Test
    void testVisitorIsReadOnly() throws Exception {
        // Can read members
        mockMvc.perform(get("/api/miembros")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "visitor@proyectodubini.org"))))
                .andExpect(status().isOk());

        // CANNOT write members (403 Forbidden)
        mockMvc.perform(post("/api/miembros")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "visitor@proyectodubini.org")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(miembroBody))
                .andExpect(status().isForbidden());

        // CANNOT write news (403 Forbidden)
        mockMvc.perform(post("/api/news")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "visitor@proyectodubini.org")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody))
                .andExpect(status().isForbidden());

        // CANNOT sync folders (403 Forbidden)
        mockMvc.perform(post("/api/miembros/documentos/sync-folders")
                        .with(oauth2Login().attributes(attrs -> attrs.put("email", "visitor@proyectodubini.org"))))
                .andExpect(status().isForbidden());
    }
}
