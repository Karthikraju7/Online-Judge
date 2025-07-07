package com.karthikd.server.repository;

import com.karthikd.server.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Problem findBySlug(String slug);
}
