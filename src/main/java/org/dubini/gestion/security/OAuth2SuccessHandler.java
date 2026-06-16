package org.dubini.gestion.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final String frontendUrl;

    public OAuth2SuccessHandler(JwtProvider jwtProvider,
                                @Value("${app.security.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.jwtProvider = jwtProvider;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");

            if (email == null || !email.endsWith("@proyectodubini.org")) {
                String errorUrl = frontendUrl + "/login?error=" + URLEncoder.encode("El correo electrónico no está autorizado.", StandardCharsets.UTF_8);
                getRedirectStrategy().sendRedirect(request, response, errorUrl);
                return;
            }

            // Generate app token
            String token = jwtProvider.generateToken(email);
            String successUrl = frontendUrl + "/login?token=" + token;
            getRedirectStrategy().sendRedirect(request, response, successUrl);
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
