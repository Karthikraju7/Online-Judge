package com.karthikd.server.controller;

import com.karthikd.server.dto.AddProblemRequest;
import com.karthikd.server.entity.Problem;
import com.karthikd.server.repository.ProblemRepository;
import com.karthikd.server.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/problems")
@Slf4j
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @Autowired
    private ProblemRepository problemRepository;


    @PostMapping("/add")
    public ResponseEntity<?> addProblem(@RequestBody AddProblemRequest request) {
        return ResponseEntity.ok(problemService.addProblem(request));
    }

    @PostMapping("/add-full")
    public ResponseEntity<?> addProblemWithTestCases(@RequestBody AddProblemRequest request) {
        return ResponseEntity.ok(problemService.addFullProblem(request));
    }

    @GetMapping("/{slug}/hidden")
    public ResponseEntity<?> getHidden(@PathVariable String slug) {
        Problem problem = problemService.getBySlug(slug);
        if (problem == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(problem.getHiddenTestCases());
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllProblems(@RequestParam String email) {
        return ResponseEntity.ok(problemService.getAllProblemsWithSolvedStatus(email));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllProblemsForAdmin() {
        List<Problem> problems = problemRepository.findAll();
        return ResponseEntity.ok(problems);
    }



    @GetMapping("/{slug}")
    public ResponseEntity<?> getProblemBySlug(@PathVariable String slug) {
        Problem problem = problemService.getBySlug(slug);
        if (problem == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(Map.of(
                "title", problem.getTitle(),
                "slug", problem.getSlug(),
                "description", problem.getDescription(),
                "difficulty", problem.getDifficulty(),
                "sampleInput", problem.getSampleInput(),
                "sampleOutput", problem.getSampleOutput(),
                "hiddenTestCases", problem.getHiddenTestCases()
        ));
    }

    @PutMapping("/{slug}")
    public ResponseEntity<?> updateProblem(@PathVariable String slug, @RequestBody AddProblemRequest request) {
        return ResponseEntity.ok(problemService.updateProblem(slug, request));
    }


}
