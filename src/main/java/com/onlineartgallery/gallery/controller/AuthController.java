package com.onlineartgallery.gallery.controller;

import com.onlineartgallery.gallery.dto.AuthRequest;
import com.onlineartgallery.gallery.dto.AuthResponse;
import com.onlineartgallery.gallery.dto.SignupRequest;
import com.onlineartgallery.gallery.dto.ErrorResponse;
import com.onlineartgallery.gallery.model.User;
import com.onlineartgallery.gallery.security.JwtUtil;
import com.onlineartgallery.gallery.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        try {
            // Validate required fields
            if (req.getName() == null || req.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Name is required"));
            }
            if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Email is required"));
            }
            if (req.getPassword() == null || req.getPassword().length() < 6) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Password must be at least 6 characters"));
            }
            if (!req.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid email format"));
            }

            // Check if email exists
            if (userService.findByEmail(req.getEmail()).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Email already in use"));
            }

            // Process registration
            try {
                User user = new User();
                user.setName(req.getName().trim());
                user.setEmail(req.getEmail().trim().toLowerCase());
                user.setPassword(req.getPassword());  // UserService will handle password encoding
                
                String requestRole = req.getRole();
                System.out.println("Received role in request: " + requestRole);

                // Validate and normalize role
                String role = requestRole != null ? requestRole.toUpperCase() : "CUSTOMER";
                if (!Arrays.asList("ARTIST", "CUSTOMER", "ADMIN").contains(role)) {
                    return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Invalid role specified"));
                }
                
                System.out.println("Normalized role for storage: " + role);
                user.setRole(role);

                User saved = userService.createUser(user);
                String token = jwtUtil.generateToken(saved.getEmail(), saved.getRole());

                AuthResponse resp = new AuthResponse();
                resp.setId(saved.getId());
                resp.setName(saved.getName());
                resp.setEmail(saved.getEmail());
                resp.setRole(saved.getRole());
                resp.setCreatedAt(saved.getCreatedAt());
                resp.setToken(token);

                return ResponseEntity.created(URI.create("/api/users/" + saved.getId()))
                    .body(resp);
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Registration failed: " + e.getMessage()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Invalid request: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        return userService.findByEmail(req.getEmail()).map(user -> {
            if (passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                AuthResponse resp = new AuthResponse();
                resp.setId(user.getId());
                resp.setName(user.getName());
                resp.setEmail(user.getEmail());
                resp.setRole(user.getRole());
                resp.setCreatedAt(user.getCreatedAt());
                resp.setToken(token);
                return ResponseEntity.ok(resp);
            }
            return ResponseEntity.status(401).body("Invalid credentials");
        }).orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String email = (String) authentication.getPrincipal();
        return userService.findByEmail(email).map(user -> {
            AuthResponse resp = new AuthResponse();
            resp.setId(user.getId());
            resp.setName(user.getName());
            resp.setEmail(user.getEmail());
            resp.setRole(user.getRole());
            resp.setCreatedAt(user.getCreatedAt());
            return ResponseEntity.ok(resp);
        }).orElseGet(() -> ResponseEntity.status(404).build());
    }
}
