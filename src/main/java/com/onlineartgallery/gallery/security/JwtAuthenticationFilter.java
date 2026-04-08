package com.onlineartgallery.gallery.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/payments/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Jws<Claims> claimsJws = jwtUtil.parseToken(token);
                Claims claims = claimsJws.getBody();
                String subject = claims.getSubject();
                String roleClaim = claims.get("role", String.class);

                // Normalize the role claim and ensure we provide a Spring Security authority
                // Accept either 'ROLE_ARTIST' or 'ARTIST' (or lowercase variants)
                String normalizedRole = "";
                if (roleClaim != null) {
                    String r = roleClaim.trim();
                    if (r.toUpperCase().startsWith("ROLE_")) {
                        normalizedRole = r.toUpperCase();
                    } else {
                        normalizedRole = "ROLE_" + r.toUpperCase();
                    }
                }

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        subject,
                        null,
                        normalizedRole.isEmpty() ? List.of() : List.of(new SimpleGrantedAuthority(normalizedRole))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ex) {
                // invalid token - clear context
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
