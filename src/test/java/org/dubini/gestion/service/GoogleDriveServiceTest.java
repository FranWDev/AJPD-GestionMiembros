package org.dubini.gestion.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.google.auth.oauth2.GoogleCredentials;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GoogleDriveServiceTest {

    @Mock
    private Drive driveService;

    @Mock
    private Drive.Files filesMock;

    @Mock
    private Drive.Files.List listMock;

    @Mock
    private Drive.Files.Create createMock;

    @Mock
    private Drive.Files.Delete deleteMock;

    @Mock
    private GoogleCredentials googleCredentials;

    private GoogleDriveService googleDriveService;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        googleDriveService = new GoogleDriveService(Optional.of(driveService), Optional.of(googleCredentials));
        ReflectionTestUtils.setField(googleDriveService, "parentFolderId", "parent-folder-id");

        when(driveService.files()).thenReturn(filesMock);
        when(filesMock.list()).thenReturn(listMock);
        when(filesMock.create(any(File.class))).thenReturn(createMock);
        when(filesMock.delete(anyString())).thenReturn(deleteMock);

        // Configure lists to return empty by default
        when(listMock.setQ(anyString())).thenReturn(listMock);
        when(listMock.setSpaces(anyString())).thenReturn(listMock);
        when(listMock.setFields(anyString())).thenReturn(listMock);
        
        FileList emptyFileList = new FileList();
        emptyFileList.setFiles(Collections.emptyList());
        when(listMock.execute()).thenReturn(emptyFileList);

        // Configure create to return a dummy file with ID
        File dummyFile = new File();
        dummyFile.setId("dummy-id");
        when(createMock.setFields(anyString())).thenReturn(createMock);
        when(createMock.execute()).thenReturn(dummyFile);
    }

    @Test
    void testCreateMemberFolders() throws IOException {
        googleDriveService.createMemberFolders(123L);

        // Verify that create was called for SOCIO_123, DNI, and FOTO (3 folders total)
        verify(filesMock, times(3)).create(any(File.class));
        verify(createMock, times(3)).execute();
    }

    @Test
    void testDeleteFile() throws IOException {
        googleDriveService.deleteFile("file-id-123");

        verify(filesMock, times(1)).delete("file-id-123");
        verify(deleteMock, times(1)).execute();
    }

    @Test
    void testListFiles() throws IOException {
        // Mock subfolder searches
        // 1. SOCIO_123 search
        FileList socioList = new FileList();
        File socioFolder = new File();
        socioFolder.setId("socio-folder-id");
        socioList.setFiles(List.of(socioFolder));

        // 2. DNI search
        FileList dniList = new FileList();
        File dniFolder = new File();
        dniFolder.setId("dni-folder-id");
        dniList.setFiles(List.of(dniFolder));

        // 3. Files inside DNI search
        FileList filesList = new FileList();
        File file1 = new File();
        file1.setId("file1-id");
        file1.setName("DNI-1.pdf");
        file1.setWebViewLink("https://view/file1");
        file1.setSize(1024L);
        file1.setMimeType("application/pdf");
        filesList.setFiles(List.of(file1));

        // Stub execute to return sequentially
        when(listMock.execute()).thenReturn(socioList, dniList, filesList);

        List<GoogleDriveService.DriveFileDto> result = googleDriveService.listFiles(123L, "DNI");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DNI-1.pdf", result.get(0).getName());
        assertEquals("file1-id", result.get(0).getId());
        assertEquals("https://view/file1", result.get(0).getUrl());
    }
}
