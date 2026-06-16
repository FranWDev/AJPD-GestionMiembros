package org.dubini.gestion.controller;

import org.dubini.gestion.client.CacheInvalidationClient;
import org.dubini.gestion.config.AccessKeyProperties;
import org.dubini.gestion.config.SupabaseStorageProperties;
import org.dubini.gestion.dto.response.HttpResponse;
import org.dubini.gestion.dto.response.ImageResponseDTO;
import org.dubini.gestion.repository.NewsRepository;
import org.dubini.gestion.service.CacheInvalidatorService;
import org.dubini.gestion.service.ImageService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.Cookie;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class NewsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessKeyProperties accessKeyProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private CacheInvalidatorService cacheInvalidatorService;

    @Autowired
    private ImageService imageService;

    private String authHeader;
    private String jwtToken;

    @TestConfiguration
    static class TestConfig {
        static class TestCacheInvalidatorService extends CacheInvalidatorService {
            public TestCacheInvalidatorService(CacheInvalidationClient client) {
                super(client);
            }

            @Override
            public HttpResponse invalidateNewsCache() {
                return new HttpResponse("News cache invalidated");
            }

            @Override
            public HttpResponse invalidateServiceWorkersCache() {
                return new HttpResponse("Service worker cache invalidated");
            }
        }

        static class TestImageService extends ImageService {
            public TestImageService(SupabaseStorageProperties properties) {
                super(properties);
            }

            @Override
            public ImageResponseDTO saveImage(MultipartFile file) throws IOException {
                return new ImageResponseDTO("test.png", "https://mock.supabase.co/storage/v1/object/public/bucket/test.png", 15);
            }
        }

        @Bean
        @Primary
        public CacheInvalidatorService cacheInvalidatorService(CacheInvalidationClient client) {
            return new TestCacheInvalidatorService(client);
        }

        @Bean
        @Primary
        public ImageService imageService(SupabaseStorageProperties properties) {
            return new TestImageService(properties);
        }
    }

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

        jwtToken = new JSONObject(loginResponse).getString("token");
        authHeader = "Bearer " + jwtToken;
    }

    @Test
    void testNewsFlow() throws Exception {
        // 1. Get all news (initially empty)
        mockMvc.perform(get("/api/news")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 2. Create a news article
        String newsBody = new JSONObject()
                .put("title", "Test Title")
                .put("description", "Test Description")
                .put("imageUrl", "http://image.url")
                .put("createdAt", "2026-06-14T18:00:00Z")
                .put("editorContent", new JSONObject()
                        .put("time", 123456789L)
                        .put("blocks", new org.json.JSONArray())
                        .put("version", "2.22.2"))
                .toString();

        mockMvc.perform(post("/api/news")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        // 3. Get all news (should contain 1)
        mockMvc.perform(get("/api/news")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Title"))
                .andExpect(jsonPath("$[0].description").value("Test Description"));

        // 4. Get specific news by identifier
        mockMvc.perform(get("/api/news/test-title")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Title"));

        // 5. Delete news
        mockMvc.perform(delete("/api/news/test-title")
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        // 6. Get deleted news (should return 404)
        mockMvc.perform(get("/api/news/test-title")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    @Test
    void testNewsTitleEdit() throws Exception {
        // 1. Create a news article
        String newsBody = new JSONObject()
                .put("title", "Old Title")
                .put("description", "Old Description")
                .put("imageUrl", "http://image.url")
                .put("createdAt", "2026-06-14T18:00:00Z")
                .put("editorContent", new JSONObject()
                        .put("time", 123456789L)
                        .put("blocks", new org.json.JSONArray())
                        .put("version", "2.22.2"))
                .toString();

        mockMvc.perform(post("/api/news")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Old Title"));

        // 2. Edit the news title using oldTitle parameter
        String editedBody = new JSONObject()
                .put("title", "New Title")
                .put("oldTitle", "Old Title")
                .put("description", "Updated Description")
                .put("imageUrl", "http://image.url")
                .put("createdAt", "2026-06-14T18:00:00Z")
                .put("editorContent", new JSONObject()
                        .put("time", 123456789L)
                        .put("blocks", new org.json.JSONArray())
                        .put("version", "2.22.2"))
                .toString();

        mockMvc.perform(post("/api/news")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(editedBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));

        // 3. Verify old title is 404
        mockMvc.perform(get("/api/news/old-title")
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());

        // 4. Verify new title is accessible
        mockMvc.perform(get("/api/news/new-title")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    void testNewsPaginationForAdmin() throws Exception {
        // Create a news article first so we have data to paginate
        String newsBody = new JSONObject()
                .put("title", "Pagination Title")
                .put("description", "Pagination Description")
                .put("imageUrl", "http://image.url")
                .put("createdAt", "2026-06-14T18:00:00Z")
                .put("editorContent", new JSONObject()
                        .put("time", 123456789L)
                        .put("blocks", new org.json.JSONArray())
                        .put("version", "2.22.2"))
                .toString();

        mockMvc.perform(post("/api/news")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody))
                .andExpect(status().isOk());

        // Perform request with page/size params and Authorization header
        mockMvc.perform(get("/api/news")
                        .header("Authorization", authHeader)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    void testNewsListForPublicWithCookie() throws Exception {
        // Create a news article first
        String newsBody = new JSONObject()
                .put("title", "Public Title")
                .put("description", "Public Description")
                .put("imageUrl", "http://image.url")
                .put("createdAt", "2026-06-14T18:00:00Z")
                .put("editorContent", new JSONObject()
                        .put("time", 123456789L)
                        .put("blocks", new org.json.JSONArray())
                        .put("version", "2.22.2"))
                .toString();

        mockMvc.perform(post("/api/news")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newsBody))
                .andExpect(status().isOk());

        // Perform request with jwt cookie and no Authorization header
        mockMvc.perform(get("/api/news")
                        .cookie(new Cookie("jwt", jwtToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

