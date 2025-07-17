package com.karthikd.server.service;

import com.karthikd.server.entity.Role;
import com.karthikd.server.entity.User;
import com.karthikd.server.model.UserModel;
import com.karthikd.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserModel userModel) {
        User user = new User();
        if (userRepository.findByUsername(userModel.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.findByEmail(userModel.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }
        if (!userModel.getPassword().equals(userModel.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        user.setEmail(userModel.getEmail());
        user.setUsername(userModel.getUsername());
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return user;
    }

    @Override
    public User loginAndFetchUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getRole() == Role.USER) // ✅ Only allow USER role
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    @Override
    public User loginAndFetchAdmin(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(u -> u.getRole() == Role.ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }


}
