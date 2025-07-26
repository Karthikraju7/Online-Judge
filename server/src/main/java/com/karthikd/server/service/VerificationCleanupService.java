package com.karthikd.server.service;

import com.karthikd.server.repository.UserRepository;
import com.karthikd.server.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificationCleanupService {

    @Autowired
    private UserRepository userRepository;

    // Runs every 1 minute (for testing)
    @Scheduled(fixedRate = 600000)
    public void deleteExpiredUnverifiedUsers() {
        System.out.println("Cleanup running at: " + LocalDateTime.now());

        List<User> expiredUsers = userRepository.findAll().stream()
                .filter(u -> !u.isVerified() &&
                        u.getTokenExpiry() != null &&
                        u.getTokenExpiry().isBefore(LocalDateTime.now()))
                .toList();

        if (expiredUsers.isEmpty()) {
            System.out.println("No expired unverified users found.");
        } else {
            System.out.println("Deleting users: ");
            expiredUsers.forEach(u -> System.out.println(" - " + u.getEmail()));
            userRepository.deleteAll(expiredUsers);
        }
    }
}
