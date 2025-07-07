package com.karthikd.server.service;

import com.karthikd.server.entity.Problem;

public interface ProblemService {
    Problem addProblem(Problem problem);

    Problem getBySlug(String slug);
}
