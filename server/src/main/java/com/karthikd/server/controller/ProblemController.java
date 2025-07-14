package com.karthikd.server.controller;

import com.karthikd.server.entity.Problem;
import com.karthikd.server.service.ProblemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/problems")
@Slf4j
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/add")
    public ResponseEntity<?> addProblem(@RequestBody Problem problem) {
        return ResponseEntity.ok(problemService.addProblem(problem));
    }

    @GetMapping("/{slug}/hidden")
    public ResponseEntity<?> getHidden(@PathVariable String slug) {
        Problem problem = problemService.getBySlug(slug);
        if (problem == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(problem.getHiddenTestCases());
    }
}
