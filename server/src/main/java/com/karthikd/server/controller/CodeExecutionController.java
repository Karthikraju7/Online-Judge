package com.karthikd.server.controller;

import com.karthikd.server.entity.Problem;
import com.karthikd.server.entity.TestCase;
import com.karthikd.server.entity.User;
import com.karthikd.server.entity.UserProblem;
import com.karthikd.server.repository.ProblemRepository;
import com.karthikd.server.repository.UserProblemRepository;
import com.karthikd.server.repository.UserRepository;
import com.karthikd.server.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.karthikd.server.util.CppWrapper.wrapCppCode;
import static com.karthikd.server.util.JavaWrapper.wrapJavaCode;
import static com.karthikd.server.util.PythonWrapper.wrapPythonCode;

@RestController
@RequestMapping("/problems")
@CrossOrigin(origins = "*")
public class CodeExecutionController {

    @Autowired
    private CodeExecutionService executionService;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProblemRepository userProblemRepository;

    // RUN
    @PostMapping("/run")
    public Map<String, String> runCode(@RequestBody Map<String, String> request) {
        try {
            String rawCode = request.get("code");
            String input = request.get("input");
            String language = request.get("language");
            String slug = request.get("slug");

            String finalCode = switch (language) {
                case "python" -> wrapPythonCode(rawCode, slug);
                case "cpp" -> wrapCppCode(rawCode, slug);
                case "java" -> wrapJavaCode(rawCode, slug);
                default -> rawCode;
            };

            String identifier = "temp_user_" + slug + "_" + language;

            Map<String, String> result = executionService.runCode(language, finalCode, input, identifier);

            if (result == null || !result.containsKey("output")) {
                return Map.of("output", "❌ Execution failed", "verdict", "❌ Error");
            }

            String output = result.get("output").trim();
            long timeUsed = Long.parseLong(result.getOrDefault("timeUsed", "0").replace("ms", ""));
            long memoryUsed = Long.parseLong(result.getOrDefault("memoryUsed", "0").replace("KB", ""));

            if (timeUsed < 0) return Map.of("output", "❌ Time Limit Exceeded", "verdict", "❌ TLE");
            if (memoryUsed < 0) return Map.of("output", "❌ Memory Limit Exceeded", "verdict", "❌ MLE");

            Problem problem = problemRepository.findBySlug(slug);
            String expected = problem.getSampleOutput().trim();

            String verdict = output.equals(expected) ? "✅ Correct" : "❌ Incorrect";

            return Map.of(
                    "output", output,
                    "verdict", verdict,
                    "timeUsed", timeUsed + "ms",
                    "memoryUsed", memoryUsed + "KB"
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "output", "❌ Server error: " + e.getMessage(),
                    "verdict", "❌ Error",
                    "timeUsed", "0",
                    "memoryUsed", "0"
            );
        }
    }

    // SUBMIT
    @PostMapping("/submit")
    public Map<String, Object> submitCode(@RequestBody Map<String, String> request) throws Exception {

        String code = request.get("code");
        String language = request.get("language");
        String slug = request.get("slug");
        String email = request.get("email");

        Problem problem = problemRepository.findBySlug(slug);
        if (problem == null) return Map.of("verdict", "❌ Problem not found");

        int passed = 0;
        int total = problem.getHiddenTestCases().size();

        String finalCode = switch (language) {
            case "python" -> wrapPythonCode(code, slug);
            case "cpp" -> wrapCppCode(code, slug);
            case "java" -> wrapJavaCode(code, slug);
            default -> code;
        };

        String identifier = email + "_" + slug + "_" + language;

        // COMPILE ONCE
        Object compiled = executionService.compile(language, finalCode, identifier);

        for (TestCase test : problem.getHiddenTestCases()) {

            String input = test.getInput();
            if (!input.endsWith("\n")) input += "\n";

            //  RUN MULTIPLE TIMES
            Map<String, String> result = executionService.runCompiled(compiled, language, input);

            if (result == null || !result.containsKey("output")) {
                return Map.of("verdict", "❌ Execution Error");
            }

            String output = result.get("output").trim();
            long timeUsed = Long.parseLong(result.getOrDefault("timeUsed", "0").replace("ms", ""));
            long memoryUsed = Long.parseLong(result.getOrDefault("memoryUsed", "0").replace("KB", ""));

            // TLE
            if (timeUsed < 0 || timeUsed > 3000) {
                return Map.of(
                        "verdict", "❌ Time Limit Exceeded at test case " + (passed + 1),
                        "passed", passed,
                        "total", total
                );
            }

            // MLE
            if (memoryUsed < 0 || memoryUsed > 262144) {
                return Map.of(
                        "verdict", "❌ Memory Limit Exceeded at test case " + (passed + 1),
                        "passed", passed,
                        "total", total
                );
            }

            // Compare output
            List<String> outTokens = Arrays.stream(output.trim().split("\\s+"))
                    .filter(s -> !s.isEmpty())
                    .toList();

            List<String> expTokens = Arrays.stream(test.getExpectedOutput().trim().split("\\s+"))
                    .filter(s -> !s.isEmpty())
                    .toList();
            System.out.println(" DEBUG TC " + (passed + 1));
            System.out.println("OUTPUT: [" + output + "]");
            System.out.println("EXPECTED: [" + test.getExpectedOutput() + "]");
            System.out.println("OUT TOKENS: " + outTokens);
            System.out.println("EXP TOKENS: " + expTokens);

            if (!outTokens.equals(expTokens)) {
                return Map.of(
                        "verdict", "❌ Wrong Answer at test case " + (passed + 1),
                        "passed", passed,
                        "total", total
                );
            }

            passed++;
        }

        // Save success
        if (passed == total) {
            User user = userRepository.findByEmail(email).orElseThrow();
            userProblemRepository.findByUserAndProblem(user, problem)
                    .orElseGet(() -> userProblemRepository.save(new UserProblem(user, problem, true)));
        }

        return Map.of(
                "verdict", "✅ Accepted",
                "passed", passed,
                "total", total
        );
    }

    // AI DEBUG
    @PostMapping("/ai/debug")
    public Map<String, String> getAiDebugHelp(@RequestBody Map<String, String> request) {
        try {
            String suggestion = executionService.getAiDebugSuggestion(
                    request.get("code"),
                    request.get("output"),
                    request.get("problemDescription"),
                    request.get("language")
            );

            return Map.of("response", suggestion);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("response", "⚠️ AI failed: " + e.getMessage());
        }
    }
}