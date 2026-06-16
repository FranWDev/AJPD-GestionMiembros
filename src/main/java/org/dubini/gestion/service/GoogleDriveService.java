package org.dubini.gestion.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import jakarta.annotation.PostConstruct;

@Service
public class GoogleDriveService {

    private static final Logger log = LoggerFactory.getLogger(GoogleDriveService.class);
    private static final String FOLDER_MIME = "application/vnd.google-apps.folder";

    private final Drive driveService;
    private final GoogleCredentials googleCredentials;

    @Value("${google.drive.parent-folder-id}")
    private String parentFolderId;

    @Autowired
    public GoogleDriveService(
            Optional<Drive> driveServiceOpt,
            Optional<GoogleCredentials> googleCredentialsOpt) {
        this.driveService = driveServiceOpt.orElse(null);
        this.googleCredentials = googleCredentialsOpt.orElse(null);
    }

    @PostConstruct
    public void init() {
        if (parentFolderId != null) {
            String original = parentFolderId;
            parentFolderId = cleanFolderId(parentFolderId);
            if (!original.equals(parentFolderId)) {
                log.info("Cleaned parentFolderId configuration from '{}' to '{}'", original, parentFolderId);
            }
        }
    }

    public static String cleanFolderId(String input) {
        if (input == null) {
            return null;
        }
        input = input.trim();
        if (input.contains("/folders/")) {
            int index = input.indexOf("/folders/");
            String remainder = input.substring(index + "/folders/".length());
            int qIndex = remainder.indexOf('?');
            int slashIndex = remainder.indexOf('/');
            int endIndex = remainder.length();
            if (qIndex != -1) {
                endIndex = qIndex;
            }
            if (slashIndex != -1 && slashIndex < endIndex) {
                endIndex = slashIndex;
            }
            return remainder.substring(0, endIndex);
        }
        if (input.contains("id=")) {
            int index = input.indexOf("id=");
            String remainder = input.substring(index + 3);
            int ampersandIndex = remainder.indexOf('&');
            int slashIndex = remainder.indexOf('/');
            int endIndex = remainder.length();
            if (ampersandIndex != -1) {
                endIndex = ampersandIndex;
            }
            if (slashIndex != -1 && slashIndex < endIndex) {
                endIndex = slashIndex;
            }
            return remainder.substring(0, endIndex);
        }
        return input;
    }

    public boolean isConfigured() {
        return driveService != null && parentFolderId != null && !parentFolderId.isEmpty() && !"mock-parent-folder-id".equals(parentFolderId);
    }

    /**
     * Creates the folder structure for a member: SOCIO_{ID}/DNI and SOCIO_{ID}/FOTO
     */
    public void createMemberFolders(Long memberId) {
        if (!isConfigured()) {
            log.warn("Google Drive Service is not configured. Skipping folder creation for member {}", memberId);
            return;
        }

        try {
            String memberFolderName = "SOCIO_" + memberId;
            log.info("Creating folders in Google Drive for member: {}", memberFolderName);

            // Find or create member base folder
            String memberFolderId = getOrCreateFolder(memberFolderName, parentFolderId);

            // Create subfolders
            getOrCreateFolder("DNI", memberFolderId);
            getOrCreateFolder("FOTO", memberFolderId);

            log.info("Folders successfully created for member: {}", memberFolderName);
        } catch (IOException e) {
            log.error("Failed to create Google Drive folders for member {}", memberId, e);
            throw new RuntimeException("Error al crear las carpetas en Google Drive", e);
        }
    }

    /**
     * Lists files in the specified subfolder (DNI or FOTO) of a member.
     */
    public List<DriveFileDto> listFiles(Long memberId, String folderType) {
        if (!isConfigured()) {
            log.warn("Google Drive Service is not configured. Returning empty file list for member {}", memberId);
            return Collections.emptyList();
        }

        try {
            String subfolderId = getSubfolderId(memberId, folderType);
            if (subfolderId == null) {
                return Collections.emptyList();
            }

            FileList result = driveService.files().list()
                    .setQ("'" + subfolderId + "' in parents and trashed = false")
                    .setSpaces("drive")
                    .setFields("files(id, name, webViewLink, thumbnailLink, size, mimeType)")
                    .setSupportsAllDrives(true)
                    .setIncludeItemsFromAllDrives(true)
                    .execute();

            List<DriveFileDto> files = new ArrayList<>();
            if (result.getFiles() != null) {
                for (File file : result.getFiles()) {
                    files.add(new DriveFileDto(
                            file.getId(),
                            file.getName(),
                            file.getWebViewLink(),
                            file.getSize() != null ? file.getSize() : 0L,
                            file.getMimeType(),
                            file.getThumbnailLink()
                    ));
                }
            }
            return files;
        } catch (IOException e) {
            log.error("Failed to list files from Google Drive for member {}", memberId, e);
            throw new RuntimeException("Error al listar los archivos de Google Drive", e);
        }
    }

    /**
     * Generates a Resumable Upload Session URL directly from Google Drive API.
     */
    public String generateUploadUrl(Long memberId, String folderType, String fileName, String contentType) {
        if (!isConfigured()) {
            log.warn("Google Drive Service is not configured. Returning dummy upload URL.");
            return "https://mock.google.drive/upload/" + fileName;
        }

        try {
            String targetFolderId = getOrCreateSubfolder(memberId, folderType);

            // Get Service Account Access Token
            String accessToken = null;
            if (googleCredentials != null) {
                googleCredentials.refreshIfExpired();
                if (googleCredentials.getAccessToken() != null) {
                    accessToken = googleCredentials.getAccessToken().getTokenValue();
                }
            }

            if (accessToken == null) {
                throw new IllegalStateException("Could not obtain Access Token for Google Drive API");
            }

            // Create metadata for the new file
            JSONObject fileMetadata = new JSONObject();
            fileMetadata.put("name", fileName);
            fileMetadata.put("parents", new String[]{targetFolderId});

            // Call raw HTTP to initiate Resumable Upload
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Upload-Content-Type", contentType);

            HttpEntity<String> entity = new HttpEntity<>(fileMetadata.toString(), headers);

            String requestUrl = "https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable&supportsAllDrives=true";
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                HttpHeaders responseHeaders = response.getHeaders();
                List<String> locationHeader = responseHeaders.get("Location");
                if (locationHeader != null && !locationHeader.isEmpty()) {
                    return locationHeader.get(0);
                }
            }

            throw new RuntimeException("Google Drive did not return a Location header for upload session. Status: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to generate Google Drive upload session URL", e);
            throw new RuntimeException("Error al generar la URL de subida de Google Drive", e);
        }
    }

    /**
     * Deletes a file from Google Drive.
     */
    public void deleteFile(String fileId) {
        if (!isConfigured()) {
            log.warn("Google Drive Service is not configured. Skipping file delete for fileId {}", fileId);
            return;
        }

        try {
            // Trashing the file (setting trashed = true) instead of permanent deletion.
            // Permanent deletion (files().delete()) requires Manager permission on Shared Drives,
            // whereas trashing (files().update()) only requires edit access (Contributor/Content Manager).
            File trashedMetadata = new File().setTrashed(true);
            driveService.files().update(fileId, trashedMetadata)
                    .setSupportsAllDrives(true)
                    .execute();
            log.info("Successfully trashed/deleted Google Drive file with ID: {}", fileId);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                log.warn("Google Drive file with ID {} was already deleted or not found.", fileId);
            } else {
                log.error("Failed to delete/trash file {} from Google Drive due to Google API error", fileId, e);
                throw new RuntimeException("Error al eliminar el archivo de Google Drive", e);
            }
        } catch (IOException e) {
            log.error("Failed to delete/trash file {} from Google Drive", fileId, e);
            throw new RuntimeException("Error al eliminar el archivo de Google Drive", e);
        }
    }

    // ================= HELPERS =================

    private String getSubfolderId(Long memberId, String folderType) throws IOException {
        String memberFolderId = getFolderId("SOCIO_" + memberId, parentFolderId);
        if (memberFolderId == null) {
            return null;
        }
        return getFolderId(folderType, memberFolderId);
    }

    private String getOrCreateSubfolder(Long memberId, String folderType) throws IOException {
        String memberFolderId = getOrCreateFolder("SOCIO_" + memberId, parentFolderId);
        return getOrCreateFolder(folderType, memberFolderId);
    }

    private String getOrCreateFolder(String name, String parentId) throws IOException {
        String folderId = getFolderId(name, parentId);
        if (folderId != null) {
            return folderId;
        }

        File fileMetadata = new File();
        fileMetadata.setName(name);
        fileMetadata.setMimeType(FOLDER_MIME);
        if (parentId != null) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        }

        File folder = driveService.files().create(fileMetadata)
                .setFields("id")
                .setSupportsAllDrives(true)
                .execute();

        return folder.getId();
    }

    private String getFolderId(String name, String parentId) throws IOException {
        String query = "name = '" + name + "' and mimeType = '" + FOLDER_MIME + "' and trashed = false";
        if (parentId != null) {
            query += " and '" + parentId + "' in parents";
        }

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id)")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .execute();

        List<File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            return files.get(0).getId();
        }
        return null;
    }

    public String getAccessToken() {
        if (googleCredentials == null) {
            return null;
        }
        try {
            googleCredentials.refreshIfExpired();
            if (googleCredentials.getAccessToken() == null) {
                googleCredentials.refresh();
            }
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            log.error("Failed to get Google Credentials access token", e);
            return null;
        }
    }

    // ================= DTOs =================

    public static class DriveFileDto {
        private String id;
        private String name;
        private String url;
        private long size;
        private String mimeType;
        private String thumbnailUrl;

        public DriveFileDto() {}

        public DriveFileDto(String id, String name, String url, long size, String mimeType) {
            this(id, name, url, size, mimeType, null);
        }

        public DriveFileDto(String id, String name, String url, long size, String mimeType, String thumbnailUrl) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.size = size;
            this.mimeType = mimeType;
            this.thumbnailUrl = thumbnailUrl;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }
}
