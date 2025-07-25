package com.karthikd.server.controller;

import com.karthikd.server.entity.Role;
import com.karthikd.server.entity.User;
import com.karthikd.server.model.UserModel;
import com.karthikd.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

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

            Map<String, String> response = new HashMap<>();
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

            Map<String, String> response = new HashMap<>();
            response.put("email", user.getEmail());
            response.put("username", user.getUsername());
            response.put("role", user.getRole().toString());

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
