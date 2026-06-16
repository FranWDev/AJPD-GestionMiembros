package org.dubini.gestion.service;

import org.dubini.gestion.config.AccessKeyProperties;
import org.dubini.gestion.dto.JwtResponse;
import org.dubini.gestion.dto.LoginRequest;
import org.dubini.gestion.dto.UserPermissionResponse;
import org.dubini.gestion.repository.UserPermissionRepository;
import org.dubini.gestion.security.JwtProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtProvider jwtProvider;
    private final AccessKeyProperties accessKeyProperties;
    private final PasswordEncoder passwordEncoder;
    private final UserPermissionRepository userPermissionRepo;

    public AuthService(JwtProvider jwtProvider, AccessKeyProperties accessKeyProperties, PasswordEncoder passwordEncoder, UserPermissionRepository userPermissionRepo) {
        this.jwtProvider = jwtProvider;
        this.accessKeyProperties = accessKeyProperties;
        this.passwordEncoder = passwordEncoder;
        this.userPermissionRepo = userPermissionRepo;
    }

    public JwtResponse login(LoginRequest request) {
        if (request.getAccessKey() == null || !passwordEncoder.matches(request.getAccessKey(), accessKeyProperties.getAccessKey())) {
            throw new BadCredentialsException("Invalid access key");
        }
        String token = jwtProvider.generateToken();
        return new JwtResponse(token);
    }

    public UserPermissionResponse getCurrentUserPermissions(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new InsufficientAuthenticationException("User not authenticated");
        }

        String tempEmail = auth.getName();
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User principal = oauthToken.getPrincipal();
            if (principal != null && principal.getAttribute("email") != null) {
                tempEmail = principal.getAttribute("email");
            }
        }
        final String email = tempEmail != null ? tempEmail.toLowerCase().trim() : null;

        if ("backoffice".equals(email)) {
            return new UserPermissionResponse(
                    "backoffice", true, true, true, true
            );
        }

        return userPermissionRepo.findByEmail(email)
                .map(perm -> new UserPermissionResponse(
                        perm.getEmail(),
                        perm.isCanManagePermissions(),
                        perm.isCanManageOrganization(),
                        perm.isCanManageWeb(),
                        perm.isCanManageFinances()
                ))
                .orElseGet(() -> new UserPermissionResponse(
                        email, false, false, false, false
                ));
    }
}
