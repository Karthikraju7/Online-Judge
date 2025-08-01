package com.karthikd.server.controller;

import com.karthikd.server.dto.ResetPasswordRequest;
import com.karthikd.server.entity.Role;
import com.karthikd.server.entity.User;
import com.karthikd.server.model.UserModel;
import com.karthikd.server.security.JwtUtil;
import com.karthikd.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserModel userModel){
        try {
            User user = userService.registerUser(userModel);

            Map<String, String> response = new HashMap<>();
            response.put("email", user.getEmail());
            response.put("username", user.getUsername());
            response.put("role", user.getRole().toString());

            return ResponseEntity.ok(response);
        }
        catch (RuntimeException ex){
            return buildError(ex.getMessage(), 400);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginAndFetchUser(@RequestBody UserModel userModel){
        try {
            User user = userService.loginAndFetchUser(userModel.getEmail(), userModel.getPassword());

            if(user.getRole() != Role.USER) {
                throw new RuntimeException("User not found");
            }

            // Generate JWT Token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());

            // Response
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("email", user.getEmail());
            response.put("username", user.getUsername());
            response.put("role", user.getRole().toString());

            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return buildError(ex.getMessage(), 400);
        }
    }

    @PostMapping("/admin-login")
    public ResponseEntity<?> loginAndFetchAdmin(@RequestBody UserModel userModel){
        try {
            User user = userService.loginAndFetchAdmin(userModel.getEmail(), userModel.getPassword());

            if(user.getRole() != Role.ADMIN) {
                throw new RuntimeException("Admin not found");
            }

            // Generate JWT Token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("email", user.getEmail());
            response.put("username", user.getUsername());
            response.put("role", user.getRole().toString());

            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return buildError(ex.getMessage(), 400);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam("token") String token) {
        try {
            User user = userService.verifyUser(token);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email verified successfully! You can now login.");
            response.put("email", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return buildError(ex.getMessage(), 400);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody Map<String, String> request) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");

            if (!newPassword.equals(confirmPassword)) {
                throw new RuntimeException("New password and confirm password do not match");
            }

            userService.changePassword(email, currentPassword, newPassword);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {
            return buildError(ex.getMessage(), 400);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            userService.initiatePasswordReset(email);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset link sent to your email.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return buildError(ex.getMessage(), 400);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("newPassword");
            userService.resetPassword(token, newPassword);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successful");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return buildError(ex.getMessage(), 400);
        }
    }


    // Utility method to build error JSON response
    private ResponseEntity<?> buildError(String message, int statusCode) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        return ResponseEntity.status(statusCode).body(error);
    }
}
