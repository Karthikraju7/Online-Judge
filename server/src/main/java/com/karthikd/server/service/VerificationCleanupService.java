package com.karthikd.server.service;

import com.karthikd.server.repository.UserRepository;
import com.karthikd.server.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VerificationCleanupService {

    @Autowired
    private UserRepository userRepository;

    @Scheduled(fixedRate = 600000)
    public void deleteExpiredUnverifiedUsers() {
        log.info("Cleanup running at: {}", LocalDateTime.now());

        List<User> expiredUsers = userRepository.findAll().stream()
                .filter(u -> !u.isVerified() &&
                        u.getTokenExpiry() != null &&
                        u.getTokenExpiry().isBefore(LocalDateTime.now()))
                .toList();

        if (expiredUsers.isEmpty()) {
            log.info("No expired unverified users found.");
        } else {
            log.info("Deleting users:");
            expiredUsers.forEach(u -> System.out.println(" - " + u.getEmail()));
            userRepository.deleteAll(expiredUsers);
        }
    }
}
