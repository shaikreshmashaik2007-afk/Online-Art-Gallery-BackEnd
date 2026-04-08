package com.onlineartgallery.gallery.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;
    private static final Set<String> VALID_ROLES = new HashSet<>(Arrays.asList("CUSTOMER", "ARTIST", "ADMIN"));

    public JwtUtil(@Value("${jwt.secret:defaultsecretkeydefaultsecretkey}") String secret,
                   @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        // Ensure the secret key is at least 256 bits (32 bytes)
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 characters long");
        }
        
        // Generate a secure key from the secret
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(String subject, String role) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
        }
        
        // Normalize and validate role
        String normalizedRole = normalizeRole(role);
        if (!VALID_ROLES.contains(normalizedRole)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        
        return Jwts.builder()
                .subject(subject)
                .claim("role", normalizedRole)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Jws<Claims> parseToken(String token) throws JwtException {
        if (token == null || token.trim().isEmpty()) {
            throw new JwtException("Token cannot be null or empty");
        }
        
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token has expired", e);
        } catch (MalformedJwtException e) {
            throw new JwtException("Invalid token format", e);
        } catch (UnsupportedJwtException e) {
            throw new JwtException("Unsupported token type", e);
        } catch (IllegalArgumentException e) {
            throw new JwtException("Token claims string is empty", e);
        }
    }

    private String normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        
        // Remove ROLE_ prefix if present and convert to uppercase
        String normalizedRole = role.toUpperCase()
                .replace("ROLE_", "")
                .trim();
        
        return normalizedRole;
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token).getPayload();
        return claims.get("role", String.class);
    }
}
