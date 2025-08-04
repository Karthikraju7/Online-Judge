package com.karthikd.server.service;

import com.karthikd.server.dto.AddProblemRequest;
import com.karthikd.server.dto.HiddenTestCaseDTO;
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
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public String addFullProblem(AddProblemRequest request) {
        Problem problem = new Problem();
        problem.setTitle(request.getTitle());
        problem.setSlug(request.getSlug());
        problem.setDifficulty(request.getDifficulty());
        problem.setDescription(request.getDescription());
        problem.setSampleInput(request.getSampleInput());
        problem.setSampleOutput(request.getSampleOutput());

        List<TestCase> hiddenTestCases = request.getHiddenTestCases()
                .stream()
                .map(dto -> new TestCase(null, dto.getInput(), dto.getExpectedOutput()))
                .toList();
        problem.setHiddenTestCases(hiddenTestCases);

        problemRepository.save(problem);

        return "Problem with test cases added successfully";
    }

    @Override
    @Transactional
    public String updateProblem(String slug, AddProblemRequest request) {
        Problem existing = problemRepository.findBySlug(slug);
        if (existing == null) {
            throw new RuntimeException("Problem not found with slug: " + slug);
        }

        // Update main fields
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setDifficulty(request.getDifficulty());
        existing.setSampleInput(request.getSampleInput());
        existing.setSampleOutput(request.getSampleOutput());

        // Map existing test cases by ID
        Map<Long, TestCase> existingMap = existing.getHiddenTestCases().stream()
                .filter(tc -> tc.getId() != null)
                .collect(Collectors.toMap(TestCase::getId, tc -> tc));

        Set<Long> incomingIds = new HashSet<>();

        for (HiddenTestCaseDTO dto : request.getHiddenTestCases()) {
            if (dto.getId() != null && existingMap.containsKey(dto.getId())) {
                // Update existing
                TestCase tc = existingMap.get(dto.getId());
                tc.setInput(dto.getInput());
                tc.setExpectedOutput(dto.getExpectedOutput());
                incomingIds.add(dto.getId());
            } else {
                // Add new
                TestCase newTc = new TestCase();
                newTc.setInput(dto.getInput());
                newTc.setExpectedOutput(dto.getExpectedOutput());
                existing.getHiddenTestCases().add(newTc);
            }
        }

        existing.getHiddenTestCases().removeIf(tc ->
                tc.getId() != null && !incomingIds.contains(tc.getId())
        );

        problemRepository.save(existing);
        return "Problem updated successfully";
    }
}
