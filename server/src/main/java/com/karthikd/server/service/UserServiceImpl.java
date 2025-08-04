package com.karthikd.server.service;

import com.karthikd.server.entity.Role;
import com.karthikd.server.entity.User;
import com.karthikd.server.model.UserModel;
import com.karthikd.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    public User registerUser(UserModel userModel) {
        if (userRepository.findByUsername(userModel.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.findByEmail(userModel.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }
        if (!userModel.getPassword().equals(userModel.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = new User();
        user.setEmail(userModel.getEmail());
        user.setUsername(userModel.getUsername());
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        user.setRole(Role.USER);

        // --- Email Verification ---
        user.setVerified(false);
        user.setVerificationToken(java.util.UUID.randomUUID().toString());
        user.setTokenExpiry(java.time.LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        // Send email asynchronously to avoid delays
        new Thread(() -> {
            try {
                emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

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

        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
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

    @Override
    public User verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        // Check if token expired
        if (user.getTokenExpiry() != null && user.getTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Verification token expired. Please register again.");
        }

        // Mark as verified
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        return user;
    }

    @Override
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with this email"));

        user.setResetToken(UUID.randomUUID().toString());
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        new Thread(() -> {
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getResetToken());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }


}
