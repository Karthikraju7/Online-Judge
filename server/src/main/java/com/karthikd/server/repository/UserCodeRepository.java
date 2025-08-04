package com.karthikd.server.repository;

import com.karthikd.server.entity.User;
import com.karthikd.server.entity.UserCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCodeRepository extends JpaRepository<UserCode, Long> {
    Optional<UserCode> findByUserAndProblemSlugAndLanguage(User user, String slug, String language);
}
