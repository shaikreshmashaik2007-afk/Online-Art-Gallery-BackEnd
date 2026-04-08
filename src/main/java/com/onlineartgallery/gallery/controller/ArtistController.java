package com.onlineartgallery.gallery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/artist")
public class ArtistController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_ARTIST')")
    public ResponseEntity<?> dashboard() {
        // Example payload; replace with real service calls as needed
        Map<String, Object> payload = Map.of(
                "message", "Artist dashboard data",
                "uploadedArtworksCount", 0,
                "sales", 0
        );
        return ResponseEntity.ok(payload);
    }
}
