package com.karthikd.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserModel {
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
}
