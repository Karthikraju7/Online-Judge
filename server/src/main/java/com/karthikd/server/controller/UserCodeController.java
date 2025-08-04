package com.karthikd.server.controller;

import com.karthikd.server.service.UserCodeService;
import com.karthikd.server.security.JwtUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        codeService.saveOrUpdateCode(email, request.getSlug(), request.getLanguage(), request.getCode());
        return ResponseEntity.ok().build();
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

    @Data
    public static class CodeSaveRequest {
        private String slug;
        private String language;
        private String code;
    }
}
