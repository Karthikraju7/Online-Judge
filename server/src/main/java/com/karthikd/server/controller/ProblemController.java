package com.karthikd.server.controller;

import com.karthikd.server.entity.Problem;
import com.karthikd.server.model.RunRequest;
import com.karthikd.server.service.ProblemService;
import com.karthikd.server.util.LanguageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/problems")
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

    @PostMapping("/run")
    public ResponseEntity<?> runCode(@RequestBody RunRequest request) {
        Integer langId = LanguageMapper.LANG_ID.get(request.getLanguage().toLowerCase());
        if (langId == null) {
            return ResponseEntity.badRequest().body("Unsupported language");
        }

        // Next step: Call Judge0 API with langId, code, input
        return ResponseEntity.ok("Language ID: " + langId); // test only
    }

}
