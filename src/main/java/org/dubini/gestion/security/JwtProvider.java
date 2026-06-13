package org.dubini.gestion.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.dubini.gestion.config.JwtProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

    private final SecretKey key;
    private final long jwtExpirationMs;

    public JwtProvider(JwtProperties props) {
        String secret = props.getJwtSecret();
        SecretKey computedKey;
        try {
            computedKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        } catch (IllegalArgumentException e) {
            computedKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        this.key = computedKey;
        this.jwtExpirationMs = props.getJwtExpirationMs() > 0 ? props.getJwtExpirationMs() : 86400000L;
    }

    public String generateToken() {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("backoffice")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtExpirationMs)))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            Date expiration = claims.getExpiration();
            Instant now = Instant.now();

            return "backoffice".equals(subject) && expiration != null && expiration.toInstant().isAfter(now);
        } catch (ExpiredJwtException e) {
            log.error("ERROR: Token expired: {}", e.getMessage(), e);
            return false;
        } catch (MalformedJwtException e) {
            log.error("ERROR: Malformed JWT token: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("ERROR: Token validation failed: {}", e.getMessage(), e);
            return false;
        }
    }
}
