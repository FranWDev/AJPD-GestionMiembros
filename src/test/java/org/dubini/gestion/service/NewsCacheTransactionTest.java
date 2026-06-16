package org.dubini.gestion.service;

import org.dubini.gestion.client.CacheInvalidationClient;
import org.dubini.gestion.dto.PublicationDTO;
import org.dubini.gestion.dto.response.HttpResponse;
import org.dubini.gestion.repository.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class NewsCacheTransactionTest {

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private CacheInvalidatorService cacheInvalidatorService;

    private TransactionTemplate transactionTemplate;

    private static final AtomicBoolean invalidationCalled = new AtomicBoolean(false);

    @TestConfiguration
    static class TestConfig {
        static class TestCacheInvalidatorService extends CacheInvalidatorService {
            public TestCacheInvalidatorService(CacheInvalidationClient client) {
                super(client);
            }

            @Override
            public HttpResponse invalidateNewsCache() {
                invalidationCalled.set(true);
                return new HttpResponse("News cache invalidated");
            }
        }

        @Bean
        @Primary
        public CacheInvalidatorService cacheInvalidatorService(CacheInvalidationClient client) {
            return new TestCacheInvalidatorService(client);
        }
    }

    @BeforeEach
    void setup() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        newsRepository.deleteAll();
        
        Cache newsCache = cacheManager.getCache("news");
        if (newsCache != null) newsCache.clear();
        
        Cache newsListCache = cacheManager.getCache("newsList");
        if (newsListCache != null) newsListCache.clear();
        
        Cache newsPageCache = cacheManager.getCache("newsPage");
        if (newsPageCache != null) newsPageCache.clear();
        
        invalidationCalled.set(false);
    }

    @Test
    void testCacheEvictionAndDeferredInvalidation() {
        Cache newsCache = cacheManager.getCache("news");
        assertNotNull(newsCache, "News cache should be configured");
        assertNull(newsCache.get("test-title-cache"));

        PublicationDTO dto = new PublicationDTO();
        dto.setTitle("Test Title Cache");
        dto.setDescription("Initial Description");
        dto.setImageUrl("http://image.url");
        dto.setCreatedAt("2026-06-14T18:00:00Z");
        dto.setEditorContent(null);

        newsService.save(dto);

        PublicationDTO cachedDto = newsService.get("test-title-cache");
        assertNotNull(cachedDto);
        assertNotNull(newsCache.get("test-title-cache"));

        invalidationCalled.set(false);

        transactionTemplate.execute(status -> {
            PublicationDTO updateDto = new PublicationDTO();
            updateDto.setTitle("Test Title Cache");
            updateDto.setDescription("Updated Description");
            updateDto.setImageUrl("http://image.url");
            updateDto.setCreatedAt("2026-06-14T18:00:00Z");
            updateDto.setEditorContent(null);

            newsService.save(updateDto);

            assertNull(newsCache.get("test-title-cache"), "Cache should be evicted immediately during service call execution");
            assertFalse(invalidationCalled.get(), "Cache invalidation call should be deferred and not executed yet during the active transaction");

            return null;
        });

        assertTrue(invalidationCalled.get(), "Cache invalidation call must be executed after the transaction commits successfully");
    }
}
