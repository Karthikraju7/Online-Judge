package com.karthikd.server.service;

import com.karthikd.server.dto.AddProblemRequest;
import com.karthikd.server.dto.ProblemSummaryDTO;
import com.karthikd.server.entity.Problem;
import com.karthikd.server.entity.TestCase;
import com.karthikd.server.entity.User;
import com.karthikd.server.entity.UserProblem;
import com.karthikd.server.repository.ProblemRepository;
import com.karthikd.server.repository.UserProblemRepository;
import com.karthikd.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProblemServiceImpl implements ProblemService{

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProblemRepository userProblemRepository;

    @Override
    public Problem addProblem(AddProblemRequest request) {
        Problem problem = new Problem();
        problem.setTitle(request.getTitle()); // ✅ Missing before
        problem.setSlug(request.getSlug());
        problem.setDifficulty(request.getDifficulty()); // ✅ Missing before
        problem.setDescription(request.getDescription());
        problem.setSampleInput(request.getSampleInput());
        problem.setSampleOutput(request.getSampleOutput());

        // Convert HiddenTestCaseDTOs to TestCase entities
        List<TestCase> hiddenTestCases = request.getHiddenTestCases()
                .stream()
                .map(dto -> new TestCase(null, dto.getInput(), dto.getExpectedOutput()))
                .toList();
        problem.setHiddenTestCases(hiddenTestCases);

        return problemRepository.save(problem);
    }

    @Override
    public Problem getBySlug(String slug) {
        return problemRepository.findBySlug(slug);
    }

    @Override
    public List<ProblemSummaryDTO> getAllProblemsWithSolvedStatus(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();

        List<Problem> problems = problemRepository.findAll();

        return problems.stream().map(problem -> {
            boolean isSolved = userProblemRepository
                    .findByUserAndProblem(user, problem)
                    .map(UserProblem::isSolved)
                    .orElse(false);

            return new ProblemSummaryDTO(
                    problem.getTitle(),
                    problem.getSlug(),
                    problem.getDifficulty(),
                    isSolved
            );
        }).toList();
    }


}
