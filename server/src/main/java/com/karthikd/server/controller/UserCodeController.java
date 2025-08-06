package com.karthikd.server.controller;

import com.karthikd.server.dto.CodeSaveRequest;
import com.karthikd.server.service.UserCodeService;
import com.karthikd.server.security.JwtUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/code")
public class UserCodeController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserCodeService codeService;

    @PostMapping("/save")
    public ResponseEntity<?> saveCode(@RequestHeader("Authorization") String authHeader,
                                      @RequestBody CodeSaveRequest request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(401).body("Invalid token");
            }

            codeService.saveOrUpdateCode(email, request.getSlug(), request.getLanguage(), request.getCode());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving code: " + e.getMessage());
        }
    }


    @GetMapping("/test")
    public String testAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "Hello " + auth.getName();
    }


    @GetMapping
    public ResponseEntity<?> getCode(@RequestHeader("Authorization") String authHeader,
                                     @RequestParam String slug,
                                     @RequestParam String language) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        String savedCode = codeService.getCodeForUser(email, slug, language);
        return ResponseEntity.ok(savedCode != null ? savedCode : "");
    }
}
