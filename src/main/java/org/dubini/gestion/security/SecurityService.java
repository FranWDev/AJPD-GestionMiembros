package org.dubini.gestion.security;

import org.dubini.gestion.model.UserPermission;
import org.dubini.gestion.repository.UserPermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("securityService")
public class SecurityService {

    private static final Logger log = LoggerFactory.getLogger(SecurityService.class);
    private final UserPermissionRepository userPermissionRepo;

    public SecurityService(UserPermissionRepository userPermissionRepo) {
        this.userPermissionRepo = userPermissionRepo;
    }

    public boolean hasAccessToOrganization() {
        return checkPermission(UserPermissionType.ORGANIZATION);
    }

    public boolean hasAccessToWeb() {
        return checkPermission(UserPermissionType.WEB);
    }

    public boolean hasAccessToPermissions() {
        return checkPermission(UserPermissionType.PERMISSIONS);
    }

    public boolean hasAccessToFinances() {
        return checkPermission(UserPermissionType.FINANCES);
    }

    private boolean checkPermission(UserPermissionType type) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }

        // System calls / calls from other services via JWT
        if ("backoffice".equals(auth.getName())) {
            log.debug("System call from 'backoffice' allowed access to {}", type);
            return true;
        }

        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User principal = oauthToken.getPrincipal();
            if (principal != null) {
                String email = principal.getAttribute("email");
                if (email != null) {
                    return evaluateUserPermission(email, type);
                }
            }
        }

        String name = auth.getName();
        if (name != null && name.contains("@")) {
            return evaluateUserPermission(name, type);
        }

        return false;
    }

    @Cacheable(value = "userPermissions", key = "#email.toLowerCase().trim() + '_' + #type.name()")
    public boolean evaluateUserPermission(String email, UserPermissionType type) {
        String cleanEmail = email.toLowerCase().trim();
        log.debug("Evaluating permission {} for user {}", type, cleanEmail);
        Optional<UserPermission> permissionOpt = userPermissionRepo.findByEmail(cleanEmail);
        if (permissionOpt.isEmpty()) {
            // Default: Any other email has read-only access (evaluates to false for write permissions)
            return false;
        }

        UserPermission perm = permissionOpt.get();

        return switch (type) {
            case PERMISSIONS -> perm.isCanManagePermissions();
            case ORGANIZATION -> perm.isCanManageOrganization();
            case WEB -> perm.isCanManageWeb();
            case FINANCES -> perm.isCanManageFinances();
        };
    }

    public enum UserPermissionType {
        PERMISSIONS,
        ORGANIZATION,
        WEB,
        FINANCES
    }
}
