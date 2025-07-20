package com.karthikd.server.repository;

import com.karthikd.server.entity.UserProblem;
import com.karthikd.server.entity.User;
import com.karthikd.server.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProblemRepository extends JpaRepository<UserProblem, Long> {
    Optional<UserProblem> findByUserAndProblem(User user, Problem problem);
}
