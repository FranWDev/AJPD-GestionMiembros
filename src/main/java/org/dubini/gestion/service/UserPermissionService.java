package org.dubini.gestion.service;

import org.dubini.gestion.exception.BusinessRuleException;
import org.dubini.gestion.exception.ResourceNotFoundException;
import org.dubini.gestion.model.UserPermission;
import org.dubini.gestion.repository.UserPermissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserPermissionService {

    private final UserPermissionRepository userPermissionRepo;

    public UserPermissionService(UserPermissionRepository userPermissionRepo) {
        this.userPermissionRepo = userPermissionRepo;
    }

    public Page<UserPermission> getPermissions(String email, Pageable pageable) {
        if (email != null && !email.trim().isEmpty()) {
            return userPermissionRepo.findByEmailContainingIgnoreCase(email.trim(), pageable);
        }
        return userPermissionRepo.findAll(pageable);
    }

    public UserPermission createPermission(UserPermission permission) {
        if (permission.getEmail() == null || !permission.getEmail().trim().endsWith("@proyectodubini.org")) {
            throw new BusinessRuleException("El correo electrónico debe pertenecer al dominio @proyectodubini.org");
        }
        String cleanEmail = permission.getEmail().trim().toLowerCase();
        if (userPermissionRepo.findByEmail(cleanEmail).isPresent()) {
            throw new BusinessRuleException("Ya existe una configuración de permisos para el correo " + cleanEmail);
        }
        permission.setEmail(cleanEmail);
        return userPermissionRepo.save(permission);
    }

    public UserPermission updatePermission(Long id, UserPermission permissionDetails) {
        UserPermission permission = userPermissionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración de permisos con id " + id));

        // Validación: si se quita el permiso de administrar permisos, verificar que quede al menos otro administrador
        if (permission.isCanManagePermissions() && !permissionDetails.isCanManagePermissions()) {
            long adminCount = userPermissionRepo.findAll().stream()
                    .filter(UserPermission::isCanManagePermissions)
                    .count();
            if (adminCount <= 1) {
                throw new BusinessRuleException("No se puede revocar el permiso de administración ya que debe existir al menos un administrador de permisos activo.");
            }
        }

        permission.setCanManagePermissions(permissionDetails.isCanManagePermissions());
        permission.setCanManageOrganization(permissionDetails.isCanManageOrganization());
        permission.setCanManageWeb(permissionDetails.isCanManageWeb());
        permission.setCanManageFinances(permissionDetails.isCanManageFinances());

        return userPermissionRepo.save(permission);
    }

    public void deletePermission(Long id) {
        UserPermission permission = userPermissionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la configuración de permisos con id " + id));

        // Validación: si se elimina un administrador, verificar que quede al menos otro
        if (permission.isCanManagePermissions()) {
            long adminCount = userPermissionRepo.findAll().stream()
                    .filter(UserPermission::isCanManagePermissions)
                    .count();
            if (adminCount <= 1) {
                throw new BusinessRuleException("No se puede eliminar al único administrador de permisos.");
            }
        }

        userPermissionRepo.delete(permission);
    }
}
