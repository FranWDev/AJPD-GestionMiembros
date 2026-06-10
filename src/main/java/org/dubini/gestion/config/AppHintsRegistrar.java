package org.dubini.gestion.config;

import java.io.Serializable;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class AppHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        registerEntity(hints, "org.dubini.gestion.model.Miembro");
        registerEntity(hints, "org.dubini.gestion.model.Centro");
        registerEntity(hints, "org.dubini.gestion.model.Cargo");
        registerEntity(hints, "org.dubini.gestion.model.HistorialCargo");

        registerException(hints, "org.dubini.gestion.exception.ResourceNotFoundException");

        registerSecurityClasses(hints);

        hints.resources().registerPattern("application*.properties");
        hints.resources().registerPattern("application*.yml");
        hints.resources().registerPattern("META-INF/**");

        registerJacksonClasses(hints);
        registerPostgresClasses(hints);

        registerClassIfExists(hints, "org.dubini.gestion.dto.LoginRequest");
        registerClassIfExists(hints, "org.dubini.gestion.dto.JwtResponse");
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

        registerClassIfExists(hints, "org.springdoc.core.configuration.SpringDocConfiguration");
        registerClassIfExists(hints, "org.springdoc.core.properties.SpringDocConfigProperties");
        registerClassIfExists(hints, "org.springdoc.core.properties.SwaggerUiConfigProperties");
        registerClassIfExists(hints, "org.dubini.gestion.config.OpenApiConfig");
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
            System.err.println("Entity not found for hints: " + className);
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
            System.err.println("Exception not found for hints: " + className);
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
            System.err.println("Security hints error: " + e.getMessage());
        }
    }

    private void registerJacksonClasses(RuntimeHints hints) {
        try {
            Class<?> objectMapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            hints.reflection().registerType(objectMapperClass, MemberCategory.values());
            Class<?> jsonNodeClass = Class.forName("com.fasterxml.jackson.databind.JsonNode");
            hints.reflection().registerType(jsonNodeClass, MemberCategory.values());
        } catch (Exception e) {
            System.err.println("Jackson hints error: " + e.getMessage());
        }
    }

    private void registerPostgresClasses(RuntimeHints hints) {
        try {
            hints.reflection().registerType(
                    Class.forName("org.postgresql.Driver"),
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
            System.err.println("PostgreSQL hints error: " + e.getMessage());
        }
    }

    private void registerClassIfExists(RuntimeHints hints, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            hints.reflection().registerType(clazz, MemberCategory.values());
        } catch (ClassNotFoundException e) {
        }
    }
}
