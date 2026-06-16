package org.dubini.gestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.dubini.gestion.model.UserPermission;
import org.dubini.gestion.service.UserPermissionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/permissions")
@Tag(name = "Permisos de Usuario", description = "Endpoints para la gestión de permisos y roles de los usuarios")
@PreAuthorize("@securityService.hasAccessToPermissions()")
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    public UserPermissionController(UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los permisos de usuarios de forma paginada y filtrada")
    public ResponseEntity<Page<UserPermission>> getPermissions(
            @RequestParam(required = false) String email,
            Pageable pageable) {
        return ResponseEntity.ok(userPermissionService.getPermissions(email, pageable));
    }

    @PostMapping
    @Operation(summary = "Crear permisos para un nuevo usuario")
    @CacheEvict(value = "userPermissions", allEntries = true)
    public UserPermission createPermission(@RequestBody UserPermission permission) {
        return userPermissionService.createPermission(permission);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar permisos de un usuario")
    @CacheEvict(value = "userPermissions", allEntries = true)
    public UserPermission updatePermission(@PathVariable Long id, @RequestBody UserPermission permissionDetails) {
        return userPermissionService.updatePermission(id, permissionDetails);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar configuración de permisos de un usuario")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        userPermissionService.deletePermission(id);
        evictCache();
        return ResponseEntity.noContent().build();
    }

    @CacheEvict(value = "userPermissions", allEntries = true)
    public void evictCache() {
        // Method solely for CacheEvict of all entries
    }
}
