package org.dubini.gestion.config;

import java.io.Serializable;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppHintsRegistrar implements RuntimeHintsRegistrar {

    private static final Logger log = LoggerFactory.getLogger(AppHintsRegistrar.class);

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        registerEntity(hints, "org.dubini.gestion.model.Miembro");
        registerEntity(hints, "org.dubini.gestion.model.Centro");
        registerEntity(hints, "org.dubini.gestion.model.Cargo");
        registerEntity(hints, "org.dubini.gestion.model.HistorialCargo");
        registerEntity(hints, "org.dubini.gestion.model.News");

        registerException(hints, "org.dubini.gestion.exception.ResourceNotFoundException");

        registerSecurityClasses(hints);

        hints.resources().registerPattern("application*.properties");
        hints.resources().registerPattern("application*.yml");
        hints.resources().registerPattern("META-INF/**");

        registerJacksonClasses(hints);
        registerPostgresClasses(hints);

        registerClassIfExists(hints, "org.dubini.gestion.dto.LoginRequest");
        registerClassIfExists(hints, "org.dubini.gestion.dto.JwtResponse");
        registerClassIfExists(hints, "org.dubini.gestion.dto.UserPermissionResponse");
        registerClassIfExists(hints, "org.dubini.gestion.controller.AuthController");
        registerClassIfExists(hints, "org.dubini.gestion.service.AuthService");

        registerClassIfExists(hints, "org.dubini.gestion.dto.CentroDto");
        registerClassIfExists(hints, "org.dubini.gestion.dto.CargoDto");
        registerClassIfExists(hints, "org.dubini.gestion.dto.MiembroRequestDto");
        registerClassIfExists(hints, "org.dubini.gestion.dto.MiembroResponseDto");
        registerClassIfExists(hints, "org.dubini.gestion.dto.HistorialCargoDto");
        registerClassIfExists(hints, "org.dubini.gestion.dto.DtoMapper");
        registerClassIfExists(hints, "org.dubini.gestion.controller.CentroController");
        registerClassIfExists(hints, "org.dubini.gestion.controller.CargoController");
        registerClassIfExists(hints, "org.dubini.gestion.controller.MiembroController");
        registerClassIfExists(hints, "org.dubini.gestion.service.CentroService");
        registerClassIfExists(hints, "org.dubini.gestion.service.CargoService");
        registerClassIfExists(hints, "org.dubini.gestion.service.MiembroService");
        registerClassIfExists(hints, "org.dubini.gestion.exception.BusinessRuleException");
        registerClassIfExists(hints, "org.dubini.gestion.validation.ValidNifCif");
        registerClassIfExists(hints, "org.dubini.gestion.validation.NifCifValidator");
        registerClassIfExists(hints, "org.dubini.gestion.dto.CargoHistorialDto");
        registerClassIfExists(hints, "org.dubini.gestion.dto.CargoHistorialEditDto");
        registerClassIfExists(hints, "org.dubini.gestion.dto.MiembroFiltro");
        registerClassIfExists(hints, "org.dubini.gestion.dto.EditorJSBlock");
        registerClassIfExists(hints, "org.dubini.gestion.dto.EditorJSContentDTO");
        registerClassIfExists(hints, "org.dubini.gestion.dto.PublicationDTO");
        registerClassIfExists(hints, "org.dubini.gestion.dto.response.HttpResponse");
        registerClassIfExists(hints, "org.dubini.gestion.config.PGobjectToStringConverter");
        registerClassIfExists(hints, "org.dubini.gestion.config.PostgresJsonbWritingConverter");
        registerClassIfExists(hints, "org.dubini.gestion.config.JdbcConfiguration");
        registerClassIfExists(hints, "org.dubini.gestion.config.FrontendApiUrlProperties");
        registerClassIfExists(hints, "org.dubini.gestion.client.CacheInvalidationClient");
        registerClassIfExists(hints, "org.dubini.gestion.service.CacheInvalidatorService");
        registerClassIfExists(hints, "org.dubini.gestion.service.NewsService");
        registerClassIfExists(hints, "org.dubini.gestion.controller.NewsController");
        registerClassIfExists(hints, "org.dubini.gestion.controller.CacheInvalidatorController");
        registerClassIfExists(hints, "org.dubini.gestion.dto.response.EditorJSImageResponseDTO");
        registerClassIfExists(hints, "org.dubini.gestion.dto.response.EditorJSImageResponseDTO$FileData");
        registerClassIfExists(hints, "org.dubini.gestion.dto.response.ImageResponseDTO");
        registerClassIfExists(hints, "org.dubini.gestion.config.SupabaseStorageProperties");
        registerClassIfExists(hints, "org.dubini.gestion.service.ImageService");
        registerClassIfExists(hints, "org.dubini.gestion.controller.ImageUploadController");
        registerClassIfExists(hints, "org.dubini.gestion.service.HeroImageService");
        registerClassIfExists(hints, "org.dubini.gestion.controller.HeroImageController");
        registerClassIfExists(hints, "org.dubini.gestion.controller.HeroImageController$HeroImageUrlResponse");
        registerClassIfExists(hints, "org.dubini.gestion.service.SliderImageService");
        registerClassIfExists(hints, "org.dubini.gestion.controller.SliderImageController");
        registerClassIfExists(hints, "org.dubini.gestion.controller.SliderImageController$SliderCaptionRequest");
        registerClassIfExists(hints, "org.dubini.gestion.controller.SliderImageController$SliderCaptionUpdateResponse");
        registerClassIfExists(hints, "org.dubini.gestion.controller.SliderImageController$SliderInfoResponse");
        registerClassIfExists(hints, "org.dubini.gestion.controller.SliderImageController$SliderImageUrlResponse");
        registerClassIfExists(hints, "org.dubini.gestion.controller.SliderImageController$SliderCaptionResponse");
        registerClassIfExists(hints, "org.dubini.gestion.controller.HeartbeatController");

        // Google Drive and Permissions dynamic integration
        registerEntity(hints, "org.dubini.gestion.model.UserPermission");
        registerClassIfExists(hints, "org.dubini.gestion.repository.UserPermissionRepository");
        registerClassIfExists(hints, "org.dubini.gestion.security.SecurityService");
        registerClassIfExists(hints, "org.dubini.gestion.config.GoogleDriveConfig");
        registerClassIfExists(hints, "org.dubini.gestion.service.GoogleDriveService");
        registerClassIfExists(hints, "org.dubini.gestion.service.GoogleDriveService$DriveFileDto");
        registerClassIfExists(hints, "org.dubini.gestion.controller.MiembroDocumentoController");
        registerClassIfExists(hints, "org.dubini.gestion.controller.UserPermissionController");
        registerClassIfExists(hints, "org.dubini.gestion.service.UserPermissionService");

        // Google SDK models and client base classes
        String[] googleModelClasses = {
            "com.google.api.client.json.GenericJson",
            "com.google.api.client.util.GenericData",
            "com.google.api.client.http.GenericUrl",
            "com.google.api.services.drive.Drive",
            "com.google.api.services.drive.model.Drive",
            "com.google.api.services.drive.model.Comment$QuotedFileContent",
            "com.google.api.services.drive.model.Reply",
            "com.google.api.services.drive.model.LabelModification",
            "com.google.api.services.drive.model.File$LabelInfo",
            "com.google.api.services.drive.model.File$ShortcutDetails",
            "com.google.api.services.drive.model.Revision",
            "com.google.api.services.drive.model.Change",
            "com.google.api.services.drive.model.CommentList",
            "com.google.api.services.drive.model.File$Capabilities",
            "com.google.api.services.drive.model.TeamDrive$Capabilities",
            "com.google.api.services.drive.model.Drive$Restrictions",
            "com.google.api.services.drive.model.File$LinkShareMetadata",
            "com.google.api.services.drive.model.TeamDrive$Restrictions",
            "com.google.api.services.drive.model.LabelFieldModification",
            "com.google.api.services.drive.model.About$StorageQuota",
            "com.google.api.services.drive.model.StartPageToken",
            "com.google.api.services.drive.model.PermissionList",
            "com.google.api.services.drive.model.Label",
            "com.google.api.services.drive.model.Drive$Capabilities",
            "com.google.api.services.drive.model.Channel",
            "com.google.api.services.drive.model.TeamDrive",
            "com.google.api.services.drive.model.Permission",
            "com.google.api.services.drive.model.Permission$TeamDrivePermissionDetails",
            "com.google.api.services.drive.model.DriveList",
            "com.google.api.services.drive.model.About$DriveThemes",
            "com.google.api.services.drive.model.File$ContentHints$Thumbnail",
            "com.google.api.services.drive.model.GeneratedIds",
            "com.google.api.services.drive.model.ChangeList",
            "com.google.api.services.drive.model.LabelList",
            "com.google.api.services.drive.model.ModifyLabelsRequest",
            "com.google.api.services.drive.model.About",
            "com.google.api.services.drive.model.File$ImageMediaMetadata$Location",
            "com.google.api.services.drive.model.Permission$PermissionDetails",
            "com.google.api.services.drive.model.ContentRestriction",
            "com.google.api.services.drive.model.User",
            "com.google.api.services.drive.model.Comment",
            "com.google.api.services.drive.model.File$ContentHints",
            "com.google.api.services.drive.model.File$VideoMediaMetadata",
            "com.google.api.services.drive.model.Drive$BackgroundImageFile",
            "com.google.api.services.drive.model.ModifyLabelsResponse",
            "com.google.api.services.drive.model.File",
            "com.google.api.services.drive.model.TeamDriveList",
            "com.google.api.services.drive.model.TeamDrive$BackgroundImageFile",
            "com.google.api.services.drive.model.LabelField",
            "com.google.api.services.drive.model.File$ImageMediaMetadata",
            "com.google.api.services.drive.model.ReplyList",
            "com.google.api.services.drive.model.RevisionList",
            "com.google.api.services.drive.model.About$TeamDriveThemes",
            "com.google.api.services.drive.model.FileList",
            // Google API Client JSON and Error Models
            "com.google.api.client.googleapis.json.GoogleJsonError",
            "com.google.api.client.googleapis.json.GoogleJsonError$ErrorInfo",
            "com.google.api.client.googleapis.json.GoogleJsonError$Details",
            "com.google.api.client.googleapis.json.GoogleJsonError$ParameterViolations",
            "com.google.api.client.googleapis.json.GoogleJsonErrorContainer",
            "com.google.api.client.googleapis.json.GoogleJsonResponseException"
        };
        for (String className : googleModelClasses) {
            registerClassIfExists(hints, className);
        }

        registerClassIfExists(hints, "org.springdoc.core.configuration.SpringDocConfiguration");
        registerClassIfExists(hints, "org.springdoc.core.properties.SpringDocConfigProperties");
        registerClassIfExists(hints, "org.springdoc.core.properties.SwaggerUiConfigProperties");
        registerClassIfExists(hints, "org.dubini.gestion.config.OpenApiConfig");
        registerClassIfExists(hints, "org.dubini.gestion.config.CacheConfig");
    }

    private void registerEntity(RuntimeHints hints, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            hints.reflection().registerType(
                    clazz,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.DECLARED_FIELDS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            if (Serializable.class.isAssignableFrom(clazz)) {
                hints.serialization().registerType((Class<? extends Serializable>) clazz);
            }
        } catch (ClassNotFoundException e) {
            log.warn("Entity not found for hints: {}", className, e);
        }
    }

    private void registerException(RuntimeHints hints, String className) {
        try {
            hints.reflection().registerType(
                    Class.forName(className),
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.DECLARED_FIELDS);
        } catch (ClassNotFoundException e) {
            log.warn("Exception not found for hints: {}", className, e);
        }
    }

    private void registerSecurityClasses(RuntimeHints hints) {
        try {
            hints.reflection().registerType(
                    User.class,
                    MemberCategory.values());
            hints.reflection().registerType(
                    SimpleGrantedAuthority.class,
                    MemberCategory.values());

            registerClassIfExists(hints, "org.dubini.gestion.security.JwtFilter");
            registerClassIfExists(hints, "org.dubini.gestion.security.JwtProvider");
        } catch (Exception e) {
            log.warn("Security hints error: {}", e.getMessage(), e);
        }
    }

    private void registerJacksonClasses(RuntimeHints hints) {
        try {
            Class<?> objectMapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            hints.reflection().registerType(objectMapperClass, MemberCategory.values());
            Class<?> jsonNodeClass = Class.forName("com.fasterxml.jackson.databind.JsonNode");
            hints.reflection().registerType(jsonNodeClass, MemberCategory.values());
        } catch (Exception e) {
            log.warn("Jackson hints error: {}", e.getMessage(), e);
        }
    }

    private void registerPostgresClasses(RuntimeHints hints) {
        try {
            hints.reflection().registerType(
                    org.springframework.aot.hint.TypeReference.of("org.postgresql.Driver"),
                    MemberCategory.values());
            hints.reflection().registerType(
                    Class.forName("org.postgresql.util.PGobject"),
                    MemberCategory.values());

            registerClassIfExists(hints, "org.postgresql.jdbc.PgConnection");
            registerClassIfExists(hints, "org.postgresql.jdbc.PgStatement");
            registerClassIfExists(hints, "org.postgresql.jdbc.PgPreparedStatement");
            
            registerClassIfExists(hints, "org.springframework.data.convert.ReadingConverter");
            registerClassIfExists(hints, "org.springframework.data.convert.WritingConverter");
            registerClassIfExists(hints, "org.springframework.core.convert.converter.Converter");
        } catch (ClassNotFoundException e) {
            log.warn("PostgreSQL hints error: {}", e.getMessage(), e);
        }
    }

    private void registerClassIfExists(RuntimeHints hints, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            hints.reflection().registerType(clazz, MemberCategory.values());
        } catch (ClassNotFoundException e) {
            // Class is optional or not present on the classpath, ignore registering hints
        }
    }
}
