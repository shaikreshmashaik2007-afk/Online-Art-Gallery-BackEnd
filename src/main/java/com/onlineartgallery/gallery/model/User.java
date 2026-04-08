package com.onlineartgallery.gallery.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Arrays;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    private Instant createdAt = Instant.now();

    @PrePersist
    @PreUpdate
    public void validateRole() {
        if (role == null || role.trim().isEmpty()) {
            role = "CUSTOMER"; // Default role
        } else {
            role = role.toUpperCase().trim();
            if (!Arrays.asList("CUSTOMER", "ARTIST", "ADMIN").contains(role)) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        }
    }

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
