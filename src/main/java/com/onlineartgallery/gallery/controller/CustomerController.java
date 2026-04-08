package com.onlineartgallery.gallery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<?> dashboard() {
        Map<String, Object> payload = Map.of(
                "message", "Customer dashboard data",
                "featuredArtworksCount", 0,
                "purchaseHistoryCount", 0
        );
        return ResponseEntity.ok(payload);
    }
}
