package org.dubini.gestion.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Configuration
public class GoogleDriveConfig {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveConfig.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${google.drive.credentials-json:}")
    private String credentialsJson;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
            try {
                log.info("Loading Google Credentials from explicit JSON string.");
                GoogleCredentials creds = GoogleCredentials.fromStream(
                                new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)))
                        .createScoped(Collections.singleton(DriveScopes.DRIVE));
                if (creds instanceof com.google.auth.oauth2.ServiceAccountCredentials) {
                    log.info("Loaded Google Service Account email (from JSON): {}", ((com.google.auth.oauth2.ServiceAccountCredentials) creds).getClientEmail());
                } else {
                    log.info("Loaded Google Credentials class (from JSON): {}", creds.getClass().getName());
                }
                return creds;
            } catch (Exception e) {
                log.error("Failed to load GoogleCredentials from JSON string.", e);
            }
        }

        // Fallback to Application Default Credentials (ADC) which automatically picks up GOOGLE_APPLICATION_CREDENTIALS file path
        GoogleCredentials credentials = null;
        try {
            log.info("Attempting to load Google Application Default Credentials (ADC).");
            credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped(Collections.singleton(DriveScopes.DRIVE));
        } catch (Exception e) {
            log.warn("Could not load Google Application Default Credentials (ADC): {}", e.getMessage());
        }

        if (credentials != null) {
            if (credentials instanceof com.google.auth.oauth2.ServiceAccountCredentials) {
                log.info("Loaded Google Service Account email: {}", ((com.google.auth.oauth2.ServiceAccountCredentials) credentials).getClientEmail());
            } else {
                log.info("Loaded Google Credentials class: {}", credentials.getClass().getName());
            }
            return credentials;
        }

        log.warn("No Google Credentials could be loaded. Google Drive will run in Mock/No-op mode.");
        return null;
    }

    @Bean
    public Drive googleDriveClient(Optional<GoogleCredentials> credentialsOpt) throws IOException, GeneralSecurityException {
        if (credentialsOpt.isEmpty()) {
            log.warn("Google credentials bean is not present. Running Google Drive in Mock/No-op mode.");
            return null;
        }

        GoogleCredentials credentials = credentialsOpt.get();
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName("AJPD-GestionMiembros")
                .build();
    }
}
