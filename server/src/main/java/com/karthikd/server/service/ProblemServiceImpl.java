package com.karthikd.server.service;

import com.karthikd.server.entity.Problem;
import com.karthikd.server.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProblemServiceImpl implements ProblemService{

    @Autowired
    private ProblemRepository problemRepository;

    @Override
    public Problem addProblem(Problem problem) {
        return problemRepository.save(problem);
    }

    @Override
    public Problem getBySlug(String slug) {
        return problemRepository.findBySlug(slug);
    }
}
